package com.twilightkhq.dispatcher.task

import com.twilightkhq.base.CommonResult
import com.twilightkhq.base.ResultErrorType
import com.twilightkhq.dispatcher.BaseTask
import com.twilightkhq.dispatcher.BaseTaskWrapper

abstract class CommonTaskWrapper : BaseTaskWrapper() {

    override val taskList = mutableListOf<BaseTask>()

    override fun importConfiguration() {
        TODO("Not yet implemented")
    }

    override fun updateConfiguration() {
        TODO("Not yet implemented")
    }

    /**
     * 任务合集主要包含页面匹配任务
     * 同时匹配两个页面，匹配到页面则执行其子任务，
     * 若匹配到前一个，说明正在执行其任务；子任务只需执行一次
     * 若匹配到后一个，说明状态已经改变，切换到执行后一个页面的子任务，并将后续一个页面添加到待匹配列表中
     * 若无后续页面且不循环，子任务执行完成后就结束任务
     */
    override suspend fun onExecute(): CommonResult {
        var result = CommonResult(true)
        if (taskList.isEmpty()) {
            result = CommonResult(
                false, ResultErrorType.ParamsError, "Task:$taskDescription TaskList is empty"
            )
        } else {
            for (element in taskList) {
                val childTaskResult = element.onExecute()
                if (!childTaskResult.isSuccess) {
                    result = childTaskResult
                    break
                }
            }
        }
        return result
    }

    override fun reset() {
        TODO("Not yet implemented")
    }
}