package com.autoxing.robot_core.bean;

public class AlertInfo {
    private int mCode;
    private AlertLevel mLevel;
    private String mMessage;

    public void setCode(int code) { mCode = code; }
    public int getCode() { return mCode; }

    public void setLevel(AlertLevel level) { mLevel = level; }
    public AlertLevel getLevel() { return mLevel; }

    public void setMessage(String msg) { mMessage =msg; }
    public String getmMessage() { return mMessage; }
}
