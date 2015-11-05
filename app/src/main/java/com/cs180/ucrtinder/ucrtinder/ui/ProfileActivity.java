package com.cs180.ucrtinder.ucrtinder.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cs180.ucrtinder.ucrtinder.FragmentSupport.AndroidDrawer;
import com.cs180.ucrtinder.ucrtinder.Parse.ParseConstants;
import com.cs180.ucrtinder.ucrtinder.R;
import com.cs180.ucrtinder.ucrtinder.tindercard.SwipePhotoAdapter;
import com.parse.ParseUser;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    ParseUser currentUser = ParseUser.getCurrentUser(); //need to generalize this somehow
    private ViewPager myPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = (Toolbar)findViewById(R.id.my_toolbar);
        toolbar.setTitle(currentUser.getString(ParseConstants.KEY_NAME));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AndroidDrawer drawer = new AndroidDrawer(this,R.id.drawer_layout_profile,R.id.left_drawer_profile);

        /*this is how to update columns in parse
        String[] testInterests = {"Trucks", "Cars", "Television", "Movies", "Ghosts", "Cats", "Ghostcats", "Penguins","Computers", "blah","blah"};
        currentUser.put(ParseConstants.KEY_INTERESTS, Arrays.asList(testInterests));
        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

            }
        });
*/
        TextView text;
        text = (TextView) this.findViewById(R.id.name_textview);
        text.setText(currentUser.getString(ParseConstants.KEY_NAME) + ", " + currentUser.getInt(ParseConstants.KEY_AGE));
        text = (TextView) this.findViewById(R.id.Aboutyou_textview);
        text.setText(currentUser.getString(ParseConstants.KEY_ABOUTYOU));
        text = (TextView) this.findViewById(R.id.interests_textview);
        ArrayList<String> array = (ArrayList<String>)currentUser.get(ParseConstants.KEY_INTERESTS);
        String in = "";
        for(int i=0; i<array.size(); i++){
            in = in.concat(array.get(i));
            in = in.concat(", ");
        }
        text.setText(in);

        SwipePhotoAdapter adapter = new SwipePhotoAdapter();
        myPager = (ViewPager) findViewById(R.id.viewpager_layout);
        myPager.setAdapter(adapter);
        myPager.setCurrentItem(0);

        ViewTreeObserver viewTreeObserver = myPager.getViewTreeObserver();
        final DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

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
            Intent intent = new Intent(this, EditProfileActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
