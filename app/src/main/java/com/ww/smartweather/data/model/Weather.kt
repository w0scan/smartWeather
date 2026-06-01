package com.ww.smartweather.data.model

data class Weather(
    val city: City,
    val temp: Int,
    val weatherCode: String,
    val realFeelTemp: Int,
    val humidity: Int,
    val windDirection: String,
    val windSpeed: String,
    val aqi: Int,
    val forecasts: List<Forecast>,
    val hourForecasts: List<HourForecast>,
    val updateTime: String = "",
    val pm25: String = "--",
    val pm10: String = "--",
    val so2: String = "--",
    val no2: String = "--",
    val o3: String = "--",
    val co: String = "--",
    val uvRadiation: String = "--"
) {
    val tempF: Int get() = (temp * 9 / 5) + 32
    val realFeelTempF: Int get() = (realFeelTemp * 9 / 5) + 32

    val hourForecastsWithSun: List<HourForecast> by lazy {
        val sunRiseSet = forecasts.firstOrNull()?.sunRiseSet ?: return@lazy hourForecasts
        val parts = sunRiseSet.split("|")
        if (parts.size != 2) return@lazy hourForecasts
        val sunrise = parts[0].trim()
        val sunset = parts[1].trim()
        if (sunrise.length < 4 || sunset.length < 4) return@lazy hourForecasts

        val sunriseHour = sunrise.substringBefore(":").toIntOrNull() ?: return@lazy hourForecasts
        val sunriseMin = sunrise.substringAfter(":").toIntOrNull() ?: 0
        val sunsetHour = sunset.substringBefore(":").toIntOrNull() ?: return@lazy hourForecasts
        val sunsetMin = sunset.substringAfter(":").toIntOrNull() ?: 0

        val result = mutableListOf<HourForecast>()
        var sunriseInserted = false
        var sunsetInserted = false

        for (hf in hourForecasts) {
            val h = hf.hour.substringBefore(":").toIntOrNull() ?: 0
            val isNight = h < sunriseHour || h >= sunsetHour ||
                    (h == sunriseHour && 0 < sunriseMin) ||
                    (h == sunsetHour && 0 >= sunsetMin)

            if (!sunriseInserted && h >= sunriseHour) {
                result.add(HourForecast(
                    hour = sunrise,
                    weatherCode = "1000",
                    temp = 0,
                    isSunEvent = true,
                    sunDescription = "日出"
                ))
                sunriseInserted = true
            }

            if (!sunsetInserted && h >= sunsetHour) {
                result.add(HourForecast(
                    hour = sunset,
                    weatherCode = "1001",
                    temp = 0,
                    isSunEvent = true,
                    sunDescription = "日落"
                ))
                sunsetInserted = true
            }

            result.add(hf.copy(isNight = isNight))
        }

        if (!sunsetInserted) {
            result.add(HourForecast(
                hour = sunset,
                weatherCode = "1001",
                temp = 0,
                isSunEvent = true,
                sunDescription = "日落"
            ))
        }

        result
    }
}
