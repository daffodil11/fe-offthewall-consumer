package com.slick.offthewall;

public class Wall {

    private String[] imgUrls;
    private String streetAddress;
    private String info;
    private final int wallId;
    private float triggerWidth;
    private float triggerHeight;
    private float triggerOffsetX;
    private float triggerOffsetY;
    private float canvasWidth;
    private float canvasHeight;
    private final double latitude;
    private final double longitude;
    // private static final String TAG = "Wall";

    private static final int EARTH_RADIUS = 6371000;

    public Wall (int wallId, double latitude, double longitude) {
        this.wallId = wallId;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    public Integer getDistanceFrom(double currentLat, double currentLong) {
        final double psiOne = Math.toRadians(this.latitude);
        final double psiTwo = Math.toRadians(currentLat);
        final double deltaPsi = Math.toRadians(currentLat - this.latitude);
        final double deltaLambda = Math.toRadians(currentLong - this.longitude);

        final double a = Math.pow(Math.sin(deltaPsi / 2), 2) + Math.cos(psiOne) * Math.cos(psiTwo) * Math.pow(Math.sin(deltaLambda / 2), 2);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (int) Math.round(c * EARTH_RADIUS);
    }

    public int getWallId() {
        return this.wallId;
    }

    public void setWallData (
            String streetAddress,
            String info,
            float canvasWidth,
            float canvasHeight,
            float triggerWidth,
            float triggerHeight,
            float triggerOffsetX,
            float triggerOffsetY
    ) {
        this.streetAddress = streetAddress;
        this.info = info;
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.triggerWidth = triggerWidth;
        this.triggerHeight = triggerHeight;
        this.triggerOffsetX = triggerOffsetX;
        this.triggerOffsetY = triggerOffsetY;
    }
}
