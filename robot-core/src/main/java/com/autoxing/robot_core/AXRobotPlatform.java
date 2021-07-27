package com.autoxing.robot_core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.autoxing.robot_core.action.ActionStatus;
import com.autoxing.robot_core.bean.ChassisStatus;
import com.autoxing.robot_core.action.MoveAction;
import com.autoxing.robot_core.geometry.Line;
import com.autoxing.robot_core.bean.Location;
import com.autoxing.robot_core.bean.Map;
import com.autoxing.robot_core.bean.Mapping;
import com.autoxing.robot_core.bean.MoveDirection;
import com.autoxing.robot_core.bean.MoveOption;
import com.autoxing.robot_core.bean.OccupancyGridTopic;
import com.autoxing.robot_core.bean.Pose;
import com.autoxing.robot_core.bean.PoseTopic;
import com.autoxing.robot_core.bean.Rotation;
import com.autoxing.robot_core.bean.TopicBase;
import com.autoxing.robot_core.util.NetUtil;
import com.autoxing.robot_core.util.ReconnectingWebSocketClient;

import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Response;

public class AXRobotPlatform {

    private static final class InstanceHolder {
        private static final AXRobotPlatform INSTANCE = new AXRobotPlatform();
    }

    private AXRobotPlatform() { }

    public static AXRobotPlatform getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private final int MESSAGE_DATA = 0;
    private final int MESSAGE_CLOSE = 1;
    private final int MESSAGE_ERROR = 2;

    private ReconnectingWebSocketClient mWebSocketClient = null;
    private List<IMappingListener> mListeners = new ArrayList<>();

    private List<TopicBase> mOccupancyGrids = new ArrayList<>();

    private Pose mPose = null;

    public void connect(String ip, int port, String token) {
        if (ip != null && port != -1) {
            String urlBase = String.format("http://%s:%d", ip, port);
            NetUtil.setUrlBase(urlBase);
        }

        if (token.startsWith("Token")) {
            NetUtil.setServiceTokenKey("Authorization");
        } else {
            NetUtil.setServiceTokenKey("SECRET");
        }
        NetUtil.setServiceTokenValue(token);

        if (mWebSocketClient != null) {
            mWebSocketClient.enableForcedClose(true);
            mWebSocketClient.close();
        }
        startWebSocket();
    }

    public void addLisener(IMappingListener listener) {
        mListeners.add(listener);
    }
    public void removeLisener(IMappingListener listener) {
        if (!mListeners.isEmpty())
            mListeners.remove(listener);
    }

    // for internal
    public void enableBlockThread(boolean enabled) {
        NetUtil.enableBlockingThread(enabled);
    }

    private void notifyDataChanged() {
        for (int i = 0; i < mListeners.size(); ++i) {
            IMappingListener listener = mListeners.get(i);
            for (int j = 0; j < mOccupancyGrids.size(); ++j) {
                mListeners.get(i).onDataChanged(mOccupancyGrids.get(j));
            }
        }
    }

    private void notifyConnected(String status) {
        for (int i = 0; i < mListeners.size(); ++i) {
            mListeners.get(i).onConnected(status);
        }
    }

    private void notifyError(Exception e) {
        for (int i = 0; i < mListeners.size(); ++i) {
            mListeners.get(i).onError(e);
        }
    }

    private void parseTopicData(String data) {
        JSONObject root = JSON.parseObject(data);
        JSONArray topics = root.getJSONArray("topics");
        mOccupancyGrids.clear();
        if (topics != null) {
            for (int i = 0; i < topics.size(); i++) {
                JSONObject topicJson = topics.getJSONObject(i);
                String topicName = topicJson.getString("topic");
                if (topicName.equals("/chassis/occupancy_grid")) {
                    OccupancyGridTopic topic = new OccupancyGridTopic();
                    topic.setTopic(topicJson.getString("topic"));
                    topic.setStamp(topicJson.getLong("stamp"));
                    topic.setData(topicJson.getString("data"));
                    topic.setResolution(topicJson.getFloat("resolution"));

                    JSONArray origins = topicJson.getJSONArray("origin");
                    topic.setOriginX(origins.getFloat(0));
                    topic.setOriginY(origins.getFloat(1));
                    mOccupancyGrids.add(topic);
                } else if (topicName.equals("/chassis/pose")) {
                    PoseTopic topic = new PoseTopic();
                    topic.setTopic(topicJson.getString("topic"));
                    topic.setStamp(topicJson.getLong("stamp"));

                    Pose pose = new Pose();
                    JSONArray positions = topicJson.getJSONArray("pos");
                    pose.setX(positions.getFloat(0));
                    pose.setY(positions.getFloat(1));
                    pose.setZ(0);

                    pose.setYaw(topicJson.getFloat("ori"));

                    mPose = pose;
                    topic.setPose(pose);
                    mOccupancyGrids.add(topic);
                }
            }
        }
        notifyDataChanged();
    }

    private void startWebSocket() {
        URI serverURI = URI.create(NetUtil.getUrl(NetUtil.SERVICE_WS_TOPICS));
        java.util.Map headers = new HashMap<>();
        headers.put(NetUtil.getServiceTokenKey(), NetUtil.getServiceTokenValue());
        mWebSocketClient = new ReconnectingWebSocketClient(serverURI, new Draft_6455(), headers, 0) {

            @Override
            public void open(ServerHandshake handshakedata) {
                mWebSocketClient.send("{\"enable_topic\": \"/tracked_pose\"}");
                mWebSocketClient.send("{\"enable_topic\": \"/map\"}");

                String status = handshakedata.getHttpStatusMessage();
                notifyConnected(status);
            }

            @Override
            public void message(String message) {
                parseTopicData(message);
            }

            @Override
            public void close(int code, String reason, boolean remote) {

            }

            @Override
            public void error(Exception ex) {
                notifyError(ex);
            }
        };
        mWebSocketClient.connect();
    }

    public String getDeviceId() {
        return "81811061000021b";
    }

    public int getBatteryPercentage() {
        return 50;
    }

    public boolean getBatteryIsCharging() {
        return false;
    }

    public int getLocalizationQuality() {
        return 1;
    }

    public boolean setChassisStatus(ChassisStatus status) {
        HashMap hashmap = new HashMap();
        hashmap.put("control_mode", status.toString().toLowerCase());
        Response res = NetUtil.syncReq2(NetUtil.getUrl(NetUtil.SERVICE_CHASSIS_STATUS), hashmap, NetUtil.HTTP_METHOD.patch);
        if (res == null)
            return false;

        return res.code() == 200;
    }

    public Mapping startMapping() {
        Response res = NetUtil.syncReq2(NetUtil.getUrl(NetUtil.SERVICE_MAPPINGS) + "/?format=json", NetUtil.HTTP_METHOD.post);

        if (res == null)
            return null;

        if (res.code() / 100 != 2)
            return null;

        JSONObject jsonObject = null;
        try {
            jsonObject = JSON.parseObject(res.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }

        if (jsonObject == null)
            return null;

        Mapping mapping = new Mapping(jsonObject);
        return mapping;
    }

    public Mapping getCurrentMapping() {
        Response res = NetUtil.syncReq2(NetUtil.getUrl(NetUtil.SERVICE_MAPPINGS) + "/current" + "?format=json", NetUtil.HTTP_METHOD.get);
        if (res == null)
            return null;

        if (res.code() / 100 != 2)
            return null;

        JSONObject jsonObject = null;
        try {
            jsonObject = JSON.parseObject(res.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }

        if (jsonObject == null)
            return null;

        Mapping mapping = new Mapping(jsonObject);
        return mapping;
    }

    public List<Mapping> getMappings() {
        Response res = NetUtil.syncReq2(NetUtil.getUrl(NetUtil.SERVICE_MAPPINGS));
        if (res == null)
            return null;

        if (res.code() / 100 != 2)
            return null;

        JSONArray jsonArr = null;
        try {
            jsonArr = JSON.parseArray(res.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }

        if (jsonArr == null)
            return null;

        List<Mapping> mappings = new ArrayList<>();
        for (int i = 0; i < jsonArr.size(); i++) {
            JSONObject jsonObject = jsonArr.getJSONObject(i);
            String state = jsonObject.getString("state");
            if (state.equals("finished")) {
                Mapping mapping = new Mapping();
                mapping.setId(jsonObject.getInteger("id"));
                mapping.setStartTime(jsonObject.getLong("start_time"));
                mapping.setState(jsonObject.getString("state"));
                mapping.setUrl(jsonObject.getString("url"));
                mappings.add(mapping);
            }
        }
        return mappings;
    }

    public List<Map> getMaps() {
        Response res = NetUtil.syncReq2(NetUtil.getUrl(NetUtil.SERVICE_MAPS));
        if (res == null)
            return null;

        if (res.code() / 100 != 2)
            return null;

        JSONArray jsonArr = null;
        try {
            jsonArr = JSON.parseArray(res.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }

        if (jsonArr == null)
            return null;

        List<Map> maps = new ArrayList<>();
        for (int i = 0; i < jsonArr.size(); i++) {
            JSONObject jsonObject =  jsonArr.getJSONObject(i);
            Map map= new Map();
            map.setId(jsonObject.getInteger("id"));
            map.setUid(jsonObject.getString("uid"));
            map.setMapName(jsonObject.getString("map_name"));
            map.setCreateTime(jsonObject.getLong("create_time"));
            map.setUrl(jsonObject.getString("url"));
            maps.add(map);
        }
        return maps;
    }

    public AXRobotErrorCode addMap(String mapData) {
        AXRobotErrorCode errorCode = AXRobotErrorCode.NONE;
        Response res = NetUtil.syncReq3(NetUtil.getUrl(NetUtil.SERVICE_MAPS) + "/", mapData, NetUtil.HTTP_METHOD.post);
        if (res == null)
            return AXRobotErrorCode.NET_ERROR;

        if (res.code() / 100 != 2) {
            if (res.code() == 400) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = JSON.parseObject(res.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }

                if (jsonObject == null) {
                    return AXRobotErrorCode.GENERATE_ERROR;
                }

                if (jsonObject.containsKey("map_name")) {
                    return AXRobotErrorCode.MAP_NAME_EXIT;
                } else {
                    return AXRobotErrorCode.GENERATE_ERROR;
                }
            }
            else {
                return AXRobotErrorCode.GENERATE_ERROR;
            }
        }

        return AXRobotErrorCode.NONE;
    }

    public AXRobotErrorCode updateMap(int mapId, String mapData, boolean partial) {
        NetUtil.HTTP_METHOD method = partial ? NetUtil.HTTP_METHOD.patch : NetUtil.HTTP_METHOD.put;
        Response res = NetUtil.syncReq3(NetUtil.getUrl(NetUtil.SERVICE_MAPS) + "/" + mapId + "?format=json", mapData, method);
        if (res == null)
            return AXRobotErrorCode.NET_ERROR;

        if (res.code() / 100 != 2) {
            if (res.code() == 400) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = JSON.parseObject(res.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }

                if (jsonObject == null) {
                    return AXRobotErrorCode.GENERATE_ERROR;
                }

                if (jsonObject.containsKey("map_version")) {
                    return AXRobotErrorCode.INVALID_MAP_VERSION;
                } else if (jsonObject.containsKey("overlays_version")) {
                    return AXRobotErrorCode.INVALID_OVERLAY_VERSION;
                } else {
                    return AXRobotErrorCode.GENERATE_ERROR;
                }
            } else {
                return AXRobotErrorCode.GENERATE_ERROR;
            }
        }

        return AXRobotErrorCode.NONE;
    }

    public AXRobotErrorCode addOrUpdateMap(String data) {
        AXRobotErrorCode errorCode = AXRobotErrorCode.NONE;

        JSONObject json = JSON.parseObject(data);
        String mapUid = json.getString("uid");

        boolean partial = true;
        if (json.containsKey("map_data")) {
            JSONObject mapDataJson = json.getJSONObject("map_data");
            json.remove("map_data");
            json.putAll(mapDataJson);

            if (json.containsKey("overlays")) {
                partial = false;
            }
        }

        if (json.containsKey("overlays")) {
            JSONObject overlays = json.getJSONObject("overlays");
            json.remove("overlays");
            json.put("overlays", overlays.toString());
        }

        Map map = getMapWithUid(mapUid);
        if (map == null) {
            errorCode = addMap(json.toString());
        } else {
            errorCode = updateMap(map.getId(), json.toString(), partial);
        }

        return errorCode;
    }

    public Map getMapWithUid(String mapUid) {
        Response res = NetUtil.syncReq2(NetUtil.getUrl(NetUtil.SERVICE_MAPS) + "/?format=json&" + "uid=" + mapUid, NetUtil.HTTP_METHOD.get);
        if (res == null)
            return null;

        if (res.code() != 200)
            return null;

        JSONArray jsonArray = null;
        try {
            jsonArray = JSON.parseArray(res.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }

        if (jsonArray == null || jsonArray.size() == 0)
            return null;

        JSONObject jsonObject = jsonArray.getJSONObject(0);

        Map map = new Map();
        map.setId(jsonObject.getInteger("id"));
        map.setUid(jsonObject.getString("uid"));
        map.setMapName(jsonObject.getString("map_name"));
        map.setCreateTime(jsonObject.getLong("create_time"));
        map.setUrl(jsonObject.getString("url"));
        return map;
    }

    public boolean setCurrentMap(String mapUid, Pose pose) {
        Map map = getMapWithUid(mapUid);
        if (map == null) {
            return false;
        }

        return setCurrentMap(map.getId(), pose);
    }

    public boolean setCurrentMap(int id, Pose pose) {
        HashMap<String, Integer> hashmap = new HashMap();
        hashmap.put("map_id", id);
        Response res = NetUtil.syncReq2(NetUtil.getUrl(NetUtil.SERVICE_CHASSIS_CURRENT_MAP), hashmap, NetUtil.HTTP_METHOD.post);
        if (res == null)
            return false;

        if (res.code() != 200)
            return false;

        if (pose == null)
            return true;

        return setPose(pose);
    }

    public int getCurrentMapId() {
        String res = NetUtil.syncReq(NetUtil.getUrl(NetUtil.SERVICE_CHASSIS_CURRENT_MAP), NetUtil.HTTP_METHOD.get);
        JSONObject jsonObject = null;
        try {
            jsonObject = JSON.parseObject(res);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        if (jsonObject == null)
            return -1;

        if (jsonObject.get("map_id") == null)
            return -1;

        return jsonObject.getInteger("map_id");
    }

    public boolean removeCurrentMap() {
        Response res = NetUtil.syncReq2(NetUtil.getUrl(NetUtil.SERVICE_CHASSIS_CURRENT_MAP), NetUtil.HTTP_METHOD.delete);
        if (res == null)
            return false;

        return res.code() == 200;
    }

    public void deleteMaps(String[] mapUids, boolean exclude) {
        boolean flag = false;
        List<Map> maps = getMaps();
        if (maps == null)
            return;

        for (Map map : maps) {
            flag = false;
            String uid = map.getUid();
            for (String mapUid : mapUids) {
                if (mapUid.equals(uid)) {
                    flag = true;
                    break;
                }
            }

            if ((exclude && !flag)
            || (!exclude && flag)) {
                map.delete();
            }
        }
    }

    public Pose getPose() {
        return mPose;
    }

    public boolean setPose(Pose pose) {
        List list = new ArrayList();
        list.add(pose.getX());
        list.add(pose.getY());
        list.add(pose.getZ());
        HashMap hashMap = new HashMap();
        hashMap.put("position", list);
        hashMap.put("ori", pose.getYaw());
        System.out.println("===robot-core===============pos.x=" + pose.getX() + ", pos.y=" + pose.getY() + ", pos.z=" + pose.getZ() + ", ori=" + pose.getYaw());
        Response res = NetUtil.syncReq2(NetUtil.getUrl(NetUtil.SERVICE_CHASSIS_POSE), hashMap, NetUtil.HTTP_METHOD.post);

        if (res == null)
            return false;

        return  res.code() == 200;
    }

    public void addLines(List<Line> lines) {

    }

    public void clearLines() {

    }

    public MoveAction moveTo(Location location, MoveOption option, float yaw) {
        HashMap hashMap = new HashMap();
        hashMap.put("target_x",location.getX());
        hashMap.put("target_y",location.getY());
        hashMap.put("target_z",location.getZ());
        System.out.println("===robot-core===============x=" + location.getX() + ", y=" + location.getY() + ", z=" + location.getZ());
        if (option.isWithYaw()) {
            System.out.println("===robot-core===============yaw=" + yaw);
            hashMap.put("target_ori", yaw);
        } else {
            System.out.println("===robot-core===============not use yaw");
        }
        Response res = NetUtil.syncReq2(NetUtil.getUrl(NetUtil.SERVICE_CHASSIS_MOVES), hashMap, NetUtil.HTTP_METHOD.post);
        if (res == null)
            return null;

        if (res.code() / 100 != 2)
            return null;

        JSONObject jsonObject = null;
        try {
            jsonObject = JSON.parseObject(res.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }

        if (jsonObject == null)
            return null;

        MoveAction action = new MoveAction();
        action.setId(jsonObject.getInteger("id"));
        String stateStr = jsonObject.getString("state");
        action.setStatus(ActionStatus.valueOf(stateStr.toUpperCase()));
        return action;
    }

    public MoveAction rotateWithAbsoluteOrientation(Rotation rotation) {
        HashMap hashMap = new HashMap();
        hashMap.put("target_ori", rotation.getYaw());
        Response res = NetUtil.syncReq2(NetUtil.getUrl(NetUtil.SERVICE_CHASSIS_MOVES), hashMap, NetUtil.HTTP_METHOD.post);
        if (res == null)
            return null;

        if (res.code() != 201)
            return null;

        JSONObject jsonObject = null;
        try {
            jsonObject = JSON.parseObject(res.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }

        if (jsonObject == null)
            return null;

        MoveAction action = new MoveAction();
        action.setId(jsonObject.getInteger("id"));
        String stateStr = jsonObject.getString("state");
        action.setStatus(ActionStatus.valueOf(stateStr.toUpperCase()));
        return action;
    }

    public MoveAction rotateWithRelativeOrientation(Rotation rotation) {
        MoveAction action = new MoveAction();
        return action;
    }

    public List<MoveAction> getMoveActions() {
        String res = NetUtil.syncReq(NetUtil.getUrl(NetUtil.SERVICE_CHASSIS_MOVES));
        JSONArray jsonArr = JSON.parseArray(res);
        List<MoveAction> moveActions = new ArrayList<>();
        if (jsonArr != null) {
            for (int i = 0; i < jsonArr.size(); i++) {
                JSONObject jsonObject =  jsonArr.getJSONObject(i);
                MoveAction moveAction = new MoveAction();
                moveAction.setId(jsonObject.getInteger("id"));
                String stateStr = jsonObject.getString("state");
                moveAction.setStatus(ActionStatus.valueOf(stateStr.toUpperCase()));
                moveActions.add(moveAction);
            }
        }
        return moveActions;
    }

    public MoveAction getCurrentAction() {
        Response res = NetUtil.syncReq2(NetUtil.getUrl(NetUtil.SERVICE_CHASSIS_MOVES) + "/latest", NetUtil.HTTP_METHOD.get);
        if (res == null)
            return null;

        if (res.code() / 100 != 2)
            return null;

        JSONObject jsonObject = null;
        try {
            jsonObject = JSON.parseObject(res.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }

        if (jsonObject == null)
            return null;

        MoveAction action = new MoveAction();
        action.setId(jsonObject.getInteger("id"));
        String stateStr = jsonObject.getString("state");
        action.setStatus(ActionStatus.valueOf(stateStr.toUpperCase()));
        return action;
    }

    public boolean moveWithAction(MoveDirection direction) {
        HashMap hashmap = new HashMap();
        hashmap.put("action", direction.toString().toLowerCase());
        Response res = NetUtil.syncReq2(NetUtil.getUrl(NetUtil.SERVICE_CHASSIS_REMOTE_ACTION), hashmap, NetUtil.HTTP_METHOD.post);
        if (res == null)
            return false;

        return res.code() == 200;
    }

    public MoveAction goHome() {
        MoveAction action = new MoveAction();
        return action;
    }

    public boolean moveWithTwist(float velocityY, float angularVelocityZ) {
        HashMap hashmap = new HashMap();

        List velocity = new ArrayList();
        velocity.add(.0f);
        velocity.add(velocityY);
        velocity.add(.0f);

        List angularVelocity = new ArrayList();
        angularVelocity.add(.0f);
        angularVelocity.add(.0f);
        angularVelocity.add(angularVelocityZ);

        hashmap.put("velocity", velocity);
        hashmap.put("angular_velocity", angularVelocity);
        hashmap.put("time", 0.1f);

        Response res = NetUtil.syncReq2(NetUtil.getUrl(NetUtil.SERVICE_CHASSIS_REMOTE_TWIST), hashmap, NetUtil.HTTP_METHOD.post);
        if (res == null)
            return false;

        return res.code() == 200;
    }
}
