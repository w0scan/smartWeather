package com.ww.smartweather.data.api

import com.ww.smartweather.data.model.City
import com.ww.smartweather.data.model.Forecast
import com.ww.smartweather.data.model.HourForecast
import com.ww.smartweather.data.model.Weather
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

object WeatherParser {

    fun parseWeather(data: JSONObject, city: City): Weather? {
        val observe = data.optJSONObject("observe")?.optJSONObject("info") ?: return null

        val temp = observe.optInt("temp")
        val code = observe.optString("code", "99")
        val wind = observe.optInt("wind")
        val speed = observe.optInt("speed")
        val humidity = observe.optInt("humidity")
        val bodyFeel = observe.optInt("body_feel", temp)

        val airObj = data.optJSONObject("air")?.optJSONObject("info")
        val aqi = airObj?.optInt("aqi", -1) ?: -1
        val pm25 = airObj?.optString("pm25", "--") ?: "--"
        val pm10 = airObj?.optString("pm10", "--") ?: "--"
        val so2 = airObj?.optString("so2", "--") ?: "--"
        val no2 = airObj?.optString("no2", "--") ?: "--"
        val o3 = airObj?.optString("o3", "--") ?: "--"
        val co = airObj?.optString("co", "--") ?: "--"

        val forecasts = parseForecast(data.optJSONObject("forecast"))
        val hourForecasts = parseHourForecast(data.optJSONObject("forecast_hour"))

        val publishTime = data.optJSONObject("observe")?.optString("publish_time", "") ?: ""

        return Weather(
            city = city,
            temp = temp,
            weatherCode = code,
            realFeelTemp = bodyFeel,
            humidity = humidity,
            windDirection = windDirectionName(wind),
            windSpeed = "${speed}级",
            aqi = aqi,
            forecasts = forecasts,
            hourForecasts = hourForecasts,
            updateTime = formatPublishTime(publishTime),
            pm25 = pm25,
            pm10 = pm10,
            so2 = so2,
            no2 = no2,
            o3 = o3,
            co = co
        )
    }

    private fun parseForecast(obj: JSONObject?): List<Forecast> {
        val info = obj?.optJSONArray("info") ?: return emptyList()
        val result = mutableListOf<Forecast>()
        val today = SimpleDateFormat("dd", Locale.getDefault()).format(System.currentTimeMillis()).toInt()

        for (i in 0 until info.length()) {
            val item = info.optJSONObject(i) ?: continue
            val dateStr = item.optString("date")
            val day = dateStr.takeLast(2).toIntOrNull() ?: continue
            if (i == 0 && today - day == 1) continue

            result.add(
                Forecast(
                    date = dateStr,
                    weatherCode = item.optString("code1", "99"),
                    lowTemp = item.optInt("low"),
                    highTemp = item.optInt("high"),
                    sunRiseSet = item.optString("sun", "")
                )
            )
        }
        return result
    }

    private fun parseHourForecast(obj: JSONObject?): List<HourForecast> {
        val info = obj?.optJSONArray("info") ?: return emptyList()
        val result = mutableListOf<HourForecast>()

        for (i in 0 until info.length()) {
            val item = info.optJSONObject(i) ?: continue
            val startTime = item.optString("f_start_time", item.optString("start_time", ""))
            if (startTime.length < 10) continue

            val hour = startTime.substring(8, 10) + ":00"
            result.add(
                HourForecast(
                    hour = hour,
                    weatherCode = item.optString("code", "99"),
                    temp = item.optInt("temp")
                )
            )
        }
        return result
    }

    fun parseCitySearchResults(data: JSONObject): List<City> {
        val content = data.optJSONArray("content") ?: return emptyList()
        val result = mutableListOf<City>()
        for (i in 0 until content.length()) {
            val item = content.optJSONObject(i) ?: continue
            result.add(
                City(
                    id = item.optString("cityId", ""),
                    name = item.optString("county", item.optString("city", "")),
                    parentName = item.optString("province", "")
                )
            )
        }
        return result
    }

    private fun windDirectionName(code: Int): String = when (code) {
        0 -> "无风"
        1 -> "东北风"
        2 -> "东风"
        3 -> "东南风"
        4 -> "南风"
        5 -> "西南风"
        6 -> "西风"
        7 -> "西北风"
        8 -> "北风"
        9 -> "旋转风"
        else -> "未知"
    }

    private fun formatPublishTime(pt: String): String {
        if (pt.length < 12) return ""
        return try {
            "${pt.substring(8, 10)}:${pt.substring(10, 12)}"
        } catch (e: Exception) {
            ""
        }
    }
}
