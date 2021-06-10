package com.autoxing.util;

import android.app.Activity;
import android.widget.Toast;

import com.autoxing.robot_core.AXRobotPlatform;
import com.autoxing.robot_core.bean.ChassisStatus;
import com.autoxing.x.util.CommonCallBack;
import com.autoxing.x.util.ThreadPoolUtil;

public class RobotUtil {

    public static void setChassisStatus(Activity context, ChassisStatus status) {
        ThreadPoolUtil.run(new CommonCallBack() {
            @Override
            public void run() {
                boolean succ = AXRobotPlatform.getInstance().setChassisStatus(status);

                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        StringBuilder sb = new StringBuilder();
                        sb.append(succ ? "succed" : "failed");
                        sb.append(" to set ");
                        sb.append(status.toString().toLowerCase());
                        sb.append(" chassis status");
                        Toast.makeText(context, sb.toString(),1200).show();
                    }
                });
            }
        });
    }

    public static void getCurrentMapId(Activity context) {
        ThreadPoolUtil.run(new CommonCallBack() {
            @Override
            public void run() {
                int mapId = AXRobotPlatform.getInstance().getCurrentMapId();

                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        StringBuilder sb = new StringBuilder();
                        sb.append(mapId != -1 ? "succed" : "failed");
                        sb.append(" to get current map");
                        sb.append(mapId != -1 ? "<" + mapId + ">": "");
                        Toast.makeText(context, sb.toString(),1200).show();
                    }
                });
            }
        });
    }
}
