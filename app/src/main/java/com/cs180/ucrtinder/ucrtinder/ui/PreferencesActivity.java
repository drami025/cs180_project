package com.cs180.ucrtinder.ucrtinder.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.cs180.ucrtinder.ucrtinder.FragmentSupport.AndroidDrawer;
import com.cs180.ucrtinder.ucrtinder.FragmentSupport.NavigationListener;
import com.cs180.ucrtinder.ucrtinder.Parse.ParseConstants;
import com.cs180.ucrtinder.ucrtinder.R;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.edmodo.rangebar.RangeBar;
import com.parse.SaveCallback;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public class PreferencesActivity extends AppCompatActivity{

    Switch discovery;
    Switch menSwitch;
    Switch womanSwitch;
    SeekBar disSeekBar;
    TextView textView;
    TextView leftTextView;
    TextView rightTextView;


    private static final int DEFAULT_BAR_COLOR = 0xffcccccc;
    private static final int DEFAULT_CONNECTING_LINE_COLOR = 0xff33b5e5;
    private static final int HOLO_BLUE = 0xff33b5e5;
    private static final int DEFAULT_THUMB_COLOR_NORMAL = -1;
    private static final int DEFAULT_THUMB_COLOR_PRESSED = -1;

    private int mBarColor = DEFAULT_BAR_COLOR;
    private int mConnectingLineColor = DEFAULT_CONNECTING_LINE_COLOR;
    private int mThumbColorNormal = DEFAULT_THUMB_COLOR_NORMAL;
    private int mThumbColorPressed = DEFAULT_THUMB_COLOR_PRESSED;

    private static final String PREF_SAVES = "pred_saves";
    private static final String PREF_PROG = "pred_prog";
    private static final String PREF_MEN = "pred_men";
    private static final String PREF_WOMEN = "pred_woman";
    private static final String PREF_DISC = "pred_disc";
    private static final String PREF_DISTTEXT = "pred_dist_text";
    private static final String PREF_LEFTRANGE = "pred_left_range";
    private static final String PREF_RIGHTRANGE = "pred_right_range";

    private RangeBar rangeBar;
    private int leftIndex = 0;
    private int rightIndex = 0;

    public int progress;
    public boolean discoveryBool;
    public boolean menBool;
    public boolean womanBool;

    private ExecutorService executor;
    private static final Integer MYTHREADS = 3;

    private ParseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        try {
            user = ParseUser.getCurrentUser();
            if (user == null) {
                startActivity(new Intent(this, LoginActivity.class));
            }
        } catch (NullPointerException n) {
            n.printStackTrace();
        }


        // Set up executor to handle the saving of data
        executor = Executors.newFixedThreadPool(MYTHREADS);
        executor.execute(new getDataRunnable());
        executor.shutdown();

        // Creating an android drawer to slide in from the left side
        AndroidDrawer mAndroidDrawer = new AndroidDrawer
                (this, R.id.drawer_layout_preferences, R.id.left_drawer_preferences, R.id.preferences_profile_drawer_pic);

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


        discovery = (Switch) findViewById(R.id.discoveryswitch);
        menSwitch = (Switch) findViewById(R.id.menswitch);
        womanSwitch = (Switch) findViewById(R.id.womanswitch);
        disSeekBar = (SeekBar) findViewById(R.id.distanceseekBar);
        textView = (TextView) findViewById(R.id.distancetextView);
        leftTextView = (TextView) findViewById(R.id.leftIndexValue);
        rightTextView = (TextView) findViewById(R.id.rightIndexValue);
        rangeBar = (RangeBar) findViewById(R.id.rangebarview);

        textView.setText("");
        leftTextView.setText("");
        rightTextView.setText("");

        discovery.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                discoveryBool = b;
                //Toast.makeText(getApplicationContext(), "Discovery Switch worked", Toast.LENGTH_SHORT).show();
            }
        });

        menSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
               menBool = b;
               //Toast.makeText(getApplicationContext(), "Men Switch works", Toast.LENGTH_SHORT).show();
            }
        });

        womanSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                womanBool = b;
                //Toast.makeText(getApplicationContext(), "Woman Switch works", Toast.LENGTH_SHORT).show();
            }
        });

        disSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {


            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progress = i;
                //Toast.makeText(getApplicationContext(), "Changing seekbar progress", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(getApplicationContext(), "Starting tracking seekbar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textView.setText("Covered:" + progress + "/" + seekBar.getMax());
                //Toast.makeText(getApplicationContext(), "Stopped tracking seekbar", Toast.LENGTH_SHORT).show();
            }
        });

        rangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onIndexChangeListener(RangeBar rangeBar, int i, int i1) {
                if (i < 0) {
                    rangeBar.setLeft(0);
                    i = 0;
                }
                if (i1 > 36) {
                    rangeBar.setRight(36);
                    i1 = 36;
                }

                leftIndex = i + 18;
                rightIndex = i1 + 18;

                String temp = Integer.toString(leftIndex);
                String temp2 = Integer.toString(rightIndex);
                leftTextView.setText(temp);
                rightTextView.setText(temp2);
            }
        });

    }

    @Override
    public void onPause() {
        try {
            executor = null;
            executor = Executors.newFixedThreadPool(1);
            executor.execute(new saveRunnable());
            executor.shutdown();
        } catch (RejectedExecutionException r) {
            r.printStackTrace();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        /*
        try {
            executor.execute(new saveRunnable());
            executor.shutdown();
        } catch (RejectedExecutionException r) {
            r.printStackTrace();
        }
        */
        super.onStop();
    }

    @Override
    public void onDestroy() {
        /*
        try {
            executor.execute(new saveRunnable());
            executor.shutdown();
        } catch (RejectedExecutionException r) {
            r.printStackTrace();
        }
        */
        super.onDestroy();
    }


    private class saveRunnable implements Runnable {
        @Override
        public void run() {
            Log.d(getClass().getSimpleName(), "Saving Data in executor");
            // Get the current parse user
            ParseUser currentUser = null;
            try {
                currentUser = ParseUser.getCurrentUser();
                if (currentUser != null) {
                    Log.d(getClass().getSimpleName(), "UserName: " + currentUser.get(ParseConstants.KEY_NAME));
                }
            } catch(NullPointerException n) {
                n.printStackTrace();
            }

            // upload to parse using the current user
            if (currentUser != null) {
                currentUser.put(ParseConstants.KEY_DISTANCE, progress);
                currentUser.put(ParseConstants.KEY_MEN, menBool);
                currentUser.put(ParseConstants.KEY_WOMEN, womanBool);
                //currentUser.put(ParseConstants.KEY_, discoveryBool);

                // Save all changes in the background
                currentUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Log.d(getClass().getSimpleName(), "Saved preferences data locally/on the web");
                    }
                });
            }

            SharedPreferences sharedPreferences = getSharedPreferences(PREF_SAVES, Context.MODE_PRIVATE);

            // Open up editor to write to local memory
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(PREF_PROG, progress);
            editor.putBoolean(PREF_MEN, menBool);
            editor.putBoolean(PREF_WOMEN, womanBool);
            editor.putBoolean(PREF_DISC,discoveryBool);
            editor.putString(PREF_DISTTEXT, textView.getText().toString());
            editor.putInt(PREF_LEFTRANGE, leftIndex);
            editor.putInt(PREF_RIGHTRANGE, rightIndex);

            // Save all changes
            editor.apply();

        }
    }
    private class getDataRunnable implements Runnable {
        @Override
        public void run() {

            Log.d(getClass().getSimpleName(), "Getting Data in executor");
            // Get the current parse user
            ParseUser currentUser = null;
            try {
                currentUser = ParseUser.getCurrentUser();
                if (currentUser != null) {
                    Log.d(getClass().getSimpleName(), "UserName: " + currentUser.get(ParseConstants.KEY_NAME));
                }
            } catch(NullPointerException n) {
                n.printStackTrace();
            }

            final SharedPreferences sharedPreferences = getSharedPreferences(PREF_SAVES, Context.MODE_PRIVATE);
            if (currentUser != null) {
                // Open up editor to write to local memory
                SharedPreferences.Editor editor = sharedPreferences.edit();

                progress = sharedPreferences.getInt(PREF_PROG, -1);
                if (progress == -1) {
                    // Pull from parse to get the default progress value
                    progress =  currentUser.getInt(ParseConstants.KEY_DISTANCE);
                    editor.putInt(PREF_PROG, progress);
                }
                menBool = sharedPreferences.getBoolean(PREF_MEN, false);
                if (!menBool) {
                    menBool = currentUser.getBoolean(ParseConstants.KEY_MEN);
                    editor.putBoolean(PREF_MEN, menBool);
                }
                womanBool = sharedPreferences.getBoolean(PREF_WOMEN, false);
                if (!womanBool) {
                    womanBool = currentUser.getBoolean(ParseConstants.KEY_WOMEN);
                    editor.putBoolean(PREF_WOMEN, womanBool);
                }
                discoveryBool = sharedPreferences.getBoolean(PREF_DISC, false);
                if (!discoveryBool) {

                }

                // Save all changes
                editor.commit();
            }

            // Get the previous progress information
            progress = sharedPreferences.getInt(PREF_PROG, 0);
            menBool = sharedPreferences.getBoolean(PREF_MEN, false);
            womanBool = sharedPreferences.getBoolean(PREF_WOMEN, false);
            discoveryBool = sharedPreferences.getBoolean(PREF_DISC, false);
            final String tempText = sharedPreferences.getString(PREF_DISTTEXT, "");
            final int tempLeft = sharedPreferences.getInt(PREF_LEFTRANGE, 18);
            final int tempRight = sharedPreferences.getInt(PREF_RIGHTRANGE, 30);

            // Update the Ui with the values
            PreferencesActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(getClass().getSimpleName(), "Updating the UI from executor");
                    menSwitch.setChecked(menBool);
                    womanSwitch.setChecked(womanBool);
                    discovery.setChecked(discoveryBool);
                    disSeekBar.setProgress(progress);
                    textView.setText(tempText);
                    rangeBar.setLeft(tempLeft - 18);
                    rangeBar.setRight(tempRight - 18);

                    String temp = Integer.toString(tempLeft);
                    String temp2 = Integer.toString(tempRight);
                    leftTextView.setText(temp);
                    rightTextView.setText(temp2);
                }
            });
        }
    }
}
