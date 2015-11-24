package com.cs180.ucrtinder.ucrtinder.ui;

import android.content.Intent;
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Handler;

public class EditProfileActivity extends AppCompatActivity{

    private ParseUser currentUser;
    private Button editButton;
    private EditText editText;

    private ExecutorService executor;
    private ExecutorService executor2;

    private String pulledBioText = null;
    private boolean intentBioFound = false;
    private String intentBio = null;

    private android.os.Handler mHandler;
    private final int WAIT_TIME = 100;

    private Runnable r;
    private int count = 3;

    private ParseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        try {
            user = ParseUser.getCurrentUser();
            if (user == null) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        } catch (NullPointerException n) {
            n.printStackTrace();
        }

        // Set up the android drawer for this activity
        AndroidDrawer drawer = new AndroidDrawer
                (this,R.id.drawer_layout_edit_profile,R.id.left_drawer_edit_profile, R.id.edit_profile_drawer_pic, getApplicationContext());

        // Setup the toolbar
        Toolbar toolbar = (Toolbar)findViewById(R.id.my_edit_toolbar);
        toolbar.setTitle("Edit Profile");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_drawer);
        toolbar.setNavigationOnClickListener(new NavigationListener(drawer));
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException np) {
            np.printStackTrace();
        }


        editText = (EditText)findViewById(R.id.edit_profile_edit_text);
        editButton = (Button) findViewById(R.id.edit_profilebutton);

        mHandler = new android.os.Handler();

        // Check intent for bio text data
        Intent intent = getIntent();
        intentBio = null;
        if (intent != null) {

            if ((intentBio = intent.getStringExtra("bioText")) != null) {
                intentBioFound = true;
            }
        }

        executor = Executors.newFixedThreadPool(1);
        mHandler.postDelayed(new getBioText(), WAIT_TIME);

        editText.setFocusable(true);
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setCursorVisible(true);
            }
        });

        final Runnable r2 = new Runnable() {
            @Override
            public void run() {
                try {
                    // save bio to parse
                    String biotext = editText.getText().toString();
                    currentUser = ParseUser.getCurrentUser();
                    if (currentUser != null) {
                        currentUser.put(ParseConstants.KEY_ABOUTYOU, biotext);
                        currentUser.saveInBackground(new SaveCallback() {
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
        };

        // Save button
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(getClass().getSimpleName(), "Edit button clicked... saving bio");
                updateBioText(view);
            }
        });

    }


    public void updateBioText(View view) {
        // hide keyboard if open
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        // hide cursor
        editText.setCursorVisible(false);
        Log.e(getClass().getSimpleName(), "one");

        executor2 = null;
        executor2 = Executors.newFixedThreadPool(1);
        executor2.execute(new Runnable() {
            @Override
            public void run() {

                // save bio to parse
                final String biotext = editText.getText().toString();
                try {
                    currentUser = ParseUser.getCurrentUser();
                    if (currentUser != null) {
                        Log.e(getClass().getSimpleName(), "Two");
                        currentUser.put(ParseConstants.KEY_ABOUTYOU, biotext);
                        currentUser.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {

                                } else {
                                    Log.d(getClass().getSimpleName(), "Saved bio text successfully ");
                                }
                            }
                        });
                    }
                } catch (NullPointerException n) {
                    n.printStackTrace();
                }

                Log.e(getClass().getSimpleName(), "Three");

            }
        });
        executor2.shutdown();


        // Go back to profile activity
        EditProfileActivity.this.finish();
    }

    public class getBioText implements Runnable {

        @Override
        public void run() {
            if (intentBioFound) {
                EditProfileActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        editText.setText(intentBio, TextView.BufferType.EDITABLE);
                    }
                });
                mHandler.removeCallbacks(this);
                return;
            }

            if (!executor.isShutdown() && !intentBioFound) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            currentUser = ParseUser.getCurrentUser();
                            // Pull string from parse
                            if (currentUser != null) {
                                pulledBioText = currentUser.getString(ParseConstants.KEY_ABOUTYOU);
                            }
                        } catch (NullPointerException n) {
                            n.printStackTrace();
                        }

                    }
                });
                executor.shutdown();
            }

            if (pulledBioText != null && executor.isTerminated()) {
                Log.d(getClass().getSimpleName(), "Pulled bio: " + pulledBioText);
                EditProfileActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        editText.setText(pulledBioText, TextView.BufferType.EDITABLE);
                    }
                });

                mHandler.removeCallbacks(this);
                return;
            } else {
                count--;
            }

            if (count == 0) {
                pulledBioText = "'Error: Could not pull from Parse.'";
                EditProfileActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(getClass().getSimpleName(), "Error bio: " + pulledBioText);
                        editText.setText(pulledBioText, TextView.BufferType.EDITABLE);
                    }
                });

                mHandler.removeCallbacks(this);
                return;
            }
        }
    }
}
