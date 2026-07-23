package com.tgm.tgmc.core.media

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import com.tgm.tgmc.core.data.remote.FirebaseManager
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Handles background screen capturing and real-time streaming using VirtualDisplay and ImageReader.
 * Requires a valid MediaProjection token approved via the system permissions dialog.
 */
class ScreenMirrorHandler(
    private val context: Context,
    private val firebaseManager: FirebaseManager,
    private val deviceId: String
) {
    companion object {
        private const val TAG = "ScreenMirrorHandler"
        private const val SCREEN_WIDTH = 480  // downscaled width for bandwidth
        private const val SCREEN_HEIGHT = 800 // downscaled height
    }

    private var mediaProjectionManager: MediaProjectionManager? = null
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var captureExecutor: ExecutorService? = null
    private var isMirroring = false

    init {
        mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    /**
     * Start capturing the screen.
     * @param resultCode system permission dialog result
     * @param resultData intent data returned by System MediaProjection dialog
     */
    fun startMirroring(resultCode: Int, resultData: Intent) {
        if (isMirroring) return
        Log.i(TAG, "Starting screen mirror streaming")

        try {
            captureExecutor = Executors.newSingleThreadExecutor()
            mediaProjection = mediaProjectionManager?.getMediaProjection(resultCode, resultData)

            if (mediaProjection == null) {
                Log.e(TAG, "MediaProjection token creation failed")
                return
            }

            // Get display metrics
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getRealMetrics(metrics)
            val density = metrics.densityDpi

            // Configure ImageReader to capture display frames
            imageReader = ImageReader.newInstance(SCREEN_WIDTH, SCREEN_HEIGHT, PixelFormat.RGBA_8888, 2)
            
            virtualDisplay = mediaProjection?.createVirtualDisplay(
                "TGMC_Mirror",
                SCREEN_WIDTH,
                SCREEN_HEIGHT,
                density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader?.surface,
                null,
                null
            )

            isMirroring = true

            imageReader?.setOnImageAvailableListener({ reader ->
                if (!isMirroring) return@setOnImageAvailableListener
                
                val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
                captureExecutor?.execute {
                    try {
                        val planes = image.planes
                        val buffer = planes[0].buffer
                        val pixelStride = planes[0].pixelStride
                        val rowStride = planes[0].rowStride
                        val rowPadding = rowStride - pixelStride * SCREEN_WIDTH

                        // Create bitmap from buffer
                        val bitmap = Bitmap.createBitmap(
                            SCREEN_WIDTH + rowPadding / pixelStride,
                            SCREEN_HEIGHT,
                            Bitmap.Config.ARGB_8888
                        )
                        bitmap.copyPixelsFromBuffer(buffer)

                        // Crop and compress
                        val croppedBitmap = Bitmap.createBitmap(bitmap, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT)
                        val out = ByteArrayOutputStream()
                        croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 25, out) // compress to 25% to minimize latency

                        val bytes = out.toByteArray()
                        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                        firebaseManager.sendMirrorFrame(deviceId, base64)

                        bitmap.recycle()
                        croppedBitmap.recycle()
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to analyze screen frame: ${e.message}")
                    } finally {
                        image.close()
                    }
                }
            }, null)

        } catch (e: Exception) {
            Log.e(TAG, "Error starting screen mirror: ${e.message}")
        }
    }

    /**
     * Stop capturing the screen and release all media projection resources.
     */
    fun stopMirroring() {
        if (!isMirroring) return
        Log.i(TAG, "Stopping screen mirror streaming")
        isMirroring = false

        try {
            virtualDisplay?.release()
            virtualDisplay = null

            imageReader?.close()
            imageReader = null

            mediaProjection?.stop()
            mediaProjection = null

            captureExecutor?.shutdown()
            captureExecutor = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping screen mirror: ${e.message}")
        }
    }
}
