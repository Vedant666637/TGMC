package com.tgm.tgmc.core.media

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Base64
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.tgm.tgmc.core.data.remote.FirebaseManager
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Handles background remote camera frames capture using CameraX
 * and streams them to the server via SocketManager.
 */
class CameraHandler(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val firebaseManager: FirebaseManager,
    private val deviceId: String
) {
    companion object {
        private const val TAG = "CameraHandler"
    }

    private var cameraProvider: ProcessCameraProvider? = null
    private var analysisExecutor: ExecutorService? = null

    /**
     * Start capturing and streaming frames from the requested camera.
     * @param cameraType "front" or "rear"
     */
    fun startStreaming(cameraType: String) {
        Log.i(TAG, "Starting camera stream for: $cameraType")
        stopStreaming()

        analysisExecutor = Executors.newSingleThreadExecutor()

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()

                val cameraSelector = if (cameraType.lowercase() == "front") {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }

                // Analyze frames in background
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                    .build()

                imageAnalysis.setAnalyzer(analysisExecutor!!, ImageAnalyzer { base64Frame ->
                    firebaseManager.sendCameraFrame(deviceId, base64Frame)
                })

                // Unbind previous use cases and bind to lifecycle
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    imageAnalysis
                )
                Log.i(TAG, "Camera bound to lifecycle successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to bind camera: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Unbinds the camera provider and shuts down background thread executors.
     */
    fun stopStreaming() {
        Log.i(TAG, "Stopping camera stream")
        try {
            cameraProvider?.unbindAll()
            cameraProvider = null
            analysisExecutor?.shutdown()
            analysisExecutor = null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop camera stream: ${e.message}")
        }
    }

    /**
     * Converts YUV_420_888 ImageProxy frames into Base64 JPEG strings.
     */
    private class ImageAnalyzer(private val onFrameEncoded: (String) -> Unit) : ImageAnalysis.Analyzer {
        override fun analyze(image: ImageProxy) {
            try {
                if (image.format == ImageFormat.YUV_420_888) {
                    val yBuffer = image.planes[0].buffer
                    val uBuffer = image.planes[1].buffer
                    val vBuffer = image.planes[2].buffer

                    val ySize = yBuffer.remaining()
                    val uSize = uBuffer.remaining()
                    val vSize = vBuffer.remaining()

                    val nv21 = ByteArray(ySize + uSize + vSize)

                    // U and V are swapped
                    yBuffer.get(nv21, 0, ySize)
                    vBuffer.get(nv21, ySize, vSize)
                    uBuffer.get(nv21, ySize + vSize, uSize)

                    val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
                    val out = ByteArrayOutputStream()
                    yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 30, out) // compress quality 30% for high frame rate/low bandwidth

                    val imageBytes = out.toByteArray()
                    val base64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
                    onFrameEncoded(base64)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error analyzing image frame: ${e.message}")
            } finally {
                image.close()
            }
        }
    }
}
