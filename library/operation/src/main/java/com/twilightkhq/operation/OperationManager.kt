package com.twilightkhq.operation

import android.content.Context
import android.content.pm.PackageManager
import com.elvishew.xlog.XLog
import com.twilightkhq.base.CommonResult
import com.twilightkhq.base.CoordinatePoint


object OperationManager : OperationInterface {

    private val operationService by lazy { OperationAccessibilityService.INSTANCE }
    private val clickOperationType = OperationType.ADB
    private val swipeOperationType = OperationType.AccessibilityService

    override suspend fun click(point: CoordinatePoint): CommonResult {
        val randomDuration = (20L..60L).random()
        val result = when (clickOperationType) {
            OperationType.ADB -> {
                OperationShell.inputSwipe(arrayListOf(point), randomDuration)
            }

            OperationType.AccessibilityService -> {
                operationService?.serviceClick(point, randomDuration)
                    ?: OperationAccessibilityService.serviceNull()
            }
        }
        XLog.i("Operation=Click result=$result")
        return result
    }

    override suspend fun longClick(point: CoordinatePoint, duration: Long): CommonResult {
        TODO("Not yet implemented")
    }

    override suspend fun swipe(
        fromPoint: CoordinatePoint, toPoint: CoordinatePoint, duration: Long
    ): CommonResult {
        val pointList = arrayListOf(fromPoint, toPoint)
        val result = when (swipeOperationType) {
            OperationType.ADB -> {
                OperationShell.inputSwipe(pointList, duration)
            }

            OperationType.AccessibilityService -> {
                operationService?.serviceSwipe(pointList, duration)
                    ?: OperationAccessibilityService.serviceNull()
            }
        }
        XLog.i("Operation=Swipe result=$result")
        return result
    }

    override suspend fun startApp(context: Context, packageName: String): CommonResult {
        val packageManager: PackageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val result = intent?.let {
            context.startActivity(it)
            CommonResult(true)
        } ?: CommonResult(
            false, "Params Error", "getLaunchIntentForPackage is null"
        )
        XLog.i("Operation=StartApp result=$result")
        return result
    }

    override suspend fun killApp(packageName: String): CommonResult {
        val result = OperationShell.killApp(packageName)
        XLog.i("Operation=KillApp result=$result")
        return result
    }

    override suspend fun restartDevice(): CommonResult {
        val result = OperationShell.restartDevice()
        XLog.i("Operation=RestartDevice result=$result")
        return result
    }

    override suspend fun hideMyself(): CoordinatePoint {
        TODO("Not yet implemented")
    }

    /**
     * 操作的实现方式，目前主要有两种 Adb和无障碍服务
     */
    enum class OperationType {
        ADB, AccessibilityService
    }
}