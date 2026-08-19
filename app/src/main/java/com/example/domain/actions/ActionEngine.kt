package com.example.domain.actions

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.domain.model.ActionExecutionStatus
import com.example.domain.model.ActionType
import com.example.domain.model.AssistantAction
import com.example.service.ShreyaAccessibilityService
import com.example.service.TtsService
import kotlinx.coroutines.delay

class ActionEngine(
    private val context: Context,
    private val permissionManager: PermissionManager,
    private val ttsService: TtsService
) {

    companion object {
        private const val TAG = "ActionEngine"
        private const val NOTIFICATION_CHANNEL_ID = "shreya_actions_channel"
    }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Shreya AI Assistant Actions",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for Shreya AI Assistant actions"
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    suspend fun executeAction(action: AssistantAction, language: String = "hi"): AssistantAction {
        val isHindi = language.startsWith("hi")
        try {
            when (action.type) {
                ActionType.OPEN_APP -> {
                    val target = action.target ?: "unknown"
                    val launchIntent = resolveAppLaunchIntent(target)
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(launchIntent)
                        action.status = ActionExecutionStatus.SUCCESS
                        action.executionMessage = if (isHindi) "$target khol diya gaya hai." else "Opened $target."
                    } else {
                        action.status = ActionExecutionStatus.FAILED
                        action.executionMessage = if (isHindi) "$target device par install nahi mila." else "$target was not found on this device."
                    }
                }

                ActionType.LAUNCH_ACTIVITY, ActionType.OPEN_SETTINGS -> {
                    val intent = resolveSettingsIntent(action.target)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    action.status = ActionExecutionStatus.SUCCESS
                    action.executionMessage = if (isHindi) "Settings khol diya gaya hai." else "Opened settings."
                }

                ActionType.VOLUME_UP -> {
                    audioManager?.adjustStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE,
                        AudioManager.FLAG_SHOW_UI
                    )
                    action.status = ActionExecutionStatus.SUCCESS
                    action.executionMessage = if (isHindi) "Volume badha diya gaya hai." else "Volume increased."
                }

                ActionType.VOLUME_DOWN -> {
                    audioManager?.adjustStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_LOWER,
                        AudioManager.FLAG_SHOW_UI
                    )
                    action.status = ActionExecutionStatus.SUCCESS
                    action.executionMessage = if (isHindi) "Volume kam kar diya gaya hai." else "Volume decreased."
                }

                ActionType.SET_VOLUME -> {
                    val max = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
                    val percent = action.value?.toIntOrNull() ?: 50
                    val targetVolume = ((percent / 100f) * max).toInt().coerceIn(0, max)
                    audioManager?.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        targetVolume,
                        AudioManager.FLAG_SHOW_UI
                    )
                    action.status = ActionExecutionStatus.SUCCESS
                    action.executionMessage = if (isHindi) "Volume $percent% par set kiya gaya." else "Volume set to $percent%."
                }

                ActionType.PLAY_MEDIA -> {
                    dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY)
                    action.status = ActionExecutionStatus.SUCCESS
                    action.executionMessage = if (isHindi) "Media play command bhej diya gaya hai." else "Media playback resumed."
                }

                ActionType.PAUSE_MEDIA -> {
                    dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PAUSE)
                    action.status = ActionExecutionStatus.SUCCESS
                    action.executionMessage = if (isHindi) "Media pause kar diya gaya hai." else "Media playback paused."
                }

                ActionType.NEXT_MEDIA -> {
                    dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
                    action.status = ActionExecutionStatus.SUCCESS
                    action.executionMessage = if (isHindi) "Agla gana chalaya ja raha hai." else "Next track command sent."
                }

                ActionType.PREVIOUS_MEDIA -> {
                    dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
                    action.status = ActionExecutionStatus.SUCCESS
                    action.executionMessage = if (isHindi) "Pichhla gana chalaya ja raha hai." else "Previous track command sent."
                }

                ActionType.SET_BRIGHTNESS -> {
                    val displayIntent = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(displayIntent)
                    action.status = ActionExecutionStatus.SUCCESS
                    action.executionMessage = if (isHindi) "Brightness control ke liye Display Settings khola gaya hai." else "Opened Display settings to adjust brightness."
                }

                ActionType.SHOW_NOTIFICATION -> {
                    val title = action.target ?: "Shreya AI Assistant"
                    val message = action.value ?: "Task notification"
                    showNotification(title, message)
                    action.status = ActionExecutionStatus.SUCCESS
                    action.executionMessage = if (isHindi) "Notification dikhaya gaya." else "Notification posted."
                }

                ActionType.SPEAK -> {
                    val textToSpeak = action.target ?: action.value ?: ""
                    if (textToSpeak.isNotBlank()) {
                        ttsService.speak(textToSpeak)
                        action.status = ActionExecutionStatus.SUCCESS
                        action.executionMessage = if (isHindi) "Boli ja rahi hai." else "Spoken via TTS."
                    } else {
                        action.status = ActionExecutionStatus.SKIPPED
                    }
                }

                ActionType.WAIT -> {
                    val ms = action.value?.toLongOrNull() ?: 1000L
                    delay(ms.coerceIn(100L, 5000L))
                    action.status = ActionExecutionStatus.SUCCESS
                    action.executionMessage = "Waited $ms ms."
                }

                ActionType.SEARCH_WEB -> {
                    val query = action.target ?: action.value ?: ""
                    val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                        putExtra(android.app.SearchManager.QUERY, query)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    action.status = ActionExecutionStatus.SUCCESS
                    action.executionMessage = if (isHindi) "'$query' web par search kiya ja raha hai." else "Searching '$query' on the web."
                }

                ActionType.MAKE_CALL -> {
                    val phone = action.target ?: action.value ?: ""
                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$phone")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(dialIntent)
                    action.status = ActionExecutionStatus.SUCCESS
                    action.executionMessage = if (isHindi) "Dialer khola gaya ($phone)." else "Dialer opened for $phone."
                }

                ActionType.SEND_MESSAGE -> {
                    val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("smsto:${action.target ?: ""}")
                        putExtra("sms_body", action.value ?: "")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(smsIntent)
                    action.status = ActionExecutionStatus.SUCCESS
                    action.executionMessage = if (isHindi) "Messages app khola gaya." else "Messaging app opened."
                }

                ActionType.TAKE_PHOTO -> {
                    val camIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(camIntent)
                    action.status = ActionExecutionStatus.SUCCESS
                    action.executionMessage = if (isHindi) "Camera khola gaya." else "Camera opened."
                }

                ActionType.REQUEST_PERMISSION -> {
                    val intent = permissionManager.openAppSettingsIntent()
                    context.startActivity(intent)
                    action.status = ActionExecutionStatus.SUCCESS
                    action.executionMessage = if (isHindi) "Permissions page khola gaya." else "Opened permissions page."
                }

                // Accessibility-driven user-authorized actions
                ActionType.ACCESSIBILITY_CLICK,
                ActionType.ACCESSIBILITY_SCROLL,
                ActionType.ACCESSIBILITY_TYPE_TEXT,
                ActionType.ACCESSIBILITY_BACK,
                ActionType.ACCESSIBILITY_HOME,
                ActionType.ACCESSIBILITY_RECENTS -> {
                    val service = ShreyaAccessibilityService.instance
                    if (service == null) {
                        action.status = ActionExecutionStatus.RESTRICTED
                        action.executionMessage = if (isHindi) {
                            "Shreya Accessibility Service chalu nahi hai. Kripya Settings se enable karein."
                        } else {
                            "Accessibility Service is not enabled. Please enable it in Settings to perform UI actions."
                        }
                    } else {
                        val success = when (action.type) {
                            ActionType.ACCESSIBILITY_CLICK -> {
                                val query = action.target ?: action.value ?: ""
                                service.clickNodeByText(query)
                            }
                            ActionType.ACCESSIBILITY_SCROLL -> {
                                val forward = action.value != "up" && action.value != "backward"
                                service.scroll(forward)
                            }
                            ActionType.ACCESSIBILITY_TYPE_TEXT -> {
                                val textToType = action.value ?: action.target ?: ""
                                service.typeText(action.target, textToType)
                            }
                            ActionType.ACCESSIBILITY_BACK -> service.goBack()
                            ActionType.ACCESSIBILITY_HOME -> service.goHome()
                            ActionType.ACCESSIBILITY_RECENTS -> service.openRecents()
                            else -> false
                        }

                        if (success) {
                            action.status = ActionExecutionStatus.SUCCESS
                            action.executionMessage = if (isHindi) "UI action safalta se poora hua." else "UI action performed successfully."
                        } else {
                            action.status = ActionExecutionStatus.FAILED
                            action.executionMessage = if (isHindi) "Screen par lakshya element nahi mila." else "Target element not found on active screen."
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing action ${action.type}", e)
            action.status = ActionExecutionStatus.FAILED
            action.executionMessage = "Action execution error: ${e.localizedMessage}"
        }
        return action
    }

    private fun resolveAppLaunchIntent(target: String): Intent? {
        val pm = context.packageManager
        val cleanTarget = target.trim().lowercase()

        // 1. Direct package check
        if (cleanTarget.contains(".")) {
            val intent = pm.getLaunchIntentForPackage(cleanTarget)
            if (intent != null) return intent
        }

        // 2. Well-known aliases
        val wellKnownPackages = mapOf(
            "youtube" to "com.google.android.youtube",
            "chrome" to "com.android.chrome",
            "google chrome" to "com.android.chrome",
            "settings" to "com.android.settings",
            "maps" to "com.google.android.apps.maps",
            "google maps" to "com.google.android.apps.maps",
            "whatsapp" to "com.whatsapp",
            "gmail" to "com.google.android.gm",
            "calculator" to "com.google.android.calculator",
            "clock" to "com.google.android.deskclock",
            "spotify" to "com.spotify.music",
            "photos" to "com.google.android.apps.photos",
            "camera" to "com.google.android.GoogleCamera"
        )

        for ((alias, pkg) in wellKnownPackages) {
            if (cleanTarget.contains(alias)) {
                val intent = pm.getLaunchIntentForPackage(pkg)
                if (intent != null) return intent
            }
        }

        // 3. Search all installed launchable applications
        try {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val appList = pm.queryIntentActivities(mainIntent, 0)
            for (resolveInfo in appList) {
                val appLabel = resolveInfo.loadLabel(pm).toString().lowercase()
                val pkgName = resolveInfo.activityInfo.packageName.lowercase()

                if (appLabel.contains(cleanTarget) || pkgName.contains(cleanTarget)) {
                    val intent = pm.getLaunchIntentForPackage(resolveInfo.activityInfo.packageName)
                    if (intent != null) return intent
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to query launchable apps", e)
        }

        return null
    }

    private fun resolveSettingsIntent(target: String?): Intent {
        val clean = target?.lowercase() ?: ""
        return when {
            clean.contains("sound") || clean.contains("volume") || clean.contains("aawaz") -> Intent(Settings.ACTION_SOUND_SETTINGS)
            clean.contains("display") || clean.contains("brightness") -> Intent(Settings.ACTION_DISPLAY_SETTINGS)
            clean.contains("wifi") || clean.contains("wi-fi") || clean.contains("internet") -> Intent(Settings.ACTION_WIFI_SETTINGS)
            clean.contains("bluetooth") -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            clean.contains("accessibility") -> Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            clean.contains("notification") -> Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            clean.contains("app") || clean.contains("application") -> Intent(Settings.ACTION_APPLICATION_SETTINGS)
            else -> Intent(Settings.ACTION_SETTINGS)
        }
    }

    private fun dispatchMediaKey(keyCode: Int) {
        val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
        val upEvent = KeyEvent(KeyEvent.ACTION_UP, keyCode)
        audioManager?.dispatchMediaKeyEvent(downEvent)
        audioManager?.dispatchMediaKeyEvent(upEvent)
    }

    private fun showNotification(title: String, text: String) {
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager?.notify((System.currentTimeMillis() % 10000).toInt(), notification)
    }
}
