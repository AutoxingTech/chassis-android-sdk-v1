package com.autoxing.robot_core.bean;

import android.graphics.Point;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.autoxing.robot_core.util.NetUtil;

import java.util.concurrent.RecursiveTask;

public class Map {
    private int id;
    private String mapName;
    private String url;
    private Long createTime;
    private float gridOriginX;
    private float gridOriginY;
    private float gridResolution;
    private String mData = null;
    private boolean isDetailLoaded = false;

    public int getId() { return id; }
    public void setId(int id) {
        this.id = id;
    }

    public String getMapName() {
        return mapName;
    }
    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    public float getGridOriginX() { return gridOriginX; }
    public void setGridOriginX(float gridOriginY) { this.gridOriginX = gridOriginY; }

    public float getGridOriginY() { return gridOriginY; }
    public void setGridOriginY(float gridOriginY) { this.gridOriginY = gridOriginY; }

    public float getGridResolution() {
        return gridResolution;
    }
    public void setGridResolution(float gridResolution) {
        this.gridResolution = gridResolution;
    }

    public Long getCreateTime() { return createTime; }
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Location screenToWorld(int x, int y) {
        Location location = new Location();
        location.setX(gridOriginX + gridResolution * x);
        location.setY(gridOriginY + gridResolution * y);
        location.setZ(0);
        return location;
    }

    public Point worldToScreen(Location location) {
        Point pt = new Point();
        pt.x = (int)((location.getX() - gridOriginX) / gridResolution);
        pt.y = (int)((location.getY() - gridOriginY) / gridResolution);
        return pt;
    }

    public String getData() {
        return mData;
    }

    public void setData(String data) {
        this.mData = data;
    }

    public boolean loadDetail() {
        if (!isDetailLoaded) {
            String res = NetUtil.syncReq(this.url);
            JSONObject jsonObject = null;
            try {
                jsonObject = JSON.parseObject(res);
            } catch (ClassCastException e) {
                e.printStackTrace();
            }

            if (jsonObject == null)
                return false;

            gridOriginX = jsonObject.getFloat("grid_origin_x");
            gridOriginY = jsonObject.getFloat("grid_origin_y");
            gridResolution = jsonObject.getFloat("grid_resolution");
            isDetailLoaded = true;
        }
        return true;
    }
}
