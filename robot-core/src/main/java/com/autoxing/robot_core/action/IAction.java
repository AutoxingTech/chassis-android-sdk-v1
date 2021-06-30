package com.autoxing.robot_core.action;

public class IAction {
    protected int id;
    protected ActionStatus status;

    public int getId() { return  this.id; }
    public void setId(int id) { this.id = id; }

    public ActionStatus getStatus() { return  this.status; }
    public void setStatus(ActionStatus status) { this.status = status; }

    public ActionStatus waitUntilDone() {
        return ActionStatus.FAILED;
    }

    public boolean cancel() {
        return false;
    }

    public Path getRemainingPath() { return null; }
}
