package com.tgm.tgmc.core.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import android.app.Service
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.google.android.gms.location.*
import com.tgm.tgmc.MainActivity
import com.tgm.tgmc.R
import com.tgm.tgmc.core.data.local.TgmcDataStore
import com.tgm.tgmc.core.data.remote.FirebaseManager
import com.tgm.tgmc.core.domain.model.LocationData
import com.tgm.tgmc.core.domain.repository.LocationRepository
import com.tgm.tgmc.core.util.Constants
import com.tgm.tgmc.core.media.CameraHandler
import com.tgm.tgmc.core.media.AudioHandler
import com.tgm.tgmc.core.media.ScreenMirrorHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service that keeps monitoring active on the Child App.
 *
 * PRD §6.2 (non-negotiable): persistent, visible notification MUST be shown
 * whenever camera, microphone, or screen access is active.
 *
 * Responsibilities:
 *  1. Show mandatory persistent notification
 *  2. Connect to WebSocket (SocketManager) and keep alive
 *  3. Subscribe to location updates and forward to backend
 *  4. Listen for camera/audio/mirror:start events and delegate to handlers
 *  5. Listen for rule:update and apply locally (Room cache)
 *  6. Restart on boot via BootReceiver
 */
@AndroidEntryPoint
class MonitoringForegroundService : Service(), LifecycleOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry

    @Inject lateinit var firebaseManager: FirebaseManager
    @Inject lateinit var dataStore: TgmcDataStore
    @Inject lateinit var locationRepository: LocationRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var deviceId: String? = null

    private var cameraHandler: CameraHandler? = null
    private var audioHandler: AudioHandler? = null
    private var screenMirrorHandler: ScreenMirrorHandler? = null

    companion object {
        private const val TAG = "MonitoringService"
    }


    override fun onCreate() {
        super.onCreate()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        createNotificationChannels()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)

        when (intent?.action) {
            Constants.ACTION_START_MONITORING -> {
                startMonitoring()
                handleFcmIntent(intent)
            }
            Constants.ACTION_STOP_MONITORING  -> stopMonitoring()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
        scope.cancel()
        firebaseManager.disconnect()
        fusedLocationClient.removeLocationUpdates(locationCallback)

        cameraHandler?.stopStreaming()
        audioHandler?.stopRecording()
        screenMirrorHandler?.stopMirroring()

        Log.i(TAG, "Service destroyed")
    }

    // ── Start monitoring ──────────────────────────────────────────
    private fun startMonitoring() {
        Log.i(TAG, "Starting monitoring service")

        // 1. Mandatory foreground notification (PRD §6.2)
        try {
            startForeground(Constants.NOTIF_ID_FOREGROUND, buildMonitoringNotification())
        } catch (e: SecurityException) {
            Log.e(TAG, "Missing FGS permissions for camera/mic/location. Stopping service.", e)
            stopSelf()
            return
        }

        // 2. Load device ID, then connect WebSocket
        scope.launch {
            deviceId = dataStore.deviceId.firstOrNull()
            if (deviceId == null) {
                Log.w(TAG, "No deviceId — service started before pairing? Stopping.")
                stopSelf()
                return@launch
            }

            cameraHandler = CameraHandler(this@MonitoringForegroundService, this@MonitoringForegroundService, firebaseManager, deviceId!!)
            audioHandler = AudioHandler(firebaseManager, deviceId!!)
            screenMirrorHandler = ScreenMirrorHandler(this@MonitoringForegroundService, firebaseManager, deviceId!!)

            // 3. Media handlers don't need persistent connection until requested
            // We only keep the connection active if a stream is running (managed by handlers)
            // But we can notify the backend that we are online
            try {
                val apiService = (applicationContext as com.tgm.tgmc.TgmcApplication)
                    // You would inject TgmcApiService here if it wasn't a Service, or use an EntryPoint
                    // Assuming we have locationRepository which has apiService inside it or we just use REST
            } catch(e: Exception) {}

        }

        // 5. Start GPS location updates
        startLocationUpdates()
    }

    // ── Inbound FCM Command Handlers ────────────────────────────────
    private fun handleFcmIntent(intent: Intent) {
        val fcmAction = intent.getStringExtra("fcm_action") ?: return
        val payloadStr = intent.getStringExtra("fcm_payload") ?: "{}"
        Log.i(TAG, "Processing FCM Action: $fcmAction")

        try {
            val data = org.json.JSONObject(payloadStr)
            when (fcmAction) {
                "camera_request" -> {
                    val camera = data.optString("camera", "rear")
                    if (camera == "stop" || camera == "none") {
                        cameraHandler?.stopStreaming()
                        updateNotification(getString(R.string.monitoring_notification_text))
                    } else {
                        updateNotification(getString(R.string.camera_active_notification))
                        cameraHandler?.startStreaming(camera)
                    }
                }
                "mirror_start" -> {
                    val action = data.optString("action", "start")
                    if (action == "stop") {
                        screenMirrorHandler?.stopMirroring()
                        updateNotification(getString(R.string.monitoring_notification_text))
                    } else {
                        updateNotification(getString(R.string.screen_active_notification))
                        val mockIntent = Intent()
                        screenMirrorHandler?.startMirroring(-1, mockIntent)
                    }
                }
                "audio_start" -> {
                    val action = data.optString("action", "start")
                    if (action == "stop") {
                        audioHandler?.stopRecording()
                        updateNotification(getString(R.string.monitoring_notification_text))
                    } else {
                        updateNotification(getString(R.string.mic_active_notification))
                        audioHandler?.startRecording()
                    }
                }
                "rule_update" -> {
                    scope.launch {
                        // App blocking rules
                        val blockedAppsArray = data.optJSONArray("blockedApps")
                        if (blockedAppsArray != null) {
                            val set = mutableSetOf<String>()
                            for (i in 0 until blockedAppsArray.length()) set.add(blockedAppsArray.getString(i))
                            dataStore.saveBlockedPackages(set)
                        }

                        // Web filter — blocked domains
                        val blockedDomainsArray = data.optJSONArray("blockedDomains")
                        if (blockedDomainsArray != null) {
                            val set = mutableSetOf<String>()
                            for (i in 0 until blockedDomainsArray.length()) set.add(blockedDomainsArray.getString(i).lowercase())
                            dataStore.saveBlockedDomains(set)
                        }

                        // Web filter — blocked keywords
                        val blockedKeywordsArray = data.optJSONArray("blockedKeywords")
                        if (blockedKeywordsArray != null) {
                            val set = mutableSetOf<String>()
                            for (i in 0 until blockedKeywordsArray.length()) set.add(blockedKeywordsArray.getString(i).lowercase())
                            dataStore.saveBlockedKeywords(set)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing FCM payload: ${e.message}")
        }
    }

    // ── GPS location updates ──────────────────────────────────────
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            val dId = deviceId ?: return

            // Send via REST API (persistence)
            scope.launch {
                locationRepository.sendLocationPing(
                    deviceId = dId,
                    location = LocationData(
                        latitude  = location.latitude,
                        longitude = location.longitude,
                        accuracy  = location.accuracy,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Location permission not granted — skipping GPS")
            return
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, Constants.LOCATION_UPDATE_INTERVAL_MS)
            .setMinUpdateDistanceMeters(10f)
            .build()

        fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        Log.i(TAG, "Location updates started (${Constants.LOCATION_UPDATE_INTERVAL_MS}ms interval)")
    }

    // ── Stop monitoring ───────────────────────────────────────────
    private fun stopMonitoring() {
        Log.i(TAG, "Stopping monitoring service")
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    // ── Notification helpers ──────────────────────────────────────
    private fun buildMonitoringNotification(
        contentText: String = getString(R.string.monitoring_notification_text)
    ): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, Constants.CHANNEL_MONITORING)
            .setContentTitle(getString(R.string.monitoring_notification_title))
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openAppIntent)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(Constants.NOTIF_ID_FOREGROUND, buildMonitoringNotification(text))
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(Constants.CHANNEL_MONITORING, getString(R.string.channel_monitoring_name),
                NotificationManager.IMPORTANCE_LOW).apply {
                description = getString(R.string.channel_monitoring_desc)
                setShowBadge(false)
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(Constants.CHANNEL_ALERTS, getString(R.string.channel_alerts_name),
                NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = getString(R.string.channel_alerts_desc)
            }
        )
    }
}
