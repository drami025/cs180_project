package com.cs180.ucrtinder.ucrtinder.ui;

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

    private RangeBar rangeBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);


        // Creating an android drawer to slide in from the left side
        AndroidDrawer mAndroidDrawer = new AndroidDrawer(this, R.id.drawer_layout_preferences, R.id.left_drawer_preferences);

        if (savedInstanceState != null) {
            savedInstanceState.putInt("BAR_COLOR", mBarColor);
            savedInstanceState.putInt("CONNECTING_LINE_COLOR", mConnectingLineColor);
            savedInstanceState.putInt("THUMB_COLOR_NORMAL", mThumbColorNormal);
            savedInstanceState.putInt("THUMB_COLOR_PRESSED", mThumbColorPressed);
        }

        discovery = (Switch) findViewById(R.id.discoveryswitch);
        menSwitch = (Switch) findViewById(R.id.menswitch);
        womanSwitch = (Switch) findViewById(R.id.womanswitch);
        disSeekBar = (SeekBar) findViewById(R.id.distanceseekBar);
        textView = (TextView) findViewById(R.id.distancetextView);
        final ParseUser currentUser = ParseUser.getCurrentUser();

        discovery.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Toast.makeText(getApplicationContext(), "Discovery Switch worked", Toast.LENGTH_SHORT).show();
            }
        });

        menSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                currentUser.put(ParseConstants.KEY_MEN,b);
                currentUser.saveInBackground();
                Toast.makeText(getApplicationContext(), "Men Switch works", Toast.LENGTH_SHORT).show();
            }
        });

        womanSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                currentUser.put(ParseConstants.KEY_WOMEN,b);
                currentUser.saveInBackground();
                Toast.makeText(getApplicationContext(), "Woman Switch works", Toast.LENGTH_SHORT).show();
            }
        });

        disSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

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
                currentUser.put(ParseConstants.KEY_DISTANCE,progress);
                textView.setText("Covered:" + progress + "/" + seekBar.getMax());
                currentUser.saveInBackground();
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
}
