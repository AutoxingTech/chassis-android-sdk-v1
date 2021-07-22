package com.autoxing.robot_core.bean;

public class Pose {
    private Location mLocation;
    private Rotation mRotation;

    public Pose() {
        mLocation = new Location();
        mRotation = new Rotation();
    }

    public Pose(Location location, Rotation rotation) {
        this.mLocation = location;
        this.mRotation = rotation;
    }

    public Pose(float x, float y, float z, float yaw, float pitch, float roll)
    {
        this.mLocation = new Location(x, y, z);
        this.mRotation = new Rotation(yaw, pitch, roll);
    }

    public Pose(Pose rhs)
    {
        this.mLocation = new Location(rhs.mLocation);
        this.mRotation = new Rotation(rhs.mRotation);
    }

    public Location getLocation() {
        return mLocation;
    }

    public void setLocation(Location location) {
        this.mLocation = location;
    }

    public Rotation getRotation() {
        return mRotation;
    }

    public void setRotation(Rotation rotation) {
        this.mRotation = rotation;
    }

    public float getX()
    {
        return this.mLocation.getX();
    }

    public void setX(float v)
    {
        this.mLocation.setX(v);
    }

    public float getY()
    {
        return this.mLocation.getY();
    }

    public void setY(float v)
    {
        this.mLocation.setY(v);
    }

    public float getZ()
    {
        return this.mLocation.getZ();
    }

    public void setZ(float v)
    {
        this.mLocation.setZ(v);
    }

    public float getYaw() { return this.mRotation.getYaw(); }

    public void setYaw(float v)
    {
        this.mRotation.setYaw(v);
    }

    public float getRoll()
    {
        return this.mRotation.getRoll();
    }

    public void setRoll(float v)
    {
        this.mRotation.setRoll(v);
    }

    public float getPitch()
    {
        return this.mRotation.getPitch();
    }

    public void setPitch(float v)
    {
        this.mRotation.setPitch(v);
    }
}
