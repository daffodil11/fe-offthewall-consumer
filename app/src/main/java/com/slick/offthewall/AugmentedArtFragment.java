package com.slick.offthewall;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.sceneform.ux.ArFragment;

public class AugmentedArtFragment extends ArFragment {

    private static final String TAG = "AugmentedArtFragment";
    private static final double MIN_OPENGL_VERSION = 3.0;

    private int triggerId;
    private float triggerWidth;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
            Toast.makeText(context, "Sceneform requires Android N or later", Toast.LENGTH_LONG);
        }

        String openGlVersionString =
                ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 or later");
            Toast.makeText(context, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        triggerId = this.getArguments().getInt("triggerId");
        triggerWidth = this.getArguments().getFloat("triggerWidth");

        getPlaneDiscoveryController().hide();
        getPlaneDiscoveryController().setInstructionView(null);
        getArSceneView().getPlaneRenderer().setEnabled(false);
        return view;
    }

    @Override
    protected Config getSessionConfiguration(Session session) {
        Config config = super.getSessionConfiguration(session);

        //Enable auto-focus.
        config.setFocusMode(Config.FocusMode.AUTO);
        config.setLightEstimationMode(Config.LightEstimationMode.DISABLED);
        if (!setupAugmentedImageDatabase(config, session)) {
            Toast.makeText(this.getContext(), "Could not setup augmented image database", Toast.LENGTH_LONG);
        }
        return config;
    }

    private boolean setupAugmentedImageDatabase(Config config, Session session) {
        return false;
    }
}
