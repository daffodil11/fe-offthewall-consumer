package com.slick.offthewall;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.sceneform.ux.ArFragment;

import java.io.IOException;
import java.io.InputStream;

public class AugmentedArtFragment extends ArFragment {

    public interface ArReadyListener {
        public void onArReady();
    }

    private ArReadyListener listener = null;

    private static final String TAG = "AugmentedArtFragment";
    private static final double MIN_OPENGL_VERSION = 3.0;

    private int triggerId;
    private String triggerFile;
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

    public void setOnArReadyListener(ArReadyListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        triggerId = this.getArguments().getInt("triggerId");
        triggerFile = triggerId + ".jpg";
        triggerWidth = this.getArguments().getFloat("triggerWidth");

        getPlaneDiscoveryController().hide();
        getPlaneDiscoveryController().setInstructionView(null);
        getArSceneView().getPlaneRenderer().setEnabled(false);
        if (listener != null) {
            this.listener.onArReady();
        }
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
        AugmentedImageDatabase augmentedImageDatabase;

        AssetManager am = getContext() != null ? getContext().getAssets() : null;
        if (am == null) {
            Log.e(TAG, "Could not get AssetManager. Augmented Image Database not initialised.");
            return false;
        }

        Bitmap triggerBitmap = loadAugmentedImageBitmap(am);
        if (triggerBitmap == null) {
            return false;
        }

        augmentedImageDatabase = new AugmentedImageDatabase(session);
        augmentedImageDatabase.addImage(triggerFile, triggerBitmap, triggerWidth);

        config.setAugmentedImageDatabase(augmentedImageDatabase);

        return true;
    }

    private Bitmap loadAugmentedImageBitmap(AssetManager am) {
        try (InputStream is = am.open(triggerFile)) {
            return BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            Log.e(TAG, "IOException loading trigger image bitmap", e);
            return null;
        }
    }
}
