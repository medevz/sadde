package com.example.ui.permissions

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsAccessibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.actions.PermissionManager
import com.example.domain.model.AssistantState
import com.example.ui.components.FuturisticAvatar
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPink
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.viewmodel.SettingsViewModel

@Composable
fun OnboardingScreen(
    settingsViewModel: SettingsViewModel,
    onCompleteOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val permissionManager = remember { PermissionManager(context) }
    var currentStep by remember { mutableIntStateOf(0) }

    var hasMic by remember { mutableStateOf(permissionManager.hasRecordAudioPermission()) }
    var hasAccessibility by remember { mutableStateOf(permissionManager.isAccessibilityServiceEnabled()) }

    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMic = granted
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Step Indicator Dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(width = if (currentStep == index) 24.dp else 8.dp, height = 8.dp)
                        .clip(CircleShape)
                        .background(if (currentStep == index) CyberCyan else CyberSurface)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                slideInHorizontally { width -> width } togetherWith slideOutHorizontally { width -> -width }
            },
            modifier = Modifier.weight(1f),
            label = "onboarding_step"
        ) { step ->
            when (step) {
                0 -> WelcomeStep()
                1 -> CapabilitiesStep()
                2 -> PermissionsSetupStep(
                    hasMic = hasMic,
                    hasAccessibility = hasAccessibility,
                    onGrantMic = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    onOpenAccessibility = { context.startActivity(permissionManager.openAccessibilitySettingsIntent()) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentStep > 0) {
                TextButton(onClick = { currentStep -= 1 }) {
                    Text("Peeche", color = TextSecondaryDark)
                }
            } else {
                Spacer(modifier = Modifier.width(60.dp))
            }

            Button(
                onClick = {
                    if (currentStep < 2) {
                        currentStep += 1
                    } else {
                        settingsViewModel.setOnboardingCompleted(true)
                        onCompleteOnboarding()
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberCyan,
                    contentColor = ObsidianDark
                ),
                modifier = Modifier.testTag("onboarding_next_button")
            ) {
                Text(
                    text = if (currentStep == 2) "Aage Badhein (Start)" else "Continue",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = if (currentStep == 2) Icons.Default.Check else Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun WelcomeStep() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        FuturisticAvatar(
            state = AssistantState.Idle,
            size = 180.dp
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Namaste! 👋\nMain Shreya AI Assistant hoon.",
            color = TextPrimaryDark,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 30.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Aapka ultra-fast Android personal voice & text assistant. Aapki aawaz ya text se phone ke tasks aasani se karein.",
            color = TextSecondaryDark,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun CapabilitiesStep() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "What Shreya Can Do",
            color = TextPrimaryDark,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Aapke rozana ke phone tasks ko aasan banayein:",
            color = TextSecondaryDark,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        CapabilityCard(
            icon = Icons.Default.Apps,
            title = "Open Applications",
            description = "\"YouTube kholo\", \"Chrome kholo\", \"WhatsApp open karo\""
        )

        Spacer(modifier = Modifier.height(12.dp))

        CapabilityCard(
            icon = Icons.Default.VolumeUp,
            title = "Sound & Media Controls",
            description = "\"Volume badhao\", \"Music pause karo\", \"Agla gana chalao\""
        )

        Spacer(modifier = Modifier.height(12.dp))

        CapabilityCard(
            icon = Icons.Default.SettingsAccessibility,
            title = "UI Interaction Assistance",
            description = "\"Search box mein maths likho\", \"Back jao\", \"Click submit\""
        )
    }
}

@Composable
private fun CapabilityCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(CyberPurple.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    color = TextPrimaryDark,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = CyberCyan,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun PermissionsSetupStep(
    hasMic: Boolean,
    hasAccessibility: Boolean,
    onGrantMic: () -> Unit,
    onOpenAccessibility: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Permissions Setup",
            color = TextPrimaryDark,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Voice sunne aur UI assistance ke liye optional permissions enable karein.",
            color = TextSecondaryDark,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Microphone", color = TextPrimaryDark, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (hasMic) "✓ Granted" else "Not Granted",
                        color = if (hasMic) CyberEmerald else CyberPink,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text("Voice commands recognize karne ke liye.", color = TextSecondaryDark, fontSize = 12.sp)
                if (!hasMic) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onGrantMic,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Grant Microphone Access", color = CyberCyan)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Accessibility Service", color = TextPrimaryDark, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (hasAccessibility) "✓ Enabled" else "Disabled",
                        color = if (hasAccessibility) CyberEmerald else CyberPink,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text("Screen click, scroll aur text field typing ke liye.", color = TextSecondaryDark, fontSize = 12.sp)
                if (!hasAccessibility) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onOpenAccessibility,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Enable in Accessibility Settings", color = CyberPurple)
                    }
                }
            }
        }
    }
}
