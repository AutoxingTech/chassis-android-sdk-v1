package com.autoxing.robot_core.bean;

public class PoseTopic extends TopicBase {
    private Pose mPose;

    public PoseTopic() {
        super();
        mPose = new Pose();
    }

    public PoseTopic(Pose pose) {
        super();
        this.mPose = pose;
    }

    public Pose getPose() {
        return mPose;
    }

    public void setPose(Pose pose) {
        this.mPose = pose;
    }
}
