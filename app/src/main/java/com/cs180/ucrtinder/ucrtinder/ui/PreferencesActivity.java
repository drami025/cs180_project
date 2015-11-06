package com.cs180.ucrtinder.ucrtinder.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.cs180.ucrtinder.ucrtinder.FragmentSupport.AndroidDrawer;
import com.cs180.ucrtinder.ucrtinder.Parse.ParseConstants;
import com.cs180.ucrtinder.ucrtinder.R;
import com.parse.ParseUser;
import com.edmodo.rangebar.RangeBar;

public class PreferencesActivity extends AppCompatActivity implements ColorPickerDialog.OnColorChangedListener{

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

    // @Override
    protected void onSavedInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
        bundle.putInt("BAR_COLOR", mBarColor);
        bundle.putInt("CONNECTING_LINE_COLOR", mConnectingLineColor);
        bundle.putInt("THUMB_COLOR_NORMAL", mThumbColorNormal);
        bundle.putInt("THUMB_COLOR_PRESSED", mThumbColorPressed);
    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle){
        super.onRestoreInstanceState(bundle);
        mBarColor = bundle.getInt("BAR_COLOR");
        mConnectingLineColor = bundle.getInt("CONNECTING_LINE_COLOR");
        mThumbColorNormal = bundle.getInt("THUMB_COLOR_NORMAL");
        mThumbColorPressed = bundle.getInt("THUMB_COLOR_PRESSED");

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

    Switch discovery;
    Switch menSwitch;
    Switch womanSwitch;
    SeekBar disSeekBar;
    TextView textView;

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

        // Removes title bar and sets content view
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        // Sets fonts for all
        Typeface font = Typeface.createFromAsset(getAssets(), "Roboto-Thin.ttf");
        ViewGroup root = (ViewGroup) findViewById(R.id.mylayout);
        setFont(root, font);

        //DONT NEED
        final Button barColor = (Button) findViewById(R.id.barColor);
        final Button connectingLineColor = (Button) findViewById(R.id.connectingLineColor);
        final Button thumbColorNormal = (Button) findViewById(R.id.thumbColorNormal);
        final Button thumbColorPressed = (Button) findViewById(R.id.thumbColorPressed);
        final Button resetThumbColors = (Button) findViewById(R.id.resetThumbColors);
        final Button refreshButton = (Button) findViewById(R.id.refresh);

        //Sets the buttons to bold.
        refreshButton.setTypeface(font,Typeface.BOLD);
        barColor.setTypeface(font,Typeface.BOLD);
        connectingLineColor.setTypeface(font,Typeface.BOLD);
        thumbColorNormal.setTypeface(font,Typeface.BOLD);
        thumbColorPressed.setTypeface(font,Typeface.BOLD);
        resetThumbColors.setTypeface(font,Typeface.BOLD);

        // Sets initial colors for the Color buttons
        barColor.setTextColor(DEFAULT_BAR_COLOR);
        connectingLineColor.setTextColor(DEFAULT_CONNECTING_LINE_COLOR);
        thumbColorNormal.setTextColor(HOLO_BLUE);
        thumbColorPressed.setTextColor(HOLO_BLUE);

        rangeBar = (RangeBar) findViewById(R.id.rangebarview);

        // Gets the index value TextViews
        final EditText leftIndexValue = (EditText) findViewById(R.id.leftIndexValue);
        final EditText rightIndexValue = (EditText) findViewById(R.id.rightIndexValue);

        rangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onIndexChangeListener(RangeBar rangeBar, int leftThumbIndex, int rightThumbIndex) {

                leftIndexValue.setText("" + leftThumbIndex);
                rightIndexValue.setText("" + rightThumbIndex);
            }
        });

        // Sets the indices themselves upon input from the user
        refreshButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Gets the String values of all the texts
                String leftIndex = leftIndexValue.getText().toString();
                String rightIndex = rightIndexValue.getText().toString();

                // Catches any IllegalArgumentExceptions; if fails, should throw
                // a dialog warning the user
                try {
                    if (!leftIndex.isEmpty() && !rightIndex.isEmpty()) {
                        int leftIntIndex = Integer.parseInt(leftIndex);
                        int rightIntIndex = Integer.parseInt(rightIndex);
                        rangeBar.setThumbIndices(leftIntIndex, rightIntIndex);
                    }
                } catch (IllegalArgumentException e) {
                }
            }
        });

        final TextView tickCount = (TextView) findViewById(R.id.tickCount);
        SeekBar tickCountSeek = (SeekBar) findViewById(R.id.tickCountSeek);
        tickCountSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                try {
                    rangeBar.setTickCount(i);
                } catch (IllegalArgumentException e) {
                }
                tickCount.setText("tickCount" + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // Sets tickHeight
        final TextView tickHeight = (TextView) findViewById(R.id.tickHeight);
        SeekBar tickHeightSeek = (SeekBar) findViewById(R.id.tickHeightSeek);
        tickHeightSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                rangeBar.setTickHeight(i);
                tickHeight.setText("TickHeight= " + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final TextView barWeight = (TextView) findViewById(R.id.barWeight);
        SeekBar barWeightSeek = (SeekBar) findViewById(R.id.barWeightSeek);
        barWeightSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                rangeBar.setBarWeight(i);
                barWeight.setText("barWeight= " + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final TextView connectingLineWeight = (TextView) findViewById(R.id.connectingLineWeight);
        SeekBar connectingLineWeightSeek = (SeekBar) findViewById(R.id.connectingLineWeightSeek);
        connectingLineWeightSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                rangeBar.setConnectingLineWeight(i);
                connectingLineWeight.setText("connectingLineWeight = " + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final TextView thumbRadius = (TextView) findViewById(R.id.thumbRadius);
        SeekBar thumbRadiusSeek = (SeekBar) findViewById(R.id.thumbRadiusSeek);
        thumbRadiusSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(i == 0){
                    rangeBar.setThumbRadius(-1);
                    thumbRadius.setText("thumbraduis = N/A");
                }

                else {
                    rangeBar.setThumbRadius(i);
                    thumbRadius.setText("thumbRadius = " + i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        barColor.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                initColorPicker(Component.CONNECTING_LINE_COLOR, mConnectingLineColor, mConnectingLineColor);
            }
        });

        connectingLineColor.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                initColorPicker(Component.CONNECTING_LINE_COLOR, mConnectingLineColor, mConnectingLineColor);
            }
        });

        thumbColorNormal.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                initColorPicker(Component.THUMB_COLOR_NORMAL, mThumbColorNormal, mThumbColorNormal);
            }
        });

        thumbColorPressed.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                initColorPicker(Component.THUMB_COLOR_PRESSED, mThumbColorPressed, mThumbColorPressed);
            }
        });

        resetThumbColors.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                rangeBar.setThumbColorNormal(-1);
                rangeBar.setThumbColorPressed(-1);

                mThumbColorNormal = -1;
                mThumbColorPressed = -1;

                thumbColorNormal.setText("thumbColorNormal = N/A");
                thumbColorPressed.setText("thumbColorPressed = N/A");
                thumbColorNormal.setTextColor(HOLO_BLUE);
                thumbColorPressed.setTextColor(HOLO_BLUE);
            }
        });
    }

    @Override
    public void colorChanged(Component component, int newColor) {

        String hexColor = String.format("#%06X", (0xFFFFFF & newColor));

        switch (component)
        {
            case BAR_COLOR:
                mBarColor = newColor;
                rangeBar.setBarColor(newColor);
                final TextView barColorText = (TextView) findViewById(R.id.barColor);
                barColorText.setText("barColor = " + hexColor);
                barColorText.setTextColor(newColor);
                break;

            case CONNECTING_LINE_COLOR:
                mConnectingLineColor = newColor;
                rangeBar.setConnectingLineColor(newColor);
                final TextView connectingLineColorText = (TextView) findViewById(R.id.connectingLineColor);
                connectingLineColorText.setText("connectingLineColor = " + hexColor);
                connectingLineColorText.setTextColor(newColor);
                break;

            case THUMB_COLOR_NORMAL:
                mThumbColorNormal = newColor;
                rangeBar.setThumbColorNormal(newColor);
                final TextView thumbColorNormalText = (TextView) findViewById(R.id.thumbColorNormal);

                if (newColor == -1) {
                    thumbColorNormalText.setText("thumbColorNormal = N/A");
                    thumbColorNormalText.setTextColor(HOLO_BLUE);
                }
                else {
                    thumbColorNormalText.setText("thumbColorNormal = " + hexColor);
                    thumbColorNormalText.setTextColor(newColor);
                }
                break;

            case THUMB_COLOR_PRESSED:
                mThumbColorPressed = newColor;
                rangeBar.setThumbColorPressed(newColor);
                final TextView thumbColorPressedText = (TextView) findViewById(R.id.thumbColorPressed);

                if (newColor == -1) {
                    thumbColorPressedText.setText("thumbColorPressed = N/A");
                    thumbColorPressedText.setTextColor(HOLO_BLUE);
                }
                else {
                    thumbColorPressedText.setText("thumbColorPressed = " + hexColor);
                    thumbColorPressedText.setTextColor(newColor);
                }
        }
    }

    /**
     * Sets the font on all TextViews in the ViewGroup. Searches recursively for
     * all inner ViewGroups as well. Just add a check for any other views you
     * want to set as well (EditText, etc.)
     */
    private void setFont(ViewGroup group, Typeface font) {
        int count = group.getChildCount();
        View v;
        for (int i = 0; i < count; i++) {
            v = group.getChildAt(i);
            if (v instanceof TextView || v instanceof EditText || v instanceof Button) {
                ((TextView) v).setTypeface(font);
            } else if (v instanceof ViewGroup)
                setFont((ViewGroup) v, font);
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.layout.activity_preferences, menu);
        return true;
    }*/

    /**
     * Initiates the colorPicker from within a button function.
     *
     * @param component Component specifying which input is being used
     * @param initialColor Integer specifying the initial color choice. *
     * @param defaultColor Integer specifying the default color choice.
     */
    private void initColorPicker(Component component, int initialColor, int defaultColor)
    {
        ColorPickerDialog colorPicker = new ColorPickerDialog(this,
                this,
                component,
                initialColor,
                defaultColor);
        colorPicker.show();

    }

}