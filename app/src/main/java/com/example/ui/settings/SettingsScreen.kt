package com.example.ui.settings

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.AiProviderType
import com.example.service.TtsService
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPink
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    ttsService: TtsService,
    onNavigateBack: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var isApiKeyVisible by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianDark),
        containerColor = ObsidianDark,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings & AI Brain",
                        color = TextPrimaryDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CyberCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ObsidianDark),
                modifier = Modifier.statusBarsPadding()
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. LANGUAGE SELECTOR ---
            SettingsSectionHeader(icon = Icons.Default.Language, title = "Language Preference")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.setLanguage("hi") }
                            .padding(8.dp)
                    ) {
                        RadioButton(
                            selected = settings.language.startsWith("hi"),
                            onClick = { viewModel.setLanguage("hi") },
                            colors = RadioButtonDefaults.colors(selectedColor = CyberCyan)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Hindi / Hinglish (हिंदी)", color = TextPrimaryDark, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("Default language for voice and responses", color = TextMutedDark, fontSize = 12.sp)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.setLanguage("en") }
                            .padding(8.dp)
                    ) {
                        RadioButton(
                            selected = settings.language.startsWith("en"),
                            onClick = { viewModel.setLanguage("en") },
                            colors = RadioButtonDefaults.colors(selectedColor = CyberCyan)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("English (Indian / International)", color = TextPrimaryDark, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("English voice recognition and responses", color = TextMutedDark, fontSize = 12.sp)
                        }
                    }
                }
            }

            // --- 2. AI BRAIN PROVIDER ---
            SettingsSectionHeader(icon = Icons.Default.Psychology, title = "AI Brain Engine")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AiProviderType.values().forEach { provider ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (settings.aiProvider == provider) CyberSurfaceVariant else CyberSurface)
                                .clickable { viewModel.setAiProvider(provider) }
                                .padding(10.dp)
                        ) {
                            RadioButton(
                                selected = settings.aiProvider == provider,
                                onClick = { viewModel.setAiProvider(provider) },
                                colors = RadioButtonDefaults.colors(selectedColor = CyberCyan)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = provider.displayName,
                                    color = TextPrimaryDark,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = when (provider) {
                                        AiProviderType.GEMINI -> "Gemini 3.5 Flash via REST API (Automatic Offline Fallback)"
                                        AiProviderType.OPENAI_COMPATIBLE -> "Custom OpenAI-compatible LLM endpoint"
                                        AiProviderType.OFFLINE_RULE_ENGINE -> "Fast zero-latency on-device rule engine (No internet needed)"
                                        AiProviderType.MOCK -> "Mock engine for instant testing without API quota"
                                    },
                                    color = TextMutedDark,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // API Key Config for Cloud Providers
                    if (settings.aiProvider == AiProviderType.GEMINI || settings.aiProvider == AiProviderType.OPENAI_COMPATIBLE) {
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = settings.customApiKey,
                            onValueChange = { viewModel.setApiKey(it) },
                            label = { Text("Custom API Key (Optional)") },
                            placeholder = { Text("Enter your API Key if overriding default") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                                    Icon(
                                        imageVector = if (isApiKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle API Key Visibility",
                                        tint = CyberCyan
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = CyberBorder,
                                focusedTextColor = TextPrimaryDark,
                                unfocusedTextColor = TextPrimaryDark
                            ),
                            singleLine = true
                        )

                        if (settings.aiProvider == AiProviderType.OPENAI_COMPATIBLE) {
                            OutlinedTextField(
                                value = settings.customApiBaseUrl,
                                onValueChange = { viewModel.setApiBaseUrl(it) },
                                label = { Text("Base URL (e.g. https://api.openai.com/v1)") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberCyan,
                                    unfocusedBorderColor = CyberBorder,
                                    focusedTextColor = TextPrimaryDark,
                                    unfocusedTextColor = TextPrimaryDark
                                ),
                                singleLine = true
                            )
                        }
                    }
                }
            }

            // --- 3. SPEECH & VOICE SYNTHESIS ---
            SettingsSectionHeader(icon = Icons.Default.RecordVoiceOver, title = "Voice & Speech Controls")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Speed slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Speech Speed", color = TextPrimaryDark, fontSize = 13.sp)
                            Text(String.format("%.1fx", settings.speechRate), color = CyberCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = settings.speechRate,
                            onValueChange = { viewModel.setSpeechRate(it) },
                            valueRange = 0.6f..1.6f,
                            steps = 9,
                            colors = SliderDefaults.colors(
                                thumbColor = CyberCyan,
                                activeTrackColor = CyberCyan
                            )
                        )
                    }

                    // Pitch slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Voice Pitch", color = TextPrimaryDark, fontSize = 13.sp)
                            Text(String.format("%.1fx", settings.speechPitch), color = CyberPurple, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = settings.speechPitch,
                            onValueChange = { viewModel.setSpeechPitch(it) },
                            valueRange = 0.7f..1.5f,
                            steps = 7,
                            colors = SliderDefaults.colors(
                                thumbColor = CyberPurple,
                                activeTrackColor = CyberPurple
                            )
                        )
                    }

                    Button(
                        onClick = {
                            val sample = if (settings.language.startsWith("hi")) {
                                "Namaste! Main Shreya hoon, aapki AI assistant."
                            } else {
                                "Hello! I am Shreya, your personal AI voice assistant."
                            }
                            ttsService.speak(sample, settings.language, settings.speechRate, settings.speechPitch)
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = ObsidianDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Voice Sample")
                    }
                }
            }

            // --- 4. PREFERENCES & FEEDBACK ---
            SettingsSectionHeader(icon = Icons.Default.Vibration, title = "Haptics & Wake Word")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Haptic Vibration Feedback", color = TextPrimaryDark, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("Short vibration pulse on listening & actions", color = TextMutedDark, fontSize = 12.sp)
                        }
                        Switch(
                            checked = settings.vibrationFeedback,
                            onCheckedChange = { viewModel.setVibrationFeedback(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyberCyan, checkedTrackColor = CyberCyan.copy(alpha = 0.5f))
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Wake Word ('Hey Shreya')", color = TextPrimaryDark, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("Trigger listening hands-free when app is in foreground", color = TextMutedDark, fontSize = 12.sp)
                        }
                        Switch(
                            checked = settings.wakeWordEnabled,
                            onCheckedChange = { viewModel.setWakeWordEnabled(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyberPink, checkedTrackColor = CyberPink.copy(alpha = 0.5f))
                        )
                    }
                }
            }

            // --- 5. PERMISSIONS & PRIVACY SHORTCUTS ---
            SettingsSectionHeader(icon = Icons.Default.Security, title = "Security & Privacy")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onNavigateToPermissions() }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Manage System Permissions", color = TextPrimaryDark, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("Microphone, Accessibility, and Notifications", color = TextMutedDark, fontSize = 12.sp)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onNavigateToPrivacy() }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = CyberEmerald, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Privacy Policy & Security Guarantee", color = TextPrimaryDark, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("Learn how your data and speech stay protected", color = TextMutedDark, fontSize = 12.sp)
                        }
                    }
                }
            }

            // App Version Footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Shreya AI Assistant v1.0.0 • Production Quality",
                    color = TextMutedDark,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)) {
        Icon(imageVector = icon, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = CyberCyan,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
