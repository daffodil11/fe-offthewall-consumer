package com.slick.offthewall;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private FloatingActionButton floatingMapButton;
    private FloatingActionButton floatingCameraButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        floatingMapButton = findViewById(R.id.floatingActionButton);
        floatingMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMapActivity();
            }
        });

        floatingCameraButton = findViewById(R.id.floatingActionButton2);
        floatingCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openARActivity();
            }
        });
    }
    public void openMapActivity() {
        Intent mapIntent = new Intent(this, MapsActivity.class);
        startActivity(mapIntent);

    }
    public void openARActivity() {
        Intent ARIntent = new Intent(this, ARActivity.class);
        startActivity(ARIntent);
    }
}
