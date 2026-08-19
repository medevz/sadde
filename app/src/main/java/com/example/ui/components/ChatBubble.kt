package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ActionExecutionStatus
import com.example.domain.model.AssistantAction
import com.example.domain.model.ChatMessage
import com.example.domain.model.MessageRole
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPink
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberRose
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatBubble(
    message: ChatMessage,
    onReplayAudio: ((String) -> Unit)? = null,
    onDelete: ((Long) -> Unit)? = null,
    onActionClick: ((AssistantAction) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == MessageRole.USER
    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("chat_bubble_${message.id}"),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            // Assistant avatar mini icon
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(CyberCyan, CyberPurple))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("S", color = ObsidianDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.weight(1f, fill = false),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .background(
                        if (isUser) {
                            Brush.linearGradient(listOf(CyberPurple, CyberCyan))
                        } else {
                            Brush.linearGradient(listOf(CyberSurface, CyberSurfaceVariant))
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = if (isUser) CyberCyan.copy(alpha = 0.4f) else CyberBorder,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    Text(
                        text = message.text,
                        color = if (isUser) Color.White else TextPrimaryDark,
                        fontSize = 15.sp,
                        lineHeight = 21.sp
                    )

                    // Display Executed Actions List if present
                    if (message.actions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            message.actions.forEach { action ->
                                ActionStatusChip(action = action, onClick = { onActionClick?.invoke(action) })
                            }
                        }
                    }
                }
            }

            // Message footer (timestamp, replay, delete)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
            ) {
                Text(
                    text = timeStr,
                    color = TextMutedDark,
                    fontSize = 11.sp
                )

                if (message.isOffline) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• Offline",
                        color = CyberEmerald,
                        fontSize = 11.sp
                    )
                }

                if (!isUser && onReplayAudio != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Speak response again",
                        tint = CyberCyan,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onReplayAudio(message.text) }
                    )
                }

                if (onDelete != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete message",
                        tint = TextMutedDark,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onDelete(message.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ActionStatusChip(
    action: AssistantAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (statusColor, statusIcon, statusLabel) = when (action.status) {
        ActionExecutionStatus.SUCCESS -> Triple(CyberEmerald, Icons.Default.CheckCircle, "Executed")
        ActionExecutionStatus.FAILED -> Triple(CyberRose, Icons.Default.ErrorOutline, "Failed")
        ActionExecutionStatus.RESTRICTED -> Triple(CyberAmber, Icons.Default.Shield, "Restricted")
        ActionExecutionStatus.PENDING -> Triple(CyberCyan, Icons.Default.PlayArrow, "Pending")
        ActionExecutionStatus.SKIPPED -> Triple(TextMutedDark, Icons.Default.CheckCircle, "Skipped")
    }

    val chipText = action.executionMessage ?: "${action.type.name} ${action.target ?: ""}".trim()

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = ObsidianDark.copy(alpha = 0.7f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.5f)),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = statusIcon,
                contentDescription = statusLabel,
                tint = statusColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = chipText,
                color = TextPrimaryDark,
                fontSize = 12.sp,
                maxLines = 1
            )
        }
    }
}
