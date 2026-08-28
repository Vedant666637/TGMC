package com.tgm.tgmc.core.data.remote

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tgm.tgmc.core.data.local.TgmcDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Realtime Database Manager for TGM-C.
 * Replaces Socket.IO for signaling, commands, and location.
 */
@Singleton
class FirebaseManager @Inject constructor(
    private val dataStore: TgmcDataStore
) {
    companion object {
        private const val TAG = "FirebaseManager"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val database = FirebaseDatabase.getInstance().reference

    // ── Connection state ───────────────────────────────────────────
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // ── Real-time child device online/offline status ───────────────
    private val _deviceOnlineStatus = MutableStateFlow(false)
    val deviceOnlineStatus: StateFlow<Boolean> = _deviceOnlineStatus.asStateFlow()

    // ── Inbound event flows ────────────────────────────────────────
    private val _locationEvents   = MutableSharedFlow<JSONObject>(extraBufferCapacity = 64)
    private val _sosTrigger       = MutableSharedFlow<JSONObject>(extraBufferCapacity = 8)
    private val _ruleUpdate       = MutableSharedFlow<JSONObject>(extraBufferCapacity = 16)
    
    // WebRTC Signaling Events
    private val _webrtcOffer      = MutableSharedFlow<JSONObject>(extraBufferCapacity = 8)
    private val _webrtcAnswer     = MutableSharedFlow<JSONObject>(extraBufferCapacity = 8)
    private val _webrtcIce        = MutableSharedFlow<JSONObject>(extraBufferCapacity = 64)
    private val _cameraRequest    = MutableSharedFlow<JSONObject>(extraBufferCapacity = 8)
    private val _mirrorStart      = MutableSharedFlow<JSONObject>(extraBufferCapacity = 8)
    private val _audioStart       = MutableSharedFlow<JSONObject>(extraBufferCapacity = 8)

    val locationEvents:  SharedFlow<JSONObject> = _locationEvents.asSharedFlow()
    val sosTrigger:      SharedFlow<JSONObject> = _sosTrigger.asSharedFlow()
    val ruleUpdate:      SharedFlow<JSONObject> = _ruleUpdate.asSharedFlow()
    
    val webrtcOffer:     SharedFlow<JSONObject> = _webrtcOffer.asSharedFlow()
    val webrtcAnswer:    SharedFlow<JSONObject> = _webrtcAnswer.asSharedFlow()
    val webrtcIce:       SharedFlow<JSONObject> = _webrtcIce.asSharedFlow()
    val cameraRequest:   SharedFlow<JSONObject> = _cameraRequest.asSharedFlow()
    val mirrorStart:     SharedFlow<JSONObject> = _mirrorStart.asSharedFlow()
    val audioStart:      SharedFlow<JSONObject> = _audioStart.asSharedFlow()

    // Deprecated for WebRTC
    val cameraFrame: SharedFlow<JSONObject> = MutableSharedFlow<JSONObject>().asSharedFlow()
    val mirrorFrame: SharedFlow<JSONObject> = MutableSharedFlow<JSONObject>().asSharedFlow()
    val audioChunk: SharedFlow<JSONObject> = MutableSharedFlow<JSONObject>().asSharedFlow()

    private var currentWatchedDeviceId: String? = null
    private var watchingListener: ValueEventListener? = null

    fun connect() {
        // Firebase handles connection automatically, but we track state
        val connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                _isConnected.value = connected
                Log.i(TAG, "Firebase connected: $connected")
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Connection listener cancelled: ${error.message}")
            }
        })
    }

    // ── Child Methods ──────────────────────────────────────────────

    fun joinAsDevice(deviceId: String) {
        val deviceRef = database.child("devices").child(deviceId)
        deviceRef.child("status").setValue("online")
        deviceRef.child("status").onDisconnect().setValue("offline")
        
        // Listen for incoming commands (camera, mirror, audio)
        deviceRef.child("commands").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val command = snapshot.child("type").getValue(String::class.java) ?: return
                val payload = snapshot.child("payload").getValue(String::class.java) ?: "{}"
                val json = JSONObject(payload)
                
                scope.launch {
                    when (command) {
                        "camera_request" -> _cameraRequest.emit(json)
                        "mirror_start" -> _mirrorStart.emit(json)
                        "audio_start" -> _audioStart.emit(json)
                        "rule_update" -> _ruleUpdate.emit(json)
                    }
                }
                // Clear command after processing
                deviceRef.child("commands").removeValue()
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Listen for WebRTC Signaling (Offers, ICE)
        deviceRef.child("signaling").child("to_child").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val type = snapshot.child("type").getValue(String::class.java) ?: return
                val payload = snapshot.child("payload").getValue(String::class.java) ?: "{}"
                val json = JSONObject(payload)
                scope.launch {
                    when (type) {
                        "offer" -> _webrtcOffer.emit(json)
                        "ice" -> _webrtcIce.emit(json)
                    }
                }
                snapshot.ref.removeValue()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun sendLocation(deviceId: String, lat: Double, lng: Double, accuracy: Float) {
        val loc = mapOf("lat" to lat, "lng" to lng, "accuracy" to accuracy, "timestamp" to System.currentTimeMillis())
        database.child("devices").child(deviceId).child("location").setValue(loc)
    }

    fun triggerSos(deviceId: String, lat: Double? = null, lng: Double? = null) {
        val sos = mapOf("lat" to lat, "lng" to lng, "timestamp" to System.currentTimeMillis())
        database.child("devices").child(deviceId).child("sos").setValue(sos)
    }

    fun sendAppInstalled(deviceId: String, packageName: String, appName: String) {
        val appData = mapOf("packageName" to packageName, "appName" to appName, "action" to "installed")
        database.child("devices").child(deviceId).child("apps").push().setValue(appData)
    }

    fun sendAppUninstalled(deviceId: String, packageName: String) {
        val appData = mapOf("packageName" to packageName, "action" to "uninstalled")
        database.child("devices").child(deviceId).child("apps").push().setValue(appData)
    }

    // ── Parent Methods ─────────────────────────────────────────────

    fun watchDevice(deviceId: String) {
        currentWatchedDeviceId?.let { oldId ->
            watchingListener?.let { listener ->
                database.child("devices").child(oldId).removeEventListener(listener)
            }
        }

        currentWatchedDeviceId = deviceId
        watchingListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Handle location
                snapshot.child("location").value?.let { loc ->
                    val map = loc as? Map<*, *>
                    if (map != null) {
                        val lat = (map["lat"] as? Number)?.toDouble() ?: 0.0
                        val lng = (map["lng"] as? Number)?.toDouble() ?: 0.0
                        val acc = (map["accuracy"] as? Number)?.toFloat() ?: 0f
                        scope.launch {
                            _locationEvents.emit(JSONObject().put("lat", lat).put("lng", lng).put("accuracy", acc))
                        }
                    }
                }

                // Handle SOS
                snapshot.child("sos").value?.let { sos ->
                    val map = sos as? Map<*, *>
                    if (map != null) {
                        val lat = (map["lat"] as? Number)?.toDouble()
                        val lng = (map["lng"] as? Number)?.toDouble()
                        scope.launch {
                            val json = JSONObject()
                            lat?.let { json.put("lat", it) }
                            lng?.let { json.put("lng", it) }
                            _sosTrigger.emit(json)
                        }
                    }
                }

                // Handle real-time online/offline status
                val status = snapshot.child("status").getValue(String::class.java)
                _deviceOnlineStatus.value = (status == "online")
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        database.child("devices").child(deviceId).addValueEventListener(watchingListener!!)

        // Listen for WebRTC Signaling (Answers, ICE)
        database.child("devices").child(deviceId).child("signaling").child("to_parent").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val type = snapshot.child("type").getValue(String::class.java) ?: return
                val payload = snapshot.child("payload").getValue(String::class.java) ?: "{}"
                val json = JSONObject(payload)
                scope.launch {
                    when (type) {
                        "answer" -> _webrtcAnswer.emit(json)
                        "ice" -> _webrtcIce.emit(json)
                    }
                }
                snapshot.ref.removeValue()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun requestCamera(deviceId: String, camera: String = "rear") {
        sendCommand(deviceId, "camera_request", JSONObject().put("camera", camera))
    }

    fun requestMirror(deviceId: String, action: String = "start") {
        sendCommand(deviceId, "mirror_start", JSONObject().put("action", action))
    }

    fun requestAudio(deviceId: String, action: String = "start") {
        sendCommand(deviceId, "audio_start", JSONObject().put("action", action))
    }

    private fun sendCommand(deviceId: String, type: String, payload: JSONObject) {
        val command = mapOf("type" to type, "payload" to payload.toString())
        database.child("devices").child(deviceId).child("commands").setValue(command)
    }

    // ── Deprecated Streaming Methods (Pending WebRTC) ─────────────────
    fun sendCameraFrame(deviceId: String, frameBase64: String) {
        Log.w(TAG, "sendCameraFrame called but deprecated! Use WebRTC.")
    }

    fun sendMirrorFrame(deviceId: String, frameBase64: String) {
        Log.w(TAG, "sendMirrorFrame called but deprecated! Use WebRTC.")
    }

    fun sendAudioChunk(deviceId: String, chunkBase64: String) {
        Log.w(TAG, "sendAudioChunk called but deprecated! Use WebRTC.")
    }

    // ── WebRTC Signaling Helpers ───────────────────────────────────

    fun sendWebrtcOffer(deviceId: String, sdp: String) {
        val data = mapOf("type" to "offer", "payload" to JSONObject().put("sdp", sdp).toString())
        database.child("devices").child(deviceId).child("signaling").child("to_child").setValue(data)
    }

    fun sendWebrtcAnswer(deviceId: String, sdp: String) {
        val data = mapOf("type" to "answer", "payload" to JSONObject().put("sdp", sdp).toString())
        database.child("devices").child(deviceId).child("signaling").child("to_parent").setValue(data)
    }

    fun sendWebrtcIceCandidate(deviceId: String, toChild: Boolean, sdpMid: String, sdpMLineIndex: Int, sdp: String) {
        val target = if (toChild) "to_child" else "to_parent"
        val payload = JSONObject().put("sdpMid", sdpMid).put("sdpMLineIndex", sdpMLineIndex).put("sdp", sdp)
        val data = mapOf("type" to "ice", "payload" to payload.toString())
        database.child("devices").child(deviceId).child("signaling").child(target).setValue(data)
    }

    fun disconnect() {
        currentWatchedDeviceId?.let { oldId ->
            watchingListener?.let { listener ->
                database.child("devices").child(oldId).removeEventListener(listener)
            }
        }
        watchingListener = null
        currentWatchedDeviceId = null
    }
}
