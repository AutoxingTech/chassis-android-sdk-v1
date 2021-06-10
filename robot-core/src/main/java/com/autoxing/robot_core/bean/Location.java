package com.autoxing.robot_core.bean;

public class Location {

    private float x;
    private float y;
    private float z;

    public Location() {
        this.x = this.y = this.z = .0f;
    }

    public Location(Location rhs) {
        this.x = rhs.x;
        this.y = rhs.y;
        this.z = rhs.z;
    }

    public Location(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getX() {
        return x;
    }
    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }
    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }
    public void setZ(float z) {
        this.z = z;
    }

    public float distanceTo(Location that) {
        float dx = that.x - this.x;
        float dy = that.y - this.y;
        float dz = that.z - this.z;
        return (float)Math.sqrt((double)(dx * dx + dy * dy + dz * dz));
    }
}
