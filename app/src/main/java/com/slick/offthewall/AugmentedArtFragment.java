package com.slick.offthewall;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.google.ar.sceneform.ux.ArFragment;

public class AugmentedArtFragment extends ArFragment {

    private static final String TAG = "AugmentedArtFragment";
    private static final double MIN_OPENGL_VERSION = 3.0;

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
}
