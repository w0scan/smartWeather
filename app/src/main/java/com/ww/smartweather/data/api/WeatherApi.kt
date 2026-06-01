package com.ww.smartweather.data.api

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

object WeatherApi {
    private const val BASE_URL = "https://api-weather.smartisan.com/v3/info.php"
    private const val CITY_SEARCH_URL = "http://api-weather.smartisan.com/v3/info/getCity"
    private const val PRIVATE_KEY = "smartisan_weather_api"
    private const val APP_NAME = "com.android.providers.weather"
    private const val VCODE = "812"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val fields = listOf("forecast", "observe", "air", "forecast_hour", "alert", "allergy")

    suspend fun fetchWeather(cityId: String): JSONObject? = withContext(Dispatchers.IO) {
        val rtime = System.currentTimeMillis().toString()
        val builder = Uri.parse(BASE_URL).buildUpon()
            .appendQueryParameter("app", APP_NAME)
            .appendQueryParameter("city_id", cityId)
            .appendQueryParameter("fields", fields.joinToString(","))
            .appendQueryParameter("rtime", rtime)
            .appendQueryParameter("vcode", VCODE)

        val query = builder.build().query ?: return@withContext null
        val signInput = query.replace("&", "") + PRIVATE_KEY
        val key = md5(signInput)
        builder.appendQueryParameter("key", key)

        val url = builder.build().toString()
        val request = Request.Builder().url(url).build()
        try {
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext null
            val json = JSONObject(body)
            if (json.optInt("code") == 0) {
                json.optJSONObject("data")
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun searchCity(query: String, page: Int = 1): JSONObject? = withContext(Dispatchers.IO) {
        val signRaw = "page=${page}q=${query}size=20$PRIVATE_KEY"
        val key = md5(signRaw)

        val url = Uri.parse(CITY_SEARCH_URL).buildUpon()
            .appendQueryParameter("q", query)
            .appendQueryParameter("page", page.toString())
            .appendQueryParameter("size", "20")
            .appendQueryParameter("key", key)
            .build().toString()

        val request = Request.Builder().url(url).build()
        try {
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext null
            val json = JSONObject(body)
            if (json.optInt("code") == 0) json.optJSONObject("data") else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun md5(input: String): String {
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
