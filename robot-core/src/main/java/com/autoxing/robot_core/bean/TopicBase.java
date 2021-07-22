package com.autoxing.robot_core.bean;

public class TopicBase {
    private String mTopic;
    private long mStamp;

    public TopicBase() {
        mTopic = null;
        mStamp = 0l;
    }

    public TopicBase(String topic, long stamp) {
        this.mTopic = topic;
        this.mStamp = stamp;
    }

    public String getTopic() { return this.mTopic; }
    public void setTopic(String topic) { this.mTopic = topic; }

    public long getStamp() { return this.mStamp; }
    public void setStamp(long stamp) { this.mStamp = stamp; }
}
