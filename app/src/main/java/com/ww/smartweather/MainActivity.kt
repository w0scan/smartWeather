package com.ww.smartweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ww.smartweather.ui.WeatherViewModel
import com.ww.smartweather.ui.screen.CityListScreen
import com.ww.smartweather.ui.screen.CitySearchScreen
import com.ww.smartweather.ui.screen.WeatherScreen
import com.ww.smartweather.ui.theme.SmartWeatherTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartWeatherTheme {
                val viewModel: WeatherViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "weather") {
                    composable("weather") {
                        WeatherScreen(
                            uiState = uiState,
                            onPageChanged = { viewModel.setCurrentPage(it) },
                            onCityListClick = { navController.navigate("cityList") },
                            onAddCityClick = { navController.navigate("citySearch") },
                            onToggleUnit = { viewModel.toggleTemperatureUnit() },
                            onRefresh = { viewModel.refresh() }
                        )
                    }
                    composable("cityList") {
                        CityListScreen(
                            weatherList = uiState.weatherList,
                            useCelsius = uiState.useCelsius,
                            onBack = { navController.popBackStack() },
                            onAddCity = { navController.navigate("citySearch") },
                            onRemoveCity = { viewModel.removeCity(it) },
                            onReorder = { from, to -> viewModel.reorderCities(from, to) }
                        )
                    }
                    composable("citySearch") {
                        CitySearchScreen(
                            searchResults = uiState.searchResults,
                            isSearching = uiState.isSearching,
                            addedCityIds = uiState.weatherList.map { it.city.id }.toSet(),
                            onSearch = { viewModel.searchCity(it) },
                            onCitySelected = { city ->
                                viewModel.addCity(city)
                                navController.popBackStack("weather", false)
                            },
                            onBack = {
                                viewModel.searchCity("")
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}
