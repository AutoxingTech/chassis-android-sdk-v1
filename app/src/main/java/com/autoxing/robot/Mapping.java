package com.autoxing.robot;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.autoxing.robot.utils.NetUtil;

public class Mapping {

    private int id;
    private long startTime;
    private String state;
    private String url;

    public int getId() {
        return id;
    }

    protected void setId(int id) {
        this.id = id;
    }

    public long getStartTime() {
        return startTime;
    }

    protected void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    protected String getState() {
        return state;
    }

    protected void setState(String state) {
        this.state = state;
    }

    public String getUrl() {
        return url;
    }

    protected void setUrl(String url) {
        this.url = url;
    }

    public MappingStatus getStatus(){

        String res = NetUtil.syncReq(NetUtil.url_mappings+"/"+this.getId());
        JSONObject jsonObject = null;
        try {
            jsonObject = JSON.parseObject(res);
        }catch(ClassCastException e) {
//            return null;
        }

        if(jsonObject!=null){
            this.state = jsonObject.getString("state") ;
        }

        if(state.equals("running")){
            return MappingStatus.RUNNING;
        }else if(state.equals("finished")){
            return MappingStatus.FINISHED;
        }else if(state.equals("failed")){
            return MappingStatus.FAILED;
        }
        return null;
    }
    public void saveToMap(String mapName){

    }
}
