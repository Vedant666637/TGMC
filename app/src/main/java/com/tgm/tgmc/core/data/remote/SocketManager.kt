package com.tgm.tgmc.core.data.remote

import android.util.Log
import com.tgm.tgmc.BuildConfig
import com.tgm.tgmc.core.data.local.TgmcDataStore
import com.tgm.tgmc.core.util.Constants
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton Socket.IO client for the Android app.
 *
 * - Manages a single persistent WebSocket connection to the backend
 * - Authenticates with the JWT access token via handshake auth
 * - Exposes cold flows for each WebSocket event the UI/services can collect
 * - Handles reconnection automatically via Socket.IO's built-in retry
 * - Called from MonitoringForegroundService (Child) and feature ViewModels (Parent)
 */
@Singleton
class SocketManager @Inject constructor(
    private val dataStore: TgmcDataStore
) {
    companion object {
        private const val TAG = "SocketManager"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var socket: Socket? = null

    // ── Connection state ───────────────────────────────────────────
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // ── Inbound event flows ────────────────────────────────────────
    private val _locationEvents   = MutableSharedFlow<JSONObject>(extraBufferCapacity = 64)
    private val _cameraRequest    = MutableSharedFlow<JSONObject>(extraBufferCapacity = 8)
    private val _cameraFrame      = MutableSharedFlow<JSONObject>(extraBufferCapacity = 64)
    private val _mirrorStart      = MutableSharedFlow<JSONObject>(extraBufferCapacity = 8)
    private val _mirrorFrame      = MutableSharedFlow<JSONObject>(extraBufferCapacity = 64)
    private val _audioStart       = MutableSharedFlow<JSONObject>(extraBufferCapacity = 8)
    private val _audioChunk       = MutableSharedFlow<JSONObject>(extraBufferCapacity = 64)
    private val _sosTrigger       = MutableSharedFlow<JSONObject>(extraBufferCapacity = 8)
    private val _ruleUpdate       = MutableSharedFlow<JSONObject>(extraBufferCapacity = 16)

    val locationEvents:  SharedFlow<JSONObject> = _locationEvents.asSharedFlow()
    val cameraRequest:   SharedFlow<JSONObject> = _cameraRequest.asSharedFlow()
    val cameraFrame:     SharedFlow<JSONObject> = _cameraFrame.asSharedFlow()
    val mirrorStart:     SharedFlow<JSONObject> = _mirrorStart.asSharedFlow()
    val mirrorFrame:     SharedFlow<JSONObject> = _mirrorFrame.asSharedFlow()
    val audioStart:      SharedFlow<JSONObject> = _audioStart.asSharedFlow()
    val audioChunk:      SharedFlow<JSONObject> = _audioChunk.asSharedFlow()
    val sosTrigger:      SharedFlow<JSONObject> = _sosTrigger.asSharedFlow()
    val ruleUpdate:      SharedFlow<JSONObject> = _ruleUpdate.asSharedFlow()

    // ── Connect ───────────────────────────────────────────────────
    fun connect() {
        scope.launch {
            val token = dataStore.accessToken.firstOrNull() ?: run {
                Log.w(TAG, "No access token — skipping WebSocket connect")
                return@launch
            }

            if (socket?.connected() == true) {
                Log.d(TAG, "Already connected")
                return@launch
            }

            val options = IO.Options.builder()
                .setAuth(mapOf("token" to token))
                .setReconnection(true)
                .setReconnectionAttempts(Int.MAX_VALUE)
                .setReconnectionDelay(1000)
                .setReconnectionDelayMax(30_000)
                .setTimeout(20_000)
                .build()

            socket = IO.socket(URI.create(BuildConfig.WS_URL), options).apply {
                on(Socket.EVENT_CONNECT) {
                    Log.i(TAG, "WebSocket connected")
                    _isConnected.value = true
                }
                on(Socket.EVENT_DISCONNECT) { args ->
                    Log.i(TAG, "WebSocket disconnected: ${args.firstOrNull()}")
                    _isConnected.value = false
                }
                on(Socket.EVENT_CONNECT_ERROR) { args ->
                    Log.e(TAG, "WebSocket error: ${args.firstOrNull()}")
                    _isConnected.value = false
                }

                // ── Inbound events ──────────────────────────────
                on(Constants.WS_DEVICE_LOCATION)  { args -> emit(_locationEvents,   args) }
                on(Constants.WS_CAMERA_REQUEST)   { args -> emit(_cameraRequest,    args) }
                on(Constants.WS_CAMERA_FRAME)     { args -> emit(_cameraFrame,      args) }
                on(Constants.WS_MIRROR_START)     { args -> emit(_mirrorStart,      args) }
                on(Constants.WS_MIRROR_FRAME)     { args -> emit(_mirrorFrame,      args) }
                on(Constants.WS_AUDIO_START)      { args -> emit(_audioStart,       args) }
                on(Constants.WS_AUDIO_CHUNK)      { args -> emit(_audioChunk,       args) }
                on(Constants.WS_SOS_TRIGGER)      { args -> emit(_sosTrigger,       args) }
                on(Constants.WS_RULE_UPDATE)      { args -> emit(_ruleUpdate,       args) }

                connect()
            }
        }
    }

    // ── Outbound emit helpers ─────────────────────────────────────

    /** Child device announces it is online */
    fun joinAsDevice(deviceId: String) {
        emit(Constants.WS_DEVICE_JOIN, JSONObject().put("deviceId", deviceId))
    }

    /** Parent starts watching a device's real-time events */
    fun watchDevice(deviceId: String) {
        emit("watch:device", JSONObject().put("deviceId", deviceId))
    }

    /** Child sends location update */
    fun sendLocation(deviceId: String, lat: Double, lng: Double, accuracy: Float) {
        emit(Constants.WS_DEVICE_LOCATION, JSONObject()
            .put("deviceId", deviceId)
            .put("lat", lat)
            .put("lng", lng)
            .put("accuracy", accuracy))
    }

    /** Parent requests camera snapshot/stream */
    fun requestCamera(deviceId: String, camera: String = "rear") {
        emit(Constants.WS_CAMERA_REQUEST, JSONObject()
            .put("deviceId", deviceId)
            .put("camera", camera))
    }

    /** Child sends camera frame (base64 JPEG) */
    fun sendCameraFrame(deviceId: String, frameBase64: String) {
        emit(Constants.WS_CAMERA_FRAME, JSONObject()
            .put("deviceId", deviceId)
            .put("frameBase64", frameBase64))
    }

    /** Parent requests screen mirror */
    fun requestMirror(deviceId: String, action: String = "start") {
        emit(Constants.WS_MIRROR_START, JSONObject().put("deviceId", deviceId).put("action", action))
    }

    /** Child sends screen frame */
    fun sendMirrorFrame(deviceId: String, frameBase64: String) {
        emit(Constants.WS_MIRROR_FRAME, JSONObject()
            .put("deviceId", deviceId)
            .put("frameBase64", frameBase64))
    }

    /** Parent requests live audio */
    fun requestAudio(deviceId: String, action: String = "start") {
        emit(Constants.WS_AUDIO_START, JSONObject().put("deviceId", deviceId).put("action", action))
    }

    /** Child sends audio chunk */
    fun sendAudioChunk(deviceId: String, chunkBase64: String) {
        emit(Constants.WS_AUDIO_CHUNK, JSONObject()
            .put("deviceId", deviceId)
            .put("chunkBase64", chunkBase64))
    }

    /** Child triggers SOS */
    fun triggerSos(deviceId: String, lat: Double? = null, lng: Double? = null) {
        val payload = JSONObject().put("deviceId", deviceId)
        lat?.let { payload.put("lat", it) }
        lng?.let { payload.put("lng", it) }
        emit(Constants.WS_SOS_TRIGGER, payload)
    }

    /** Child reports app installation */
    fun sendAppInstalled(deviceId: String, packageName: String, appName: String) {
        emit("device:app_installed", JSONObject()
            .put("deviceId", deviceId)
            .put("packageName", packageName)
            .put("appName", appName))
    }

    /** Child reports app uninstallation */
    fun sendAppUninstalled(deviceId: String, packageName: String) {
        emit("device:app_uninstalled", JSONObject()
            .put("deviceId", deviceId)
            .put("packageName", packageName))
    }

    // ── Disconnect ────────────────────────────────────────────────
    fun disconnect() {
        socket?.disconnect()
        socket = null
        _isConnected.value = false
        Log.i(TAG, "WebSocket disconnected by app")
    }

    // ── Internals ─────────────────────────────────────────────────
    private fun emit(event: String, data: JSONObject) {
        if (socket?.connected() == true) {
            socket?.emit(event, data)
        } else {
            Log.w(TAG, "Cannot emit '$event' — not connected")
        }
    }

    private fun emit(flow: MutableSharedFlow<JSONObject>, args: Array<Any?>) {
        scope.launch {
            try {
                val json = args.firstOrNull() as? JSONObject ?: JSONObject()
                flow.emit(json)
            } catch (e: Exception) {
                Log.e(TAG, "Error emitting to flow: ${e.message}")
            }
        }
    }
}
