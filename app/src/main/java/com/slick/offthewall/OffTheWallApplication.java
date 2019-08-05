package com.slick.offthewall;


import android.app.Application;

import com.apollographql.apollo.ApolloClient;
import okhttp3.OkHttpClient;

public class OffTheWallApplication extends Application {
    private static final String BASE_URL = "https://offthewall-teamslick.herokuapp.com/graphql";
    private static ApolloClient apolloClient;

    @Override
    public void onCreate() {
        super.onCreate();
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        apolloClient = ApolloClient.builder()
                .serverUrl(BASE_URL)
                .okHttpClient(okHttpClient)
                .build();
    }

    public static ApolloClient getApolloClient() {
        return apolloClient;
    }
}
