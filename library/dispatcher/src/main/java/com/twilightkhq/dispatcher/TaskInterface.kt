package com.twilightkhq.dispatcher

import com.twilightkhq.base.CommonResult
import com.twilightkhq.base.RectColorInfo
import com.twilightkhq.base.seconds
import com.twilightkhq.dispatcher.entity.OperationType

interface BaseTask {
    val taskId: Int
    val taskName: String
    val taskType: TaskType              // 任务类型 类型统一 便于在需要的时候根据Type进行类型转换
    val taskList: List<BaseTask>        // 子任务
    var taskDescription: String         // 任务描述 一般用于记录任务调用链

    // 初始化时导入对应配置
    fun importConfiguration()

    fun updateConfiguration()

    suspend fun onExecute(): CommonResult

    // 重置任务属性
    fun reset()
}

abstract class BaseTaskWrapper : BaseTask {

    open var taskPriority = TaskPriority.Level100        // 任务优先级
    override val taskType: TaskType
        get() = TaskType.TaskWrapper
    override var taskDescription: String = ""
        set(value) {
            field = "$value-$taskName"
        }

    /**
     * 任务可用 -> 在任务执行期内
     * 任务是否完成 -> 当前任务执行期内已完成
     * 利用CRON表达式计算出下一次执行时间
     * 可用时间和刷新时间、完成时间
     */
    open var defeatCycleTimes: Int = 0              // 初始循环次数 1
    open var remainingCycleTimes: Int = 0           // 剩余的循环次数 0表示已完成 >0表示循环次数
    open var scheduledUpdateRules: String = ""      // 定时任务更新规则 利用CRON表达式表示下次任务时间 字段为空则表示非定时任务
    open var taskAvailableRules: String = ""        // 任务可用规则 当前时间与下一个可用时间在1min内容则任务可用 为空则全时段可用
    open var nextExecuteTime: Long = 0L             // 下次任务执行时间 毫秒计时规则
    open var canBeInterrupted: Boolean = false      // 任务可以被打断 在执行前或执行完一轮后再次开始前打断

    /**
     * 更新任务的可用状态
     */
    fun updateTaskStatus() {
    }

}

/**
 * 更新任务的可用状态时 当前时间currentTime
 * lastDoneTime != 0 当前任务已完成
 * currentTime > nextExecuteTime 需要更新任务的下次执行时间
 * lastDoneTime <=nextExecuteTime 当前任务未完成
 */
abstract class TodoTask : BaseTask {

    override val taskType: TaskType
        get() = TaskType.TodoTask

    override var taskDescription: String = ""       // 待办事项的描述 例如：手动操作时的一些注意事项
    open var lastDoneTime: Long = -1                // -1 标识任务未启用 0表示未执行过
    open var scheduledUpdateRules: String = ""      // 定时任务更新规则 利用CRON表达式表示下次任务时间 字段为空则表示非定时任务
    open var nextExecuteTime: Long = 0L             // 当前任务执行时间 毫秒计时规则

    /**
     * 更新任务的可用状态
     */
    fun updateTaskStatus() {
        // todo 或状态怎么判断
    }
}

abstract class BaseMatchStatusTask : BaseTask {

    override val taskType: TaskType
        get() = TaskType.MatchStatus
    override var taskDescription: String = ""
        set(value) {
            field = "$value-$taskName"
        }

    open var durationLimit: Long = 20.seconds()         // 匹配状态的最长无效时间
    abstract var templateStatusInfo: RectColorInfo      // 匹配状态

}

/**
 * 确认参数数值、确认文本、确认按钮
 */
abstract class ConfirmStatusTask : BaseTask {

    override val taskType: TaskType
        get() = TaskType.ConfirmStatus
    override var taskDescription: String = ""
        set(value) {
            field = "$value-$taskName"
        }

    abstract var confirmStatus: RectColorInfo    // 确认状态

}

abstract class ExecuteOperationTask : BaseTask {

    override val taskType: TaskType
        get() = TaskType.ExecuteOperation
    override var taskDescription: String = ""
        set(value) {
            field = "$value-$taskName"
        }

    abstract var operationType: OperationType    // 操作类型
    abstract var operationInfo: String           // 操作信息

}