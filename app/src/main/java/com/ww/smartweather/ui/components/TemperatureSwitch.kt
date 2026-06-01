package com.ww.smartweather.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ww.smartweather.R

// Original dimensions from xxhdpi assets:
// Track (frame_sunny): 260x172 px = 87x57 dp
// Thumb (btn_unpressed_sunny): 378x172 px = 126x57 dp
// Thumb travel: Celsius at x=-118px (circle at left), Fahrenheit at x=0 (circle at right)

private val trackWidthDp = 87.dp
private val trackHeightDp = 57.dp
private val thumbWidthDp = 126.dp
private val thumbHeightDp = 57.dp

@Composable
fun TemperatureSwitch(
    useCelsius: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    // Original: Celsius drawing offset = trackWidth - thumbWidth, Fahrenheit = 0
    val trackWidthPx = with(density) { trackWidthDp.toPx() }
    val thumbWidthPx = with(density) { thumbWidthDp.toPx() }
    val celsiusOffsetPx = trackWidthPx - thumbWidthPx  // negative, shifts thumb left
    val fahrenheitOffsetPx = 0f

    val targetOffset = if (useCelsius) celsiusOffsetPx else fahrenheitOffsetPx
    val animatedOffset by animateFloatAsState(
        targetValue = targetOffset,
        animationSpec = tween(durationMillis = 250),
        label = "switch_anim"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { onToggle() }
    ) {
        // C icon
        Image(
            painter = painterResource(
                if (useCelsius) R.drawable.celsuis_focus_sunny
                else R.drawable.celsuis_unfocus_sunny
            ),
            contentDescription = "℃",
            modifier = Modifier.height(38.dp).width(19.dp),
            contentScale = ContentScale.Fit
        )

        // Switch: custom layout that measures thumb at full size regardless of track constraints
        SwitchLayout(
            trackWidth = with(density) { trackWidthDp.roundToPx() },
            trackHeight = with(density) { trackHeightDp.roundToPx() },
            thumbOffsetPx = animatedOffset
        ) {
            // Child 0: Track
            Image(
                painter = painterResource(R.drawable.switch_track),
                contentDescription = null,
                modifier = Modifier.width(trackWidthDp).height(trackHeightDp),
                contentScale = ContentScale.FillBounds
            )
            // Child 1: Thumb (measured unconstrained)
            Image(
                painter = painterResource(R.drawable.switch_thumb),
                contentDescription = null,
                modifier = Modifier.width(thumbWidthDp).height(thumbHeightDp),
                contentScale = ContentScale.FillBounds
            )
        }

        // F icon
        Image(
            painter = painterResource(
                if (!useCelsius) R.drawable.fahrenheit_focus_sunny
                else R.drawable.fahrenheit_unfocus_sunny
            ),
            contentDescription = "℉",
            modifier = Modifier.height(38.dp).width(19.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun SwitchLayout(
    trackWidth: Int,
    trackHeight: Int,
    thumbOffsetPx: Float,
    content: @Composable () -> Unit
) {
    Layout(content = content) { measurables, _ ->
        // Measure track with its own constraints
        val trackPlaceable = measurables[0].measure(
            androidx.compose.ui.unit.Constraints.fixed(trackWidth, trackHeight)
        )
        // Measure thumb unconstrained so it can be full size
        val thumbPlaceable = measurables[1].measure(
            androidx.compose.ui.unit.Constraints()
        )

        // Layout size = track size (thumb can overflow)
        layout(trackWidth, trackHeight) {
            trackPlaceable.place(0, 0)
            thumbPlaceable.place(thumbOffsetPx.toInt(), 0)
        }
    }
}