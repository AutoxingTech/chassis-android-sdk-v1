package com.autoxing.robot_core.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.autoxing.robot_core.util.NetUtil;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Response;

public class Mapping {

    private int id;
    private long startTime;
    private String state;
    private String url;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public long getStartTime() {
        return startTime;
    }
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    public MappingStatus getStatus() {
        String res = NetUtil.syncReq(this.url);
        JSONObject jsonObject = null;
        try {
            jsonObject = JSON.parseObject(res);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }

        if (jsonObject == null) {
            return null;
        }

        this.state = jsonObject.getString("state");
        if (state.equals("running")) {
            return MappingStatus.RUNNING;
        } else if (state.equals("finished")) {
            return MappingStatus.FINISHED;
        } else if (state.equals("failed")) {
            return MappingStatus.FAILED;
        }
        return null;
    }

    public Map saveToMap(String mapName) {
        HashMap hashMap = new HashMap();
        hashMap.put("mapping_id", this.getId());
        hashMap.put("map_name", mapName);
        Response res = NetUtil.syncReq2(NetUtil.url_maps + "/", hashMap, NetUtil.HTTP_METHOD.post);

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

    public boolean stopMapping() {
        HashMap hashMap = new HashMap();
        hashMap.put("state", "finished");
        Response res = NetUtil.syncReq2(NetUtil.url_mappings + "/" + this.id +  "?format=json", hashMap, NetUtil.HTTP_METHOD.patch);
        if (res == null)
            return false;

        return res.code() == 200;
    }

    public boolean delete() {
        Response res = NetUtil.syncReq2(NetUtil.url_mappings + "/" + this.id +  "?format=json", NetUtil.HTTP_METHOD.delete);
        if (res == null)
            return false;

        return res.code() == 204;
    }
}
