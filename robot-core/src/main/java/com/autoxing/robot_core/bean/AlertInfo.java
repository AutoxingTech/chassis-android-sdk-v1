package com.autoxing.robot_core.bean;

public class AlertInfo {
    private AlertCode mCode;
    private AlertLevel mLevel;
    private String mMessage;

    public AlertInfo() {
        mCode = AlertCode.UNKNOWN;
        mLevel = AlertLevel.WARNING;
        mMessage = null;
    }

    public void setCode(AlertCode code) { mCode = code; }
    public AlertCode getCode() { return mCode; }

    public void setLevel(AlertLevel level) { mLevel = level; }
    public AlertLevel getLevel() { return mLevel; }

    public void setMessage(String msg) { mMessage =msg; }
    public String getmMessage() { return mMessage; }
}
