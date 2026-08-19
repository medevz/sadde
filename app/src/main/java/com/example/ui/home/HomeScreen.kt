package com.example.ui.home

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.AssistantState
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.ChatBubble
import com.example.ui.components.FuturisticAvatar
import com.example.ui.components.QuickActionPills
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPink
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberRose
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.viewmodel.AssistantViewModel

@Composable
fun HomeScreen(
    viewModel: AssistantViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val assistantState by viewModel.assistantState.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val liveTranscript by viewModel.liveTranscript.collectAsStateWithLifecycle()
    val suggestedPrompts by viewModel.suggestedPrompts.collectAsStateWithLifecycle()
    val volumeLevel by viewModel.rmsVolume.collectAsStateWithLifecycle()

    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Request Audio Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        } else {
            Toast.makeText(context, "Microphone permission is needed for voice assistant.", Toast.LENGTH_SHORT).show()
        }
    }

    // Auto scroll chat when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val isListening = assistantState is AssistantState.Listening
    val isSpeaking = assistantState is AssistantState.Speaking
    val isProcessing = assistantState is AssistantState.Processing

    // Infinite pulse transition for microphone button when active
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val micPulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianDark),
        containerColor = ObsidianDark,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- TOP BAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(CyberSurface)
                        .border(1.dp, CyberBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isListening -> CyberCyan
                                        isSpeaking -> CyberPink
                                        isProcessing -> CyberPurple
                                        else -> CyberEmerald
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                isListening -> "Listening..."
                                isSpeaking -> "Speaking..."
                                isProcessing -> "Thinking..."
                                else -> settings.aiProvider.displayName.substringBefore(" ")
                            },
                            color = TextPrimaryDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Action Bar Icons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Accessibility status warning badge if not enabled
                    if (!viewModel.permissionManager.isAccessibilityServiceEnabled()) {
                        IconButton(
                            onClick = onNavigateToPermissions,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Accessibility Service not enabled",
                                tint = CyberAmber,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onNavigateToHistory,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Conversation History",
                            tint = CyberCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = onNavigateToPermissions,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("permissions_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Permissions",
                            tint = CyberPurple,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextSecondaryDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // --- MAIN CONTENT AREA ---
            if (messages.isEmpty()) {
                // Empty State / Welcome Screen with Large Futuristic Avatar
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    FuturisticAvatar(
                        state = assistantState,
                        volumeLevel = volumeLevel,
                        size = 180.dp,
                        modifier = Modifier.clickable {
                            if (isSpeaking) {
                                viewModel.stopSpeaking()
                            } else if (isListening) {
                                viewModel.stopListening()
                            } else {
                                if (viewModel.permissionManager.hasRecordAudioPermission()) {
                                    viewModel.startListening()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = if (settings.language.startsWith("hi")) {
                            "Namaste! Main Shreya hoon.\nAap kya karna chahte hain?"
                        } else {
                            "Hello! I am Shreya.\nHow can I help you today?"
                        },
                        color = TextPrimaryDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 26.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Voice ya text commands se phone control karein.",
                        color = TextMutedDark,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    if (isListening || isSpeaking) {
                        Spacer(modifier = Modifier.height(16.dp))
                        AudioWaveformVisualizer(
                            volumeLevel = volumeLevel,
                            isSpeaking = isSpeaking
                        )
                    }

                    if (liveTranscript.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "\"$liveTranscript\"",
                            color = CyberCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Chat Conversation View with Mini Avatar Header
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // Small header avatar & live visualizer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        FuturisticAvatar(
                            state = assistantState,
                            volumeLevel = volumeLevel,
                            size = 46.dp
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        if (isListening || isSpeaking) {
                            AudioWaveformVisualizer(
                                volumeLevel = volumeLevel,
                                isSpeaking = isSpeaking,
                                height = 20.dp
                            )
                        } else if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = CyberPurple,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Shreya is thinking...", color = CyberPurple, fontSize = 12.sp)
                        } else {
                            Text(
                                text = "Shreya Assistant Ready",
                                color = TextMutedDark,
                                fontSize = 12.sp
                            )
                        }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(messages, key = { it.id }) { message ->
                            ChatBubble(
                                message = message,
                                onReplayAudio = { text -> viewModel.sendMessage(text) },
                                onDelete = { id -> viewModel.deleteMessage(id) },
                                onActionClick = { action -> viewModel.executeSingleAction(action) }
                            )
                        }
                    }
                }
            }

            // --- SUGGESTION PILLS ---
            QuickActionPills(
                prompts = suggestedPrompts,
                onPromptSelected = { prompt ->
                    textInput = ""
                    viewModel.sendMessage(prompt)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            // --- BOTTOM CONTROL DECK ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Text Input Field
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(
                            text = if (isListening) "Listening to you..." else "Type command (e.g. YouTube kholo)...",
                            color = TextMutedDark,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("text_input"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CyberSurface,
                        unfocusedContainerColor = CyberSurface,
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CyberBorder,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    singleLine = true,
                    trailingIcon = {
                        if (textInput.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    val text = textInput
                                    textInput = ""
                                    viewModel.sendMessage(text)
                                },
                                modifier = Modifier.testTag("send_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send Command",
                                    tint = CyberCyan
                                )
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Big Animated Microphone Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(56.dp)
                ) {
                    if (isListening) {
                        // Outer pulsing ripple ring
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .scale(micPulseScale)
                                .clip(CircleShape)
                                .background(CyberCyan.copy(alpha = 0.25f))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(
                                if (isListening) {
                                    Brush.linearGradient(listOf(CyberRose, CyberPink))
                                } else if (isSpeaking) {
                                    Brush.linearGradient(listOf(CyberPurple, CyberPink))
                                } else {
                                    Brush.linearGradient(listOf(CyberCyan, CyberPurple))
                                }
                            )
                            .clickable {
                                if (isSpeaking) {
                                    viewModel.stopSpeaking()
                                } else if (isListening) {
                                    viewModel.stopListening()
                                } else {
                                    if (viewModel.permissionManager.hasRecordAudioPermission()) {
                                        viewModel.startListening()
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            }
                            .testTag("mic_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isSpeaking -> Icons.Default.Stop
                                isListening -> Icons.Default.Mic
                                else -> Icons.Default.Mic
                            },
                            contentDescription = if (isListening) "Stop Listening" else "Start Voice Input",
                            tint = if (isListening) Color.White else ObsidianDark,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}
