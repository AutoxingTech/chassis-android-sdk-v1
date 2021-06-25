package com.autoxing.robot_core.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.autoxing.robot_core.util.NetUtil;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Response;

public class Mapping {

    private int mId;
    private long mStartTime;
    private String mState;
    private String mUrl;

    public Mapping() {
        mId = -1;
        mStartTime = 0;
        mState = null;
        mUrl = null;
    }

    public Mapping(JSONObject json) {
        mId = json.getInteger("id");
        mStartTime = json.getLong("start_time");
        mState = json.getString("state");
        mUrl = json.getString("url");
    }

    public int getId() {
        return mId;
    }
    public void setId(int id) {
        this.mId = id;
    }

    public long getStartTime() {
        return mStartTime;
    }
    public void setStartTime(long startTime) {
        this.mStartTime = startTime;
    }

    public String getState() {
        return mState;
    }
    public void setState(String state) {
        this.mState = state;
    }

    public String getUrl() {
        return mUrl;
    }
    public void setUrl(String url) {
        this.mUrl = url;
    }

    public MappingStatus getStatus() {
        String res = NetUtil.syncReq(this.mUrl);
        JSONObject jsonObject = null;
        try {
            jsonObject = JSON.parseObject(res);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }

        if (jsonObject == null) {
            return null;
        }

        this.mState = jsonObject.getString("state");
        if (mState.equals("running")) {
            return MappingStatus.RUNNING;
        } else if (mState.equals("finished")) {
            return MappingStatus.FINISHED;
        } else if (mState.equals("failed")) {
            return MappingStatus.FAILED;
        }
        return null;
    }

    public Map saveToMap(String mapName) {
        HashMap hashMap = new HashMap();
        hashMap.put("mapping_id", this.getId());
        hashMap.put("map_name", mapName != null && !mapName.trim().isEmpty() ? mapName : "newmap" + this.getId());
        Response res = NetUtil.syncReq2(NetUtil.getUrl(NetUtil.SERVICE_MAPS) + "/", hashMap, NetUtil.HTTP_METHOD.post);

        if (res.code() != 201)
            return null;

        JSONObject jsonObject = null;
        try {
            jsonObject = JSON.parseObject(res.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (jsonObject == null)
            return null;

        Map map = new Map();
        map.setId(jsonObject.getInteger("id"));
        map.setMapName(jsonObject.getString("map_name"));
        map.setCreateTime(jsonObject.getLong("create_time"));
        map.setUrl(jsonObject.getString("url"));
        return map;
    }

    public boolean stop() {
        HashMap hashMap = new HashMap();
        hashMap.put("state", "finished");
        Response res = NetUtil.syncReq2(NetUtil.getUrl(NetUtil.SERVICE_MAPPINGS) + "/" + this.mId +  "?format=json", hashMap, NetUtil.HTTP_METHOD.patch);
        if (res == null)
            return false;

        return res.code() == 200;
    }

    public boolean cancel() {
        HashMap hashMap = new HashMap();
        hashMap.put("state", "cancelled");
        Response res = NetUtil.syncReq2(NetUtil.getUrl(NetUtil.SERVICE_MAPPINGS) + "/" + this.mId +  "?format=json", hashMap, NetUtil.HTTP_METHOD.patch);
        if (res == null)
            return false;

        return res.code() == 200;
    }

    public String downloadMapData() {
        String res = NetUtil.syncReq(NetUtil.getUrl(NetUtil.SERVICE_MAPPINGS) + "/" + this.mId +  "/download?format=json", NetUtil.HTTP_METHOD.get);
        return res;
    }

    public boolean delete() {
        Response res = NetUtil.syncReq2(NetUtil.getUrl(NetUtil.SERVICE_MAPPINGS) + "/" + this.mId +  "?format=json", NetUtil.HTTP_METHOD.delete);
        if (res == null)
            return false;

        return res.code() == 204;
    }
}
