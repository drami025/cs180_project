package com.cs180.ucrtinder.ucrtinder.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.cs180.ucrtinder.ucrtinder.FragmentSupport.AndroidDrawer;
import com.cs180.ucrtinder.ucrtinder.FragmentSupport.NavigationListener;
import com.cs180.ucrtinder.ucrtinder.R;
import com.parse.Parse;
import com.parse.ParseUser;

public class SettingsActivity extends AppCompatActivity {

    Switch newMatches;
    Switch newMessages;

    private ParseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        try {
            user = ParseUser.getCurrentUser();
            if (user == null) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        } catch (NullPointerException n) {
            n.printStackTrace();
        }

        // Creating an android drawer to slide in from the left side
        AndroidDrawer mAndroidDrawer = new AndroidDrawer
                (this, R.id.drawer_layout_settings, R.id.left_drawer_settings, R.id.settings_profile_drawer_pic);

        // Setup Toolbar
        Toolbar toolbar = (Toolbar)findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_drawer);
        toolbar.setNavigationOnClickListener(new NavigationListener(mAndroidDrawer));
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException np) {
            np.printStackTrace();
        }

        newMatches = (Switch) findViewById(R.id.matchesswitch);

        newMatches.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Toast.makeText(getApplicationContext(), "New Match switch", Toast.LENGTH_SHORT).show();
            }
        });

        newMessages = (Switch) findViewById(R.id.messageswitch);

        newMessages.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Toast.makeText(getApplicationContext(), "New Message switch", Toast.LENGTH_SHORT).show();
            }
        });
    }
}