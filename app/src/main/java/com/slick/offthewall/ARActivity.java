package com.slick.offthewall;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ARActivity extends AppCompatActivity {

    private double currentLat;
    private double currentLong;
    private static final double NC_LAT = 53.7949778;
    private static final double NC_LONG = -1.5449472;

    private static final String TAG = "ARActivity";

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);
        currentLat = NC_LAT;
        currentLong = NC_LONG;
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
                Log.i(TAG, data.street_address);
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e(TAG, "Could not get data for closest wall!");
            }
        });
    }

}
