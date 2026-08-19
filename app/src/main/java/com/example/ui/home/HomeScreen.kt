package com.example.ui.home

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.ActionType
import com.example.domain.model.AssistantState
import com.example.ui.components.ConversationCard
import com.example.ui.components.LeftStatusCards
import com.example.ui.components.NavItem
import com.example.ui.components.NowPlayingCard
import com.example.ui.components.RightActionCards
import com.example.ui.components.ShreyaBottomNav
import com.example.ui.components.ShreyaCharacterAvatar
import com.example.ui.components.SystemStatusDeck
import com.example.ui.components.VoiceWaveformDeck
import com.example.ui.viewmodel.AssistantViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: AssistantViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val assistantState by viewModel.assistantState.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val volumeLevel by viewModel.rmsVolume.collectAsStateWithLifecycle()
    val systemStatus by viewModel.systemTelemetryManager.status.collectAsStateWithLifecycle()

    var showProDialog by remember { mutableStateOf(false) }
    var selectedBottomNav by remember { mutableStateOf(NavItem.HOME) }

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

    val isListening = assistantState is AssistantState.Listening
    val isSpeaking = assistantState is AssistantState.Speaking

    fun handleMicToggle() {
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

    // Modal Navigation Drawer for Hamburger Menu
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF0F0B1A),
                drawerContentColor = Color.White
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Shreya AI Assistant",
                        color = Color(0xFFC084FC),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Version 2.0 • Ultra AI",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color(0xFFFFD700)) },
                        label = { Text("Shreya Pro Upgrade", color = Color.White) },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            showProDialog = true
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFFC084FC)) },
                        label = { Text("App & AI Settings", color = Color.White) },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            onNavigateToSettings()
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )
                }
            }
        }
    ) {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0A0714)),
            containerColor = Color(0xFF0A0714),
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Background Ambient Glow Gradients
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF0E0B1A),
                                    Color(0xFF0A0714),
                                    Color(0xFF080510)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ==========================================
                    // 1. TOP HEADER
                    // ==========================================
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: Hamburger Menu
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF161026))
                                .border(1.dp, Color(0xFF9333EA).copy(alpha = 0.35f), CircleShape)
                                .clickable { coroutineScope.launch { drawerState.open() } },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Center: Title & Subtitle
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "Shreya AI Assistant",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Always here to help you 💜",
                                color = Color(0xFFC084FC).copy(alpha = 0.9f),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        // Right: Pro Button + Settings Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Pro Pill Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF8B5CF6).copy(alpha = 0.3f), Color(0xFF6D28D9).copy(alpha = 0.5f))
                                        )
                                    )
                                    .border(1.dp, Color(0xFFC084FC).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                    .clickable { showProDialog = true }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = "Pro",
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Pro",
                                        color = Color.White,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Settings Button
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF161026))
                                    .border(1.dp, Color(0xFF9333EA).copy(alpha = 0.35f), CircleShape)
                                    .clickable { onNavigateToSettings() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // ==========================================
                    // 2. CENTER SECTION: CHARACTER + SIDE CARDS
                    // ==========================================
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Background Center Halo
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            Color(0xFF7E22CE).copy(alpha = 0.35f),
                                            Color(0xFF3B0764).copy(alpha = 0.15f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        // Center Prominent Avatar
                        ShreyaCharacterAvatar(
                            state = assistantState,
                            volumeLevel = volumeLevel,
                            size = 230.dp,
                            modifier = Modifier.clickable { handleMicToggle() }
                        )

                        // Left Side Cards Deck
                        LeftStatusCards(
                            state = assistantState,
                            volumeLevel = volumeLevel,
                            currentLanguage = settings.language,
                            onLanguageChange = { newLang -> viewModel.setLanguage(newLang) },
                            onMicClick = { handleMicToggle() },
                            modifier = Modifier.align(Alignment.CenterStart)
                        )

                        // Right Side Cards Deck
                        RightActionCards(
                            onOpenYoutube = { viewModel.openApp("youtube") },
                            onOpenGoogle = { viewModel.sendMessage("Google par search karo") },
                            onOpenSettings = { viewModel.openSettings() },
                            onPlayMusic = { viewModel.dispatchMedia(ActionType.PLAY_MEDIA) },
                            onToolClick = { tool ->
                                when (tool) {
                                    "apps" -> viewModel.openSettings("app")
                                    "volume" -> viewModel.adjustVolume(up = true)
                                    "brightness" -> viewModel.openSettings("display")
                                    "wifi" -> viewModel.openSettings("wifi")
                                    "bluetooth" -> viewModel.openSettings("bluetooth")
                                    "torch" -> {
                                        val toggled = viewModel.toggleTorch()
                                        if (toggled) {
                                            Toast.makeText(context, "Torch toggled", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Camera permission needed for torch", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // ==========================================
                    // 3. CONVERSATION CARD
                    // ==========================================
                    val lastAssistantMsg = messages.lastOrNull { it.role == com.example.domain.model.MessageRole.ASSISTANT }?.text
                    ConversationCard(
                        greeting = "Namaste! 🙏",
                        message = lastAssistantMsg ?: "Main Shreya hoon, aap kya karna chahte hain?",
                        onQuickPromptSelected = { prompt ->
                            viewModel.sendMessage(prompt)
                        },
                        onSendMessage = { text ->
                            viewModel.sendMessage(text)
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // ==========================================
                    // 4. VOICE CONTROL DECK (Waveform + Mic)
                    // ==========================================
                    VoiceWaveformDeck(
                        state = assistantState,
                        volumeLevel = volumeLevel,
                        onMicClick = { handleMicToggle() }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // ==========================================
                    // 5. BOTTOM NAVIGATION
                    // ==========================================
                    ShreyaBottomNav(
                        selectedItem = selectedBottomNav,
                        onItemSelected = { item ->
                            selectedBottomNav = item
                            when (item) {
                                NavItem.HOME -> { /* Already on Home */ }
                                NavItem.CHAT -> onNavigateToHistory()
                                NavItem.HISTORY -> onNavigateToHistory()
                                NavItem.PROFILE -> onNavigateToPermissions()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // ==========================================
                    // 6. NOW PLAYING CARD
                    // ==========================================
                    NowPlayingCard(
                        songTitle = "Kesariya - Arijit Singh",
                        subtitle = "Brahmāstra",
                        volumePercent = 60,
                        onPrevious = { viewModel.dispatchMedia(ActionType.PREVIOUS_MEDIA) },
                        onPlayPause = { viewModel.dispatchMedia(ActionType.PLAY_MEDIA) },
                        onNext = { viewModel.dispatchMedia(ActionType.NEXT_MEDIA) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // ==========================================
                    // 7. SYSTEM STATUS TELEMETRY DECK
                    // ==========================================
                    SystemStatusDeck(
                        systemStatus = systemStatus
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Pro Dialog
    if (showProDialog) {
        AlertDialog(
            onDismissRequest = { showProDialog = false },
            containerColor = Color(0xFF130E20),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = "Pro",
                        tint = Color(0xFFFFD700)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Shreya Pro Access", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "✨ Unlocked Features:",
                        color = Color(0xFFC084FC),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("• Gemini Flash 2.0 Ultra Fast Reasoning", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                    Text("• Offline Neural Indian Voice Synthesis", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                    Text("• Full System Accessibility Automation", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                    Text("• Unlimited Smart Actions & Custom Routines", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showProDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA))
                ) {
                    Text("Activate Shreya Pro", color = Color.White)
                }
            },
            dismissButton = {
                IconButton(onClick = { showProDialog = false }) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }
}
