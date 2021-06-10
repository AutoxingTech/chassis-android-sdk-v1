package com.autoxing.robot_core.bean;

public class Rotation {
    private float mRoll;
    private float mPitch;
    private float mYaw;

    public Rotation() {
        this.mRoll = this.mPitch = this.mYaw = .0f;
    }

    public Rotation(float yaw) {
        this.mRoll = this.mPitch = .0f;
        this.mYaw = yaw;
    }

    public Rotation(Rotation rhs) {
        this.mRoll = rhs.mRoll;
        this.mPitch = rhs.mPitch;
        this.mYaw = rhs.mYaw;
    }

    public Rotation(float roll, float pitch, float yaw) {
        this.mRoll = roll;
        this.mPitch = pitch;
        this.mYaw = yaw;
    }

    public float getRoll() {
        return mRoll;
    }

    public void setRoll(float roll) {
        this.mRoll = roll;
    }

    public float getPitch() {
        return mPitch;
    }

    public void setPitch(float pitch) {
        this.mPitch = pitch;
    }

    public float getYaw() {
        return mYaw;
    }

    public void setYaw(float yaw) {
        this.mYaw = yaw;
    }
}
