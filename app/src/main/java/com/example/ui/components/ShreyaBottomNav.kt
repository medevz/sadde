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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class NavItem {
    HOME, CHAT, HISTORY, PROFILE
}

@Composable
fun ShreyaBottomNav(
    selectedItem: NavItem = NavItem.HOME,
    onItemSelected: (NavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF100B1C).copy(alpha = 0.95f))
            .border(1.dp, Color(0xFF9333EA).copy(alpha = 0.4f), RoundedCornerShape(22.dp))
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavButton(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = selectedItem == NavItem.HOME,
                onClick = { onItemSelected(NavItem.HOME) },
                tag = "nav_home"
            )
            NavButton(
                icon = Icons.Default.ChatBubbleOutline,
                label = "Chat",
                isSelected = selectedItem == NavItem.CHAT,
                onClick = { onItemSelected(NavItem.CHAT) },
                tag = "nav_chat"
            )
            NavButton(
                icon = Icons.Default.History,
                label = "History",
                isSelected = selectedItem == NavItem.HISTORY,
                onClick = { onItemSelected(NavItem.HISTORY) },
                tag = "nav_history"
            )
            NavButton(
                icon = Icons.Default.PersonOutline,
                label = "Profile",
                isSelected = selectedItem == NavItem.PROFILE,
                onClick = { onItemSelected(NavItem.PROFILE) },
                tag = "nav_profile"
            )
        }
    }
}

@Composable
private fun NavButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    val activeColor = Color(0xFFC084FC)
    val inactiveColor = Color.White.copy(alpha = 0.5f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag(tag)
            .padding(horizontal = 14.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) activeColor else inactiveColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = if (isSelected) activeColor else inactiveColor,
            fontSize = 10.5.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
