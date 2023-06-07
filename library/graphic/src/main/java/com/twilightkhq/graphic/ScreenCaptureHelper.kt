package com.twilightkhq.graphic

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.activity.result.ActivityResult
import com.elvishew.xlog.XLog
import com.twilightkhq.base.CommonResult
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicReference

/**
 *  Android系统高版本需要指定其前台服务才能截屏
 */
object ScreenCaptureHelper {

    private lateinit var imageReader: ImageReader

    private val cachedImage = AtomicReference<Image>()
    private var mediaProjection: MediaProjection? = null
    private var mediaProjectionManager: MediaProjectionManager? = null

    fun getMediaProjectionManager(activity: Activity): MediaProjectionManager? {
        if (mediaProjectionManager == null) {
            mediaProjectionManager = activity.getSystemService(
                Context.MEDIA_PROJECTION_SERVICE
            ) as? MediaProjectionManager
        }
        return mediaProjectionManager
    }

    fun setRequestResult(result: ActivityResult) {
        mediaProjection = result.data?.let { resultData ->
            mediaProjectionManager?.getMediaProjection(result.resultCode, resultData)
        }
        if (mediaProjection == null) {
            XLog.i("ScreenCapture request failed.")
        }
    }

    @SuppressLint("WrongConstant")
    fun startMediaProjection(activity: Activity) {
        // Get the screen dimensions
        val metrics = DisplayMetrics()
        val windowManager = activity.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        windowManager?.defaultDisplay?.getRealMetrics(metrics)
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels

        // Create an ImageReader to capture the screen content
        imageReader = ImageReader.newInstance(
            screenWidth, screenHeight, PixelFormat.RGBA_8888, 2
        )

        // Set up an OnImageAvailableListener to handle the captured frames
        imageReader.setOnImageAvailableListener({ reader ->
            cachedImage.getAndSet(null)?.close()
            cachedImage.set(reader.acquireLatestImage())
        }, null)

        mediaProjection?.createVirtualDisplay(
            "ScreenCapture", screenWidth, screenHeight, metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader.surface, null, null
        )
        mediaProjection?.registerCallback(MediaProjectionStopCallback(), null)
    }

    fun saveScreenCapture(savedPath: String): CommonResult {
        val latestImage = cachedImage.getAndSet(null)
        return saveImageToStorage(savedPath, latestImage)
    }

    private fun saveImageToStorage(savedPath: String, image: Image): CommonResult {
        val result = CommonResult(true)
        var outputStream: FileOutputStream? = null
        try {
            val bitmap = ImageUtils().image2Bitmap(image, Bitmap.Config.ARGB_8888)
            outputStream = FileOutputStream(File(savedPath))
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        } catch (e: Exception) {
            result.apply {
                isSuccess = false
                errorType = "SaveError"
                errorMsg = e.message.orEmpty()
            }
            e.printStackTrace()
            XLog.e("saveImageToStorage Error")
        } finally {
            try {
                outputStream?.close()
                image.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return result
    }

    fun stopMediaProjection() {
        mediaProjection?.stop()
    }

    fun isInitialized() = mediaProjection != null

    fun releaseAll() {
        stopMediaProjection()
        cachedImage.getAndSet(null)?.close()
    }

    private class MediaProjectionStopCallback : MediaProjection.Callback() {
        override fun onStop() {
            XLog.d("ScreenCapture not valid")
        }
    }
}