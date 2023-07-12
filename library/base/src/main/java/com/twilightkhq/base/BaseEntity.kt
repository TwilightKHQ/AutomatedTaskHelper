package com.twilightkhq.base

/**
 * 基础坐标点
 */
data class CoordinatePoint(
    var x: Double, var y: Double
)

/**
 * 某个操作或者任务执行的结果
 * 任务状态应该有以下几种：
 * 1. 所需操作全部执行完成，
 * 2. 操作达到限定条件被中止， -> 超时，达到限定标准 且需要中止当前任务
 * 3. 操作失败，返回失败原因
 */
data class CommonResult(
    var isSuccess: Boolean,             // 顺利执行完成
    var errorType: ResultErrorType = ResultErrorType.UnknownError,
    var errorMsg: String = "",
    // 补充内容
)

enum class ResultErrorType(val errorType: String) {
    ParamsError("ParamsError"),
    ColorMatchError("ColorMatchError"),
    ExceptionError("ExceptionError"),
    UnknownError("UnknownError"),
}

/**
 * 颜色点的基础类，包含如下信息
 * 颜色值、坐标点、
 * 校对后的颜色值、校对后的坐标点、完成校对的标志位
 */
data class ColorPoint(
    val color: Int,
    val coordinatePoint: CoordinatePoint,
)

enum class RectColorType {
    FixedPosition,          // 固定位置 色点的位置在固定的坐标
    RelativePosition,       // 相对位置 第一个色点需要全图查找，后续色点相对于第一个色点的坐标
    FixedInterval           // 固定间隔 色点的位置是相对的，但是间隔是固定的 -> 星级图标
}

/**
 * 矩形区域的颜色信息，包含如下信息
 * 一系列的颜色点、所有的颜色点完成校对的标志位
 * 起始点坐标、屏幕尺寸信息（相对于起始点的坐标）、截图保存的路径
 * 校对逻辑：
 * 1. 比较截图大小和屏幕尺寸，如果截图大小不等于屏幕尺寸，说明截图有问题，直接返回
 * 2. 在第一个颜色点坐标范围内（1%屏幕尺寸，最小值为1 720*1280-> x+-7 y+-12），查找误差范围内的颜色值
 * 3. 若一系列的颜色点都在误差范围内，记录校对后的颜色值和坐标点，标记校对完成
 * 4. 后续比较时，直接比较校对后的颜色值和坐标点
 */
/**
 * 1. 固定位置的固定色点
 * 2. 不固定位置的相对色点 -> 全图检测查找
 */
data class RectColorInfo(
    val rectName: String,                   // 矩形区域的名称 全局唯一
    val colorPointList: List<ColorPoint>,   // 矩形区域的颜色点列表 后续坐标表示相对于上一点的坐标 便于匹配不同位置的同一元素
    var rectColorType: RectColorType,
    var rectSize: String = "720*1280",      // 适用的分辨率
    var isCalibrated: Boolean = false,      // 是否完成校准
    var calibratedList: List<ColorPoint> = emptyList(),
    var screenshotPath: String = "",
    var fixedInterval: Int = 0,             // 固定间隔的间隔值
)

/**
 * 星级判断
 */