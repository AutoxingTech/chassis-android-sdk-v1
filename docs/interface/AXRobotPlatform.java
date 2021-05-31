public final class AXRobotPlatform {

    AXRobotPlatform(String ip, int port) {
    }

    public String getDeviceId() throws Exception {
        return "car_88888888";
    }

    public Pose getPose() throws Exception {
        return null;
    }

    public int getBatteryPercentage() throws Exception {
        return 0;
    }

    public boolean getBatteryIsCharging() throws Exception {
        return false;
    }

    public int getLocalizationQuality() throws Exception {
        return 0;
    }

    public HashMap getSensorValues() throws Exception {
        return null;
    }

    public IMoveAction getCurrentAction() throws Exception {
        return null;
    }

    public boolean clearLines(ArtifactUsage artifactUsage) throws Exception {
        return false;
    }

    public IMoveAction rotateTo(Rotation rotation) throws Exception {
        return null;
    }

    public void setCompositeMap(CompositeMap compositeMap, Pose pose) throws Exception {
    }

    public boolean getMapLocalization() throws Exception {
        return false;
    }

    public void setMapLocalization(boolean isSetMapLoc) throws Exception {
    }

    public IMoveAction recoverLocalization(RectF area, RecoverLocalizationOptions recoverLocalizationOptions) throws Exception {
        return null;
    }

    public IMoveAction moveTo(Location location, MoveOption moveOption, float toYaw) throws Exception {
        return null;
    }

    public void setPose(Pose pose) throws Exception {
    }

    public IMoveAction goHome() throws Exception {
        return null;
    }

    public boolean addLines(ArtifactUsage artifactUsage, List<Line> lines) throws Exception {
        return false;
    }

    public IMoveAction moveBy(MoveDirection direction) throws Exception {
        return null;
    }
}