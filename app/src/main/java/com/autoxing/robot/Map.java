package com.autoxing.robot;

import com.alibaba.fastjson.JSONObject;
import com.autoxing.robot.utils.NetUtil;

public class Map {
    private int id;
    private String mapName;
    private String url;
    private String carToMap;
    private Long createTime;

    public int getId() {
        return id;
    }

    protected void setId(int id) {
        this.id = id;
    }

    public String getMapName() {
        return mapName;
    }

    protected void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public String getUrl() {
        return url;
    }

    protected void setUrl(String url) {
        this.url = url;
    }

    public Long getCreateTime() {
        return createTime;
    }

    protected void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public String getCarToMap(){
        if(carToMap==null){
            String res = NetUtil.syncReq(this.url);
            JSONObject jsonObject = JSONObject.parseObject(res);
            if(jsonObject!=null)
                carToMap = jsonObject.getString("carto_map");
        }
        return carToMap;
    }
}
