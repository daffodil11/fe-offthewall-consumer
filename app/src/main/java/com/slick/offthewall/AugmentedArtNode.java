package com.slick.offthewall;

import android.content.Context;

import com.google.ar.sceneform.AnchorNode;

public class AugmentedArtNode extends AnchorNode {

    private static final String TAG = "AugmentedArtNode";

    private final Wall wall;

    public AugmentedArtNode(Context context, Wall wall) {
        this.wall = wall;
    }

}
