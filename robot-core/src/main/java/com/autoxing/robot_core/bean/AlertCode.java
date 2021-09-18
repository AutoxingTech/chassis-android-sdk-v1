package com.autoxing.robot_core.bean;

public enum AlertCode {
    UNKNOWN(0),                                         // 未知错误

    PLANNING_NOT_RUNNING(1001),                         // 规控节点未运行
    PLANNING_OCCUPANCY_GRID_SERVER_NOT_RUNNING(1002),   // 避障图节点未运行

    WHEEL_NOT_RUNNING(2001),                            // 轮控节点未运行
    WHEEL_OVERLOADED(2002),                             // 轮子过载

    ODOM_NOT_RUNNING(3001),                             // odom 节点未运行
    ODOM_RATE_EXCEPTION(3002),                          // odom 消息频率异常

    IMU_NOT_RUNNING(4001),                              // imu 节点未运行
    IMU_RATE_EXCEPTION(4002),                           // imu 消息频率异常
    IMU_ANGULAR_VELOCITY_EXCEPTION(4003),               // imu 转速异常
    IMU_VERTICAL_EXCEPTION(4004),                       // 竖直角度异常,可能侧翻

    LIDAR_NOT_RUNNING(5001),                            // lidar 节点未运行
    LIDAR_RATE_EXCEPTION(5002),                         // lidar 消息频率异常
    LIDAR_SCAN_TIME_EXCEPTION(5003),                    // lidar 扫描时间异常

    STORAGE_FREE_SPACE_VERYLOW(6001),                   // 存储空间严重不足
    STORAGE_FREE_SPACE_LOW(6501),                       // 存储空间不足

    POSITIONING_NOT_RUNNING(7001),                      // 定位节点未运行
    POSITIONING_NOT_RELIABLE(7002);                     // 定位失败

    private int value = 0;

    private AlertCode(int code) {
        this.value = code;
    }

    public int value() {
        return this.value;
    }

    public static AlertCode valueOf(int value) {
        switch (value) {
            case 1001:
                return PLANNING_NOT_RUNNING;
            case 1002:
                return PLANNING_OCCUPANCY_GRID_SERVER_NOT_RUNNING;
            case 2001:
                return WHEEL_NOT_RUNNING;
            case 2002:
                return WHEEL_OVERLOADED;
            case 3001:
                return ODOM_NOT_RUNNING;
            case 3002:
                return ODOM_RATE_EXCEPTION;
            case 4001:
                return IMU_NOT_RUNNING;
            case 4002:
                return IMU_RATE_EXCEPTION;
            case 4003:
                return IMU_ANGULAR_VELOCITY_EXCEPTION;
            case 4004:
                return IMU_VERTICAL_EXCEPTION;
            case 5001:
                return LIDAR_NOT_RUNNING;
            case 5002:
                return LIDAR_RATE_EXCEPTION;
            case 5003:
                return LIDAR_SCAN_TIME_EXCEPTION;
            case 6001:
                return STORAGE_FREE_SPACE_VERYLOW;
            case 6501:
                return STORAGE_FREE_SPACE_LOW;
            case 7001:
                return POSITIONING_NOT_RUNNING;
            case 7002:
                return POSITIONING_NOT_RELIABLE;
            default:
                return UNKNOWN;
        }
    }
}
