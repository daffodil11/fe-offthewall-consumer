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
                    Wall closestWall = closestWallOptional.get();
                    Log.i(TAG, "Success!!!");
                } else {
                    Log.e(TAG, "Failure!!!");
                }

            }

            @Override
            public void onFailure(@NotNull ApolloException e) {

            }
        });
    }

}
