package com.autoxing.robot_core.bean;

public class IAction {
    protected int id;
    protected ActionStatus status;

    public int getId() { return  this.id; }
    public void setId(int id) { this.id = id; }

    public ActionStatus getStatus() { return  this.status; }
    public void setStatus(ActionStatus status) { this.status = status; }

    public ActionStatus waitUntilDone() {
        return ActionStatus.ERROR;
    }

    public boolean cancel() {
        return false;
    }
}
