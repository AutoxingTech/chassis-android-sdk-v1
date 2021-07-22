package com.autoxing.robot_core.geometry;

public class PointF {

    private float mX;
    private float mY;

    public PointF()
    {
        this.mX = 0.0F;
        this.mY = 0.0F;
    }

    public PointF(float x, float y) {
        this.mX = x;
        this.mY = y;
    }

    public PointF(PointF rhs) {
        this.mX = rhs.mX;
        this.mY = rhs.mY;
    }

    public float getX() {
        return this.mX;
    }

    public void setX(float x) {
        this.mX = x;
    }

    public float getY() {
        return this.mY;
    }

    public void setY(float y) {
        this.mY = y;
    }
}
