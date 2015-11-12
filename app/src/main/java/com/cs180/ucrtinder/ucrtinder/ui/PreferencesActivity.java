package com.cs180.ucrtinder.ucrtinder.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.cs180.ucrtinder.ucrtinder.FragmentSupport.AndroidDrawer;
import com.cs180.ucrtinder.ucrtinder.Parse.ParseConstants;
import com.cs180.ucrtinder.ucrtinder.R;
import com.parse.Parse;
import com.parse.ParseUser;
import com.edmodo.rangebar.RangeBar;

public class PreferencesActivity extends AppCompatActivity{ //} implements ColorPickerDialog.OnColorChangedListener{

    Switch discovery;
    Switch menSwitch;
    Switch womanSwitch;
    SeekBar disSeekBar;
    TextView textView;

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
    private static final String PREF_PROG = "pred_proG";
    private static final String PREF_MEN = "pred_men";
    private static final String PREF_WOMEN = "pred_woman";
    private static final String PREF_DISC = "pred_disc";

    private RangeBar rangeBar;

    public int progress;
    public boolean discoveryBool;
    public boolean menBool;
    public boolean womanBool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);


        // Creating an android drawer to slide in from the left side
        AndroidDrawer mAndroidDrawer = new AndroidDrawer
                (this, R.id.drawer_layout_preferences, R.id.left_drawer_preferences, R.id.preferences_profile_drawer_pic);

        //
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_SAVES, Context.MODE_PRIVATE);
        ParseUser currentUser = ParseUser.getCurrentUser();
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
            editor.apply();
        }
        // Get the previous progress information
        progress = sharedPreferences.getInt(PREF_PROG, 0);
        menBool = sharedPreferences.getBoolean(PREF_MEN, false);
        womanBool = sharedPreferences.getBoolean(PREF_WOMEN, false);
        discoveryBool = sharedPreferences.getBoolean(PREF_DISC, false);


        discovery = (Switch) findViewById(R.id.discoveryswitch);
        menSwitch = (Switch) findViewById(R.id.menswitch);
        womanSwitch = (Switch) findViewById(R.id.womanswitch);
        disSeekBar = (SeekBar) findViewById(R.id.distanceseekBar);
        textView = (TextView) findViewById(R.id.distancetextView);

        discovery.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                discoveryBool = b;
                Toast.makeText(getApplicationContext(), "Discovery Switch worked", Toast.LENGTH_SHORT).show();
            }
        });

        menSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
               menBool = b;
                Toast.makeText(getApplicationContext(), "Men Switch works", Toast.LENGTH_SHORT).show();
            }
        });

        womanSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                womanBool = b;
                Toast.makeText(getApplicationContext(), "Woman Switch works", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getApplicationContext(), "Stopped tracking seekbar", Toast.LENGTH_SHORT).show();
            }
        });
    }

  /* @Override
   protected void onRestoreInstanceState(Bundle savedInstanceState){
       super.onRestoreInstanceState(savedInstanceState);
       mBarColor = savedInstanceState.getInt("BAR_COLOR");
       mConnectingLineColor = savedInstanceState.getInt("CONNECTING_LINE_COLOR");
       mThumbColorNormal = savedInstanceState.getInt("THUMB_COLOR_NORMAL");
       mThumbColorPressed = savedInstanceState.getInt("THUMB_COLOR_PRESSED");

       colorChanged(Component.BAR_COLOR, mBarColor);
       colorChanged(Component.CONNECTING_LINE_COLOR, mConnectingLineColor);
       colorChanged(Component.THUMB_COLOR_NORMAL, mThumbColorNormal);
       colorChanged(Component.THUMB_COLOR_PRESSED, mThumbColorPressed);

       rangeBar = (RangeBar) findViewById(R.id.rangebarview);

       final TextView leftIndexValue = (TextView) findViewById(R.id.leftIndexValue);
       final TextView rightIndexValue = (TextView) findViewById(R.id.rightIndexValue);

       leftIndexValue.setText("" + rangeBar.getLeftIndex());
       rightIndexValue.setText("" + rangeBar.getRightIndex());

       findViewById(R.id.mylayout).requestFocus();
   }

   @Override
   protected void onCreate(Bundle savedInstanceState){
       super.onCreate(savedInstanceState);

       // Removes title bar and sets content view
       this.requestWindowFeature(Window.FEATURE_NO_TITLE);
       setContentView(R.layout.activity_main);

       // Sets fonts for all
       Typeface font = Typeface.createFromAsset(getAssets(), "Roboto-Thin.ttf");
       ViewGroup root = (ViewGroup) findViewById(R.id.mylayout);
       setFont(root, font);

       final Button barColor = (Button) findViewById(R.id.barColor);
       final Button connectingLineColor = (Button) findViewById(R.id.connectingLineColor);
       final Button thumbColorNormal = (Button) findViewById(R.id.thumbColorNormal);
       final Button thumbColorPressed = (Button) findViewById(R.id.thumbColorPressed);
       final Button resetThumbColors = (Button) findViewById(R.id.resetThumbColors);
       final Button refreshButton = (Button) findViewById(R.id.refresh);
   }*/

    @Override
    public void onPause() {
        super.onPause();

        // Get the current parse user
        final ParseUser currentUser = ParseUser.getCurrentUser();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                // upload to parse using the current user
                if (currentUser != null) {
                    currentUser.put(ParseConstants.KEY_DISTANCE, progress);
                    currentUser.put(ParseConstants.KEY_MEN, menBool);
                    currentUser.put(ParseConstants.KEY_WOMEN, womanBool);
                    //currentUser.put(ParseConstants.KEY_, discoveryBool);

                    // Save all changes in the background
                    currentUser.saveInBackground();
                }
            }
        });
    }
}
