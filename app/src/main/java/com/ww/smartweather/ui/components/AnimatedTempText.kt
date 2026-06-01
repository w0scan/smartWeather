package com.ww.smartweather.ui.components

import androidx.compose.animation.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit

@Composable
fun AnimatedTempText(
    text: String,
    useCelsius: Boolean,
    fontSize: TextUnit,
    color: Color,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.Bold,
    textAlign: TextAlign? = null
) {
    AnimatedContent(
        targetState = Pair(text, useCelsius),
        transitionSpec = {
            val goingToF = !targetState.second
            if (goingToF) {
                (slideInVertically { it } + fadeIn()) togetherWith
                        (slideOutVertically { -it } + fadeOut())
            } else {
                (slideInVertically { -it } + fadeIn()) togetherWith
                        (slideOutVertically { it } + fadeOut())
            }
        },
        label = "temp_text_anim",
        modifier = modifier
    ) { (targetText, _) ->
        Text(
            text = targetText,
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = color,
            textAlign = textAlign
        )
    }
}
