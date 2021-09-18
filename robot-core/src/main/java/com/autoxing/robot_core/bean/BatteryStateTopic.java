package com.autoxing.robot_core.bean;

public class BatteryStateTopic extends TopicBase {

    private float mVoltage;
    private float mCurrent;
    private float mPercentage;
    private PowerSupplyStatus mPowerStatus;

    public BatteryStateTopic() {
        super();
        mVoltage = .0f;
        mCurrent = .0f;
        mPercentage = .0f;
        mPowerStatus = PowerSupplyStatus.UNKNOWN;
    }

    public void setVoltage(float voltage) { mVoltage = voltage; }
    public float getVoltage() { return mVoltage; }

    public void setCurrent(float current) { mCurrent = current; }
    public float getCurrent() { return mCurrent; }

    public void setPercentage(float percentage) { mPercentage = percentage; }
    public float getPercentage() { return mPercentage; }

    public void setPowerSupplyStatus(PowerSupplyStatus status) { mPowerStatus = status; }
    public PowerSupplyStatus getPowerSupplyStatus() { return mPowerStatus; }
}
