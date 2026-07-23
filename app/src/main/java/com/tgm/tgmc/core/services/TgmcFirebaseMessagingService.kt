package com.tgm.tgmc.core.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tgm.tgmc.MainActivity
import com.tgm.tgmc.R
import com.tgm.tgmc.core.data.local.TgmcDataStore
import com.tgm.tgmc.core.data.remote.TgmcApiService
import com.tgm.tgmc.core.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Handles Firebase Cloud Messaging (FCM) push notifications and downstream messages.
 *
 * FCM is used for:
 *  - Parent pushing rules/downtime changes while child is offline
 *  - Background wake-up trigger for child device
 *  - Delivering alert notifications to the Parent App (e.g., geofence breach, SOS)
 */
@AndroidEntryPoint
class TgmcFirebaseMessagingService : FirebaseMessagingService() {

    @Inject lateinit var dataStore: TgmcDataStore
    @Inject lateinit var apiService: TgmcApiService

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "TgmcFCMService"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(TAG, "New FCM Token generated: $token")
        
        scope.launch {
            // Save locally
            dataStore.saveDeviceId(token)
            
            // If device is already paired, upload it to the backend
            val deviceId = dataStore.deviceId.firstOrNull()
            if (deviceId != null) {
                try {
                    Log.i(TAG, "Uploading FCM token for device: $deviceId")
                    apiService.updateFcmToken(deviceId, mapOf("fcmToken" to token))
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to upload new FCM token: ${e.message}")
                }
            }
        }
    }

    /**
     * Called when a message is received from FCM.
     * Determines the message payload type and routes it accordingly.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "FCM message received from: ${message.from}")

        // 1. Data payload (silent commands / rules update / background sync)
        if (message.data.isNotEmpty()) {
            handleDataPayload(message.data)
        }

        // 2. Notification payload (visible alert UI for Parent / Child)
        message.notification?.let {
            showNotification(it.title ?: "Alert", it.body ?: "")
        }
    }

    private fun handleDataPayload(data: Map<String, String>) {
        Log.d(TAG, "FCM Data payload: $data")
        val action = data["action"]
        val deviceId = data["deviceId"]

        when (action) {
            "sync_rules", "rule_update" -> {
                Log.i(TAG, "FCM triggered app/downtime rules synchronization")
                // Start a one-off worker or service command to sync rules from backend
                val intent = Intent(this, MonitoringForegroundService::class.java).apply {
                    this.action = Constants.ACTION_START_MONITORING
                    putExtra("fcm_action", "rule_update")
                    putExtra("fcm_payload", data["payload"])
                }
                startService(intent)
            }
            "ping" -> {
                Log.i(TAG, "FCM wake-up ping received. Triggering background location ping.")
                val intent = Intent(this, MonitoringForegroundService::class.java).apply {
                    this.action = Constants.ACTION_START_MONITORING
                }
                startService(intent)
            }
            "camera_request", "mirror_start", "audio_start" -> {
                Log.i(TAG, "FCM stream command received: $action")
                val intent = Intent(this, MonitoringForegroundService::class.java).apply {
                    this.action = Constants.ACTION_START_MONITORING
                    putExtra("fcm_action", action)
                    putExtra("fcm_payload", data["payload"])
                }
                startService(intent)
            }
            "sos_alert" -> {
                // If this is the parent app, show high priority SOS notification
                val childName = data["childName"] ?: "Child"
                showSosNotification(
                    title = "🚨 SOS Emergency Alert",
                    message = "$childName has triggered an SOS alert!"
                )
            }
        }
    }

    private fun showNotification(title: String, message: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, Constants.CHANNEL_ALERTS)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun showSosNotification(title: String, message: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, Constants.CHANNEL_SOS)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(Constants.NOTIF_ID_SOS, notification)
    }
}
