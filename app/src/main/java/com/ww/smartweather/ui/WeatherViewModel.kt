package com.ww.smartweather.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ww.smartweather.data.DemoDataProvider
import com.ww.smartweather.data.api.WeatherApi
import com.ww.smartweather.data.api.WeatherParser
import com.ww.smartweather.data.model.City
import com.ww.smartweather.data.model.Weather
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class WeatherUiState(
    val weatherList: List<Weather> = emptyList(),
    val currentPage: Int = 0,
    val useCelsius: Boolean = true,
    val isDemo: Boolean = false,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val searchResults: List<City> = emptyList(),
    val isSearching: Boolean = false
)

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val prefs = application.getSharedPreferences("weather_cities", Context.MODE_PRIVATE)

    init {
        val cities = loadSavedCities()
        if (cities.isEmpty()) {
            val defaults = listOf(
                City("101010100", "北京", "北京"),
                City("101020100", "上海", "上海"),
                City("101280101", "广州", "广东")
            )
            saveCities(defaults)
            loadWeatherData(defaults)
        } else {
            loadWeatherData(cities)
        }
    }

    private fun loadWeatherData(cities: List<City>) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val weatherList = mutableListOf<Weather>()
            for (city in cities) {
                val data = WeatherApi.fetchWeather(city.id)
                if (data != null) {
                    val weather = WeatherParser.parseWeather(data, city)
                    if (weather != null) {
                        weatherList.add(weather)
                        continue
                    }
                }
                weatherList.add(DemoDataProvider.getWeatherForCity(city))
            }

            if (weatherList.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    weatherList = DemoDataProvider.getCities().map { DemoDataProvider.getWeatherForCity(it) },
                    isDemo = true,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    weatherList = weatherList,
                    isDemo = false,
                    isLoading = false
                )
            }
        }
    }

    fun refresh() {
        val cities = loadSavedCities()
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        viewModelScope.launch {
            val weatherList = mutableListOf<Weather>()
            for (city in cities) {
                val data = WeatherApi.fetchWeather(city.id)
                if (data != null) {
                    val weather = WeatherParser.parseWeather(data, city)
                    if (weather != null) {
                        weatherList.add(weather)
                        continue
                    }
                }
                val existing = _uiState.value.weatherList.find { it.city.id == city.id }
                if (existing != null) weatherList.add(existing)
            }
            _uiState.value = _uiState.value.copy(
                weatherList = weatherList.ifEmpty { _uiState.value.weatherList },
                isRefreshing = false,
                isDemo = false
            )
        }
    }

    fun setCurrentPage(page: Int) {
        _uiState.value = _uiState.value.copy(currentPage = page)
    }

    fun toggleTemperatureUnit() {
        _uiState.value = _uiState.value.copy(useCelsius = !_uiState.value.useCelsius)
    }

    fun searchCity(query: String) {
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList(), isSearching = false)
            return
        }
        _uiState.value = _uiState.value.copy(isSearching = true)
        viewModelScope.launch {
            val data = WeatherApi.searchCity(query)
            val results = if (data != null) WeatherParser.parseCitySearchResults(data) else emptyList()
            _uiState.value = _uiState.value.copy(searchResults = results, isSearching = false)
        }
    }

    fun addCity(city: City) {
        val cities = loadSavedCities().toMutableList()
        if (cities.any { it.id == city.id }) return
        cities.add(city)
        saveCities(cities)

        viewModelScope.launch {
            val data = WeatherApi.fetchWeather(city.id)
            val weather = if (data != null) WeatherParser.parseWeather(data, city) else null
            val list = _uiState.value.weatherList.toMutableList()
            list.add(weather ?: DemoDataProvider.getWeatherForCity(city))
            _uiState.value = _uiState.value.copy(
                weatherList = list,
                currentPage = list.size - 1,
                searchResults = emptyList()
            )
        }
    }

    fun removeCity(cityId: String) {
        val cities = loadSavedCities().toMutableList()
        cities.removeAll { it.id == cityId }
        saveCities(cities)

        val list = _uiState.value.weatherList.toMutableList()
        list.removeAll { it.city.id == cityId }
        val page = _uiState.value.currentPage.coerceAtMost((list.size - 1).coerceAtLeast(0))
        _uiState.value = _uiState.value.copy(weatherList = list, currentPage = page)
    }

    fun reorderCities(from: Int, to: Int) {
        val list = _uiState.value.weatherList.toMutableList()
        if (from < 0 || from >= list.size || to < 0 || to >= list.size) return
        val item = list.removeAt(from)
        list.add(to, item)
        _uiState.value = _uiState.value.copy(weatherList = list)
        saveCities(list.map { it.city })
    }

    private fun loadSavedCities(): List<City> {
        val json = prefs.getString("cities_json", null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                City(obj.getString("id"), obj.getString("name"), obj.optString("parent", ""))
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveCities(cities: List<City>) {
        val arr = JSONArray()
        cities.forEach { city ->
            arr.put(JSONObject().apply {
                put("id", city.id)
                put("name", city.name)
                put("parent", city.parentName)
            })
        }
        prefs.edit().putString("cities_json", arr.toString()).apply()
    }
}
