package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.model.AssistantState
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPink
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberRose
import com.example.ui.theme.ObsidianDark
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ShreyaCharacterAvatar(
    state: AssistantState,
    volumeLevel: Float = 0f,
    size: Dp = 180.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shreya_avatar_anim")

    // Gentle breathing cycle
    val breathing by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shreya_breathing"
    )

    // Speaking lip/voice pulse cycle
    val speakingMouthMotion by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 180, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shreya_lip_motion"
    )

    // Eye blink animation cycle
    val eyeBlink by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shreya_eye_blink"
    )

    // Orbital ring rotation
    val auraRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shreya_aura_rot"
    )

    val isListening = state is AssistantState.Listening
    val isSpeaking = state is AssistantState.Speaking
    val isProcessing = state is AssistantState.Processing

    // Dynamic glow colors
    val glowColor = when {
        isListening -> CyberCyan
        isSpeaking -> CyberPurple
        isProcessing -> CyberPink
        state is AssistantState.Error -> CyberRose
        else -> CyberPurple
    }

    Box(
        modifier = modifier
            .size(size)
            .shadow(24.dp, shape = CircleShape, spotColor = glowColor, ambientColor = CyberPurple)
            .clip(CircleShape)
            .background(ObsidianDark)
            .border(2.dp, Brush.radialGradient(listOf(glowColor, CyberPurple.copy(alpha = 0.3f))), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = this.size.width
            val canvasH = this.size.height
            val cx = canvasW / 2f
            val cy = canvasH / 2f

            // --- 1. STUDIO BACKGROUND & AURA GLOW ---
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF2A163B),
                        Color(0xFF140D1D),
                        Color(0xFF08060B)
                    ),
                    center = Offset(cx, cy),
                    radius = canvasW * 0.7f
                ),
                radius = canvasW * 0.5f,
                center = Offset(cx, cy)
            )

            // Halo Rings for listening/speaking
            val haloPulse = if (isListening) 1f + (volumeLevel * 0.4f) else breathing
            drawCircle(
                color = glowColor.copy(alpha = if (isSpeaking || isListening) 0.35f else 0.15f),
                radius = (canvasW * 0.46f) * haloPulse,
                center = Offset(cx, cy),
                style = Stroke(width = 2.dp.toPx())
            )

            // Rotating cyber nodes on halo
            val rad = Math.toRadians(auraRotation.toDouble())
            val orbX1 = cx + ((canvasW * 0.46f) * haloPulse * cos(rad)).toFloat()
            val orbY1 = cy + ((canvasW * 0.46f) * haloPulse * sin(rad)).toFloat()
            drawCircle(color = glowColor, radius = 3.dp.toPx(), center = Offset(orbX1, orbY1))

            // --- 2. HIGH PONYTAIL & BACKGROUND HAIR ---
            val hairDark = Color(0xFF120E18)
            val hairHighlight = Color(0xFF5A2A78)

            // Ponytail body extending upwards to the left & right
            val ponytailPath = Path().apply {
                moveTo(cx - 24.dp.toPx(), cy - (canvasH * 0.28f))
                cubicTo(
                    cx - 50.dp.toPx(), cy - (canvasH * 0.42f),
                    cx - 30.dp.toPx(), cy - (canvasH * 0.52f),
                    cx, cy - (canvasH * 0.48f)
                )
                cubicTo(
                    cx + 30.dp.toPx(), cy - (canvasH * 0.52f),
                    cx + 50.dp.toPx(), cy - (canvasH * 0.42f),
                    cx + 24.dp.toPx(), cy - (canvasH * 0.28f)
                )
                close()
            }
            drawPath(ponytailPath, Brush.verticalGradient(listOf(hairHighlight, hairDark)))

            // Glowing purple hair tie
            drawCircle(
                color = CyberPurple,
                radius = 7.dp.toPx(),
                center = Offset(cx, cy - (canvasH * 0.44f))
            )
            drawCircle(
                color = CyberPink,
                radius = 3.dp.toPx(),
                center = Offset(cx, cy - (canvasH * 0.44f))
            )

            // --- 3. CLOTHING: FUTURISTIC BLACK & PURPLE SAREE ---
            val sareeBlack = Color(0xFF100D14)
            val sareePurple = Color(0xFF38154D)
            val sareeGoldCircuit = Color(0xFFB57EDC)

            // Shoulders & Chest silhouette
            val chestPath = Path().apply {
                moveTo(cx - (canvasW * 0.48f), canvasH)
                lineTo(cx - (canvasW * 0.35f), cy + (canvasH * 0.22f))
                cubicTo(
                    cx - (canvasW * 0.20f), cy + (canvasH * 0.16f),
                    cx - (canvasW * 0.10f), cy + (canvasH * 0.22f),
                    cx, cy + (canvasH * 0.23f)
                )
                cubicTo(
                    cx + (canvasW * 0.10f), cy + (canvasH * 0.22f),
                    cx + (canvasW * 0.20f), cy + (canvasH * 0.16f),
                    cx + (canvasW * 0.35f), cy + (canvasH * 0.22f)
                )
                lineTo(cx + (canvasW * 0.48f), canvasH)
                close()
            }
            drawPath(
                chestPath,
                Brush.verticalGradient(listOf(sareePurple, sareeBlack), startY = cy, endY = canvasH)
            )

            // Saree Pallu Drape across chest (Diagonal flowing drape)
            val palluPath = Path().apply {
                moveTo(cx - (canvasW * 0.38f), canvasH)
                cubicTo(
                    cx - (canvasW * 0.20f), cy + (canvasH * 0.30f),
                    cx + (canvasW * 0.05f), cy + (canvasH * 0.20f),
                    cx + (canvasW * 0.35f), cy + (canvasH * 0.22f)
                )
                lineTo(cx + (canvasW * 0.44f), cy + (canvasH * 0.34f))
                cubicTo(
                    cx + (canvasW * 0.15f), cy + (canvasH * 0.38f),
                    cx - (canvasW * 0.05f), cy + (canvasH * 0.48f),
                    cx - (canvasW * 0.22f), canvasH
                )
                close()
            }
            drawPath(
                palluPath,
                Brush.linearGradient(
                    colors = listOf(Color(0xFF4A1E6D), Color(0xFF1E0A2F), Color(0xFF110719)),
                    start = Offset(cx - canvasW * 0.3f, cy),
                    end = Offset(cx + canvasW * 0.3f, canvasH)
                )
            )

            // Glowing cybernetic embroidery lines along saree border
            drawPath(
                palluPath,
                color = sareeGoldCircuit.copy(alpha = 0.75f),
                style = Stroke(width = 1.8.dp.toPx())
            )

            // --- 4. NECK & CYBERNETIC CHOKER ---
            val skinTone = Color(0xFFD6A47C)
            val skinShadow = Color(0xFFB8825D)
            val skinHighlight = Color(0xFFE8BA93)

            val neckPath = Path().apply {
                moveTo(cx - 16.dp.toPx(), cy + (canvasH * 0.05f))
                lineTo(cx - 18.dp.toPx(), cy + (canvasH * 0.22f))
                lineTo(cx + 18.dp.toPx(), cy + (canvasH * 0.22f))
                lineTo(cx + 16.dp.toPx(), cy + (canvasH * 0.05f))
                close()
            }
            drawPath(neckPath, Brush.verticalGradient(listOf(skinShadow, skinTone)))

            // Futuristic Choker Necklace
            drawRect(
                color = Color(0xFF1A1424),
                topLeft = Offset(cx - 16.dp.toPx(), cy + (canvasH * 0.12f)),
                size = Size(32.dp.toPx(), 4.dp.toPx())
            )
            // Glowing Purple Pendant
            drawCircle(
                color = CyberPurple,
                radius = 3.5.dp.toPx(),
                center = Offset(cx, cy + (canvasH * 0.15f))
            )
            drawCircle(
                color = CyberCyan,
                radius = 1.5.dp.toPx(),
                center = Offset(cx, cy + (canvasH * 0.15f))
            )

            // --- 5. FACE & JAWLINE ---
            val facePath = Path().apply {
                moveTo(cx - 28.dp.toPx(), cy - (canvasH * 0.18f))
                cubicTo(
                    cx - 32.dp.toPx(), cy - (canvasH * 0.05f),
                    cx - 24.dp.toPx(), cy + (canvasH * 0.08f),
                    cx, cy + (canvasH * 0.12f)
                )
                cubicTo(
                    cx + 24.dp.toPx(), cy + (canvasH * 0.08f),
                    cx + 32.dp.toPx(), cy - (canvasH * 0.05f),
                    cx + 28.dp.toPx(), cy - (canvasH * 0.18f)
                )
                close()
            }
            drawPath(facePath, Brush.radialGradient(listOf(skinHighlight, skinTone), center = Offset(cx, cy - 8.dp.toPx())))

            // Soft blush on cheeks
            drawCircle(
                brush = Brush.radialGradient(listOf(Color(0x35E57373), Color.Transparent)),
                radius = 8.dp.toPx(),
                center = Offset(cx - 18.dp.toPx(), cy - (canvasH * 0.01f))
            )
            drawCircle(
                brush = Brush.radialGradient(listOf(Color(0x35E57373), Color.Transparent)),
                radius = 8.dp.toPx(),
                center = Offset(cx + 18.dp.toPx(), cy - (canvasH * 0.01f))
            )

            // --- 6. GLOWING CYBERNETIC BINDI ---
            drawCircle(
                color = CyberPurple,
                radius = 2.8.dp.toPx(),
                center = Offset(cx, cy - (canvasH * 0.10f))
            )
            drawCircle(
                color = Color.White,
                radius = 1.2.dp.toPx(),
                center = Offset(cx, cy - (canvasH * 0.10f))
            )

            // --- 7. EXPRESSIVE EYES ---
            val isBlinking = eyeBlink < 0.12f
            val eyeEyeLidH = if (isBlinking) 1.dp.toPx() else 4.dp.toPx()

            // Left Eye
            val leftEyeX = cx - 13.dp.toPx()
            val leftEyeY = cy - (canvasH * 0.04f)
            // Right Eye
            val rightEyeX = cx + 13.dp.toPx()
            val rightEyeY = cy - (canvasH * 0.04f)

            if (!isBlinking) {
                // Eye whites
                drawOval(
                    color = Color.White,
                    topLeft = Offset(leftEyeX - 6.dp.toPx(), leftEyeY - 3.dp.toPx()),
                    size = Size(12.dp.toPx(), 6.dp.toPx())
                )
                drawOval(
                    color = Color.White,
                    topLeft = Offset(rightEyeX - 6.dp.toPx(), rightEyeY - 3.dp.toPx()),
                    size = Size(12.dp.toPx(), 6.dp.toPx())
                )

                // Warm Amber-Brown & Purple Iris
                drawCircle(
                    brush = Brush.radialGradient(listOf(CyberPurple, Color(0xFF421E0B))),
                    radius = 3.5.dp.toPx(),
                    center = Offset(leftEyeX, leftEyeY)
                )
                drawCircle(
                    brush = Brush.radialGradient(listOf(CyberPurple, Color(0xFF421E0B))),
                    radius = 3.5.dp.toPx(),
                    center = Offset(rightEyeX, rightEyeY)
                )

                // Pupil & Catchlight spark
                drawCircle(color = Color.Black, radius = 1.8.dp.toPx(), center = Offset(leftEyeX, leftEyeY))
                drawCircle(color = Color.Black, radius = 1.8.dp.toPx(), center = Offset(rightEyeX, rightEyeY))
                drawCircle(color = Color.White, radius = 0.8.dp.toPx(), center = Offset(leftEyeX - 1.dp.toPx(), leftEyeY - 1.dp.toPx()))
                drawCircle(color = Color.White, radius = 0.8.dp.toPx(), center = Offset(rightEyeX - 1.dp.toPx(), rightEyeY - 1.dp.toPx()))
            }

            // Eyelashes & Eyeliner
            drawLine(
                color = Color(0xFF1E1428),
                start = Offset(leftEyeX - 7.dp.toPx(), leftEyeY - 2.dp.toPx()),
                end = Offset(leftEyeX + 7.dp.toPx(), leftEyeY - 2.dp.toPx()),
                strokeWidth = 1.8.dp.toPx()
            )
            drawLine(
                color = Color(0xFF1E1428),
                start = Offset(rightEyeX - 7.dp.toPx(), rightEyeY - 2.dp.toPx()),
                end = Offset(rightEyeX + 7.dp.toPx(), rightEyeY - 2.dp.toPx()),
                strokeWidth = 1.8.dp.toPx()
            )

            // Eyebrows
            drawLine(
                color = Color(0xFF281C30),
                start = Offset(leftEyeX - 7.dp.toPx(), leftEyeY - 7.dp.toPx()),
                end = Offset(leftEyeX + 6.dp.toPx(), leftEyeY - 6.5.dp.toPx()),
                strokeWidth = 1.5.dp.toPx()
            )
            drawLine(
                color = Color(0xFF281C30),
                start = Offset(rightEyeX - 6.dp.toPx(), rightEyeY - 6.5.dp.toPx()),
                end = Offset(rightEyeX + 7.dp.toPx(), rightEyeY - 7.dp.toPx()),
                strokeWidth = 1.5.dp.toPx()
            )

            // Nose
            val noseY = cy + (canvasH * 0.015f)
            drawLine(
                color = skinShadow,
                start = Offset(cx, cy - (canvasH * 0.03f)),
                end = Offset(cx - 1.dp.toPx(), noseY),
                strokeWidth = 1.2.dp.toPx()
            )
            drawArc(
                color = skinShadow,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(cx - 3.dp.toPx(), noseY - 1.dp.toPx()),
                size = Size(6.dp.toPx(), 2.dp.toPx()),
                style = Stroke(1.2.dp.toPx())
            )

            // --- 8. MOUTH & LIP-SYNC TALKING ANIMATION ---
            val mouthY = cy + (canvasH * 0.06f)
            val lipColor = Color(0xFFBF5B75)
            val lipHighlight = Color(0xFFE8829C)

            if (isSpeaking) {
                // Dynamic animated speaking mouth aperture
                val mouthOpening = (4.dp.toPx() * speakingMouthMotion).coerceAtLeast(1.5.dp.toPx())
                val mouthW = 10.dp.toPx()

                // Open mouth inner
                drawOval(
                    color = Color(0xFF4A1020),
                    topLeft = Offset(cx - (mouthW / 2), mouthY - (mouthOpening / 2)),
                    size = Size(mouthW, mouthOpening)
                )
                // Upper & lower lip
                drawArc(
                    color = lipHighlight,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(cx - (mouthW / 2), mouthY - (mouthOpening / 2) - 1.5.dp.toPx()),
                    size = Size(mouthW, 3.dp.toPx()),
                    style = Stroke(1.2.dp.toPx())
                )
            } else {
                // Gentle, calm resting smile
                val smilePath = Path().apply {
                    moveTo(cx - 7.dp.toPx(), mouthY)
                    cubicTo(
                        cx - 3.dp.toPx(), mouthY + 2.dp.toPx(),
                        cx + 3.dp.toPx(), mouthY + 2.dp.toPx(),
                        cx + 7.dp.toPx(), mouthY
                    )
                }
                drawPath(smilePath, color = lipColor, style = Stroke(1.8.dp.toPx()))

                // Upper lip center cupid's bow
                drawCircle(color = lipHighlight, radius = 1.2.dp.toPx(), center = Offset(cx, mouthY - 0.8.dp.toPx()))
            }

            // --- 9. FRONT HAIR BANGS & SIDE TRESSES ---
            val bangsPath = Path().apply {
                moveTo(cx - 32.dp.toPx(), cy - (canvasH * 0.18f))
                cubicTo(
                    cx - 20.dp.toPx(), cy - (canvasH * 0.26f),
                    cx + 20.dp.toPx(), cy - (canvasH * 0.26f),
                    cx + 32.dp.toPx(), cy - (canvasH * 0.18f)
                )
                // Left fringe
                cubicTo(
                    cx + 18.dp.toPx(), cy - (canvasH * 0.10f),
                    cx - 10.dp.toPx(), cy - (canvasH * 0.12f),
                    cx - 32.dp.toPx(), cy - (canvasH * 0.18f)
                )
                close()
            }
            drawPath(bangsPath, Brush.verticalGradient(listOf(hairDark, Color(0xFF281C38))))

            // Left and Right Hair strands framing face
            val leftStrand = Path().apply {
                moveTo(cx - 28.dp.toPx(), cy - (canvasH * 0.14f))
                cubicTo(
                    cx - 34.dp.toPx(), cy + (canvasH * 0.05f),
                    cx - 28.dp.toPx(), cy + (canvasH * 0.20f),
                    cx - 22.dp.toPx(), cy + (canvasH * 0.30f)
                )
                lineTo(cx - 26.dp.toPx(), cy + (canvasH * 0.30f))
                cubicTo(
                    cx - 36.dp.toPx(), cy + (canvasH * 0.18f),
                    cx - 38.dp.toPx(), cy + (canvasH * 0.02f),
                    cx - 30.dp.toPx(), cy - (canvasH * 0.14f)
                )
                close()
            }
            drawPath(leftStrand, Brush.verticalGradient(listOf(hairDark, hairHighlight)))

            val rightStrand = Path().apply {
                moveTo(cx + 28.dp.toPx(), cy - (canvasH * 0.14f))
                cubicTo(
                    cx + 34.dp.toPx(), cy + (canvasH * 0.05f),
                    cx + 28.dp.toPx(), cy + (canvasH * 0.20f),
                    cx + 22.dp.toPx(), cy + (canvasH * 0.30f)
                )
                lineTo(cx + 26.dp.toPx(), cy + (canvasH * 0.30f))
                cubicTo(
                    cx + 36.dp.toPx(), cy + (canvasH * 0.18f),
                    cx + 38.dp.toPx(), cy + (canvasH * 0.02f),
                    cx + 30.dp.toPx(), cy - (canvasH * 0.14f)
                )
                close()
            }
            drawPath(rightStrand, Brush.verticalGradient(listOf(hairDark, hairHighlight)))
        }
    }
}
