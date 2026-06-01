package com.ww.smartweather.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ww.smartweather.data.WeatherCode

@Composable
fun AqiIndicator(aqi: Int, modifier: Modifier = Modifier) {
    val level = WeatherCode.getAqiLevel(aqi)
    val color = Color(WeatherCode.getAqiColor(aqi))

    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "AQI $aqi $level",
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
