package com.tgm.tgmc.core.media

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Base64
import android.util.Log
import com.tgm.tgmc.core.data.remote.FirebaseManager
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Handles real-time microphone audio capture and streaming.
 * Records 16kHz Mono PCM audio chunks and streams them via socket connection.
 */
class AudioHandler(
    private val firebaseManager: FirebaseManager,
    private val deviceId: String
) {
    companion object {
        private const val TAG = "AudioHandler"
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    private var audioRecord: AudioRecord? = null
    private var isRecording = AtomicBoolean(false)
    private var recordingThread: Thread? = null

    /**
     * Start recording microphone and piping PCM chunks to the backend
     */
    @SuppressLint("MissingPermission")
    fun startRecording() {
        if (isRecording.get()) return
        Log.i(TAG, "Starting audio recording stream")

        try {
            val minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                minBufferSize * 2
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord initialization failed")
                return
            }

            audioRecord?.startRecording()
            isRecording.set(true)

            // Dedicated audio thread to prevent UI blocks
            recordingThread = Thread({
                val buffer = ByteArray(minBufferSize)
                while (isRecording.get()) {
                    val readBytes = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (readBytes > 0) {
                        // Compress or simply base64 encode PCM data
                        val chunkBase64 = Base64.encodeToString(buffer, 0, readBytes, Base64.NO_WRAP)
                        firebaseManager.sendAudioChunk(deviceId, chunkBase64)
                    }
                }
            }, "AudioRecordingThread").apply { start() }

        } catch (e: Exception) {
            Log.e(TAG, "Error starting audio stream: ${e.message}")
        }
    }

    /**
     * Stops the audio recording thread and releases hardware resources.
     */
    fun stopRecording() {
        if (!isRecording.get()) return
        Log.i(TAG, "Stopping audio recording stream")
        try {
            isRecording.set(false)
            recordingThread?.join(1000)
            recordingThread = null

            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping audio stream: ${e.message}")
        }
    }
}
