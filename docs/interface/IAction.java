package com.autoxing.android.robot.api;

public interface IAction {
    public void cancel();
    public String getActionName();
    public double getProgress();
    public ActionStatus getStatus();
    public void waitUntilDone();
}
