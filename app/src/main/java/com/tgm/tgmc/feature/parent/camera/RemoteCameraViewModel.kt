package com.tgm.tgmc.feature.parent.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tgm.tgmc.core.data.local.TgmcDataStore
import com.tgm.tgmc.core.data.remote.FirebaseManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RemoteCameraUiState(
    val deviceId: String? = null,
    val isStreaming: Boolean = false,
    val selectedCamera: String = "rear",
    val latestFrame: Bitmap? = null,
    val error: String? = null
)

@HiltViewModel
class RemoteCameraViewModel @Inject constructor(
    private val firebaseManager: FirebaseManager,
    private val dataStore: TgmcDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(RemoteCameraUiState())
    val uiState: StateFlow<RemoteCameraUiState> = _uiState.asStateFlow()

    init {
        // Read deviceId directly from local DataStore — no REST API call needed
        viewModelScope.launch {
            val deviceId = dataStore.selectedDeviceId.firstOrNull()
            if (deviceId != null) {
                _uiState.update { it.copy(deviceId = deviceId) }
            } else {
                _uiState.update { it.copy(error = "No child device selected. Go back to the dashboard first.") }
            }
        }

        // Collect incoming camera frames from Firebase
        viewModelScope.launch {
            firebaseManager.cameraFrame.collect { data ->
                val frameBase64 = data.optString("frameBase64")
                if (frameBase64.isNotEmpty()) {
                    try {
                        val decodedBytes = Base64.decode(frameBase64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        _uiState.update { it.copy(latestFrame = bitmap, error = null) }
                    } catch (e: Exception) {
                        Log.e("CameraViewModel", "Failed to decode frame: ${e.message}")
                    }
                }
            }
        }
    }

    fun startCameraStream(camera: String = "rear") {
        val targetId = _uiState.value.deviceId ?: run {
            _uiState.update { it.copy(error = "No child device selected. Go back to the dashboard first.") }
            return
        }
        _uiState.update { it.copy(isStreaming = true, selectedCamera = camera, latestFrame = null) }
        firebaseManager.requestCamera(targetId, camera)
    }

    fun stopCameraStream() {
        val targetId = _uiState.value.deviceId ?: return
        _uiState.update { it.copy(isStreaming = false, latestFrame = null) }
        firebaseManager.requestCamera(targetId, "stop")
    }

    override fun onCleared() {
        super.onCleared()
        stopCameraStream()
    }
}
