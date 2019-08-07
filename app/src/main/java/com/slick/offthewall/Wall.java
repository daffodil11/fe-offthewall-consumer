package com.slick.offthewall;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Wall {

    private static final String TAG = "Wall";

    private String[] imgUrls;
    private List<Art> artworks;
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

    /*public void setWallArt (List<Art> artworks) {
        this.artworks = artworks;
    }*/

    /*public List<Bitmap> getArtBitmaps () {
        return this.artworks.stream().map(art -> {
            try {
                URL url = new URL(art.getUrl());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                connection.disconnect();
                input.close();
                return bitmap;
            } catch (Exception e) {
                Log.e(TAG, "Bad artwork URL.", e);
                return null;
            }
        }).collect(Collectors.toList());
    }*/

    public List<URL> getArtUrls () {
        return this.artworks.stream().map(artwork -> {
            try {
                return new URL(artwork.getUrl());
            } catch (MalformedURLException e) {
                Log.e(TAG, "Bad artwork URL", e);
                return null;
            }
        }).collect(Collectors.toList());
    }

    public List<Bitmap> getArtBitmaps (AssetManager am) {
        String[] placeholders = new String[]{"raven_nc.png", "sunmoondance_nc.png"};
        return Arrays.stream(placeholders).map(art -> {
            InputStream is = null;
            try {
                is = am.open(art);
                return BitmapFactory.decodeStream(is);
            } catch (Exception e) {
                Log.e(TAG, "Bad artwork URL.", e);
                return null;
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ignored) {}
                }
            }
        }).collect(Collectors.toList());
    }

    public float getTriggerWidth() {
        return this.triggerWidth;
    }

    public float getTriggerHeight() {
        return this.triggerHeight;
    }

    public float getTriggerOffsetX() {
        return this.triggerOffsetX;
    }

    public float getTriggerOffsetY() {
        return this.triggerOffsetY;
    }

    public float getCanvasWidth() {
        return this.canvasWidth;
    }

    public float getCanvasHeight() {
        return this.canvasHeight;
    }
}
