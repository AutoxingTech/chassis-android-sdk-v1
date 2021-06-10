package com.autoxing.robot_core.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.autoxing.robot_core.util.NetUtil;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Response;

public class MoveAction extends IAction {

    public ActionStatus waitUntilDone() {
        int count = 0;
        ActionStatus status = ActionStatus.ERROR;
        for (;;) {
            status = getCurrentStatus();
            if (status != ActionStatus.MOVING) {
                return status;
            }

            count++;
            if (count % 1000 == 0) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean cancel() {
        HashMap hashMap = new HashMap();
        hashMap.put("state", "cancelled");
        Response res = NetUtil.syncReq2(NetUtil.url_chassis_moves + "/" + this.id +  "?format=json", hashMap, NetUtil.HTTP_METHOD.patch);
        if (res == null)
            return false;

        return res.code() == 200;
    }

    private ActionStatus getCurrentStatus() {
        Response res = NetUtil.syncReq2(NetUtil.url_chassis_moves + "/" + this.id +  "?format=json", NetUtil.HTTP_METHOD.get);
        if (res == null)
            return ActionStatus.ERROR;

        if (res.code() != 200)
            return ActionStatus.ERROR;

        JSONObject jsonObject = null;
        try {
            jsonObject = JSON.parseObject(res.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }

        if (jsonObject == null)
            return ActionStatus.ERROR;

        String stateStr = jsonObject.getString("state");
        return ActionStatus.valueOf(stateStr.toUpperCase());
    }
}
