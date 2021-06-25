package com.autoxing.robot_core;

public enum AXRobotErrorCode {
    NONE,
    GENERATE_ERROR,                 // 业务请求失败
    MAP_EXIST,                      // 地图不存在
    MAP_NOT_EXIST,                  // 地图已存在
    INVALID_MAP_VERSION,            // 地图版本无效（不能低于本地版本）
    INVALID_OVERLAY_VERSION,        // Overlay 版本无效（不能低于本地版本）

    NET_ERROR
}
