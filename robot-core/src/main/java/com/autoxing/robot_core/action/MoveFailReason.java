package com.autoxing.robot_core.action;

public enum MoveFailReason {
    NONE(0),
    UNKNOWN(1),                                   // 其它原因
    GET_MAP_FAILED(2),                            // 获取地图失败（这里的地图是指 WorldMap）
    STARTING_POINT_OUT_OF_MAP(3),                 // 起点在地图之外
    ENDING_POINT_OUT_OF_MAP(4),                   // 终点在地图之外
    STARTING_POINT_NOT_IN_GROUND(5),              // 起点不在可通行区域
    ENDING_POINT_NOT_IN_GROUND(6),                // 终点不在可通行区域
    STARTING_EQUAL_ENDING(7),                     // 起点和终点相同
    CALCULATE_GLOBAL_PATH_EXTENDED_DATA_ERROR(8), // 计算全局路线扩展信息失败
    CALCULATION_FAILED(9),                        // 算路失败
    CALCULATION_TIMEOUT(10),                      // 算路超时
    NO_GLOBAL_PATH(11),                           // 没有全局路线
    NOT_GRAB_START_INDEX_ON_GLOBAL_PATH(12),      // 在全局路线上抓取起点失败
    NOT_GRAB_END_INDEX_ON_GLOBAL_PATH(13),        // 在全局路线上抓取终点失败
    PLANNING_TIMEOUT(14),                         // 路径规划长时间不成功
    MOVE_TIMEOUT(15),                             // 路径规划成功，但是避让长时间过不去
    CONTROL_COSTMAP_ERROR(16), // 局部避障地图数据出现错误,当前避障传感器数据异常

    CHARGE_RETRY_COUNT_EXCEEDED(100), // 超过充电重试次数
    CHARGE_DOCK_DETECTION_ERROR(101), // 充电底座未识别
    CHARGE_DOCK_SIGNAL_ERROR(102),    // 没有接收到充电桩对桩成功的信号

    PLATFORM_ALERT_ERROR(1000), // 系统异常
    SERVICE_CALL_ERROR(1001);   // 请求规控服务异常

    private int value = 0;

    private MoveFailReason(int code) {
        this.value = code;
    }

    public int value() {
        return this.value;
    }

    public static MoveFailReason valueOf(int value) {
        switch (value) {
            case 0:
                return NONE;
            case 2:
                return GET_MAP_FAILED;
            case 3:
                return STARTING_POINT_OUT_OF_MAP;
            case 4:
                return ENDING_POINT_OUT_OF_MAP;
            case 5:
                return STARTING_POINT_NOT_IN_GROUND;
            case 6:
                return ENDING_POINT_NOT_IN_GROUND;
            case 7:
                return STARTING_EQUAL_ENDING;
            case 8:
                return CALCULATE_GLOBAL_PATH_EXTENDED_DATA_ERROR;
            case 9:
                return CALCULATION_FAILED;
            case 10:
                return CALCULATION_TIMEOUT;
            case 11:
                return NO_GLOBAL_PATH;
            case 12:
                return NOT_GRAB_START_INDEX_ON_GLOBAL_PATH;
            case 13:
                return NOT_GRAB_END_INDEX_ON_GLOBAL_PATH;
            case 14:
                return PLANNING_TIMEOUT;
            case 15:
                return MOVE_TIMEOUT;
            case 16:
                return CONTROL_COSTMAP_ERROR;
            case 100:
                return CHARGE_RETRY_COUNT_EXCEEDED;
            case 101:
                return CHARGE_DOCK_DETECTION_ERROR;
            case 102:
                return CHARGE_DOCK_SIGNAL_ERROR;
            case 1000:
                return PLATFORM_ALERT_ERROR;
            default:
                return UNKNOWN;
        }
    }
}
