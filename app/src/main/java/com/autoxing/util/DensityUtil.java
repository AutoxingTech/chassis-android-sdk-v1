package com.autoxing.util;

import android.content.Context;

public class DensityUtil {

    public static float dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return dpValue * scale + 0.5f;
    }

    public static float px2dip(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return pxValue / scale + 0.5f;
    }
}
