package com.autoxing.android.robot.api;

public class Rotation/* extends com.slamtec.slamware.robot.Rotation*/ {
//    public Rotation(float yaw, float pitch, float roll) {
//        super(yaw, pitch, roll);
//    }

    public float roll, pitch, yaw;
    public Rotation(float _roll, float _pitch, float _yaw)
    {
        roll = _roll;
        pitch = _pitch;
        yaw = _yaw;
    }

    public String toString()
    {
        String retString = "Rotation:(" + roll + "," + pitch + "," + yaw + ")";

        return retString;
    }
}
