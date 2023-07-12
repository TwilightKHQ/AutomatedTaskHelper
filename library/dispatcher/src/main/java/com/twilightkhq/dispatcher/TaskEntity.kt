package com.twilightkhq.dispatcher

/**
 * 任务阶段
 * 例如，一个重启登录任务可以划分为杀死App、启动App、等待Splash进入登录页面、点击登录按钮、等待登录成功等步骤
 * 等待Splash进入登录页面需要匹配到登录页面状态，然后确认登录按钮位置，执行点击登录按钮操作
 */
enum class TaskType(val property: Int, val phaseType: Int) {
    TaskWrapper(10,0),         // 任务合集
    TodoTask(20,0),            // 待办事项，需要手动执行的任务

    MatchStatus(0,10),          // 匹配状态
    ConfirmStatus(0,20),        // 确认状态
    ExecuteOperation(0,30),     // 执行操作
}

// 阶段
enum class TaskPhaseType(val type: Int) {
    MatchStatus(0),         // 匹配状态
    ConfirmStatus(1),       // 确认状态
    ExecuteOperation(2),    // 执行操作
}

// 根据收益的时效性划分任务优先级
enum class TaskPriority(val priority: Int) {
    Level10(10),            // 没事干就去搞这个
    Level100(100),          // 收益较低, 几乎无时效性
    Level200(200),          // 收益中等，有一定的时效性
    Level500(500),          // 收益较高，时效性较强
    Level1000(1000),        // 收益很高，时效性很强
    Level2000(10000),       // 强制执行任务 需要占用指定时间
}