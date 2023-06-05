package com.twilightkhq.operation

import android.text.TextUtils
import android.util.Log
import com.twilightkhq.base.CommonResult
import com.twilightkhq.base.CoordinatePoint
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object OperationShell {

    suspend fun inputTap(coordinatePoint: CoordinatePoint): CommonResult {
        return execShellCommandWithEmptyResult(arrayListOf(cmdInputTap(coordinatePoint)))
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

    suspend fun killApp(packageName: String): CommonResult {
        return execShellCommandWithEmptyResult(arrayListOf("am force-stop $packageName"))
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
        commandList: MutableList<String>, needRoot: Boolean = false
    ): CommonResult {
        return suspendCoroutine { coroutine ->
            try {
                if (needRoot) {
                    commandList.add(0, "su")
                    commandList.add("exit")
                }
                val process = Runtime.getRuntime().exec(TextUtils.join("\n", commandList))
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val output = TextUtils.join("\n", reader.readLines())
                reader.close()
                // 处理adb命令的输出
                Log.d(
                    "twilight",
                    "execShellCommand: commandOutput=$output exitCode=${process.exitValue()}"
                )
                if (output.isBlank()) {
                    coroutine.resume(CommonResult(true))
                } else {
                    coroutine.resume(CommonResult(false, "Shell Error", output))
                }
            } catch (e: Exception) {
                coroutine.resume(
                    CommonResult(
                        false, "Shell Error", e.message.orEmpty()
                    )
                )
            }
        }
    }
}