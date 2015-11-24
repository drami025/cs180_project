package com.cs180.ucrtinder.ucrtinder.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cs180.ucrtinder.ucrtinder.FragmentSupport.AndroidDrawer;
import com.cs180.ucrtinder.ucrtinder.FragmentSupport.NavigationListener;
import com.cs180.ucrtinder.ucrtinder.Parse.ParseConstants;
import com.cs180.ucrtinder.ucrtinder.R;
import com.cs180.ucrtinder.ucrtinder.tindercard.SwipePhotoAdapter;
import com.layer.sdk.internal.persistence.sync.Load;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Arrays;

public class ProfileActivity extends AppCompatActivity {

    //ParseUser currentUser = ParseUser.getCurrentUser(); //need to generalize this somehow
    private ViewPager myPager;
    private TextView text;
    public static final String KEY_USERPROFILE = "userprofile";
    public static final String KEY_USERABOUTYOU = "useraboutyou";
    public static final String KEY_USERTOOLBARTITLE = "usertoolbartitle";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_USERINTEREST = "userinterest";
    public static final String USER_PROF = "userprof";


    private String toolBarTitle = "";
    private String nameText = "";
    private String aboutyouText = "";
    private String InterestText = "";

    boolean jumpedToNew;

    final static int WAIT_TIME = 10;
    private Handler mHandler;
    private SwipePhotoAdapter adapter;

    private ParseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        try {
            user = ParseUser.getCurrentUser();
            if (user == null) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        } catch (NullPointerException n) {
            n.printStackTrace();
        }


        AndroidDrawer drawer = new AndroidDrawer
                (this,R.id.drawer_layout_profile,R.id.left_drawer_profile, R.id.profile_profile_drawer_pic);

        jumpedToNew = false;

        // Update the strings on the for this profile - AP
        getUpdatedStrings();

        Toolbar toolbar = (Toolbar)findViewById(R.id.my_toolbar);
        if (toolBarTitle != null) {
            toolbar.setTitle(toolBarTitle);
        }
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_drawer);
        toolbar.setNavigationOnClickListener(new NavigationListener(drawer));
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException np) {
            np.printStackTrace();
        }


        /*
        //this is how to update columns in parse
        String[] testInterests = {"Trucks", "Cars", "Television", "Movies", "Ghosts", "Cats", "Ghostcats", "Penguins","Computers", "blah","blah"};
        currentUser.put(ParseConstants.KEY_INTERESTS, Arrays.asList(testInterests));
        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

            }
        });
        */

        text = (TextView) this.findViewById(R.id.name_textview);
        //text.setText(currentUser.getString(ParseConstants.KEY_NAME) + ", " + currentUser.getInt(ParseConstants.KEY_AGE));
        text.setText(nameText);
        text = (TextView) this.findViewById(R.id.Aboutyou_textview);
        //text.setText(currentUser.getString(ParseConstants.KEY_ABOUTYOU));
        text.setText(aboutyouText);
        text = (TextView) this.findViewById(R.id.interests_textview);
        /*
        ArrayList<String> array = (ArrayList<String>)currentUser.get(ParseConstants.KEY_INTERESTS);
        String in = "";

        if(array != null) {
            for (int i = 0; i < array.size(); i++) {
                in = in.concat(array.get(i));
                in = in.concat(", ");
            }
        }*/
        //text.setText(in);
        text.setText(InterestText);

        adapter = new SwipePhotoAdapter(ProfileActivity.KEY_USERPROFILE, "");
        adapter.pullPhotos();

        mHandler = new Handler();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem item = menu.getItem(0);
        SpannableString s = new SpannableString(item.getTitle());
        s.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.length(), 0);
        item.setTitle(s);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_edit) {
            jumpedToNew = true;
            Intent intent = new Intent(this, EditProfileActivity.class);
            text = (TextView) findViewById(R.id.Aboutyou_textview);
            intent.putExtra("bioText", text.getText().toString());
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (adapter.getCurrentUserPhotoBool() || !adapter.getAllNullBool()) {
                    Log.e(getClass().getSimpleName(), "Updated images in the slider");
                    mHandler.removeCallbacks(this);


                    myPager = (ViewPager) findViewById(R.id.viewpager_layout);
                    myPager.setAdapter(adapter);
                    myPager.setCurrentItem(0);

                    adapter.notifyDataSetChanged();
                    myPager.destroyDrawingCache();

                    ViewTreeObserver viewTreeObserver = myPager.getViewTreeObserver();
                    final DisplayMetrics displayMetrics = ProfileActivity.this.getResources().getDisplayMetrics();
                    viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {

                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 400);
                            float dpheight = displayMetrics.heightPixels / 3 * 2;
                            int width = myPager.getWidth();

                            params.width = width;
                            params.height = (int) dpheight;

                            myPager.setLayoutParams(params);
                        }
                    });

                    //adapter.notifyDataSetChanged();
                } else {
                    mHandler.postDelayed(this, WAIT_TIME);
                }
            }
        }, WAIT_TIME);

    }

    @Override
    public void onResume(){
        super.onResume();

        if (jumpedToNew) {
            Intent intent = new Intent(this, LoadingScreenActivity.class);
            startActivity(intent);
            jumpedToNew = false;
        }
    }

    private void getUpdatedStrings(){
        String oldtoolBarTitle = "";
        String oldnameText = "";
        String oldaboutyouText = "";
        String oldInterestText = "";

        SharedPreferences preferences = getSharedPreferences(USER_PROF, MODE_PRIVATE);

        oldtoolBarTitle = preferences.getString(KEY_USERTOOLBARTITLE, "");
        oldnameText = preferences.getString(KEY_USERNAME, "");
        oldaboutyouText = preferences.getString(KEY_USERABOUTYOU, "");
        oldInterestText = preferences.getString(KEY_USERINTEREST, "");

        Intent intent = getIntent();
        if (intent != null) {
            SharedPreferences.Editor editor = preferences.edit();
            toolBarTitle = intent.getStringExtra(KEY_USERTOOLBARTITLE);
            nameText = intent.getStringExtra(KEY_USERNAME);
            aboutyouText = intent.getStringExtra(KEY_USERABOUTYOU);
            InterestText = intent.getStringExtra(KEY_USERINTEREST);

            if (toolBarTitle != null && !toolBarTitle.equals(oldtoolBarTitle)) {
                editor.putString(KEY_USERTOOLBARTITLE, toolBarTitle);
            } else {
                toolBarTitle = oldtoolBarTitle;
            }
            if (nameText != null && !nameText.equals(oldnameText)) {
                editor.putString(KEY_USERNAME, nameText);
            } else {
                nameText = oldnameText;
            }
            if (aboutyouText != null && !aboutyouText.equals(oldaboutyouText)) {
                editor.putString(KEY_USERABOUTYOU, aboutyouText);
            } else {
                aboutyouText = oldaboutyouText;
            }
            if (InterestText != null && !InterestText.equals(oldInterestText)) {
                editor.putString(KEY_USERINTEREST, InterestText);
            } else {
                InterestText = oldInterestText;
            }
            editor.apply();
        } else {
            toolBarTitle = "";
            nameText = "";
            aboutyouText = "";
            InterestText = "";
        }
    }
}
