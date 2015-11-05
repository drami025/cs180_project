package com.cs180.ucrtinder.ucrtinder.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import com.cs180.ucrtinder.ucrtinder.R;

public class PreferencesActivity extends AppCompatActivity {

    Switch discovery;
    Switch menSwitch;
    Switch womanSwitch;
    SeekBar disSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        discovery = (Switch) findViewById(R.id.discoveryswitch);
        menSwitch = (Switch) findViewById(R.id.menswitch);
        womanSwitch = (Switch) findViewById(R.id.womanswitch);
        disSeekBar = (SeekBar) findViewById(R.id.distanceseekBar);

        discovery.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Toast.makeText(getApplicationContext(), "Discovery Switch worked", Toast.LENGTH_SHORT).show();
            }
        });

        menSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Toast.makeText(getApplicationContext(), "Men Switch works", Toast.LENGTH_SHORT).show();
            }
        });

        womanSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Toast.makeText(getApplicationContext(), "Woman Switch works", Toast.LENGTH_SHORT).show();
            }
        });
    }



}
