package com.autoxing.robot_core.action;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.autoxing.robot_core.util.NetUtil;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Response;

public class MoveAction extends IAction {

    private int mCostTime = -1;
    private int mUpdateStateTime = -1;
    private int mCallServiceTime = -1;

    @Override
    public ActionStatus waitUntilDone() {
        ActionStatus status = ActionStatus.FAILED;
        for (;;) {
            long timeStamp = System.currentTimeMillis();
            status = getCurrentStatus();
            long clientTime = System.currentTimeMillis() - timeStamp;
            System.out.println("===robot-core-net=====get status = " + status.toString() + ",clientTime = " + clientTime + ",costTime = " + mCostTime + ",updateStateTime = " + mUpdateStateTime + ",callServiceTime = " + mCallServiceTime);
            if (status != ActionStatus.MOVING) {
                mStatus = status;
                return status;
            }
        }
    }

    @Override
    public boolean cancel() {
        HashMap hashMap = new HashMap();
        hashMap.put("state", "cancelled");
        Response res = NetUtil.syncReq2(NetUtil.getUrl(NetUtil.SERVICE_CHASSIS_MOVES) + "/" + this.mId +  "?format=json", hashMap, NetUtil.HTTP_METHOD.patch);
        if (res == null)
            return false;

        boolean succ = res.code() / 100 == 2;
        if (succ)
            mStatus = ActionStatus.CANCELLED;

        return succ;
    }

    // not used
    @Override
    public Path getRemainingPath() { return new Path(); }

    public ActionStatus getCurrentStatus() {
        Response res = NetUtil.syncReq2(NetUtil.getUrl(NetUtil.SERVICE_CHASSIS_MOVES) + "/" + this.mId +  "?format=json", NetUtil.HTTP_METHOD.get);
        if (res == null)
            return ActionStatus.FAILED;

        if (res.code() != 200)
            return ActionStatus.FAILED;

        JSONObject jsonObject = null;
        try {
            jsonObject = JSON.parseObject(res.body().string());

            // 1. 整个服务的响应时间, ms
            String costTimeStr = res.header("X-Page-Generation-Duration-Ms", "-1");
            mCostTime = Integer.parseInt(costTimeStr);

            // 2. 调用service 并更新 sqlite 的时间，s
            String updateStateTimeStr = res.header("X-Planning-Update-State-Cost-Time", "-0.00000000001");
            double updateStateTimeD = Double.parseDouble(updateStateTimeStr) * 1000;
            mUpdateStateTime = (int)updateStateTimeD;

            // 3. call service 的时间，s
            String callServiceTimeStr = res.header("X-Planning-Get-State-Service-Cost-Time", "-0.00000000001");
            double callServiceTimeD = Double.parseDouble(callServiceTimeStr) * 1000;
            mCallServiceTime = (int)callServiceTimeD;

            // 1 包含 2， 2 包含 3
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }

        if (jsonObject == null)
            return ActionStatus.FAILED;

        String stateStr = jsonObject.getString("state");
        mStatus = ActionStatus.valueOf(stateStr.toUpperCase());
        int failReasonValue = jsonObject.getIntValue("fail_reason");
        mFailReason= MoveFailReason.valueOf(failReasonValue);
        return mStatus;
    }

    public void setCostTime(int costTime) { mCostTime = costTime; }
    public int getCostTime() { return mCostTime; }
}
