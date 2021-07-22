package com.autoxing.robot_core.action;

public class IAction {
    protected int mId;
    protected ActionStatus mStatus;

    public int getId() { return  this.mId; }
    public void setId(int id) { this.mId = id; }

    public ActionStatus getStatus() { return  this.mStatus; }
    public void setStatus(ActionStatus status) { this.mStatus = status; }

    public ActionStatus waitUntilDone() {
        return ActionStatus.FAILED;
    }

    public boolean cancel() {
        return false;
    }

    public Path getRemainingPath() { return null; }
}
