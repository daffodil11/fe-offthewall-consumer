package com.slick.offthewall;

public class Wall {

    private String[] imgUrls;
    private final int wallID;
    /*private final float triggerWidth;
    private final float triggerHeight;
    private final float triggerOffsetX;
    private final float triggerOffsetY;
    private final float wallWidth;
    private final float wallHeight;*/
    private final float longitude;
    private final float latitude;
    // private static final String TAG = "Wall";

    private static final int EARTH_RADIUS = 6371000;

    public Wall (int wallID, float latitude, float longitude) {
        this.wallID = wallID;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    public int getDistanceFrom(float currentLat, float currentLong) {
        final double psiOne = Math.toRadians(this.latitude);
        final double psiTwo = Math.toRadians(currentLat);
        final double deltaPsi = Math.toRadians(currentLat - this.latitude);
        final double deltaLambda = Math.toRadians(currentLong - this.longitude);

        final double a = Math.pow(Math.sin(deltaPsi / 2), 2) + Math.cos(psiOne) * Math.cos(psiTwo) * Math.pow(Math.sin(deltaLambda / 2), 2);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (int) Math.round(c * EARTH_RADIUS);
    }
}
