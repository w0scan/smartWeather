package com.ww.smartweather.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ww.smartweather.R
import com.ww.smartweather.data.model.City

private val hotCities = listOf(
    City("101010100", "北京", "北京"),
    City("101030100", "天津", "天津"),
    City("101020100", "上海", "上海"),
    City("101280101", "广州", "广东"),
    City("101040100", "重庆", "重庆"),
    City("101190101", "南京", "江苏"),
    City("101210101", "杭州", "浙江"),
    City("101230101", "福州", "福建"),
    City("101250101", "长沙", "湖南"),
    City("101200101", "武汉", "湖北"),
    City("101310101", "海口", "海南"),
    City("101270101", "成都", "四川"),
    City("101110101", "西安", "陕西"),
    City("101230201", "厦门", "福建"),
    City("101190401", "苏州", "江苏")
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CitySearchScreen(
    searchResults: List<City>,
    isSearching: Boolean,
    addedCityIds: Set<String>,
    onSearch: (String) -> Unit,
    onCitySelected: (City) -> Unit,
    onBack: () -> Unit
) {
    var query by remember { mutableStateOf("") }

    LaunchedEffect(query) {
        if (query.isBlank()) {
            onSearch("")
        } else {
            kotlinx.coroutines.delay(300)
            onSearch(query)
        }
    }

    val stripeWidthPx = with(LocalDensity.current) { 8.dp.toPx() }
    val stripeColor = Color(0xFFFCFCFC)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(Color.White)
                var x = stripeWidthPx
                while (x < size.width) {
                    drawRect(
                        color = stripeColor,
                        topLeft = Offset(x, 0f),
                        size = Size(stripeWidthPx, size.height)
                    )
                    x += stripeWidthPx * 2
                }
            }
            .statusBarsPadding()
    ) {
        // Top bar with search + close
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .background(Color.White)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Search pill
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .border(1.dp, Color(0xFFE7E7E7), RoundedCornerShape(18.dp))
                    .background(Color(0xFFFAFAFA), RoundedCornerShape(18.dp))
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFFA6A6A6)
                )
                Spacer(Modifier.width(10.dp))
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = "输入城市和地区",
                            fontSize = 14.sp,
                            color = Color(0xFFD0D0D0)
                        )
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = Color(0xFF333333)
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(Color(0xFF333333)),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSearch(query) })
                    )
                }
            }
            // Close button
            Text(
                text = "×",
                fontSize = 20.sp,
                color = Color(0xFF9F9F9F),
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(horizontal = 4.dp)
            )
        }
        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFEEEEEE))
        )

        if (query.isBlank()) {
            // Hot cities
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 24.dp, top = 26.dp, end = 24.dp)
            ) {
                Text(
                    text = "热门城市",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFA0A0A0),
                    modifier = Modifier.padding(start = 8.dp, bottom = 16.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    hotCities.forEach { city ->
                        val isAdded = addedCityIds.contains(city.id)
                        CityChip(
                            name = city.name,
                            isAdded = isAdded,
                            onClick = { if (!isAdded) onCitySelected(city) }
                        )
                    }
                }
            }
        } else if (isSearching) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color(0xFFA0A0A0)
                )
            }
        } else {
            // Search results
            LazyColumn(
                modifier = Modifier.background(Color.White)
            ) {
                items(searchResults) { city ->
                    val isAdded = addedCityIds.contains(city.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { if (!isAdded) onCitySelected(city) }
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (city.parentName.isNotEmpty()) "${city.name} · ${city.parentName}" else city.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAdded) Color(0x4D000000) else Color(0x99000000)
                        )
                        if (isAdded) {
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = "已添加",
                                fontSize = 12.sp,
                                color = Color(0x4D000000)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .height(0.5.dp)
                            .background(Color(0xFFEEEEEE))
                    )
                }
            }
        }
    }
}

@Composable
private fun CityChip(
    name: String,
    isAdded: Boolean,
    onClick: () -> Unit
) {
    val textColor = if (isAdded) Color(0x4D000000) else Color(0x99000000)
    val borderColor = if (isAdded) Color(0xFFEFEFEF) else Color(0xFFDCDCDC)

    Box(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
