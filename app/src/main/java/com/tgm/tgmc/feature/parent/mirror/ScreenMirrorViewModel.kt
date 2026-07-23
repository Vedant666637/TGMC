package com.tgm.tgmc.feature.parent.mirror

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tgm.tgmc.core.data.remote.SocketManager
import com.tgm.tgmc.core.domain.repository.DeviceRepository
import com.tgm.tgmc.core.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScreenMirrorUiState(
    val deviceId: String? = null,
    val isStreaming: Boolean = false,
    val latestFrame: Bitmap? = null,
    val error: String? = null
)

@HiltViewModel
class ScreenMirrorViewModel @Inject constructor(
    private val socketManager: SocketManager,
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScreenMirrorUiState())
    val uiState: StateFlow<ScreenMirrorUiState> = _uiState.asStateFlow()

    init {
        // Resolve active target child device
        viewModelScope.launch {
            when (val result = deviceRepository.getDevices()) {
                is Result.Success -> {
                    val activeDevice = result.data.firstOrNull { it.isOnline } ?: result.data.firstOrNull()
                    _uiState.update { it.copy(deviceId = activeDevice?.deviceId) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(error = "No paired child devices found") }
                }
                is Result.Loading -> { /* Handled via flow or ignore */ }
            }
        }

        // Collect incoming mirror frames
        viewModelScope.launch {
            socketManager.mirrorFrame.collect { data ->
                val frameBase64 = data.optString("frameBase64")
                if (frameBase64.isNotEmpty()) {
                    try {
                        val decodedBytes = Base64.decode(frameBase64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        _uiState.update { it.copy(latestFrame = bitmap, error = null) }
                    } catch (e: Exception) {
                        Log.e("ScreenMirrorViewModel", "Failed to decode mirror frame: ${e.message}")
                    }
                }
            }
        }
    }

    fun startMirroring() {
        val targetId = _uiState.value.deviceId ?: run {
            _uiState.update { it.copy(error = "No active device connection") }
            return
        }

        _uiState.update { it.copy(isStreaming = true, latestFrame = null) }
        // Request start mirroring via Socket.IO
        socketManager.requestMirror(targetId)
    }

    fun stopMirroring() {
        val targetId = _uiState.value.deviceId ?: return
        _uiState.update { it.copy(isStreaming = false, latestFrame = null) }
        socketManager.requestMirror(targetId, "stop")
    }

    override fun onCleared() {
        super.onCleared()
        stopMirroring()
    }
}
