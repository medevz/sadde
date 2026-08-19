package com.example.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class ShreyaNotificationListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "ShreyaNotifListener"
        var instance: ShreyaNotificationListenerService? = null
            private set

        fun isConnected(): Boolean = instance != null

        // In-memory buffer of recent authorized notification snippets
        private val recentNotifications = mutableListOf<NotificationItem>()

        fun getRecentNotifications(): List<NotificationItem> {
            return synchronized(recentNotifications) {
                recentNotifications.toList()
            }
        }
    }

    data class NotificationItem(
        val packageName: String,
        val title: String,
        val text: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        Log.d(TAG, "Notification listener connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        if (instance == this) {
            instance = null
        }
        Log.d(TAG, "Notification listener disconnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val extras = sbn.notification.extras ?: return
        val title = extras.getCharSequence("android.title")?.toString() ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""

        if (title.isNotBlank() || text.isNotBlank()) {
            synchronized(recentNotifications) {
                recentNotifications.add(
                    0,
                    NotificationItem(
                        packageName = sbn.packageName,
                        title = title,
                        text = text
                    )
                )
                if (recentNotifications.size > 20) {
                    recentNotifications.removeAt(recentNotifications.size - 1)
                }
            }
        }
    }
}
