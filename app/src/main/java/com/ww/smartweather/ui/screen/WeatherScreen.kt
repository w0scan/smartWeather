package com.ww.smartweather.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ww.smartweather.R
import com.ww.smartweather.data.WeatherCode
import com.ww.smartweather.data.model.Weather
import com.ww.smartweather.ui.WeatherUiState
import com.ww.smartweather.ui.components.AnimatedTempText
import com.ww.smartweather.ui.components.ImageTemperature
import com.ww.smartweather.ui.components.PageIndicator
import com.ww.smartweather.ui.components.TemperatureSwitch
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.absoluteValue

@Composable
fun WeatherScreen(
    uiState: WeatherUiState,
    onPageChanged: (Int) -> Unit,
    onCityListClick: () -> Unit,
    onAddCityClick: () -> Unit,
    onToggleUnit: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.isLoading && uiState.weatherList.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (uiState.weatherList.isEmpty()) return

    val pagerState = rememberPagerState(
        initialPage = uiState.currentPage,
        pageCount = { uiState.weatherList.size }
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { onPageChanged(it) }
    }

    val currentWeather = uiState.weatherList.getOrNull(pagerState.currentPage)
    val bgGradient = getWeatherGradient(currentWeather?.weatherCode ?: "99")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(bgGradient))
    ) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                WeatherPage(
                    weather = uiState.weatherList[page],
                    useCelsius = uiState.useCelsius,
                    isDemo = uiState.isDemo,
                    isRefreshing = uiState.isRefreshing,
                    onCityListClick = onCityListClick,
                    onAddCityClick = onAddCityClick,
                    onToggleUnit = onToggleUnit,
                    onRefresh = onRefresh
                )
            }

            PageIndicator(
                pageCount = uiState.weatherList.size,
                currentPage = pagerState.currentPage,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun WeatherPage(
    weather: Weather,
    useCelsius: Boolean,
    isDemo: Boolean,
    isRefreshing: Boolean,
    onCityListClick: () -> Unit,
    onAddCityClick: () -> Unit,
    onToggleUnit: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(12.dp))

        // City name - 18sp bold (fixed)
        Text(
            text = weather.city.name,
            fontSize = 18.sp,
            color = Color(0xFF333333),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        // Thin line under city name
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .width(24.dp)
                .height(1.dp)
                .background(Color(0xFF333333).copy(alpha = 0.2f))
        )

        Spacer(Modifier.height(9.dp))

        // Toolbar: C/F toggle (left) + refresh/add/list buttons (right) (fixed)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 3.dp, end = 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TemperatureSwitch(
                useCelsius = useCelsius,
                onToggle = onToggleUnit
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isDemo) {
                    Text(
                        text = "演示",
                        fontSize = 10.sp,
                        color = Color(0xFF999999),
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(47.dp)
                        .clickable(enabled = !isRefreshing) { onRefresh() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.button_refresh_sunny_normal),
                        contentDescription = "刷新",
                        modifier = Modifier.size(47.dp)
                    )
                    Image(
                        painter = painterResource(R.drawable.button_refresh_icon_sunny),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        alpha = if (isRefreshing) 0.4f else 1f
                    )
                }
                Image(
                    painter = painterResource(R.drawable.button_add_sunny_normal),
                    contentDescription = "添加城市",
                    modifier = Modifier
                        .size(47.dp)
                        .clickable { onAddCityClick() }
                )
                Image(
                    painter = painterResource(R.drawable.button_list_sunny_normal),
                    contentDescription = "城市列表",
                    modifier = Modifier
                        .size(47.dp)
                        .clickable { onCityListClick() }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Card 1: Temperature card with weather background (fixed size)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            Image(
                painter = painterResource(getCardBgRes(weather.weatherCode)),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier.padding(top = 20.dp, bottom = 20.dp, start = 22.dp, end = 22.dp)
            ) {
                ImageTemperature(
                    celsiusTemp = weather.temp,
                    fahrenheitTemp = weather.tempF,
                    useCelsius = useCelsius
                )

                Spacer(Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = WeatherCode.getDescription(weather.weatherCode),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (weather.aqi > 0) {
                        Spacer(Modifier.width(7.dp))
                        Text(
                            text = "AQI ${weather.aqi}",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = WeatherCode.getAqiLevel(weather.aqi),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                Spacer(Modifier.height(7.dp))

                if (weather.updateTime.isNotEmpty()) {
                    Text(
                        text = "更新于 ${weather.updateTime}",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Card 2: Detail card — scrollable with elastic bounce, weather background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
        ) {
            Image(
                painter = painterResource(getForecastBgRes(weather.weatherCode)),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )

            BouncyScrollContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp, bottom = 16.dp, start = 22.dp, end = 22.dp)
            ) {
                // Today section
                if (weather.forecasts.isNotEmpty()) {
                    val today = weather.forecasts.first()
                    val low = if (useCelsius) today.lowTemp else (today.lowTemp * 9 / 5) + 32
                    val high = if (useCelsius) today.highTemp else (today.highTemp * 9 / 5) + 32
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f).padding(end = 32.dp)) {
                            Text(
                                text = today.dayOfWeek,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                            Text(
                                text = " 今天",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                        }
                        Row(modifier = Modifier.weight(1f).padding(start = 20.dp), horizontalArrangement = Arrangement.End) {
                            AnimatedTempText(
                                text = "$low°",
                                useCelsius = useCelsius,
                                fontSize = 13.5.sp,
                                color = Color(0x4C000000),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(40.dp)
                            )
                            AnimatedTempText(
                                text = "$high°",
                                useCelsius = useCelsius,
                                fontSize = 13.5.sp,
                                color = Color(0x4C000000),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(40.dp)
                            )
                        }
                    }
                }

                // Separator
                SectionDivider()

                // Hourly forecast
                if (weather.hourForecastsWithSun.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        weather.hourForecastsWithSun.forEach { hour ->
                            Column(
                                modifier = Modifier
                                    .width(48.dp)
                                    .height(108.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = hour.hour,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0x4C000000)
                                )
                                Spacer(Modifier.height(4.dp))
                                Image(
                                    painter = painterResource(WeatherCode.getIconRes(hour.weatherCode)),
                                    contentDescription = null,
                                    modifier = Modifier.size(width = 30.dp, height = 48.dp),
                                    contentScale = ContentScale.Fit
                                )
                                Spacer(Modifier.height(4.dp))
                                if (hour.isSunEvent) {
                                    Text(
                                        text = hour.sunDescription,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0x4C000000)
                                    )
                                } else {
                                    val hTemp = if (useCelsius) hour.temp else (hour.temp * 9 / 5) + 32
                                    AnimatedTempText(
                                        text = "$hTemp°",
                                        useCelsius = useCelsius,
                                        fontSize = 12.sp,
                                        color = Color(0x4C000000)
                                    )
                                }
                            }
                        }
                    }

                    SectionDivider()
                }

                // Multi-day forecast rows
                if (weather.forecasts.size > 1) {
                    weather.forecasts.drop(1).forEach { forecast ->
                        val low = if (useCelsius) forecast.lowTemp else (forecast.lowTemp * 9 / 5) + 32
                        val high = if (useCelsius) forecast.highTemp else (forecast.highTemp * 9 / 5) + 32
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${forecast.displayDate} ${forecast.dayOfWeek}",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333),
                                modifier = Modifier.weight(1f).padding(end = 32.dp)
                            )
                            Row(
                                modifier = Modifier.weight(1f).padding(start = 20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(WeatherCode.getIconRes(forecast.weatherCode)),
                                    contentDescription = null,
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    contentScale = ContentScale.Fit
                                )
                                AnimatedTempText(
                                    text = "$low°",
                                    useCelsius = useCelsius,
                                    fontSize = 13.5.sp,
                                    color = Color(0x4C000000),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                                AnimatedTempText(
                                    text = "$high°",
                                    useCelsius = useCelsius,
                                    fontSize = 13.5.sp,
                                    color = Color(0x4C000000),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    SectionDivider()
                }

                // Weather detail rows — matching original APK layout order
                // Row 1: 体感温度 | 相对湿度
                Row(
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DetailRow(
                        label = "体感温度",
                        value = "${if (useCelsius) weather.realFeelTemp else weather.realFeelTempF}°",
                        useCelsius = useCelsius,
                        modifier = Modifier.weight(1f).padding(end = 32.dp)
                    )
                    DetailRow(
                        label = "相对湿度",
                        value = "${weather.humidity}%",
                        useCelsius = useCelsius,
                        modifier = Modifier.weight(1f).padding(start = 20.dp)
                    )
                }
                // Row 2: 风向风速 | 紫外线辐射
                Row(
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DetailRow(
                        label = weather.windDirection,
                        value = weather.windSpeed,
                        useCelsius = useCelsius,
                        modifier = Modifier.weight(1f).padding(end = 32.dp)
                    )
                    DetailRow(
                        label = "紫外线辐射",
                        value = weather.uvRadiation,
                        useCelsius = useCelsius,
                        modifier = Modifier.weight(1f).padding(start = 20.dp)
                    )
                }

                SectionDivider()

                // Air quality section with dot indicators
                // PM2.5 | PM10
                Row(
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AirQualityRow(
                        label = "PM2.5",
                        value = weather.pm25,
                        indicatorRes = getAirIndicator("pm25", weather.pm25),
                        modifier = Modifier.weight(1f).padding(end = 32.dp)
                    )
                    AirQualityRow(
                        label = "PM10",
                        value = weather.pm10,
                        indicatorRes = getAirIndicator("pm10", weather.pm10),
                        modifier = Modifier.weight(1f).padding(start = 20.dp)
                    )
                }
                // SO₂ | NO₂
                Row(
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AirQualityRow(
                        label = "SO₂",
                        value = weather.so2,
                        indicatorRes = getAirIndicator("so2", weather.so2),
                        modifier = Modifier.weight(1f).padding(end = 32.dp)
                    )
                    AirQualityRow(
                        label = "NO₂",
                        value = weather.no2,
                        indicatorRes = getAirIndicator("no2", weather.no2),
                        modifier = Modifier.weight(1f).padding(start = 20.dp)
                    )
                }
                // O₃ | CO
                Row(
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AirQualityRow(
                        label = "O₃",
                        value = weather.o3,
                        indicatorRes = getAirIndicator("o3", weather.o3),
                        modifier = Modifier.weight(1f).padding(end = 32.dp)
                    )
                    AirQualityRow(
                        label = "CO",
                        value = weather.co,
                        indicatorRes = getAirIndicator("co", weather.co),
                        modifier = Modifier.weight(1f).padding(start = 20.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun BouncyScrollContent(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val overscrollOffset = remember { Animatable(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val current = overscrollOffset.value
                if (current == 0f) return Offset.Zero

                val scrollingBack = (current > 0f && available.y < 0f) ||
                        (current < 0f && available.y > 0f)
                if (!scrollingBack) return Offset.Zero

                val next = current + available.y
                val crossedZero = (current > 0f && next <= 0f) ||
                        (current < 0f && next >= 0f)

                return if (crossedZero) {
                    val consumed = -current
                    scope.launch { overscrollOffset.snapTo(0f) }
                    Offset(0f, consumed)
                } else {
                    scope.launch { overscrollOffset.snapTo(next) }
                    available
                }
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y == 0f) return Offset.Zero

                val current = overscrollOffset.value
                val resistance = (1f - abs(current) / MAX_OVERSCROLL).coerceIn(0.05f, 1f)
                val rate = if (source == NestedScrollSource.UserInput) {
                    RUBBER_BAND_RATE
                } else {
                    RUBBER_BAND_RATE * FLING_RATE
                }
                scope.launch { overscrollOffset.snapTo(current + available.y * resistance * rate) }
                return available
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (overscrollOffset.value != 0f) {
                    overscrollOffset.animateTo(0f, BOUNCE_SPRING)
                    return available
                }
                return Velocity.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (overscrollOffset.value != 0f) {
                    overscrollOffset.animateTo(0f, BOUNCE_SPRING)
                }
                return available
            }
        }
    }

    Column(
        modifier = modifier
            .nestedScroll(nestedScrollConnection)
            .clipToBounds()
            .graphicsLayer { translationY = overscrollOffset.value }
            .verticalScroll(scrollState),
        content = content
    )
}

@Composable
private fun SectionDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 15.dp)
            .height(0.67.dp)
            .background(Color(0xFF333333).copy(alpha = 0.1f))
    )
}

@Composable
private fun DetailRow(label: String, value: String, useCelsius: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0x9A000000)
        )
        Spacer(Modifier.weight(1f))
        AnimatedTempText(
            text = value,
            useCelsius = useCelsius,
            fontSize = 13.5.sp,
            color = Color(0x4C000000),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun AirQualityRow(label: String, value: String, indicatorRes: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(indicatorRes),
            contentDescription = null,
            modifier = Modifier.size(6.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0x9A000000)
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = value,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0x4C000000),
            textAlign = TextAlign.End
        )
    }
}

private fun getAirIndicator(type: String, valueStr: String): Int {
    val v = valueStr.toDoubleOrNull() ?: return R.drawable.weather_air_good
    return when (type) {
        "pm25" -> when {
            v <= 35 -> R.drawable.weather_air_perfect
            v <= 75 -> R.drawable.weather_air_good
            v <= 115 -> R.drawable.weather_air_mild_polluted
            v <= 150 -> R.drawable.weather_air_moderately_polluted
            v <= 250 -> R.drawable.weather_air_severe_pollution
            else -> R.drawable.weather_air_severe_pollution_most
        }
        "pm10" -> when {
            v <= 50 -> R.drawable.weather_air_perfect
            v <= 150 -> R.drawable.weather_air_good
            v <= 250 -> R.drawable.weather_air_mild_polluted
            v <= 350 -> R.drawable.weather_air_moderately_polluted
            v <= 420 -> R.drawable.weather_air_severe_pollution
            else -> R.drawable.weather_air_severe_pollution_most
        }
        "so2" -> when {
            v <= 150 -> R.drawable.weather_air_perfect
            v <= 500 -> R.drawable.weather_air_good
            v <= 650 -> R.drawable.weather_air_mild_polluted
            v <= 800 -> R.drawable.weather_air_moderately_polluted
            v <= 1600 -> R.drawable.weather_air_severe_pollution
            else -> R.drawable.weather_air_severe_pollution_most
        }
        "no2" -> when {
            v <= 100 -> R.drawable.weather_air_perfect
            v <= 200 -> R.drawable.weather_air_good
            v <= 700 -> R.drawable.weather_air_mild_polluted
            v <= 1200 -> R.drawable.weather_air_moderately_polluted
            v <= 2340 -> R.drawable.weather_air_severe_pollution
            else -> R.drawable.weather_air_severe_pollution_most
        }
        "o3" -> when {
            v <= 160 -> R.drawable.weather_air_perfect
            v <= 200 -> R.drawable.weather_air_good
            v <= 300 -> R.drawable.weather_air_mild_polluted
            v <= 400 -> R.drawable.weather_air_moderately_polluted
            v <= 800 -> R.drawable.weather_air_severe_pollution
            else -> R.drawable.weather_air_severe_pollution_most
        }
        "co" -> when {
            v <= 5 -> R.drawable.weather_air_perfect
            v <= 10 -> R.drawable.weather_air_good
            v <= 35 -> R.drawable.weather_air_mild_polluted
            v <= 60 -> R.drawable.weather_air_moderately_polluted
            v <= 90 -> R.drawable.weather_air_severe_pollution
            else -> R.drawable.weather_air_severe_pollution_most
        }
        else -> R.drawable.weather_air_good
    }
}

private const val RUBBER_BAND_RATE = 0.55f
private const val FLING_RATE = 0.3f
private const val MAX_OVERSCROLL = 800f
private val BOUNCE_SPRING = spring<Float>(dampingRatio = 0.7f, stiffness = 350f)

private fun getWeatherGradient(code: String): List<Color> {
    val theme = getThemeType(code)
    return when (theme) {
        "00", "01" -> listOf(Color(0xFFe8eefc), Color(0xFFf6faff))
        "02", "03" -> listOf(Color(0xFFe9ecf1), Color(0xFFf4f6fa))
        "04" -> listOf(Color(0xFFe7eefc), Color(0xFFf3faff))
        "05" -> listOf(Color(0xFFedeff3), Color(0xFFf6f8fa))
        "06" -> listOf(Color(0xFFebebed), Color(0xFFf6f6f7))
        "07" -> listOf(Color(0xFFf8f6f2), Color(0xFFfbf9f7))
        else -> listOf(Color(0xFFe8eefc), Color(0xFFf6faff))
    }
}

private fun getCardBgRes(code: String): Int {
    val theme = getThemeType(code)
    return when (theme) {
        "00" -> R.drawable.bg_weather_info_sunny
        "01" -> R.drawable.bg_weather_info_cloud
        "02" -> R.drawable.bg_weather_info_overcast
        "03" -> R.drawable.bg_weather_info_rain
        "04" -> R.drawable.bg_weather_info_snow
        "05" -> R.drawable.bg_weather_info_foggy
        "06" -> R.drawable.bg_weather_info_haze
        "07" -> R.drawable.bg_weather_info_sandstorm
        else -> R.drawable.bg_weather_info_sunny
    }
}

private fun getForecastBgRes(code: String): Int {
    val theme = getThemeType(code)
    return when (theme) {
        "00", "01", "02" -> R.drawable.bg_forecast_sunny
        "03" -> R.drawable.bg_forecast_rain
        "04" -> R.drawable.bg_forecast_snow
        "05" -> R.drawable.bg_forecast_foggy
        "06" -> R.drawable.bg_forecast_haze
        "07" -> R.drawable.bg_forecast_sandstorm
        else -> R.drawable.bg_forecast_sunny
    }
}

private fun getThemeType(code: String): String = when (code) {
    "00", "99" -> "00"
    "01" -> "01"
    "02" -> "02"
    "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "19", "21", "22", "23", "24", "25" -> "03"
    "13", "14", "15", "16", "17", "26", "27", "28" -> "04"
    "18", "32", "49", "57", "58" -> "05"
    "53", "54", "55", "56" -> "06"
    "20", "29", "30", "31" -> "07"
    else -> "00"
}
