package com.autoxing.robot_core.bean;

public class MoveOption {

    private boolean mWithYaw;

    public MoveOption() {
        mWithYaw = false;
    }

    public boolean isWithYaw() {
        return mWithYaw;
    }

    public void setWithYaw(boolean withYaw) {
        this.mWithYaw = withYaw;
    }
}
