package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.model.AssistantState

@Composable
fun FuturisticAvatar(
    state: AssistantState,
    volumeLevel: Float = 0f,
    size: Dp = 180.dp,
    modifier: Modifier = Modifier
) {
    ShreyaCharacterAvatar(
        state = state,
        volumeLevel = volumeLevel,
        size = size,
        modifier = modifier
    )
}
