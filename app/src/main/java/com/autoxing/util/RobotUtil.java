package com.autoxing.util;

import android.app.Activity;
import android.widget.Toast;

import com.autoxing.robot_core.AXRobotPlatform;
import com.autoxing.robot_core.bean.ChassisControlMode;
import com.autoxing.robot_core.bean.Map;
import com.autoxing.robot_core.util.CommonCallback;
import com.autoxing.robot_core.util.ThreadPoolUtil;

public class RobotUtil {

    public static void setChassisControlMode(Activity context, ChassisControlMode status) {
        ThreadPoolUtil.runAsync(new CommonCallback() {
            @Override
            public void run() {
                boolean succ = AXRobotPlatform.getInstance().setControlMode(status);

                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!succ) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("failed to set ");
                            sb.append(status.toString().toLowerCase());
                            sb.append(" chassis status");
                            Toast.makeText(context, sb.toString(),1200).show();
                        }
                    }
                });
            }
        });
    }

    public static void setEmergencyStopPressed(Activity context, boolean stopPressed) {
        ThreadPoolUtil.runAsync(new CommonCallback() {
            @Override
            public void run() {
                boolean succ = AXRobotPlatform.getInstance().setEmergencyStopPressed(stopPressed);

                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!succ) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("failed to set ");
                            sb.append(stopPressed);
                            sb.append(" stop pressed");
                            Toast.makeText(context, sb.toString(),1200).show();
                        }
                    }
                });
            }
        });
    }

    public static void getCurrentMap(Activity context) {
        ThreadPoolUtil.runAsync(new CommonCallback() {
            @Override
            public void run() {
                Map map = AXRobotPlatform.getInstance().getCurrentMap();

                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        StringBuilder sb = new StringBuilder();
                        sb.append(map != null ? "succed" : "failed");
                        sb.append(" to get current map");
                        sb.append(map != null ? "<" + map.getId() + ">": "");
                        Toast.makeText(context, sb.toString(),1200).show();
                    }
                });
            }
        });
    }
}
