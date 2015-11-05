package com.cs180.ucrtinder.ucrtinder.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;

import com.cs180.ucrtinder.ucrtinder.R;

public class PreferencesActivity extends AppCompatActivity {

    final Switch discovery = (Switch) findViewById(R.id.discoveryswitch);
    final Switch menSwitch = (Switch) findViewById(R.id.menswitch);
    final Switch womanSwitch = (Switch) findViewById(R.id.womanswitch);
    final SeekBar disSeekBar = (SeekBar) findViewById(R.id.distanceseekBar);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prefereces);

        discovery.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

            }
        });
    }



}
