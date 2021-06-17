package com.autoxing.robot_core.util;

import com.autoxing.robot_core.bean.Location;
import com.autoxing.robot_core.geometry.PointF;

public class CoordinateUtil {

    private float mOriginX;
    private float mOriginY;
    private float mResolution;

    public void setOrigin(float originX, float originY) {
        mOriginX = originX;
        mOriginY = originY;
    }

    public void setResolution(float resolution) {
        mResolution = resolution;
    }

    public Location screenToWorld(float x, float y) {
        Location location = new Location();
        location.setX(mOriginX + mResolution * x);
        location.setY(mOriginY + mResolution * y);
        location.setZ(0);
        return location;
    }

    public PointF worldToScreen(Location location) {
        PointF pt = new PointF();
        pt.setX((location.getX() - mOriginX) / mResolution);
        pt.setY((location.getY() - mOriginY) / mResolution);
        return pt;
    }
}
