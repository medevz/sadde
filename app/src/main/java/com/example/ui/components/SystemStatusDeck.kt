package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.system.SystemStatus

@Composable
fun SystemStatusDeck(
    systemStatus: SystemStatus,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Battery Card
        SystemStatusMiniCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.BatteryFull,
            iconColor = Color(0xFF10B981),
            label = "Battery",
            value = "${systemStatus.batteryPercent}%",
            valueColor = Color(0xFF10B981)
        )

        // Storage Card
        SystemStatusMiniCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.PieChart,
            iconColor = Color(0xFF38BDF8),
            label = "Storage",
            value = "${systemStatus.storagePercent}%",
            valueColor = Color(0xFF38BDF8)
        )

        // RAM Card
        SystemStatusMiniCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Memory,
            iconColor = Color(0xFFA855F7),
            label = "RAM",
            value = "${systemStatus.ramUsedGb} GB",
            valueColor = Color(0xFFA855F7)
        )

        // Network Card
        SystemStatusMiniCard(
            modifier = Modifier.weight(1.1f),
            icon = Icons.Default.Wifi,
            iconColor = Color(0xFF34D399),
            label = "Network",
            value = if (systemStatus.isNetworkConnected) "Connected" else "Offline",
            valueColor = if (systemStatus.isNetworkConnected) Color(0xFF34D399) else Color(0xFFEF4444)
        )
    }
}

@Composable
private fun SystemStatusMiniCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    valueColor: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF130E20).copy(alpha = 0.9f))
            .border(1.dp, Color(0xFF9333EA).copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(horizontal = 6.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Column {
                Text(
                    text = label,
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1
                )
                Text(
                    text = value,
                    color = valueColor,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}
