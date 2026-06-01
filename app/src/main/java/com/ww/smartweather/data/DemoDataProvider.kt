package com.ww.smartweather.data

import com.ww.smartweather.data.model.City
import com.ww.smartweather.data.model.Forecast
import com.ww.smartweather.data.model.HourForecast
import com.ww.smartweather.data.model.Weather

object DemoDataProvider {

    fun getCities(): List<City> = listOf(
        City("101010100", "北京", "北京"),
        City("101020100", "上海", "上海"),
        City("101280101", "广州", "广东")
    )

    fun getWeatherForCity(city: City): Weather = when (city.id) {
        "101020100" -> shanghaiWeather(city)
        "101280101" -> guangzhouWeather(city)
        else -> beijingWeather(city)
    }

    private fun beijingWeather(city: City) = Weather(
        city = city,
        temp = 25,
        weatherCode = "01",
        realFeelTemp = 27,
        humidity = 45,
        windDirection = "南风",
        windSpeed = "0级",
        aqi = 85,
        updateTime = "14:30",
        pm25 = "58",
        pm10 = "92",
        so2 = "8",
        no2 = "42",
        o3 = "126",
        co = "0.7",
        uvRadiation = "弱",
        forecasts = listOf(
            Forecast("2026-05-25", "01", 18, 28, "05:15|19:32"),
            Forecast("2026-05-26", "00", 19, 30, "05:14|19:33"),
            Forecast("2026-05-27", "02", 17, 26, "05:14|19:34"),
            Forecast("2026-05-28", "07", 16, 24, "05:13|19:34"),
            Forecast("2026-05-29", "01", 18, 27, "05:13|19:35")
        ),
        hourForecasts = listOf(
            HourForecast("12:00", "01", 25),
            HourForecast("13:00", "01", 26),
            HourForecast("14:00", "00", 27),
            HourForecast("15:00", "00", 28),
            HourForecast("16:00", "01", 27),
            HourForecast("17:00", "01", 26),
            HourForecast("18:00", "02", 24),
            HourForecast("19:00", "02", 22),
            HourForecast("20:00", "02", 21),
            HourForecast("21:00", "02", 20)
        )
    )

    private fun shanghaiWeather(city: City) = Weather(
        city = city,
        temp = 28,
        weatherCode = "02",
        realFeelTemp = 31,
        humidity = 72,
        windDirection = "东风",
        windSpeed = "3级",
        aqi = 62,
        updateTime = "14:30",
        pm25 = "38",
        pm10 = "65",
        so2 = "5",
        no2 = "31",
        o3 = "98",
        co = "0.5",
        uvRadiation = "中等",
        forecasts = listOf(
            Forecast("2026-05-25", "02", 22, 30, "05:08|18:56"),
            Forecast("2026-05-26", "07", 21, 28, "05:08|18:57"),
            Forecast("2026-05-27", "08", 20, 25, "05:07|18:57"),
            Forecast("2026-05-28", "01", 21, 29, "05:07|18:58"),
            Forecast("2026-05-29", "00", 22, 31, "05:07|18:58")
        ),
        hourForecasts = listOf(
            HourForecast("12:00", "02", 28),
            HourForecast("13:00", "02", 29),
            HourForecast("14:00", "02", 30),
            HourForecast("15:00", "01", 29),
            HourForecast("16:00", "07", 27),
            HourForecast("17:00", "07", 26),
            HourForecast("18:00", "08", 24),
            HourForecast("19:00", "08", 23),
            HourForecast("20:00", "07", 22),
            HourForecast("21:00", "02", 21)
        )
    )

    private fun guangzhouWeather(city: City) = Weather(
        city = city,
        temp = 32,
        weatherCode = "00",
        realFeelTemp = 36,
        humidity = 80,
        windDirection = "南风",
        windSpeed = "2级",
        aqi = 42,
        updateTime = "14:30",
        pm25 = "22",
        pm10 = "48",
        so2 = "3",
        no2 = "18",
        o3 = "78",
        co = "0.4",
        uvRadiation = "很强",
        forecasts = listOf(
            Forecast("2026-05-25", "00", 25, 33, "05:42|19:10"),
            Forecast("2026-05-26", "00", 26, 34, "05:42|19:11"),
            Forecast("2026-05-27", "03", 24, 31, "05:42|19:11"),
            Forecast("2026-05-28", "10", 23, 28, "05:41|19:12"),
            Forecast("2026-05-29", "01", 24, 32, "05:41|19:12")
        ),
        hourForecasts = listOf(
            HourForecast("12:00", "00", 32),
            HourForecast("13:00", "00", 33),
            HourForecast("14:00", "00", 34),
            HourForecast("15:00", "00", 33),
            HourForecast("16:00", "01", 32),
            HourForecast("17:00", "01", 30),
            HourForecast("18:00", "02", 28),
            HourForecast("19:00", "02", 27),
            HourForecast("20:00", "02", 26),
            HourForecast("21:00", "01", 25)
        )
    )
}
