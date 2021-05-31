package com.autoxing.robot;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.autoxing.robot.utils.NetUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Platform {
    private static Platform currentPlatform = null;
    public static Platform newInstance(String PlatFormAddress){
        if(currentPlatform ==null){
            currentPlatform = new Platform("PlatFormAddress");
        }
        return currentPlatform;
    }
    public static Platform getInstance(){
        return currentPlatform;
    }

    Platform(){
        super();
    }
    Platform(String PlatFormAddress){
        super();
        NetUtil.setBaseUrl(PlatFormAddress);
    }

    public void setCurrentMap(int id){
        HashMap<String,Integer> hashmap = new HashMap();
        hashmap.put("map_id",id);
        NetUtil.syncReq(NetUtil.url_current_map,hashmap);
    }
    public IMoveAction moveTo(Loaction location, MoveOption option, float yaw){
        HashMap hashMap = new HashMap();
        hashMap.put("target_x",location.getX());
        hashMap.put("target_y",location.getY());
        hashMap.put("target_z",location.getZ());
        hashMap.put("target_ori",yaw);
        String res = NetUtil.syncReq(NetUtil.url_chassis_moves,hashMap);

        return  null;
    }
    public IAction getCurrentAction(){
        return  null;
    }

    public Mapping startMapping(){
        String res = NetUtil.syncReq(NetUtil.url_mappings+"/?format=json", NetUtil.HTTP_METHOD.post);
        JSONObject jsonObject;
        try {
            jsonObject = JSON.parseObject(res);
        }catch(ClassCastException e) {
            return null;
        }

        if(jsonObject!=null){
            Mapping mapping = new Mapping();
            mapping.setId(jsonObject.getInteger("id"));
            mapping.setStartTime(jsonObject.getLong("start_time"));
            mapping.setState(jsonObject.getString("state"));
            mapping.setUrl(jsonObject.getString("url"));
            return mapping;
        }
        return  null;
    }

    public void stop(){
        String res = NetUtil.syncReq(NetUtil.url_mappings+"/?format=json", NetUtil.HTTP_METHOD.delete);
    }

    public List<Map> getMaps(){
        String res = NetUtil.syncReq(NetUtil.url_maps);
        JSONArray jsonArr = JSON.parseArray(res);
        List<Map> maps = new ArrayList<>();
        if(jsonArr!=null){
            for(int i=0;i<jsonArr.size();i++){
                JSONObject jsonObject =  jsonArr.getJSONObject(i);
                Map map= new Map();
                map.setId(jsonObject.getInteger("id"));
                map.setMapName(jsonObject.getString("map_name"));
                map.setCreateTime(jsonObject.getLong("create_time"));
                map.setUrl(jsonObject.getString("url"));
                maps.add(map);
            }
        }
        return maps;
    }
    public List<Mapping> getMappings(){
        String res = NetUtil.syncReq(NetUtil.url_mappings);
        JSONArray jsonArr = JSON.parseArray(res);
        List<Mapping> mappings = new ArrayList<>();
        if(jsonArr!=null){
            for(int i=0;i<jsonArr.size();i++){
                JSONObject jsonObject =  jsonArr.getJSONObject(i);
                Mapping mapping= new Mapping();
                mapping.setId(jsonObject.getInteger("id"));
                mapping.setStartTime(jsonObject.getLong("start_time"));
                mapping.setState(jsonObject.getString("state"));
                mapping.setUrl(jsonObject.getString("url"));
                mappings.add(mapping);
            }
        }
        return mappings;
    }

}
