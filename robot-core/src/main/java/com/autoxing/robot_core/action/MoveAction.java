package com.autoxing.robot_core.action;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.autoxing.robot_core.util.NetUtil;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Response;

public class MoveAction extends IAction {

    @Override
    public ActionStatus waitUntilDone() {
        ActionStatus status = ActionStatus.FAILED;
        for (;;) {
            status = getCurrentStatus();
            if (status != ActionStatus.MOVING) {
                return status;
            }
        }
    }

    @Override
    public boolean cancel() {
        HashMap hashMap = new HashMap();
        hashMap.put("state", "cancelled");
        Response res = NetUtil.syncReq2(NetUtil.getUrl(NetUtil.SERVICE_CHASSIS_MOVES) + "/" + this.id +  "?format=json", hashMap, NetUtil.HTTP_METHOD.patch);
        if (res == null)
            return false;

        return res.code() == 200;
    }

    @Override
    public Path getRemainingPath() { return new Path(); }

    private ActionStatus getCurrentStatus() {
        Response res = NetUtil.syncReq2(NetUtil.getUrl(NetUtil.SERVICE_CHASSIS_MOVES) + "/" + this.id +  "?format=json", NetUtil.HTTP_METHOD.get);
        if (res == null)
            return ActionStatus.FAILED;

        if (res.code() != 200)
            return ActionStatus.FAILED;

        JSONObject jsonObject = null;
        try {
            jsonObject = JSON.parseObject(res.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }

        if (jsonObject == null)
            return ActionStatus.FAILED;

        String stateStr = jsonObject.getString("state");
        return ActionStatus.valueOf(stateStr.toUpperCase());
    }
}
