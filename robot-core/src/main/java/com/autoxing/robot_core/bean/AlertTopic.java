package com.autoxing.robot_core.bean;

import java.util.ArrayList;
import java.util.List;

public class AlertTopic extends TopicBase {
    private String mPartName;
    private List<AlertInfo> mInfos = new ArrayList<>();

    public AlertTopic() {
        super();
    }

    public void setPartName(String partName) { mPartName = partName; }
    public String getPartName() { return mPartName; }

    public void addAlertInfo(AlertInfo info) { mInfos.add(info); }
    public List<AlertInfo> getAlertInfos() { return mInfos; }
}
