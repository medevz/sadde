package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.domain.model.AssistantState
import kotlin.math.sin

@Composable
fun VoiceWaveformDeck(
    state: AssistantState,
    volumeLevel: Float,
    onMicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "voice_wave_deck")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val isListening = state is AssistantState.Listening
    val isSpeaking = state is AssistantState.Speaking

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Waveform Bars
        WaveformSide(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            isLeft = true,
            isActive = isListening || isSpeaking,
            volume = volumeLevel,
            phase = wavePhase
        )

        // Center Big Glowing Mic Button
        Box(
            modifier = Modifier
                .size(64.dp)
                .shadow(16.dp, CircleShape, spotColor = Color(0xFF9333EA))
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            if (isListening) Color(0xFF06B6D4) else Color(0xFF9333EA),
                            Color(0xFF581C87),
                            Color(0xFF1E1035)
                        )
                    )
                )
                .border(2.dp, Brush.sweepGradient(listOf(Color(0xFFC084FC), Color(0xFF818CF8), Color(0xFFC084FC))), CircleShape)
                .clickable { onMicClick() }
                .testTag("main_mic_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when {
                    isSpeaking -> Icons.Default.Stop
                    isListening -> Icons.Default.Mic
                    else -> Icons.Default.Mic
                },
                contentDescription = "Microphone",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }

        // Right Waveform Bars
        WaveformSide(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            isLeft = false,
            isActive = isListening || isSpeaking,
            volume = volumeLevel,
            phase = wavePhase
        )
    }
}

@Composable
private fun WaveformSide(
    modifier: Modifier = Modifier,
    isLeft: Boolean,
    isActive: Boolean,
    volume: Float,
    phase: Float
) {
    Canvas(modifier = modifier) {
        val count = 22
        val barWidth = 2.5.dp.toPx()
        val spacing = (size.width - (count * barWidth)) / count.coerceAtLeast(1)
        val cy = size.height / 2

        for (i in 0 until count) {
            val progress = if (isLeft) (i.toFloat() / count) else (1f - (i.toFloat() / count))
            val sinVal = sin((phase + (i * 0.35f)).toDouble()).toFloat()
            val ampFactor = if (isActive) (0.3f + volume * 0.7f + (sinVal * 0.25f)) else (0.15f + (sinVal * 0.08f))
            val barH = (size.height * 0.75f * progress * ampFactor).coerceAtLeast(3.dp.toPx())

            val x = if (isLeft) {
                i * (barWidth + spacing)
            } else {
                size.width - ((i + 1) * (barWidth + spacing))
            }

            drawLine(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFFC084FC).copy(alpha = progress * 0.9f),
                        Color(0xFF7E22CE).copy(alpha = progress * 0.7f)
                    )
                ),
                start = Offset(x, cy - (barH / 2)),
                end = Offset(x, cy + (barH / 2)),
                strokeWidth = barWidth
            )
        }
    }
}
