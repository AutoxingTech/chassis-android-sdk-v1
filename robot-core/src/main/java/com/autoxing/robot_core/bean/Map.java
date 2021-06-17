package com.autoxing.robot_core.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.autoxing.robot_core.util.NetUtil;

public class Map {
    private int mId;
    private String mMapName;
    private String mUrl;
    private Long mCreateTime;
    private float mOriginX;
    private float mOriginY;
    private float mResolution;
    private String mData = null;
    private boolean mIsDetailLoaded = false;

    public int getId() { return mId; }
    public void setId(int id) {
        this.mId = id;
    }

    public String getMapName() {
        return mMapName;
    }
    public void setMapName(String mapName) {
        this.mMapName = mapName;
    }

    public String getUrl() {
        return mUrl;
    }
    public void setUrl(String url) {
        this.mUrl = url;
    }

    public float getOriginX() { return mOriginX; }
    public void setOriginX(float originX) { this.mOriginX = originX; }

    public float getOriginY() { return mOriginY; }
    public void setOriginY(float originY) { this.mOriginY = originY; }

    public float getResolution() {
        return mResolution;
    }
    public void setResolution(float resolution) {
        this.mResolution = resolution;
    }

    public Long getCreateTime() { return mCreateTime; }
    public void setCreateTime(Long createTime) {
        this.mCreateTime = createTime;
    }

    public String getData() {
        return mData;
    }

    public void setData(String data) {
        this.mData = data;
    }

    public boolean isDetailLoaded() { return mIsDetailLoaded; }

    public boolean loadDetail() {
        if (!mIsDetailLoaded) {
            String res = NetUtil.syncReq(this.mUrl);
            JSONObject jsonObject = null;
            try {
                jsonObject = JSON.parseObject(res);
            } catch (ClassCastException e) {
                e.printStackTrace();
            }

            if (jsonObject == null)
                return false;

            mOriginX = jsonObject.getFloat("grid_origin_x");
            mOriginY = jsonObject.getFloat("grid_origin_y");
            mResolution = jsonObject.getFloat("grid_resolution");
            mIsDetailLoaded = true;
        }
        return true;
    }

    public String downloadMap() {
        String res = NetUtil.syncReq(NetUtil.getUrl(NetUtil.SERVICE_MAPS) + "/" + this.mId +  "/download?format=json", NetUtil.HTTP_METHOD.get);
        return res;
    }
}
