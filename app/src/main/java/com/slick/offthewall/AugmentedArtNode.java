package com.slick.offthewall;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageButton;

import com.google.ar.core.AugmentedImage;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.DpToMetersViewSizer;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.Texture;
import com.google.ar.sceneform.rendering.ViewRenderable;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class AugmentedArtNode extends AnchorNode {

    private static final String TAG = "AugmentedArtNode";

    private Context context;
    private static List<CompletableFuture<Material>> materialFutures = null;
    private static List<Material> materials;
    private Node artNode;

    private int activeImage = 0;
    private Vector3 canvasDims;
    private int dpPerM;

    public AugmentedArtNode(Context context) {
        this.context = context;
    }

    public void setArt(Bitmap[] bitmaps) {
        materialFutures = Arrays.stream(bitmaps).map(bitmap -> {
            return Texture
                    .builder()
                    .setSource(bitmap)
                    .build()
                    .thenCompose(texture -> MaterialFactory.makeTransparentWithTexture(this.context, texture));
        }).collect(Collectors.toList());
    }

    public boolean setImage(AugmentedImage image, Wall wall) {
        if (materialFutures == null) {
            return false;
        }
        final float triggerWidth = wall.getTriggerWidth();
        final float triggerHeight = wall.getTriggerHeight();
        final float triggerOffsetX = wall.getTriggerOffsetX();
        final float triggerOffsetY = wall.getTriggerOffsetY();
        final float canvasWidth = wall.getCanvasWidth();
        final float canvasHeight = wall.getCanvasHeight();

        final float artLocationX = -(triggerOffsetX + (triggerWidth / 2)) + (canvasWidth / 2);
        final float artLocationY = -(triggerOffsetY + (triggerHeight / 2)) + (canvasHeight / 2);

        setAnchor(image.createAnchor(image.getCenterPose()));

        Vector3 localPosition = new Vector3(artLocationX, 0.0f, artLocationY);
        final Vector3 rightButtonOffset = new Vector3(-(canvasWidth / 2.0f), 0.0f, -0.15f);
        final Vector3 leftButtonOffset = new Vector3((canvasWidth / 2.0f), 0.0f, -0.15f);

        dpPerM = (int) Math.floor(250 / canvasWidth);

        artNode = new Node();
        artNode.setParent(this);
        artNode.setLocalPosition(localPosition);
        float[] yAxis = image.getCenterPose().getYAxis();
        Vector3 planeNormal = new Vector3(yAxis[0], yAxis[1], yAxis[2]);
        Quaternion upQuat = Quaternion.lookRotation(planeNormal, Vector3.up());
        artNode.setWorldRotation(upQuat);

        canvasDims = new Vector3(canvasWidth, canvasHeight, 0.0f);

        if (materialFutures.stream().anyMatch(future -> !future.isDone())) {
            CompletableFuture.allOf(materialFutures.toArray(new CompletableFuture[materialFutures.size()]))
                    .thenAccept((Void theVoid) -> setImage(image, wall))
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

        Node rightButtonNode = new Node();
        Node leftButtonNode = new Node();
        buildButton(rightButtonNode, rightButtonOffset, R.layout.button_right, 1);
        buildButton(leftButtonNode, leftButtonOffset, R.layout.button_left, -1);

        return true;
    }

    private void buildButton(Node node, Vector3 position, int layout, int increment) {
        node.setParent(artNode);
        node.setLocalPosition(position);
        ViewRenderable.builder()
                .setView(this.context, layout)
                .setVerticalAlignment(ViewRenderable.VerticalAlignment.CENTER)
                .setHorizontalAlignment(ViewRenderable.HorizontalAlignment.CENTER)
                .build()
                .thenAccept(
                        (renderableButton) -> {
                            renderableButton.setSizer(new DpToMetersViewSizer(dpPerM));
                            node.setRenderable(renderableButton);
                            ImageButton button = (ImageButton) renderableButton.getView();
                            button.setOnClickListener(view -> {
                                activeImage = ((activeImage + increment) % materials.size() + materials.size()) % materials.size();
                                artNode.setRenderable(ShapeFactory.makeCube(canvasDims, new Vector3(), materials.get(activeImage)));
                            });
                        });
    }

}
