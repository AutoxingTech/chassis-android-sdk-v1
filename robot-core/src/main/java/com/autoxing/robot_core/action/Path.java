package com.autoxing.robot_core.action;

import com.autoxing.robot_core.bean.Location;
import java.util.Vector;

public class Path {
    private Vector<Location> mPoints;

    public Path() {
        mPoints = new Vector<>();
    }

    public Path(Vector<Location> points) {
        mPoints = new Vector<>();
        copyLocations(points);
    }

    public Path(Path path) {
        mPoints = new Vector<>();
        copyLocations(path.getPoints());
    }

    public Vector<Location> getPoints() {
        return mPoints;
    }

    public void setPoints(Vector<Location> points) {
        copyLocations(points);
    }

    private void copyLocations(Vector<Location> points) {
        for (Location location : points)
            mPoints.add(location);
    }
}
