package com.ww.smartweather.data.model

data class City(
    val id: String,
    val name: String,
    val parentName: String = ""
)
