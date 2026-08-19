package com.example.ui.permissions

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsAccessibility
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.actions.PermissionManager
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberRose
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(
    permissionManager: PermissionManager,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasMic by remember { mutableStateOf(permissionManager.hasRecordAudioPermission()) }
    var hasNotif by remember { mutableStateOf(permissionManager.hasNotificationPermission()) }
    var hasAccessibility by remember { mutableStateOf(permissionManager.isAccessibilityServiceEnabled()) }
    var hasNotifListener by remember { mutableStateOf(permissionManager.isNotificationListenerEnabled()) }

    // Launcher for mic permission
    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMic = granted
    }

    // Launcher for notifications (Android 13+)
    val notifLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotif = granted
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Permissions & Security",
                        color = TextPrimaryDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CyberCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ObsidianDark)
            )
        },
        containerColor = ObsidianDark,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Security Guarantee Banner
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Security Guarantee",
                        tint = CyberCyan,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Security & Privacy First",
                            color = TextPrimaryDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Shreya executes actions strictly within Android official API guidelines. Passwords and sensitive data are never accessed.",
                            color = TextSecondaryDark,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // 1. Microphone Card
            PermissionItemCard(
                icon = Icons.Default.Mic,
                title = "Microphone (Speech-to-Text)",
                hindiSubtitle = "Voice commands sunne ke liye aavashyak hai.",
                description = "Enables speech recognition when you press the microphone button or issue voice requests.",
                isGranted = hasMic,
                onGrantClick = {
                    micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                },
                buttonLabel = "Grant Microphone"
            )

            // 2. Accessibility Service Card
            PermissionItemCard(
                icon = Icons.Default.SettingsAccessibility,
                title = "Shreya Accessibility Service",
                hindiSubtitle = "User ke kehne par screen clicks, scrolling aur typing ke liye.",
                description = "Allows the assistant to assist with UI navigation, back/home gestures, and typing search queries when authorized.",
                isGranted = hasAccessibility,
                onGrantClick = {
                    context.startActivity(permissionManager.openAccessibilitySettingsIntent())
                },
                buttonLabel = "Open Accessibility Settings"
            )

            // 3. Notification Access Card
            PermissionItemCard(
                icon = Icons.Default.Notifications,
                title = "Notifications Permission",
                hindiSubtitle = "Action results aur background updates dikhane ke liye.",
                description = "Shows completion alerts when an automated task completes.",
                isGranted = hasNotif,
                onGrantClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
                buttonLabel = "Grant Notifications"
            )

            // 4. Notification Listener Card
            PermissionItemCard(
                icon = Icons.Default.VolumeUp,
                title = "Notification Listener Service",
                hindiSubtitle = "Notifications ko bol kar sunane ke liye (optional).",
                description = "Enables Shreya to read out your incoming notifications when you explicitly ask.",
                isGranted = hasNotifListener,
                onGrantClick = {
                    context.startActivity(permissionManager.openNotificationListenerSettingsIntent())
                },
                buttonLabel = "Open Listener Settings"
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PermissionItemCard(
    icon: ImageVector,
    title: String,
    hindiSubtitle: String,
    description: String,
    isGranted: Boolean,
    onGrantClick: () -> Unit,
    buttonLabel: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isGranted) CyberEmerald.copy(alpha = 0.3f) else CyberBorder
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isGranted) CyberEmerald.copy(alpha = 0.2f) else CyberPurple.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = if (isGranted) CyberEmerald else CyberPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            color = TextPrimaryDark,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = hindiSubtitle,
                            color = CyberCyan,
                            fontSize = 12.sp
                        )
                    }
                }

                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isGranted) CyberEmerald.copy(alpha = 0.15f) else CyberAmber.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isGranted) "Enabled" else "Required",
                        color = if (isGranted) CyberEmerald else CyberAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                color = TextSecondaryDark,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            if (!isGranted) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onGrantClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = ObsidianDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = buttonLabel, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
