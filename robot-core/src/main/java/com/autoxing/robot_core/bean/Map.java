package com.autoxing.robot_core.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.autoxing.robot_core.util.NetUtil;

import okhttp3.Response;

public class Map {
    private int mId;
    private String mUid;
    private String mMapName;
    private Long mCreateTime;
    private float mOriginX;
    private float mOriginY;
    private float mResolution;
    private int mMapVersion;
    private int mOverlayVersion;

    private String mData;
    private String mUrl;
    private boolean mIsDetailLoaded;

    public Map() {
        mId = -1;
        mUid = mMapName = null;
        mCreateTime = -1l;
        mOriginX = .0f;
        mOriginY = .0f;
        mResolution = .0f;
        mMapVersion = Integer.MAX_VALUE;
        mOverlayVersion = Integer.MAX_VALUE;

        mData = null;
        mUrl = null;
        mIsDetailLoaded = false;
    }

    public Map(JSONObject json) {
        mId = json.getInteger("id");
        mUid = json.getString("uid");
        mMapName = json.getString("map_name");
        mCreateTime = json.getLong("create_time");
        mOriginX = json.getFloat("grid_origin_x");
        mOriginY = json.getFloat("grid_origin_y");
        mResolution = json.getFloat("grid_resolution");
        mMapVersion = json.getInteger("map_version");
        mOverlayVersion = json.getInteger("overlays_version");

        mData = null;
        mUrl = null;
        mIsDetailLoaded = false;
    }

    public int getId() { return mId; }
    public void setId(int id) {
        this.mId = id;
    }

    public String getUid() { return mUid; }
    public void setUid(String uid) {
        this.mUid = uid;
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

    public int getMapVersion() {
        return mMapVersion;
    }

    public void setMapVersion(int mapVersion) {
        this.mMapVersion = mapVersion;
    }

    public int getOverlayVersion() {
        return mOverlayVersion;
    }

    public void setOverlayVersion(int overlayVersion) {
        this.mOverlayVersion = overlayVersion;
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
            mMapVersion = jsonObject.getInteger("map_version");
            mOverlayVersion = jsonObject.getInteger("overlays_version");
            mIsDetailLoaded = true;
        }
        return true;
    }

    public String downloadMap() {
        String res = NetUtil.syncReq(NetUtil.getUrl(NetUtil.SERVICE_MAPS) + "/" + this.mId +  "/download?format=json", NetUtil.HTTP_METHOD.get);
        return res;
    }

    public boolean isMapNeedUpdate(int mapVersion) {
        return mMapVersion != mapVersion;
    }

    public boolean isOverlayNeedUpdate(int overlayVersion) {
        return mOverlayVersion != overlayVersion;
    }

    public boolean delete() {
        Response res = NetUtil.syncReq2(NetUtil.getUrl(NetUtil.SERVICE_MAPS) + "/" + this.mId +  "?format=json", NetUtil.HTTP_METHOD.delete);
        if (res == null)
            return false;

        return res.code() / 100 == 2;
    }
}
