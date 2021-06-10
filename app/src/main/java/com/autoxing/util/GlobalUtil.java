package com.autoxing.util;

import android.content.Context;
import android.graphics.Point;
import android.view.WindowManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class GlobalUtil {

    public static Point getScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point outSize = new Point();
        windowManager.getDefaultDisplay().getRealSize(outSize);
        Point screenSize = new Point();
        screenSize.x = outSize.x;
        screenSize.y = outSize.y;
        return screenSize;
    }

    public static String convertTimestampToFormatStr(long stamp) {
        Date date = new Date(stamp * 1000L);
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormatGmt.format(date);
    }

    public static int yawToAngel(float yaw) {

        return  0;
    }
}
