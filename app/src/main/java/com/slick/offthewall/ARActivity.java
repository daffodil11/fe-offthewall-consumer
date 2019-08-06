package com.slick.offthewall;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.sceneform.FrameTime;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ARActivity extends AppCompatActivity {

    private static final double NC_LAT = 53.7949778;
    private static final double NC_LONG = -1.5449472;

    private FragmentTransaction fragmentTransaction;
    private AugmentedArtFragment augmentedArtFragment;
    private ImageView hintImageView;
    private FloatingActionButton floatingMapButton;
    private FusedLocationProviderClient mFusedLocationClient;
    private OffTheWallApplication application;
    private Resources res;
    private Wall closestWall;

    private static final String TAG = "ARActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private final Map<AugmentedImage, AugmentedArtNode> augmentedImageMap = new HashMap<>();

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);
        res = getResources();
        application = (OffTheWallApplication) getApplication();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        hintImageView = findViewById(R.id.hint);
        closestWall = null;

        floatingMapButton = findViewById(R.id.map_button);
        Intent mapIntent = new Intent(this, MapsActivity.class);
        floatingMapButton.setOnClickListener(view ->{
                startActivity(mapIntent);
        });

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        augmentedArtFragment = new AugmentedArtFragment();

        getLocationPermission();

        // getClosestWall();
    }

    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getDeviceLocation();
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getDeviceLocation();
                }
            }
        }
    }

    private void getDeviceLocation() {
        try{
            Task location = mFusedLocationClient.getLastLocation();
            location.addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    Log.d(TAG, "onComplete: found location!");
                    Location currentLocation = (Location) task.getResult();
                    getClosestWall(currentLocation.getLatitude(), currentLocation.getLongitude());
                    // getClosestWall(53.371440, -1.523465);
                } else {
                    Log.d(TAG, "onComplete: current location is null");
                    Toast.makeText(ARActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    public void getClosestWall(double currentLat, double currentLong) {
        application.getApolloClient().query(
                WallLocationsQuery.builder().build()
        ).enqueue(new ApolloCall.Callback<WallLocationsQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<WallLocationsQuery.Data> response) {
                List<Wall> walls = response.data().fetchAllWalls().stream().map(wall -> new Wall(
                        Integer.valueOf(wall.wall_id),
                        wall.latitude,
                        wall.longitude
                )).collect(Collectors.toList());
                Optional<Wall> closestWallOptional = walls.stream().sorted((wall1, wall2) ->
                        wall1
                        .getDistanceFrom(currentLat, currentLong)
                        .compareTo(wall2.getDistanceFrom(currentLat, currentLong))
                )
                .findFirst();
                if (closestWallOptional.isPresent()) {
                    launchAR(closestWallOptional.get());
                } else {
                    Log.e(TAG, "Closest wall could not be found");
                }

            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e(TAG, "Could not get wall locations!");
            }
        });
    }

    public void launchAR(Wall wall) {
        application.getApolloClient().query(
                WallByIDQuery.builder().wall_id(String.valueOf(wall.getWallId())).build()
        ).enqueue(new ApolloCall.Callback<WallByIDQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<WallByIDQuery.Data> response) {
                WallByIDQuery.FetchWallById data = response.data().fetchWallById();
                wall.setWallData(
                        data.street_address,
                        data.info,
                        (float) data.canvas_width,
                        (float) data.canvas_height,
                        (float) data.trigger_width,
                        (float) data.trigger_height,
                        (float) data.trigger_offset_x,
                        (float) data.trigger_offset_y
                        );
                List<Art> artworks = data.images.stream().map(image -> new Art(image.image_url, image.blurb, image.artist_id, new Date(Long.valueOf(image.created_at)))).collect(Collectors.toList());
                wall.setWallArt(artworks);
                closestWall = wall;
                Bundle bundle = new Bundle();
                bundle.putInt("triggerId", Integer.valueOf(data.wall_id));
                bundle.putFloat("triggerWidth", (float) data.trigger_width);
                augmentedArtFragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.placeholder, augmentedArtFragment);
                fragmentTransaction.commit();

                String resource = "t" + wall.getWallId();
                int id = res.getIdentifier(resource, "drawable", "com.slick.offthewall");
                RoundedBitmapDrawable hintImage = RoundedBitmapDrawableFactory.create(res, BitmapFactory.decodeResource(res, id));
                hintImageView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
                    int diameter = hintImageView.getWidth();
                    Log.i(TAG, String.valueOf(diameter));
                    hintImage.setCornerRadius((float) diameter);
                    hintImageView.setImageDrawable(hintImage);
                    hintImageView.setVisibility(ImageView.VISIBLE);
                });
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e(TAG, "Could not get data for closest wall!");
            }
        });
    }

    /**
     * Registered with the Sceneform Scene object, this method is called at the start of each frame.
     *
     * @param frameTime - time since last frame.
     */
    private void onUpdateFrame(FrameTime frameTime) {
        Frame frame = augmentedArtFragment.getArSceneView().getArFrame();

        if (frame == null) {
            return;
        }

        Collection<AugmentedImage> updatedAugmentedImages = frame.getUpdatedTrackables(AugmentedImage.class);

        for (AugmentedImage augmentedImage : updatedAugmentedImages) {
            switch (augmentedImage.getTrackingState()) {
                case PAUSED:
                    // Detected but not yet tracked.
                    break;
                case TRACKING:
                    if (!augmentedImageMap.containsKey(augmentedImage)) {
                        AugmentedArtNode node = new AugmentedArtNode(this, closestWall);
                    }
                    break;
                case STOPPED:
                    augmentedImageMap.remove(augmentedImage);
                    break;
            }
        }
    }
}
