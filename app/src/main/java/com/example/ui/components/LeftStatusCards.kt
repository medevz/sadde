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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AssistantState
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPink
import com.example.ui.theme.CyberPurple

@Composable
fun LeftStatusCards(
    state: AssistantState,
    volumeLevel: Float,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    onMicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var langDropdownExpanded by remember { mutableStateOf(false) }
    var voiceDropdownExpanded by remember { mutableStateOf(false) }
    var selectedVoice by remember { mutableStateOf("Shreya Voice") }

    val infiniteTransition = rememberInfiniteTransition(label = "status_anim")
    val waveAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_pulse"
    )

    Column(
        modifier = modifier.width(135.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. STATUS CARD
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF130E20).copy(alpha = 0.85f))
                .border(1.dp, Color(0xFF9333EA).copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .padding(10.dp)
        ) {
            Column {
                Text(
                    text = "Status",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Online",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                // Mini Waveform
                Canvas(modifier = Modifier.fillMaxWidth().height(14.dp)) {
                    val bars = 9
                    val barWidth = 2.dp.toPx()
                    val gap = (size.width - (bars * barWidth)) / (bars - 1)
                    for (i in 0 until bars) {
                        val factor = if (i % 2 == 0) waveAnim else (1.3f - waveAnim)
                        val h = (size.height * factor * 0.85f).coerceAtLeast(3.dp.toPx())
                        val x = i * (barWidth + gap)
                        drawLine(
                            brush = Brush.verticalGradient(listOf(Color(0xFFB57EDC), Color(0xFF7928CA))),
                            start = Offset(x, (size.height - h) / 2),
                            end = Offset(x, (size.height + h) / 2),
                            strokeWidth = barWidth
                        )
                    }
                }
            }
        }

        // 2. LISTENING CARD
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF130E20).copy(alpha = 0.85f))
                .border(1.dp, Color(0xFF9333EA).copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .clickable { onMicClick() }
                .padding(10.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Listening",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF8B5CF6).copy(alpha = 0.3f), Color(0xFF6D28D9).copy(alpha = 0.5f))
                            )
                        )
                        .border(1.dp, Color(0xFFB57EDC).copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Mic",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                // Dotted audio wave
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(7) { index ->
                        val isLit = (index == 3 || state is AssistantState.Listening)
                        Box(
                            modifier = Modifier
                                .size(if (index == 3) 4.dp else 2.5.dp)
                                .clip(CircleShape)
                                .background(if (isLit) Color(0xFFB57EDC) else Color.White.copy(alpha = 0.3f))
                        )
                    }
                }
            }
        }

        // 3. LANGUAGE CARD
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF130E20).copy(alpha = 0.85f))
                .border(1.dp, Color(0xFF9333EA).copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .clickable { langDropdownExpanded = true }
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Column {
                Text(
                    text = "Language",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Language",
                            tint = Color(0xFFB57EDC),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (currentLanguage.startsWith("hi")) "Hindi (India)" else "English (India)",
                            color = Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Select",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                }

                DropdownMenu(
                    expanded = langDropdownExpanded,
                    onDismissRequest = { langDropdownExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Hindi (India)") },
                        onClick = {
                            onLanguageChange("hi")
                            langDropdownExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("English (India)") },
                        onClick = {
                            onLanguageChange("en")
                            langDropdownExpanded = false
                        }
                    )
                }
            }
        }

        // 4. VOICE CARD
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF130E20).copy(alpha = 0.85f))
                .border(1.dp, Color(0xFF9333EA).copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .clickable { voiceDropdownExpanded = true }
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Column {
                Text(
                    text = "Voice",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = "Voice",
                            tint = Color(0xFFB57EDC),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = selectedVoice,
                            color = Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Select",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                }

                DropdownMenu(
                    expanded = voiceDropdownExpanded,
                    onDismissRequest = { voiceDropdownExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Shreya Voice (Warm Indian)") },
                        onClick = {
                            selectedVoice = "Shreya Voice"
                            voiceDropdownExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Cyber Aura (Futuristic)") },
                        onClick = {
                            selectedVoice = "Cyber Aura"
                            voiceDropdownExpanded = false
                        }
                    )
                }
            }
        }
    }
}
