package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RightActionCards(
    onOpenYoutube: () -> Unit,
    onOpenGoogle: () -> Unit,
    onOpenSettings: () -> Unit,
    onPlayMusic: () -> Unit,
    onToolClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(135.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. QUICK ACTIONS CARD
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF130E20).copy(alpha = 0.85f))
                .border(1.dp, Color(0xFF9333EA).copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .padding(8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Bolt,
                        contentDescription = "Quick Actions",
                        tint = Color(0xFFB57EDC),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Quick Actions",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                QuickActionButton(
                    icon = Icons.Default.PlayArrow,
                    iconTint = Color(0xFFEF4444),
                    text = "YouTube kholo",
                    onClick = onOpenYoutube,
                    tag = "action_youtube"
                )
                QuickActionButton(
                    icon = Icons.Default.Search,
                    iconTint = Color(0xFF3B82F6),
                    text = "Google kholo",
                    onClick = onOpenGoogle,
                    tag = "action_google"
                )
                QuickActionButton(
                    icon = Icons.Default.Settings,
                    iconTint = Color(0xFFE2E8F0),
                    text = "Settings kholo",
                    onClick = onOpenSettings,
                    tag = "action_settings"
                )
                QuickActionButton(
                    icon = Icons.Default.MusicNote,
                    iconTint = Color(0xFFA855F7),
                    text = "Music play karo",
                    onClick = onPlayMusic,
                    tag = "action_music"
                )
            }
        }

        // 2. TOOLS GRID CARD (2 Columns x 3 Rows)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF130E20).copy(alpha = 0.85f))
                .border(1.dp, Color(0xFF9333EA).copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .padding(8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Build,
                        contentDescription = "Tools",
                        tint = Color(0xFFB57EDC),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Tools",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Row 1: Apps & Volume
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ToolItem(icon = Icons.Default.Apps, label = "Apps", onClick = { onToolClick("apps") })
                    ToolItem(icon = Icons.Default.VolumeUp, label = "Volume", onClick = { onToolClick("volume") })
                }

                // Row 2: Brightness & Wi-Fi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ToolItem(icon = Icons.Default.WbSunny, label = "Brightness", onClick = { onToolClick("brightness") })
                    ToolItem(icon = Icons.Default.Wifi, label = "Wi-Fi", onClick = { onToolClick("wifi") })
                }

                // Row 3: Bluetooth & Torch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ToolItem(icon = Icons.Default.Bluetooth, label = "Bluetooth", onClick = { onToolClick("bluetooth") })
                    ToolItem(icon = Icons.Default.FlashlightOn, label = "Torch", onClick = { onToolClick("torch") })
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    iconTint: Color,
    text: String,
    onClick: () -> Unit,
    tag: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E1730))
            .clickable { onClick() }
            .testTag(tag)
            .padding(horizontal = 6.dp, vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = iconTint,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ToolItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(56.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(vertical = 3.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFFD8B4FE),
            modifier = Modifier.size(17.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Normal
        )
    }
}
