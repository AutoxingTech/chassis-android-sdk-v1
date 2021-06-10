package com.autoxing.robot_core;

import android.os.Handler;
import android.os.Message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.autoxing.robot_core.bean.ActionStatus;
import com.autoxing.robot_core.bean.ChassisStatus;
import com.autoxing.robot_core.bean.MoveAction;
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

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.enums.ReadyState;
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

    private AXRobotPlatform() {
    }

    public static AXRobotPlatform getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private final int MESSAGE_DATA = 0;
    private final int MESSAGE_CLOSE = 1;
    private final int MESSAGE_ERROR = 2;

    private WebSocketClient mWebSocketClient;
    private List<IMappingListener> mListeners = new ArrayList<>();

    private List<TopicBase> mOccupancyGrids = new ArrayList<>();

    private Pose mPose = null;

    public void connect(String ip, int port) {
        String urlBase = String.format("http://%s:%d", ip, port);
        NetUtil.setUrlBase(urlBase);
    }

    public void addLisener(IMappingListener listener) {
        mListeners.add(listener);
    }

    private void notifyDataChanged() {
        for (int i = 0; i < mListeners.size(); ++i) {
            IMappingListener listener = mListeners.get(i);
            mListeners.get(i).onDataChanged(mOccupancyGrids);
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

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what) {
                case MESSAGE_DATA:
                    String data = msg.obj.toString();
                    parseTopicData(data);
                    break;
                case MESSAGE_CLOSE:
                case MESSAGE_ERROR:
                    webscoketAutoConnect();
                    break;
                default:break;
            }

            return true;
        }
    });

    private void webscoketAutoConnect() {
        if (mWebSocketClient != null && !mWebSocketClient.isOpen()) {
            ReadyState readyState = mWebSocketClient.getReadyState();
            if (readyState.equals(ReadyState.NOT_YET_CONNECTED)) {
                try {
                    mWebSocketClient.connect();
                } catch (IllegalStateException e) {
                }
            } else if (readyState.equals(ReadyState.CLOSED) || readyState.equals(ReadyState.CLOSING)) {
                mWebSocketClient.reconnect();
            }
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

                    setPose(pose);
                    topic.setPose(pose);
                    mOccupancyGrids.add(topic);
                }
            }
        }
        notifyDataChanged();
    }

    public void startWebSocket() {
        URI serverURI = URI.create(NetUtil.url_ws_topics);
        mWebSocketClient = new WebSocketClient(serverURI) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                String status = handshakedata.getHttpStatusMessage();
                notifyConnected(status);
            }

            @Override
            public void onMessage(String message) {
                Message handlerMessage = Message.obtain();
                handlerMessage.what = MESSAGE_DATA;
                handlerMessage.obj = message;
                mHandler.sendMessage(handlerMessage);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Message handlerMessage = Message.obtain();
                handlerMessage.what = MESSAGE_CLOSE;
                mHandler.sendMessageDelayed(handlerMessage, 3000);
            }

            @Override
            public void onError(Exception e) {
                notifyError(e);
                Message handlerMessage = Message.obtain();
                handlerMessage.what = MESSAGE_ERROR;
                mHandler.sendMessageDelayed(handlerMessage, 3000);
            }
        };
        mWebSocketClient.connect();
//        webSocketClient.send("");
    }

    public String getDeviceId() {
        return "0";
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
        Response res = NetUtil.syncReq2(NetUtil.url_chassis_status, hashmap, NetUtil.HTTP_METHOD.patch);
        if (res == null)
            return false;

        return res.code() == 200;
    }

    public Mapping startMapping() {
        String res = NetUtil.syncReq(NetUtil.url_mappings + "/?format=json", NetUtil.HTTP_METHOD.post);

        JSONObject jsonObject = null;
        try {
            jsonObject = JSON.parseObject(res);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        if (jsonObject == null)
            return null;

        Mapping mapping = new Mapping();
        mapping.setId(jsonObject.getInteger("id"));
        mapping.setStartTime(jsonObject.getLong("start_time"));
        mapping.setState(jsonObject.getString("state"));
        mapping.setUrl(jsonObject.getString("url"));
        return mapping;
    }

    public boolean stopCurrentMapping() {
        HashMap hashMap = new HashMap();
        hashMap.put("state", "finished");
        Response res = NetUtil.syncReq2(NetUtil.url_mappings + "/current" + "?format=json", hashMap, NetUtil.HTTP_METHOD.patch);
        if (res == null)
            return false;

        return res.code() == 200;
    }

    public List<Mapping> getMappingTasks() {
        String res = NetUtil.syncReq(NetUtil.url_mappings);
        JSONArray jsonArr = JSON.parseArray(res);
        List<Mapping> mappings = new ArrayList<>();
        if (jsonArr != null) {
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
        }
        return mappings;
    }

    public List<Map> getMaps() {
        String res = NetUtil.syncReq(NetUtil.url_maps);
        JSONArray jsonArr = JSON.parseArray(res);
        List<Map> maps = new ArrayList<>();
        if (jsonArr != null) {
            for (int i = 0; i < jsonArr.size(); i++) {
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

    public boolean setCurrentMap(Map map, Pose pose) {
        Response res = null;

        String data = map.getData();
        if (data == null) {
            HashMap<String, Integer> hashmap = new HashMap();
            hashmap.put("map_id", map.getId());
            res = NetUtil.syncReq2(NetUtil.url_chassis_current_map, hashmap, NetUtil.HTTP_METHOD.post);
        } else {
            res = NetUtil.syncReq3(NetUtil.url_chassis_current_map, data, NetUtil.HTTP_METHOD.post);
        }

        if (res == null)
            return false;

        return  res.code() == 200;
    }

    public int getCurrentMapId() {
        String res = NetUtil.syncReq(NetUtil.url_chassis_current_map, NetUtil.HTTP_METHOD.get);
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

        return  jsonObject.getInteger("map_id");
    }

    public boolean removeCurrentMap() {
        Response res = NetUtil.syncReq2(NetUtil.url_chassis_current_map, NetUtil.HTTP_METHOD.delete);
        if (res == null)
            return false;

        return res.code() == 200;
    }

    public void setPose(Pose pose) {
        mPose = pose;
    }

    public Pose getPose() {
        return mPose;
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
        if (option.isWithYaw())
            hashMap.put("target_ori", yaw);
        Response res = NetUtil.syncReq2(NetUtil.url_chassis_moves, hashMap, NetUtil.HTTP_METHOD.post);
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

    public MoveAction rotateTo(Rotation rotation) {
        HashMap hashMap = new HashMap();
        hashMap.put("target_ori", rotation.getYaw());
        Response res = NetUtil.syncReq2(NetUtil.url_chassis_moves, hashMap, NetUtil.HTTP_METHOD.post);
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

    public List<MoveAction> getMoveActions() {
        String res = NetUtil.syncReq(NetUtil.url_chassis_moves);
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
        List<MoveAction> moveActions = getMoveActions();
        for (int i = 0; i < moveActions.size(); i++) {
            MoveAction action = moveActions.get(i);
            if (action.getStatus() == ActionStatus.MOVING) {
                return action;
            }
        }
        return null;
    }

    public boolean moveWithAction(MoveDirection direction) {
        HashMap hashmap = new HashMap();
        hashmap.put("action", direction.toString().toLowerCase());
        Response res = NetUtil.syncReq2(NetUtil.url_chassis_remote_action, hashmap, NetUtil.HTTP_METHOD.post);
        if (res == null)
            return false;

        return res.code() == 200;
    }

    public MoveAction goHome() {
        MoveAction action = new MoveAction();
        return action;
    }

    // not complete, don't use it
    public boolean remoteTwist(float yaw) {
        HashMap hashmap = new HashMap();

        /*hashmap.put("velocity", );
        hashmap.put("angular_velocity", );
        hashmap.put("time", );*/

        Response res = NetUtil.syncReq2(NetUtil.url_chassis_remote_twist, hashmap, NetUtil.HTTP_METHOD.post);
        if (res == null)
            return false;

        return res.code() == 200;
    }
}
