package com.slick.offthewall;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.google.ar.core.AugmentedImage;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.Texture;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class AugmentedArtNode extends AnchorNode {

    private static final String TAG = "AugmentedArtNode";

    private final Wall wall;
    private static List<CompletableFuture<Material>> materialFutures;
    private static List<Material> materials;
    private Bitmap[] artworks = null;

    private int activeImage = 0;

    public AugmentedArtNode(Context context, Wall wall) {
        this.wall = wall;
        materialFutures = wall.getArtBitmaps(context.getAssets()).stream().map(bitmap -> {
           return Texture
                   .builder()
                   .setSource(bitmap)
                   .build()
                   .thenCompose(texture -> MaterialFactory.makeTransparentWithTexture(context, texture));
        }).collect(Collectors.toList());
    }



    public void setImage(AugmentedImage image) {
        final float triggerWidth = this.wall.getTriggerWidth();
        final float triggerHeight = this.wall.getTriggerHeight();
        final float triggerOffsetX = this.wall.getTriggerOffsetX();
        final float triggerOffsetY = this.wall.getTriggerOffsetY();
        final float canvasWidth = this.wall.getCanvasWidth();
        final float canvasHeight = this.wall.getCanvasHeight();

        final float artLocationX = -(triggerOffsetX + (triggerWidth / 2)) + (canvasWidth / 2);
        final float artLocationY = -(triggerOffsetY + (triggerHeight / 2)) + (canvasHeight / 2);

        setAnchor(image.createAnchor(image.getCenterPose()));

        Vector3 localPosition = new Vector3(artLocationX, 0.0f, artLocationY);
        Node artNode;
        artNode = new Node();
        artNode.setParent(this);
        artNode.setLocalPosition(localPosition);
        float[] yAxis = image.getCenterPose().getYAxis();
        Vector3 planeNormal = new Vector3(yAxis[0], yAxis[1], yAxis[2]);
        Quaternion upQuat = Quaternion.lookRotation(planeNormal, Vector3.up());
        artNode.setWorldRotation(upQuat);

        final Vector3 canvasDims = new Vector3(canvasWidth, canvasHeight, 0.0f);

        if (materialFutures.stream().anyMatch(future -> !future.isDone())) {
            CompletableFuture.allOf(materialFutures.toArray(new CompletableFuture[materialFutures.size()]))
                    .thenAccept((Void theVoid) -> setImage(image))
                    .exceptionally(throwable -> {
                        Log.e(TAG, "Exception loading artworks", throwable);
                        return null;
                    });
        } else {
            if (materials == null || materials.isEmpty()) {
                materials = materialFutures.stream()
                        .map(future -> future.join())
                        .map(material -> {
                            Log.i(TAG, "Successfully made materials!");
                            material.setFloat(MaterialFactory.MATERIAL_REFLECTANCE, 0.0f);
                            material.setFloat(MaterialFactory.MATERIAL_ROUGHNESS, 0.8f);
                            material.setFloat(MaterialFactory.MATERIAL_METALLIC, 0.0f);
                            return material;
                        })
                        .collect(Collectors.toList());
            }
            artNode.setRenderable(ShapeFactory.makeCube(canvasDims, new Vector3(), materials.get(activeImage)));
        }
    }

}
