package com.tgm.tgmc.feature.parent.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tgm.tgmc.core.data.remote.FirebaseManager
import com.tgm.tgmc.core.domain.repository.DeviceRepository
import com.tgm.tgmc.core.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LiveAudioUiState(
    val deviceId: String? = null,
    val isStreaming: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LiveAudioViewModel @Inject constructor(
    private val firebaseManager: FirebaseManager,
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveAudioUiState())
    val uiState: StateFlow<LiveAudioUiState> = _uiState.asStateFlow()

    private var audioTrack: AudioTrack? = null

    companion object {
        private const val TAG = "LiveAudioViewModel"
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_OUT = AudioFormat.CHANNEL_OUT_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

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

        // Collect incoming audio chunks from Firebase and write to AudioTrack
        viewModelScope.launch(Dispatchers.IO) {
            firebaseManager.audioChunk.collect { data ->
                val chunkBase64 = data.optString("chunkBase64")
                if (chunkBase64.isNotEmpty() && _uiState.value.isStreaming) {
                    try {
                        val decodedBytes = Base64.decode(chunkBase64, Base64.NO_WRAP)
                        audioTrack?.write(decodedBytes, 0, decodedBytes.size)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to write audio chunk: ${e.message}")
                    }
                }
            }
        }
    }

    fun startListening() {
        val targetId = _uiState.value.deviceId ?: run {
            _uiState.update { it.copy(error = "No active device connection") }
            return
        }

        try {
            val minBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_OUT, AUDIO_FORMAT)
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AUDIO_FORMAT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(CHANNEL_OUT)
                        .build()
                )
                .setBufferSizeInBytes(minBufferSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
            _uiState.update { it.copy(isStreaming = true, error = null) }

            // Request child device to start recording via Firebase
            firebaseManager.requestAudio(targetId, "start")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AudioTrack: ${e.message}")
            _uiState.update { it.copy(error = "Failed to open speaker output") }
        }
    }

    fun stopListening() {
        val targetId = _uiState.value.deviceId ?: return
        _uiState.update { it.copy(isStreaming = false) }

        firebaseManager.requestAudio(targetId, "stop")

        try {
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning AudioTrack resources: ${e.message}")
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}
