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

    public static List<String> serverDataset = new LinkedList<>(Arrays.asList("tun.autoxing.com:6622:proto-1",
            "tun.autoxing.com:6672:proto-2",
            "tun.autoxing.com:6612:proto-3",
            "tun.autoxing.com:6682:proto-4",
            "tun.autoxing.com:6632:waiter-1",
            "tun.autoxing.com:6642:waiter-2",
            "tun.autoxing.com:6652:waiter-3",
            "tun.autoxing.com:6662:waiter-4",
            "192.168.43.92:8000",
            "10.10.40.135:8000",
            "192.168.1.108:8000"));
    public static List<String> tokenDataset = new LinkedList<>(Arrays.asList("f547b1b87c6e8b259bced0b81da8dd48082cf169",
            "25c9a5ee941d55a68493240c965d73767c4d6e4f",
            "8a3c5e000d75fa6322abe5fe23af778c0a95a860",
            "9e112f8e5faa13114c1b420fd334c59008079ea1",
            "e0dd50717770cf4dfe33666c13f9b2f77be2b84a",
            "7af638fade88cf71c6466ee49d08eb6104dc3a74",
            "77aa042a8045dbf619929e9c70b2e11167aaa553",
            "eede1dba5530fac919d19a011b8d85d840af6cba",
            "f547b1b87c6e8b259bced0b81da8dd48082cf169",
            "cc2cdb02f542668300c861134a880d1753e7d1d1",
            "cc2cdb02f542668300c861134a880d1753e7d1d1"));

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
