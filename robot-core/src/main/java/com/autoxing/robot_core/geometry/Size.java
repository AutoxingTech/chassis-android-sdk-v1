package com.autoxing.robot_core.geometry;

public class Size {
    private int mWidth;
    private int mHeight;

    public Size()
    {
        this.mWidth = 0;
        this.mHeight = 0;
    }

    public Size(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
    }

    public Size(Size rhs) {
        this.mWidth = rhs.mWidth;
        this.mHeight = rhs.mHeight;
    }

    public int getWidth() {
        return this.mWidth;
    }

    public void setWidth(int width) {
        this.mWidth = width;
    }

    public int getHeight() {
        return this.mHeight;
    }

    public void setHeight(int height) {
        this.mHeight = height;
    }
}
