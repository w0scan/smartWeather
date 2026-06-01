package com.ww.smartweather.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ww.smartweather.data.WeatherCode
import com.ww.smartweather.data.model.Weather

@Composable
fun CityListScreen(
    weatherList: List<Weather>,
    useCelsius: Boolean,
    onBack: () -> Unit,
    onAddCity: () -> Unit,
    onRemoveCity: (String) -> Unit,
    onReorder: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    var draggedIndex by remember { mutableIntStateOf(-1) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var overIndex by remember { mutableIntStateOf(-1) }
    var currentList by remember(weatherList) { mutableStateOf(weatherList) }

    LaunchedEffect(weatherList) {
        currentList = weatherList
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // Nav bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .background(Color.White)
        ) {
            Text(
                text = "+",
                fontSize = 28.sp,
                fontWeight = FontWeight.Light,
                color = Color(0xFF858585),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 18.dp)
                    .clickable { onAddCity() }
            )
            Text(
                text = "城市列表",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF646464),
                modifier = Modifier.align(Alignment.Center)
            )
            Text(
                text = "完成",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF858585),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 18.dp)
                    .clickable { onBack() }
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFD3D3D3))
        )

        // City list with drag reorder
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(currentList, key = { _, w -> w.city.id }) { index, weather ->
                val isDragged = draggedIndex == index
                CityRow(
                    weather = weather,
                    useCelsius = useCelsius,
                    isFirst = index == 0,
                    canRemove = currentList.size > 1,
                    onRemove = { onRemoveCity(weather.city.id) },
                    modifier = Modifier
                        .then(
                            if (isDragged) Modifier
                                .zIndex(1f)
                                .graphicsLayer { translationY = dragOffset }
                            else Modifier
                        )
                        .pointerInput(index) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    draggedIndex = index
                                    overIndex = index
                                    dragOffset = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffset += dragAmount.y
                                    val rowHeight = 65f * density
                                    val targetIndex = (index + (dragOffset / rowHeight).toInt())
                                        .coerceIn(0, currentList.size - 1)
                                    if (targetIndex != overIndex) {
                                        val list = currentList.toMutableList()
                                        val item = list.removeAt(overIndex)
                                        list.add(targetIndex, item)
                                        currentList = list
                                        dragOffset += (overIndex - targetIndex) * rowHeight
                                        overIndex = targetIndex
                                    }
                                },
                                onDragEnd = {
                                    if (draggedIndex != overIndex) {
                                        onReorder(draggedIndex, overIndex)
                                    }
                                    draggedIndex = -1
                                    dragOffset = 0f
                                },
                                onDragCancel = {
                                    draggedIndex = -1
                                    dragOffset = 0f
                                    currentList = weatherList
                                }
                            )
                        }
                )
            }
        }
    }
}

@Composable
private fun CityRow(
    weather: Weather,
    useCelsius: Boolean,
    isFirst: Boolean,
    canRemove: Boolean,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val temp = if (useCelsius) weather.temp else weather.tempF
    val forecast = weather.forecasts.firstOrNull()
    val low = if (useCelsius) forecast?.lowTemp ?: 0 else ((forecast?.lowTemp ?: 0) * 9 / 5) + 32
    val high = if (useCelsius) forecast?.highTemp ?: 0 else ((forecast?.highTemp ?: 0) * 9 / 5) + 32
    val description = WeatherCode.getDescription(weather.weatherCode)

    val cityDisplay = if (weather.city.parentName.isNotEmpty() && weather.city.parentName != weather.city.name) {
        "${weather.city.name} - ${weather.city.parentName}"
    } else {
        weather.city.name
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color.White),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Red minus button
            if (canRemove) {
                Box(
                    modifier = Modifier
                        .padding(start = 20.dp)
                        .size(27.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF2504B))
                        .clickable { onRemove() },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(12.dp)
                            .height(2.5.dp)
                            .background(Color.White, CircleShape)
                    )
                }
            } else {
                Spacer(Modifier.width(47.dp))
            }

            // City info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 20.dp)
            ) {
                Text(
                    text = "$cityDisplay $temp °C",
                    fontSize = 20.sp,
                    fontWeight = if (isFirst) FontWeight.Bold else FontWeight.Normal,
                    color = Color(0xFF303238),
                    letterSpacing = 0.2.sp
                )
                Text(
                    text = "$description ${low}°C 至 ${high}°C",
                    fontSize = 15.sp,
                    color = Color(0xFF9C9C9C)
                )
            }

            // Drag handle
            Column(
                modifier = Modifier
                    .padding(end = 14.dp)
                    .width(21.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.5.dp)
                            .background(Color(0xFFD4D4D4), CircleShape)
                    )
                }
            }
        }

        // Row divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(Color(0xFFEEEEEE))
        )
    }
}
