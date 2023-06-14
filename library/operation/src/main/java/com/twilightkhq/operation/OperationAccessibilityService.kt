package com.twilightkhq.operation

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import com.twilightkhq.base.CommonResult
import com.twilightkhq.base.CoordinatePoint
import com.twilightkhq.base.ResultErrorType
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class OperationAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        if (INSTANCE == null) INSTANCE = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (INSTANCE == null) INSTANCE = this

    }

    override fun onInterrupt() {

    }

    override fun onUnbind(intent: Intent?): Boolean {
        INSTANCE = null
        return super.onUnbind(intent)
    }

    suspend fun serviceClick(
        coordinatePoint: CoordinatePoint, duration: Long
    ): CommonResult {
        return commonGestureOperation(arrayListOf(coordinatePoint), duration)
    }

    suspend fun serviceSwipe(
        pointList: List<CoordinatePoint>, duration: Long
    ): CommonResult {
        return commonGestureOperation(pointList, duration)
    }

    private suspend fun commonGestureOperation(
        pointPath: List<CoordinatePoint>, duration: Long
    ): CommonResult {
        val stroke = GestureDescription.StrokeDescription(
            convertCoordinatePoint2Path(pointPath), 0, duration
        )
        val description = GestureDescription.Builder()
            .addStroke(stroke)
            .build()
        return gesturesWithCoroutine(description)
    }

    private fun convertCoordinatePoint2Path(pointPath: List<CoordinatePoint>): Path {
        return Path().apply {
            pointPath.forEachIndexed { index, coordinatePoint ->
                if (index == 0) moveTo(coordinatePoint.x.toFloat(), coordinatePoint.y.toFloat())
                else lineTo(coordinatePoint.x.toFloat(), coordinatePoint.y.toFloat())
            }
        }
    }

    // TODO 了解清楚handler的作用
    private suspend fun gesturesWithCoroutine(description: GestureDescription): CommonResult {
        return suspendCoroutine { coroutine ->
            try {
                dispatchGesture(description, object : GestureResultCallback() {
                    override fun onCompleted(gestureDescription: GestureDescription?) {
                        super.onCompleted(gestureDescription)
                        coroutine.resume(CommonResult(true))
                    }

                    override fun onCancelled(gestureDescription: GestureDescription?) {
                        super.onCancelled(gestureDescription)
                        coroutine.resume(
                            CommonResult(false, ResultErrorType.ExceptionError, "dispatchGesture onCancelled")
                        )
                    }
                }, null)
            } catch (e: Exception) {
                coroutine.resume(
                    CommonResult(false, ResultErrorType.ExceptionError, e.message.orEmpty())
                )
            }
        }
    }

    companion object {

        var INSTANCE: OperationAccessibilityService? = null
            private set

        fun serviceNull(): CommonResult {
            return CommonResult(
                false, ResultErrorType.ExceptionError,
                "OperationAccessibilityService is null"
            )
        }
    }
}