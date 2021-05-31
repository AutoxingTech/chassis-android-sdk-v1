package com.autoxing.android.robot.api;

public enum ActionStatus {
    WAITING_FOR_START,

    //动作已创建但未开始。
    RUNNING,

    //动作正在进行。
    FINISHED,

    //动作成功完成。
    PAUSED,

    //动作已暂停。
    STOPPED,

    //动作已停止。
    ERROR
    //动作遇到错误。
}
