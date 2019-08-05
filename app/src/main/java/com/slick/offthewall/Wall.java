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

    public Wall (int wallID, float latitude, float longitude) {
        this.wallID = wallID;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
