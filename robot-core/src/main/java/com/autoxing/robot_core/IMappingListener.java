package com.autoxing.robot_core;

import com.autoxing.robot_core.bean.TopicBase;

import java.util.List;

public interface IMappingListener {
    void onConnected(String status);
    void onDataChanged(List<TopicBase> topics);
    void onError(Exception e);
}
