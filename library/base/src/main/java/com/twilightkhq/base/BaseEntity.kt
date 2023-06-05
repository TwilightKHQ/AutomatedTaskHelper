package com.twilightkhq.base

/**
 * 基础坐标点
 */
data class CoordinatePoint(
    val x: Double, val y: Double
)

/**
 * 某个操作或者任务执行的结果
 */
data class CommonResult(
    val isSuccess: Boolean,             // 顺利执行完成
    val errorType: String = "",         // TODO 后续整理错误类型
    val errorMsg: String = "",
    // 补充内容
)