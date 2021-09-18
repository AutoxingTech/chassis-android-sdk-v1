package com.autoxing.robot_core.bean;

public class TopicBase {
    private String mTopic;
    private long mTimestamp;

    public TopicBase() {
        mTopic = null;
        mTimestamp = 0l;
    }

    public TopicBase(String topic, long timestamp) {
        mTopic = topic;
        mTimestamp = timestamp;
    }

    public String getTopic() { return mTopic; }
    public void setTopic(String topic) { mTopic = topic; }

    public long getTimestamp() { return mTimestamp; }
    public void setTimestamp(long timestamp) { mTimestamp = timestamp; }
}
