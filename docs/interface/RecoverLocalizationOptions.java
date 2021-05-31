package com.autoxing.android.robot.api;

public class RecoverLocalizationOptions {
    private RecoverLocalizationMovement movementType;
    private Integer maxRecoverMS;

    public void setMaxRecoverTimeInMilliSeconds(Integer maxRecoverMS) {
        this.maxRecoverMS = maxRecoverMS;
    }

    public Integer getMaxRecoverTimeInMilliSeconds() {
        return this.maxRecoverMS;
    }

    public void setRecoverMovementType(RecoverLocalizationMovement movementType) {
        this.movementType = movementType;
    }

    public RecoverLocalizationMovement getRecoverMovementType() {
        return this.movementType;
    }
}
