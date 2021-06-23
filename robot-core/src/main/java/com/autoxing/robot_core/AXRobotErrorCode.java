package com.autoxing.robot_core;

public enum AXRobotErrorCode {
    NONE,
    GENERATE_ERROR,             // 业务请求失败
    MAP_EXIST,                  // 地图不存在
    MAP_NOT_EXIST,              // 地图已存在

    NET_ERROR
}
