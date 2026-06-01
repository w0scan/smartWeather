package com.ww.smartweather.data

import com.ww.smartweather.R

object WeatherCode {

    private val descriptionMap = mapOf(
        "00" to "晴",
        "01" to "多云",
        "02" to "阴",
        "03" to "阵雨",
        "04" to "雷阵雨",
        "05" to "雷阵雨伴有冰雹",
        "06" to "雨夹雪",
        "07" to "小雨",
        "08" to "中雨",
        "09" to "大雨",
        "10" to "暴雨",
        "11" to "大暴雨",
        "12" to "特大暴雨",
        "13" to "阵雪",
        "14" to "小雪",
        "15" to "中雪",
        "16" to "大雪",
        "17" to "暴雪",
        "18" to "雾",
        "19" to "冻雨",
        "20" to "沙尘暴",
        "21" to "小到中雨",
        "22" to "中到大雨",
        "23" to "大到暴雨",
        "24" to "暴雨到大暴雨",
        "25" to "大暴雨到特大暴雨",
        "26" to "小到中雪",
        "27" to "中到大雪",
        "28" to "大到暴雪",
        "29" to "浮尘",
        "30" to "扬沙",
        "31" to "强沙尘暴",
        "32" to "浓雾",
        "49" to "强浓雾",
        "53" to "霾",
        "54" to "中度霾",
        "55" to "重度霾",
        "56" to "严重霾",
        "57" to "大雾",
        "58" to "特强浓雾",
        "99" to "未知"
    )

    fun getDescription(code: String): String = descriptionMap[code] ?: "未知"

    fun getIconRes(code: String): Int = when (code) {
        "00" -> R.drawable.little_icon_sunny
        "01" -> R.drawable.little_icon_cloudy
        "02" -> R.drawable.little_icon_overcast
        "03" -> R.drawable.little_icon_shower
        "04" -> R.drawable.little_icon_thundershower
        "05" -> R.drawable.little_icon_thundershowerhail
        "06", "19" -> R.drawable.little_icon_icerain
        "07" -> R.drawable.little_icon_lightrain
        "08", "21" -> R.drawable.little_icon_moderaterain
        "09", "22" -> R.drawable.little_icon_heavyrain
        "10", "11", "12", "23", "24", "25" -> R.drawable.little_icon_storm
        "13" -> R.drawable.little_icon_snow
        "14" -> R.drawable.little_icon_lightsnow
        "15", "26" -> R.drawable.little_icon_moderatesnow
        "16", "17", "27", "28" -> R.drawable.little_icon_heavysnow
        "18", "32", "49", "57", "58" -> R.drawable.little_icon_foggy
        "20", "31" -> R.drawable.little_icon_sandstorm
        "29", "30", "53", "54", "55", "56" -> R.drawable.little_icon_haze
        "1000" -> R.drawable.little_icon_sunrise_shadow
        "1001" -> R.drawable.little_icon_sunset_shadow
        else -> R.drawable.little_icon_unknown
    }

    fun getAqiLevel(aqi: Int): String = when {
        aqi in 0..50 -> "优"
        aqi in 51..100 -> "良"
        aqi in 101..150 -> "轻度污染"
        aqi in 151..200 -> "中度污染"
        aqi in 201..300 -> "重度污染"
        aqi > 300 -> "严重污染"
        else -> "未知"
    }

    fun getAqiColor(aqi: Int): Long = when {
        aqi in 0..50 -> 0xFF4CAF50
        aqi in 51..100 -> 0xFFFFEB3B
        aqi in 101..150 -> 0xFFFF9800
        aqi in 151..200 -> 0xFFF44336
        aqi in 201..300 -> 0xFF9C27B0
        aqi > 300 -> 0xFF7B1FA2
        else -> 0xFF9E9E9E
    }
}
