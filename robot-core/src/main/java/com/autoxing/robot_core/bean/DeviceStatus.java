package com.autoxing.robot_core.bean;

import com.alibaba.fastjson.JSONObject;

public class DeviceStatus {
    private String mVersion;
    private String mSerialNumber;

    public DeviceStatus()
    {
        mVersion = null;
        mSerialNumber = null;
    }

    public DeviceStatus(JSONObject json)
    {
        mVersion = json.getString("version");
        mSerialNumber = json.getString("sn");
    }

    public String getVersion() { return  mVersion; }
    public void getVersion(String ver) { mVersion = ver; }

    public String getmSerialNumber() { return  mSerialNumber; }
    public void setmSerialNumber(String sn) { mSerialNumber = sn; }
}
