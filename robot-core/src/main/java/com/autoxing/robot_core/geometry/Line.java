package com.autoxing.robot_core.geometry;

public class Line {
    private int mSegmentId;
    private PointF mStartPoint;
    private PointF mEndPoint;

    public Line(int segmentId, PointF startPoint, PointF endPoint)
    {
        this.mSegmentId = segmentId;
        this.mStartPoint = new PointF(startPoint);
        this.mEndPoint = new PointF(endPoint);
    }

    public Line(int segmentId, float startX, float startY, float endX, float endY) {
        this.mSegmentId = segmentId;
        this.mStartPoint = new PointF(startX, startY);
        this.mEndPoint = new PointF(endX, endY);
    }

    public Line(Line line) {
        this.mSegmentId = line.mSegmentId;
        this.mStartPoint = new PointF(line.mStartPoint);
        this.mEndPoint = new PointF(line.mEndPoint);
    }

    public Line(PointF startP, PointF endP) {
        this.mStartPoint = startP;
        this.mEndPoint = endP;
    }

    public PointF getStartPoint() { return this.mStartPoint; }
    public void setStartPoint(PointF startPointF) { this.mStartPoint = new PointF(startPointF); }

    public PointF getEndPoint() { return this.mEndPoint; }
    public void setEndPoint(PointF endPoint) { this.mEndPoint = new PointF(endPoint); }

    public float getStartX() { return getStartPoint().getX(); }
    public float getStartY() { return getStartPoint().getY(); }

    public float getEndX() { return getEndPoint().getX(); }
    public float getEndY() { return getEndPoint().getY(); }

    public int getSegmentId() { return this.mSegmentId; }
    public void setSegmentId(int segmentId) { this.mSegmentId = segmentId; }
}
