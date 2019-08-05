package com.slick.offthewall;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.util.Log;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.jetbrains.annotations.NotNull;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private OffTheWallApplication application;
    private static final String TAG = "MapsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        application = (OffTheWallApplication) getApplication();
    }

    private void getWall(){
        application.getApolloClient().query(
                WallQuery.builder().build()).enqueue(new ApolloCall.Callback<WallQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<WallQuery.Data> response) {
                Log.i(TAG, "response: " + response.data().fetchAllWalls());
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {

            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        getWall();
        mMap = googleMap;

        String coords = "53.792873,1.54074,#53.792484,-1.540424,#53.79199,-1.537232,#53.791128,-1.537358,#53.791984,-1.531389,#53.793244,-1.537609";
        String[] coordsplit = coords.split("#");

        for (String point : coordsplit) {
            String[] pointData = point.split(",");
            Float lat= Float.parseFloat(pointData[0]);
            Float lng= Float.parseFloat(pointData[1]);

            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)));
        }

        LatLng mylocation = new LatLng(53.794872, -1.547083);
        mMap.addMarker(new MarkerOptions().position(mylocation).title("Marker in Leeds").snippet("Art posted by me"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mylocation, 14));
    }

}
