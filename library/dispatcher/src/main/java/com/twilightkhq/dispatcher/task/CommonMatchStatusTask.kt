package com.twilightkhq.dispatcher.task

import android.graphics.Bitmap
import android.graphics.Color
import com.twilightkhq.base.ColorPoint
import com.twilightkhq.base.CommonResult
import com.twilightkhq.base.CoordinatePoint
import com.twilightkhq.base.RectColorInfo
import com.twilightkhq.base.RectColorType
import com.twilightkhq.base.ResultErrorType
import com.twilightkhq.dispatcher.BaseMatchStatusTask
import com.twilightkhq.dispatcher.BaseTask
import com.twilightkhq.graphic.ScreenCaptureHelper
import kotlin.math.abs

abstract class CommonMatchStatusTask : BaseMatchStatusTask() {

    override val taskList = mutableListOf<BaseTask>()

    override fun importConfiguration() {
        TODO("Not yet implemented")
    }

    override fun updateConfiguration() {
        TODO("Not yet implemented")
    }

    /**
     * 获取截图->获取颜色->判断颜色->执行任务
     * 并行匹配多个状态
     */
    override suspend fun onExecute(): CommonResult {
        var result = CommonResult(true)
        val screenshotBitmap = ScreenCaptureHelper.getScreenCaptureBitmap()
        if (screenshotBitmap != null) {
            val matchResult = matchColor(screenshotBitmap, templateStatusInfo)
            if (matchResult.isSuccess) {
                taskList.onEach { it.onExecute() }
            } else {
                return matchResult
            }
        } else {
            result = CommonResult(
                false, ResultErrorType.ParamsError, "getScreenCaptureBitmap failed"
            )
        }
        return result
    }

    override fun reset() {
        TODO("Not yet implemented")
    }

    /**
     * 校对比较逻辑：
     * 1. 比较截图大小和屏幕尺寸，如果截图大小不等于屏幕尺寸，说明截图有问题，直接返回
     * 2. 在第一个颜色点坐标范围内（1%屏幕尺寸，最小值为1 720*1280-> x+-7 y+-12），查找误差范围内的颜色值
     * 3. 若一系列的颜色点都在误差范围内，记录校对后的颜色值和坐标点，标记校对完成
     * 4. 后续比较时，直接比较校对后的颜色值和坐标点
     */
    private fun matchColor(bitmap: Bitmap, statusInfo: RectColorInfo): CommonResult {
        val colorPointList = if (statusInfo.isCalibrated) statusInfo.calibratedList else statusInfo.colorPointList
        if (colorPointList.isEmpty()) return CommonResult(
            false, ResultErrorType.ParamsError, "Rect:${statusInfo.rectName} colorPointList is empty"
        )
        when (statusInfo.rectColorType) {
            RectColorType.FixedPosition -> {
                val currentPoint = CoordinatePoint(0.0, 0.0)
                val calibratedList = mutableListOf<ColorPoint>()
                colorPointList.forEach {
                    currentPoint.x = currentPoint.x + it.coordinatePoint.x
                    currentPoint.y = currentPoint.y + it.coordinatePoint.y
                    val compareColor = bitmap.getPixel(currentPoint.x.toInt(), currentPoint.y.toInt())
                    if (!compareColor(it.color, compareColor)) {
                        return CommonResult(
                            false, ResultErrorType.ColorMatchError, "Rect:${statusInfo.rectName} Color not match"
                        )
                    } else if (!statusInfo.isCalibrated) {
                        calibratedList.add(ColorPoint(it.color, currentPoint))
                    }
                }
                if (!statusInfo.isCalibrated) statusInfo.calibratedList = calibratedList
                return CommonResult(true)
            }
            RectColorType.RelativePosition -> {
                val firstColorPoint = colorPointList.first()
                for (positionX in 0 until bitmap.width) {
                    for (positionY in 0 until bitmap.height) {
                        var compareColor = bitmap.getPixel(positionX, positionY)
                        if (compareColor(firstColorPoint.color, compareColor)) {
                            val calibratedList = mutableListOf<ColorPoint>()
                            if (!statusInfo.isCalibrated) {
                                val coordinatePoint = CoordinatePoint(positionX.toDouble(), positionY.toDouble())
                                calibratedList.add(ColorPoint(firstColorPoint.color, coordinatePoint))
                            }
                            val currentPoint = firstColorPoint.coordinatePoint
                            for (index in 1 until colorPointList.size) {
                                val nextPoint = colorPointList[index]
                                currentPoint.x += nextPoint.coordinatePoint.x
                                currentPoint.y += nextPoint.coordinatePoint.y
                                compareColor = bitmap.getPixel(currentPoint.x.toInt(), currentPoint.y.toInt())
                                if (!compareColor(nextPoint.color, compareColor)) {
                                    continue
                                } else if (!statusInfo.isCalibrated) {
                                    calibratedList.add(ColorPoint(nextPoint.color, currentPoint))
                                }
                            }
                            // 全部都匹配
                            if (!statusInfo.isCalibrated) {
                                statusInfo.calibratedList = calibratedList
                            }
                            return CommonResult(true)
                        } else {
                            continue
                        }
                    }
                }
                return CommonResult(
                    false, ResultErrorType.ColorMatchError, "Rect:${statusInfo.rectName} Color not match"
                )
            }
            RectColorType.FixedInterval -> TODO()
        }
    }

    private fun compareColor(targetColor: Int, compareColor: Int): Boolean {
        val targetRed = Color.red(targetColor)
        val targetGreen = Color.green(targetColor)
        val targetBlue = Color.blue(targetColor)
        val compareRed = Color.red(compareColor)
        val compareGreen = Color.green(compareColor)
        val compareBlue = Color.blue(compareColor)
        val bias = abs(targetRed - compareRed) + abs(targetGreen - compareGreen) + abs(targetBlue - compareBlue)
        return bias <= BIAS_MAX_COLOR
    }

    companion object {
        const val BIAS_MAX_COLOR = 45
    }
}