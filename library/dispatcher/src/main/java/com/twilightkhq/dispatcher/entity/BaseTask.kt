package com.twilightkhq.dispatcher.entity

enum class OperationType(val type: Int) {
    Click(0),               // 点击
    Swipe(1),               // 滑动
    Input(2),               // 输入
    StartApp(3),            // 启动APP
    KillApp(4),             // 关闭APP
    RestartDevice(5),       // 重启设备
}

// TODO 如何保存任务的配置信息
/**
 * 根据任务名称进行base64编码(最大长度255)将其关键配置信息进行保存
 * 如何保存默认状态 用于重置任务属性
 */
