package com.ww.smartweather.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.ww.smartweather.R
import kotlin.math.roundToInt

private val slotHeight = 75.dp
private val digitWidth = 36.dp
private val minusWidth = 22.dp
private val unitWidth = 36.dp

private val OvershootEasing = Easing { fraction ->
    val tension = 2.0f
    val t = fraction - 1.0f
    t * t * ((tension + 1) * t + tension) + 1.0f
}

@Composable
fun ImageTemperature(
    celsiusTemp: Int,
    fahrenheitTemp: Int,
    useCelsius: Boolean,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val slotHeightPx = with(density) { slotHeight.toPx() }
    val scrollDistance = 4f * slotHeightPx

    val targetOffset = if (useCelsius) 0f else -scrollDistance
    var initialized by remember { mutableStateOf(false) }
    val animOffset = remember { Animatable(0f) }

    LaunchedEffect(useCelsius) {
        val target = if (useCelsius) 0f else -scrollDistance
        if (!initialized) {
            animOffset.snapTo(target)
            initialized = true
        } else {
            animOffset.animateTo(
                target,
                tween(1400, easing = OvershootEasing)
            )
        }
    }

    // Row: [scrolling digits] [static unit icon]
    Row(
        modifier = modifier.height(slotHeight),
        verticalAlignment = Alignment.Bottom
    ) {
        // Scrolling digits area
        Box(modifier = Modifier.height(slotHeight).clipToBounds()) {
            ScrollSlotLayout(offsetPx = animOffset.value) {
                // Slot 0: Celsius digits only
                DigitRow(celsiusTemp)
                // Slot 1-3: Blur transition frames (single 2-digit blur)
                BlurSlot(R.drawable.mohuzhong_2)
                BlurSlot(R.drawable.mohuzhong_2)
                BlurSlot(R.drawable.mohuzhong_2)
                // Slot 4: Fahrenheit digits only
                DigitRow(fahrenheitTemp)
            }
        }

        // Unit icon — static, not part of scroll
        Image(
            painter = painterResource(
                if (useCelsius) R.drawable.temp_unit_c else R.drawable.temp_unit_f
            ),
            contentDescription = null,
            modifier = Modifier.height(slotHeight).width(unitWidth),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun ScrollSlotLayout(
    offsetPx: Float,
    content: @Composable () -> Unit
) {
    Layout(content = content) { measurables, constraints ->
        val childConstraints = Constraints(
            minWidth = 0,
            maxWidth = constraints.maxWidth,
            minHeight = 0,
            maxHeight = Constraints.Infinity
        )
        val placeables = measurables.map { it.measure(childConstraints) }
        // Use width of digit rows (first and last), not blur frames
        val digitWidth = maxOf(placeables.first().width, placeables.last().width)
        val viewportHeight = constraints.maxHeight
        layout(digitWidth, viewportHeight) {
            var y = offsetPx.roundToInt()
            placeables.forEach { placeable ->
                placeable.place(0, y)
                y += placeable.height
            }
        }
    }
}

@Composable
private fun DigitRow(temp: Int) {
    Row(
        modifier = Modifier.height(slotHeight),
        verticalAlignment = Alignment.Bottom
    ) {
        val digits = temp.toString()
        digits.forEach { ch ->
            if (ch == '-') {
                Image(
                    painter = painterResource(R.drawable.temp_num_minus),
                    contentDescription = null,
                    modifier = Modifier.height(slotHeight).width(minusWidth),
                    contentScale = ContentScale.Fit
                )
            } else {
                Image(
                    painter = painterResource(getDigitRes(ch)),
                    contentDescription = null,
                    modifier = Modifier.height(slotHeight).width(digitWidth),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
private fun BlurSlot(resId: Int) {
    Image(
        painter = painterResource(resId),
        contentDescription = null,
        modifier = Modifier
            .height(slotHeight)
            .width(70.dp),
        contentScale = ContentScale.Crop
    )
}

private fun getDigitRes(ch: Char): Int = when (ch) {
    '0' -> R.drawable.temp_num_0
    '1' -> R.drawable.temp_num_1
    '2' -> R.drawable.temp_num_2
    '3' -> R.drawable.temp_num_3
    '4' -> R.drawable.temp_num_4
    '5' -> R.drawable.temp_num_5
    '6' -> R.drawable.temp_num_6
    '7' -> R.drawable.temp_num_7
    '8' -> R.drawable.temp_num_8
    '9' -> R.drawable.temp_num_9
    else -> R.drawable.temp_num_0
}
