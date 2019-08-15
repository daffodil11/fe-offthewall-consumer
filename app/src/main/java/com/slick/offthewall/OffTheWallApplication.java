package com.slick.offthewall;


import android.app.Application;

import com.apollographql.apollo.ApolloClient;

import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;

public class OffTheWallApplication extends Application {
    private static final String BASE_URL = "https://offthewall-teamslick.herokuapp.com/graphql";
    private static ApolloClient apolloClient;
    private static final Map<Integer, String> APPROVED_ARTISTS = new HashMap<Integer,String>();

    @Override
    public void onCreate() {
        super.onCreate();
        APPROVED_ARTISTS.put(1, "bobbirae");
        APPROVED_ARTISTS.put(2, "mikitillett");
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        apolloClient = ApolloClient.builder()
                .serverUrl(BASE_URL)
                .okHttpClient(okHttpClient)
                .build();
    }

    public static ApolloClient getApolloClient() {
        return apolloClient;
    }

    public static String getApprovedArtistById(int id) {
        return APPROVED_ARTISTS.get(id);
    }
}
