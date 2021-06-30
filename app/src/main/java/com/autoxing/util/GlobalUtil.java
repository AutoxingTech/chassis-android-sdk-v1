package com.autoxing.util;

import android.content.Context;
import android.graphics.Point;
import android.view.WindowManager;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class GlobalUtil {

    public static List<String> serverDataset = new LinkedList<>(Arrays.asList("tun.autoxing.com:6622", "tun.autoxing.com:6612", "192.168.43.92:8000"));
    public static List<String> tokenDataset = new LinkedList<>(Arrays.asList("f547b1b87c6e8b259bced0b81da8dd48082cf169", "8a3c5e000d75fa6322abe5fe23af778c0a95a860", "f547b1b87c6e8b259bced0b81da8dd48082cf169"));

    public static String getToken(int serverIndex) {
        return tokenDataset.get(serverIndex);
    }

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

    public static ImageLoader getImageLoader(Context context) {
        ImageLoader imageLoader = ImageLoader.getInstance();
        if (!imageLoader.isInited()) {
            imageLoader.init(ImageLoaderConfiguration.createDefault(context));
        }
        return imageLoader;
    }
}
