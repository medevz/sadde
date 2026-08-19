package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPink
import com.example.ui.theme.CyberPurple

@Composable
fun AudioWaveformVisualizer(
    volumeLevel: Float,
    isSpeaking: Boolean = false,
    height: Dp = 28.dp,
    modifier: Modifier = Modifier
) {
    val barCount = 12
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")

    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_oscillation"
    )

    Row(
        modifier = modifier.height(height),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val positionFactor = kotlin.math.sin((i.toDouble() / barCount) * Math.PI).toFloat()

            val barFraction = if (isSpeaking) {
                (positionFactor * waveOffset).coerceIn(0.15f, 1.0f)
            } else {
                (positionFactor * (0.15f + volumeLevel * 0.85f)).coerceIn(0.12f, 1.0f)
            }

            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .fillMaxHeight(barFraction)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(CyberCyan, CyberPurple, CyberPink)
                        )
                    )
            )
        }
    }
}
