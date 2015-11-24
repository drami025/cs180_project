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

import com.cs180.ucrtinder.ucrtinder.FragmentSupport.AndroidDrawer;
import com.cs180.ucrtinder.ucrtinder.Parse.ParseConstants;
import com.cs180.ucrtinder.ucrtinder.R;
import com.layer.sdk.internal.persistence.sync.Load;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


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
    private static final Integer MYTHREADS = 1;
    private ExecutorService executor;
    private LoadingScreenActivity mAct;

    private ParseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_screen);
        findViewById(R.id.mainSpinner1).setVisibility(View.VISIBLE);
        Log.e(getClass().getSimpleName(), "on Create() called");

        try {
            user = ParseUser.getCurrentUser();
            if (user == null) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        } catch (NullPointerException n) {
            n.printStackTrace();
        }

        /*
        try {
            Log.e("ENTER", "Going to get user");
            currentUser = ParseUser.getCurrentUser();
            if (currentUser != null) {
                Log.d(getClass().getSimpleName(), "UserName: " + currentUser.get(ParseConstants.KEY_NAME));
            }
        } catch (NullPointerException n) {
            n.printStackTrace();
        }
        */

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException np) {
            np.printStackTrace();
        }

        mAct = this;

        AndroidDrawer drawer = new AndroidDrawer
                (this,R.id.drawer_layout_loading,R.id.left_drawer_loading, R.id.loading_drawer_pic);

        messageHandler = new Handler();

        // Start runnables
        executor = Executors.newFixedThreadPool(MYTHREADS);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Log.e(getClass().getSimpleName(), "executor run() called");

                try {
                    Log.e("ENTER", "Going to get user");
                    currentUser = ParseUser.getCurrentUser();
                    if (currentUser != null) {
                        Log.d(getClass().getSimpleName(), "UserName: " + currentUser.get(ParseConstants.KEY_NAME));
                    }
                } catch (NullPointerException n) {
                    n.printStackTrace();
                }

                // get all of the data for the next Activity then move on
                if (currentUser != null) {
                    try {
                        toolBarTitle = currentUser.getString(ParseConstants.KEY_NAME);
                        Log.e("CHECKPOINT", "ONE");
                        nameTextView = currentUser.getString(ParseConstants.KEY_NAME) + ", " + currentUser.getInt(ParseConstants.KEY_AGE);
                        Log.e("CHECKPOINT", "TWO");
                        aboutyouTextView = currentUser.getString(ParseConstants.KEY_ABOUTYOU);
                        Log.e("CHECKPOINT", "THREE");

                        // Interest
                        ArrayList<String> array = (ArrayList<String>) currentUser.get(ParseConstants.KEY_INTERESTS);
                        String in = "";

                        if (array != null) {
                            for (int i = 0; i < array.size(); i++) {
                                Log.e("CHECKTHIS", array.get(i));
                                in = in.concat(array.get(i));
                                in = in.concat(", ");
                            }
                        }
                        InterestTextView = in;

                    } catch (NullPointerException n) {
                        n.printStackTrace();
                    }
                }

                mAct.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(getClass().getSimpleName(), "Executor finished");

                     /* Create an Intent that will start the ProfileActivity. */
                        Intent mainIntent = new Intent(LoadingScreenActivity.this, ProfileActivity.class);
                        mainIntent.putExtra(ProfileActivity.KEY_USERTOOLBARTITLE, toolBarTitle);
                        mainIntent.putExtra(ProfileActivity.KEY_USERNAME, nameTextView);
                        mainIntent.putExtra(ProfileActivity.KEY_USERABOUTYOU, aboutyouTextView);
                        mainIntent.putExtra(ProfileActivity.KEY_USERINTEREST, InterestTextView);

                        LoadingScreenActivity.this.startActivity(mainIntent);
                        LoadingScreenActivity.this.finish();
                    }
                });
            }
        });
        executor.shutdown();

        startTime = System.currentTimeMillis();

        messageHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(getClass().getSimpleName(), "Loading Screen running");

                messageHandler.postDelayed(this, WAIT_TIME);
                if (executor.isTerminated() || ParseUser.getCurrentUser() == null || ((System.currentTimeMillis() - startTime) > 8*WAIT_TIME)) {
                    messageHandler.removeCallbacks(this);
                    Log.d(getClass().getSimpleName(), "Executor finished");

                     /* Create an Intent that will start the ProfileActivity. */
                    Intent mainIntent = new Intent(LoadingScreenActivity.this, ProfileActivity.class);
                    mainIntent.putExtra(ProfileActivity.KEY_USERTOOLBARTITLE, toolBarTitle);
                    mainIntent.putExtra(ProfileActivity.KEY_USERNAME, nameTextView);
                    mainIntent.putExtra(ProfileActivity.KEY_USERABOUTYOU, aboutyouTextView);
                    mainIntent.putExtra(ProfileActivity.KEY_USERINTEREST, InterestTextView);

                    LoadingScreenActivity.this.startActivity(mainIntent);
                    LoadingScreenActivity.this.finish();
                    return;
                }


            }
        }, WAIT_TIME);

    }
}