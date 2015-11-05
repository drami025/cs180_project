package com.cs180.ucrtinder.ucrtinder.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

public class EditProfileActivity extends AppCompatActivity implements View.OnFocusChangeListener {

    ParseUser currentUser = ParseUser.getCurrentUser();

    View.OnFocusChangeListener mOnFocusChangeListener;
    Button editButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        AndroidDrawer drawer = new AndroidDrawer(this,R.id.drawer_layout_edit_profile,R.id.left_drawer_edit_profile);

        Toolbar toolbar = (Toolbar)findViewById(R.id.my_edit_toolbar);
        toolbar.setTitle("Edit Profile");
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.mipmap.ic_drawer);
        toolbar.setNavigationOnClickListener(new NavigationListener(drawer));

        editButton = (Button) findViewById(R.id.edit_profilebutton);

        final EditText editText = (EditText)findViewById(R.id.edit_profile_edit_text);
        editText.setText(currentUser.getString(ParseConstants.KEY_ABOUTYOU), TextView.BufferType.EDITABLE);

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
                String biotext = editText.getText().toString();
                currentUser.put(ParseConstants.KEY_ABOUTYOU, biotext);
                currentUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            //worked
                        } else {
                            //didn't work
                        }
                    }
                });

                // Go back to profile activity
                finish();
            }
        });

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        if (v.getId() == R.id.edit_profile_edit_text) {
            if(!hasFocus) {
                EditText editText = (EditText) findViewById(R.id.edit_profile_edit_text);
                editText.setCursorVisible(false);

                // save bio to parse
                String biotext = editText.getText().toString();
                currentUser.put(ParseConstants.KEY_ABOUTYOU, biotext);
                currentUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e == null){
                            //worked
                        }
                        else{
                            //didn't work
                        }
                    }
                });
            }
        }
    }
}
