package com.twilightkhq.operation

import android.content.Context
import com.twilightkhq.base.CommonResult
import com.twilightkhq.base.CoordinatePoint

interface OperationInterface {

    suspend fun click(point: CoordinatePoint): CommonResult

    suspend fun longClick(point: CoordinatePoint, duration: Long): CommonResult

    suspend fun swipe(
        fromPoint: CoordinatePoint, toPoint: CoordinatePoint, duration: Long
    ): CommonResult

    suspend fun startApp(context: Context, packageName: String): CommonResult

    suspend fun killApp(packageName: String): CommonResult

    suspend fun restartDevice(): CommonResult

    suspend fun hideMyself(): CoordinatePoint
}