package com.cs180.ucrtinder.ucrtinder.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cs180.ucrtinder.ucrtinder.FragmentSupport.AndroidDrawer;
import com.cs180.ucrtinder.ucrtinder.FragmentSupport.NavigationListener;
import com.cs180.ucrtinder.ucrtinder.Parse.ParseConstants;
import com.cs180.ucrtinder.ucrtinder.R;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.logging.Handler;

public class EditProfileActivity extends AppCompatActivity{

    ParseUser currentUser;
    Button editButton;

    String pulledBioText = null;

    android.os.Handler mHandler;
    private final int WAIT_TIME = 10;

    Runnable r;
    int count = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        AndroidDrawer drawer = new AndroidDrawer
                (this,R.id.drawer_layout_edit_profile,R.id.left_drawer_edit_profile, R.id.edit_profile_drawer_pic, getApplicationContext());

        Toolbar toolbar = (Toolbar)findViewById(R.id.my_edit_toolbar);
        toolbar.setTitle("Edit Profile");
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.mipmap.ic_drawer);
        toolbar.setNavigationOnClickListener(new NavigationListener(drawer));

        final EditText editText = (EditText)findViewById(R.id.edit_profile_edit_text);

        editButton = (Button) findViewById(R.id.edit_profilebutton);
        mHandler = new android.os.Handler();

        r = new Runnable() {
            @Override
            public void run() {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            currentUser = ParseUser.getCurrentUser();
                        } catch(NullPointerException n) {
                            n.printStackTrace();
                        }
                        // Pull string from parse
                        if (currentUser != null) {
                            pulledBioText = currentUser.getString(ParseConstants.KEY_ABOUTYOU);
                        }
                    }
                });

                t.start();

                if (pulledBioText != null && count >= 0) {
                    Log.d(getClass().getSimpleName(), "Pulled bio: " + pulledBioText);
                    editText.setText(pulledBioText, TextView.BufferType.EDITABLE);
                    mHandler.removeCallbacks(r);
                } else if (count > 0){
                    count--;
                }

                if (count == 0) {
                    pulledBioText = "'Error: Could not pull from Parse.'";
                    Log.d(getClass().getSimpleName(), "Error bio: " + pulledBioText);
                    editText.setText(pulledBioText, TextView.BufferType.EDITABLE);
                    mHandler.removeCallbacks(r);
                }
            }
        };

        mHandler.postDelayed(r, WAIT_TIME);

        editText.setFocusable(true);
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setCursorVisible(true);
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // hide keyboard if open
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                // hide cursor
                editText.setCursorVisible(false);

                // save bio to parse
                final String biotext = editText.getText().toString();
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            currentUser = ParseUser.getCurrentUser();
                            if (currentUser != null) {
                                currentUser.put(ParseConstants.KEY_ABOUTYOU, biotext);
                                currentUser.saveEventually(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e != null) {

                                        } else {
                                            Log.d(getClass().getSimpleName(), "Saved bio text successfully ");
                                        }
                                    }
                                });
                            }
                        }  catch(NullPointerException n) {
                            n.printStackTrace();
                        }
                    }
                });
                t.setPriority(Thread.MAX_PRIORITY);
                t.start();

                // Go back to profile activity
                finish();
            }
        });

    }


}
