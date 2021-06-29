package com.autoxing.robot_core;

public enum AXRobotErrorCode {
    NONE,
    GENERATE_ERROR,                 // 业务请求失败
    MAP_EXIST,                      // 地图已存在
    MAP_NOT_EXIST,                  // 地图不存在
    INVALID_MAP_VERSION,            // 地图版本未更新
    INVALID_OVERLAY_VERSION,        // Overlay 版本未更新
    MAP_NAME_EXIT,                  // 地图名称已存在

    NET_ERROR
}
