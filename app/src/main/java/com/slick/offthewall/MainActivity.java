package com.slick.offthewall;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView hello = (TextView) findViewById(R.id.hello);
        Intent mapIntent = new Intent(this, MapsActivity.class);
        hello.setOnClickListener(view -> {
            startActivity(mapIntent);
        });
    }
}
