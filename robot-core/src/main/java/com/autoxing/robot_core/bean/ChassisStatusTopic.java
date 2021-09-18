package com.autoxing.robot_core.bean;

public class ChassisStatusTopic extends TopicBase {
    private ChassisControlMode mControlMode;
    private boolean mEmergencyStopPressed;

    public ChassisStatusTopic() {
        super();
    }

    public ChassisControlMode getControlMode() {
        return mControlMode;
    }

    public void setControlMode(ChassisControlMode mode) {
        this.mControlMode = mode;
    }

    public boolean isEmergencyStopPressed() {
        return mEmergencyStopPressed;
    }

    public void setEmergencyStopPressed(boolean stopPressed) {
        this.mEmergencyStopPressed = stopPressed;
    }
}
