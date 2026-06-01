package com.ww.smartweather.data.model

data class HourForecast(
    val hour: String,
    val weatherCode: String,
    val temp: Int,
    val isSunEvent: Boolean = false,
    val sunDescription: String = "",
    val isNight: Boolean = false
)
