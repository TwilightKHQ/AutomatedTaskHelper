package com.twilightkhq.operation

import android.text.TextUtils
import com.twilightkhq.base.CommonResult
import com.twilightkhq.base.CoordinatePoint
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * 1. 通过协程调用时，需要在 IO 线程下运行
 * 2. Android 13 adb shell input 事件需要INJECT_EVENTS权限，需要system权限
 */
object OperationShell {

    suspend fun inputTap(coordinatePoint: CoordinatePoint): CommonResult {
        return execShellCommandWithEmptyResult(cmdInputTap(coordinatePoint))
    }

    suspend fun inputSwipe(pointList: List<CoordinatePoint>, duration: Long): CommonResult {
        val commandList = mutableListOf<String>()
        when (pointList.size) {
            0 -> return CommonResult(false, "Params Error", "pointList Empty")
            1 -> {      // 原地长按
                val coordinatePoint = pointList[0]
                commandList.add(cmdInputSwipe(coordinatePoint, coordinatePoint, duration))
            }

            else -> {   // 多点滑动
                var currentPoint = pointList[0]
                val partDuration = (duration.toFloat() / (pointList.size - 1)).toLong()
                for (index in 1 until pointList.size) {
                    val nextPoint = pointList[index]
                    commandList.add(cmdInputSwipe(currentPoint, nextPoint, partDuration))
                    currentPoint = nextPoint
                }
            }
        }
        return execShellCommandWithEmptyResult(commandList)
    }

    // 必须有 Root 权限
    suspend fun killApp(packageName: String): CommonResult {
        return execShellCommandWithEmptyResult("am force-stop $packageName", true)
    }

    // 必须有 Root 权限
    suspend fun restartDevice(): CommonResult {
        return execShellCommandWithEmptyResult("reboot", true)
    }

    private fun cmdInputText(text: String): String {
        return "input text $text"
    }

    private fun cmdInputKeyevent(
        keyCode: Int, longPress: Boolean = false, doubleTap: Boolean = false
    ): String {
        val flag = when {
            longPress -> "--longpress "
            doubleTap -> "--doubletap "
            else -> ""
        }
        return "input keyevent $flag$keyCode"
    }

    private fun cmdInputTap(coordinatePoint: CoordinatePoint): String {
        return "input tap ${coordinatePoint.x} ${coordinatePoint.y}"
    }

    private fun cmdInputSwipe(
        fromPoint: CoordinatePoint, toPoint: CoordinatePoint, duration: Long
    ): String {
        return "input swipe ${fromPoint.x} ${fromPoint.y} ${toPoint.x} ${toPoint.y} $duration"
    }

    private suspend fun execShellCommandWithEmptyResult(
        command: String, needRoot: Boolean = false
    ): CommonResult {
        return execShellCommandWithEmptyResult(arrayListOf(command), needRoot)
    }

    private suspend fun execShellCommandWithEmptyResult(
        commandList: MutableList<String>, needRoot: Boolean = false
    ): CommonResult {
        return suspendCoroutine { coroutine ->
            try {
                if (needRoot) {
                    commandList.add(0, "su")
                    commandList.add("exit")
                }
                val process = Runtime.getRuntime().exec(TextUtils.join("\n", commandList))
                val inputReader = BufferedReader(InputStreamReader(process.inputStream))
                val errorReader = BufferedReader(InputStreamReader(process.errorStream))
                val inputReaderText = TextUtils.join("\n", inputReader.readLines())
                val errorReaderText = TextUtils.join("\n", errorReader.readLines())
                val outputText = "inputStream:$inputReaderText\nerrorStream:$errorReaderText"
                inputReader.close()
                errorReader.close()
                // 处理adb命令的输出
                if (inputReaderText.isBlank() && errorReaderText.isBlank()) {
                    coroutine.resume(CommonResult(true))
                } else {
                    coroutine.resume(
                        CommonResult(false, "Shell Error", outputText)
                    )
                }
            } catch (e: Exception) {
                coroutine.resume(
                    CommonResult(false, "Shell Error", e.message.orEmpty())
                )
            }
        }
    }
}