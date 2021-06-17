package com.autoxing.robot_core.bean;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.Base64;

import java.io.ByteArrayInputStream;

public class OccupancyGridTopic extends TopicBase {

    private float mResolution;
    private float mOriginX;
    private float mOriginY;
    private String mData = null;
    private Bitmap mBitmap = null;

    public float getOriginX() { return mOriginX; }
    public void setOriginX(float originX) { this.mOriginX = originX; }

    public float getOriginY() { return mOriginY; }
    public void setOriginY(float originY) { this.mOriginY = originY; }

    public float getResolution() { return mResolution; }
    public void setResolution(float resolution) { this.mResolution = resolution; }

    public String data() { return this.mData; }
    public void setData(String data) { this.mData = data; }

    public Bitmap getBitmap() {
        if (mBitmap == null && mData != null) {
            byte[] zipData = Base64.decode(mData.getBytes(), Base64.DEFAULT);
            ByteArrayInputStream bais = new ByteArrayInputStream(zipData);
            mBitmap = BitmapFactory.decodeStream(bais);
        }
        return mBitmap;
    }
}
