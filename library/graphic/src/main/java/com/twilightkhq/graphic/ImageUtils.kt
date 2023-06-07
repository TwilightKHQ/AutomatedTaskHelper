package com.twilightkhq.graphic

import android.graphics.Bitmap
import android.media.Image

class ImageUtils {

    fun image2Bitmap(image: Image, config: Bitmap.Config): Bitmap {
        val width = image.width
        val height = image.height
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * width
        val bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, config)
        bitmap.copyPixelsFromBuffer(buffer)
        return Bitmap.createBitmap(bitmap, 0, 0, width, height)
    }

}