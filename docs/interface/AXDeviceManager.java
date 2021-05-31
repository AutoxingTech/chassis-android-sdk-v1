package com.autoxing.android.robot.api;

public final class AXDeviceManager {
    public static AXRobotPlatform connect(String ip, int port) {
        return new AXRobotPlatform(ip, port);
    }
}
