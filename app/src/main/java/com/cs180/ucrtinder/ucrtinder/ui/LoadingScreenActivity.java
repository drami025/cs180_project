package com.cs180.ucrtinder.ucrtinder.ui;

/**
 * Created by Aaron on 11/13/2015.
 */

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import com.cs180.ucrtinder.ucrtinder.Parse.ParseConstants;
import com.cs180.ucrtinder.ucrtinder.R;
import com.parse.ParseUser;

import java.util.ArrayList;


/**
 * Created by Aaron on 11/13/2015.
 */
public class LoadingScreenActivity extends AppCompatActivity {

    //Introduce an delay
    private final int WAIT_TIME = 100;

    private Handler mHandler;
    private static ParseUser currentUser = null;
    private String toolBarTitle = "";
    private String nameTextView = "";
    private String aboutyouTextView = "";
    private String InterestTextView = "";

    private long startTime = 0;

    private static Handler messageHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("LoadingScreenActivity  screen started");
        setContentView(R.layout.loading_screen);
        findViewById(R.id.mainSpinner1).setVisibility(View.VISIBLE);


        messageHandler = new Handler();


        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                currentUser = ParseUser.getCurrentUser();

                // get all of the data for the next Activity then move on
                try {
                    toolBarTitle = currentUser.getString(ParseConstants.KEY_NAME);
                    nameTextView = currentUser.getString(ParseConstants.KEY_NAME) + ", " + currentUser.getInt(ParseConstants.KEY_AGE);
                    aboutyouTextView = currentUser.getString(ParseConstants.KEY_ABOUTYOU);

                    // Interest
                    ArrayList<String> array = (ArrayList<String>) currentUser.get(ParseConstants.KEY_INTERESTS);
                    String in = "";

                    if(array != null) {
                        for (int i = 0; i < array.size(); i++) {
                            in = in.concat(array.get(i));
                            in = in.concat(", ");
                        }
                    }
                    InterestTextView = in;

                } catch (NullPointerException n) {
                    n.printStackTrace();
                }
            }
        });
        t.setPriority(Thread.MAX_PRIORITY);

        startTime = System.currentTimeMillis();

        messageHandler.postDelayed(new Runnable(){
            @Override
            public void run() {
                Log.d(getClass().getSimpleName(), "Loading Screen running");



                if ((System.currentTimeMillis() - startTime) < 2*WAIT_TIME) {
                    t.start();
                }
                try {
                    t.join();
                } catch (InterruptedException n) {
                    n.printStackTrace();
                }

                if (!t.isAlive()) {
                     /* Create an Intent that will start the ProfileActivity. */
                    Intent mainIntent = new Intent(LoadingScreenActivity.this, ProfileActivity.class);
                    mainIntent.putExtra(ProfileActivity.KEY_USERTOOLBARTITLE, toolBarTitle);
                    mainIntent.putExtra(ProfileActivity.KEY_USERNAME, nameTextView);
                    mainIntent.putExtra(ProfileActivity.KEY_USERABOUTYOU, aboutyouTextView);
                    mainIntent.putExtra(ProfileActivity.KEY_USERINTEREST, InterestTextView);

                    LoadingScreenActivity.this.startActivity(mainIntent);
                    LoadingScreenActivity.this.finish();
                }
            }
        }, WAIT_TIME);

    }
}