package com.slick.offthewall;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ARActivity extends AppCompatActivity {

    private double currentLat;
    private double currentLong;
    private static final double NC_LAT = 53.7949778;
    private static final double NC_LONG = -1.5449472;

    private FragmentTransaction fragmentTransaction;
    private AugmentedArtFragment augmentedArtFragment;
    private ImageView hintImageView;

    private static final String TAG = "ARActivity";

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);
        Resources res = getResources();

        hintImageView = findViewById(R.id.hint);
        RoundedBitmapDrawable hintImage = RoundedBitmapDrawableFactory.create(res, BitmapFactory.decodeResource(res, R.drawable.t1));
        hintImageView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int diameter = hintImageView.getWidth() + 50;
            Log.i(TAG, String.valueOf(diameter));
            hintImage.setCornerRadius(diameter / 2.0f);
            hintImageView.setImageDrawable(hintImage);
        });


        currentLat = NC_LAT;
        currentLong = NC_LONG;
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        /*FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );*/
        augmentedArtFragment = new AugmentedArtFragment();
        // augmentedArtFragment.setLayoutParams(lp);

        getClosestWall((OffTheWallApplication) getApplication());
    }

    public void getClosestWall(OffTheWallApplication application) {
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
                    launchAR(application, closestWallOptional.get());
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

    public void launchAR(OffTheWallApplication application, Wall wall) {
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
                Bundle bundle = new Bundle();
                bundle.putInt("triggerId", Integer.valueOf(data.wall_id));
                bundle.putFloat("triggerWidth", (float) data.trigger_width);
                augmentedArtFragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.placeholder, augmentedArtFragment);
                fragmentTransaction.commit();
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e(TAG, "Could not get data for closest wall!");
            }
        });
    }

}
