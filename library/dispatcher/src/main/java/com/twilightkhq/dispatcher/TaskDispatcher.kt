package com.twilightkhq.dispatcher

/**
 * 导入实现定义好的任务
 * 遍历所有任务，判断各任务的状态
 * 执行任务
 * 每10秒更新一次任务状态
 */
object TaskDispatcher {

    // 执行任务队列
    private val taskQueue = mutableListOf<BaseTaskWrapper>()

    /**
     * 准备任务队列
     * 执行任务队列为空时从中选择优先级最高的任务加入执行任务队列
     */
    private val readyQueue = mutableListOf<BaseTaskWrapper>()

    /**
     * 候补任务队列
     */
    private val standbyQueue = mutableListOf<BaseTaskWrapper>()

    /**
     * 待办事项列表
     * 记录需要手动执行的任务
     */
    private val todoTaskList = mutableListOf<TodoTask>()

    /**
     * 初始化任务列表
     */
    fun initTaskList(taskList: List<BaseTask>) {
        taskList.forEach {
            when (it) {
                is BaseTaskWrapper -> {
                    standbyQueue.add(it)
                }
                is TodoTask -> {
                    todoTaskList.add(it)
                }
            }
        }
    }

    fun update() {

    }

}