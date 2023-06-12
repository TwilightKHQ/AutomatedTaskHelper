package com.twilightkhq.graphic

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 *  Android系统高版本需要指定其前台服务才能截屏
 */
object ScreenCaptureHelper {

    private lateinit var imageReader: ImageReader

    private val cachedImage = AtomicReference<Image>()
    private var mediaProjection: MediaProjection? = null
    private var mediaProjectionManager: MediaProjectionManager? = null

    fun requestPermission(activity: Activity) {
        activity.startActivity(Intent(activity, ScreenCaptureRequestActivity::class.java))
    }

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
        // 更新频率和投影帧率相当
        imageReader.setOnImageAvailableListener({ reader ->
//            Log.d("twilight", "startMediaProjection: update Image")
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

    /**
     * 获取截图前需要保证 imageReader 已经初始化
     */
    private suspend fun getLatestImage(): Image {
        return suspendCoroutine { coroutine ->
            runBlocking {
                var latestImage: Image? = null
                while (latestImage == null) {
                    latestImage = cachedImage.getAndSet(null)
                    delay(10)
                }
                coroutine.resume(latestImage)
            }
        }
    }

    private fun saveImageToStorage(savedPath: String, image: Image?): CommonResult {
        if (image == null) {
            return CommonResult(false, "ParamsError", "image is null")
        }
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

    fun isInitialized() = ::imageReader.isInitialized

    fun releaseAll() {
        stopMediaProjection()
        cachedImage.getAndSet(null)?.close()
    }

    suspend fun getScreenCaptureBitmap(): Bitmap? {
        if (!isInitialized()) return null
        val latestImage = getLatestImage()
        val bitmap = ImageUtils().image2Bitmap(latestImage, Bitmap.Config.ARGB_8888)
        latestImage.close()
        return bitmap
    }

    suspend fun saveScreenCapture(savedPath: String): CommonResult {
        if (!isInitialized()) return CommonResult(
            false, "NotInit", "imageReader not initialized"
        )
        val latestImage = getLatestImage()
        return saveImageToStorage(savedPath, latestImage)
    }

    private class MediaProjectionStopCallback : MediaProjection.Callback() {
        override fun onStop() {
            XLog.d("ScreenCapture not valid")
        }
    }
}