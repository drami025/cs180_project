package com.cs180.ucrtinder.ucrtinder.ui;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cs180.ucrtinder.ucrtinder.FragmentSupport.NavigationListener;
import com.cs180.ucrtinder.ucrtinder.tindercard.Data;
import com.cs180.ucrtinder.ucrtinder.FragmentSupport.AndroidDrawer;
import com.cs180.ucrtinder.ucrtinder.R;
import com.cs180.ucrtinder.ucrtinder.tindercard.FlingCardListener;
import com.cs180.ucrtinder.ucrtinder.tindercard.SwipeFlingAdapterView;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Daniel removed the call to the login

public class MainActivity extends AppCompatActivity implements FlingCardListener.ActionDownInterface {

    public static MyAppAdapter myAppAdapter;
    public static ViewHolder viewHolder;
    private ParseUser user;
    private List<ParseUser> candidates;
    private ArrayList<Data> al;
    private SwipeFlingAdapterView flingContainer;
    private AndroidDrawer mAndroidDrawer;
    private Toolbar mToolbar;
    int currentCandidate = 0;
    private ImageView mProfileImage;

    private Button likebtn;
    private Button dislikebtn;

    public static void removeBackground() {


        viewHolder.background.setVisibility(View.GONE);
        myAppAdapter.notifyDataSetChanged();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        user = ParseUser.getCurrentUser();

        // Start Location when this activity is open
        Log.d(getClass().getSimpleName(), "Started the geolocation service");
        //startService(new Intent(this, GeoLocationService.class));

        // Creating an android drawer to slide in from the left side

        mProfileImage = (ImageView) findViewById(R.id.main_profile_drawer_pic);

        ExecutorService es = Executors.newFixedThreadPool(1);
        es.execute(new ProfileImageRunnable());

        mAndroidDrawer = new AndroidDrawer(this, R.id.drawer_layout_main, R.id.left_drawer_main);

        //Set up toolbar
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.mipmap.ic_drawer);
        mToolbar.setNavigationOnClickListener(new NavigationListener(mAndroidDrawer));

        // Builds Fling card container
        flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frame);

        pullCandidates();

        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {

                View view = flingContainer.getSelectedView();
                view.findViewById(R.id.background).setAlpha(0);

                myAppAdapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "Clicked card", Toast.LENGTH_SHORT).show();

                Intent cardProfileIntent = new Intent(getApplicationContext(), CardProfileActivity.class);
                startActivity(cardProfileIntent);
            }
        });

        // Set buttons on main activity
        likebtn = (Button) findViewById(R.id.likebtn);
        dislikebtn = (Button) findViewById(R.id.dislikebtn);


        likebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!al.isEmpty()) {
                    View view = flingContainer.getSelectedView();
                    view.findViewById(R.id.background).setAlpha(0);
                    view.findViewById(R.id.item_swipe_left_indicator).setAlpha(1);
                    flingContainer.getTopCardListener().selectRight();
                    myAppAdapter.notifyDataSetChanged();
                }
            }
        });

        dislikebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!al.isEmpty()) {
                    View view = flingContainer.getSelectedView();
                    view.findViewById(R.id.background).setAlpha(0);
                    view.findViewById(R.id.item_swipe_right_indicator).setAlpha(1);
                    flingContainer.getTopCardListener().selectLeft();
                    myAppAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    class ProfileImageRunnable implements Runnable {
        @Override
        public void run() {
            final Bitmap bmp;

            try {
                ParseUser user = ParseUser.getCurrentUser();
                String urlString = user.getString("photo0");
                URL url = new URL(urlString);
                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProfileImage.setImageBitmap(bmp);
                    }
                });
            }
            catch(MalformedURLException e){
                e.printStackTrace();
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public void pullCandidates(){
        candidates = getCandidates();
        al = new ArrayList<>();
        for (int i = 0; i < candidates.size(); ++i) {
            al.add(new Data(candidates.get(i).getString("profilePictureUrl"), candidates.get(i).getString("name") +
                    ", " + candidates.get(i).getNumber("age")));
        }
        myAppAdapter = new MyAppAdapter(al, MainActivity.this);
        flingContainer.setAdapter(myAppAdapter);
        flingContainer.setFlingListener(new CardSwipeListener());
    }

    @Override
    public void onActionDownPerform() {
        Log.e("action", "bingo");
    }

    public static class ViewHolder {
        public static FrameLayout background;
        public TextView DataText;
        public ImageView cardImage;


    }

    public class MyAppAdapter extends BaseAdapter {


        public List<Data> parkingList;
        public Context context;

        private MyAppAdapter(List<Data> apps, Context context) {
            this.parkingList = apps;
            this.context = context;
        }

        @Override
        public int getCount() {
            return parkingList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View rowView = convertView;


            if (rowView == null) {

                LayoutInflater inflater = getLayoutInflater();
                rowView = inflater.inflate(R.layout.item, parent, false);
                // configure view holder
                viewHolder = new ViewHolder();
                viewHolder.DataText = (TextView) rowView.findViewById(R.id.bookText);
                viewHolder.background = (FrameLayout) rowView.findViewById(R.id.background);
                viewHolder.cardImage = (ImageView) rowView.findViewById(R.id.cardImage);

                rowView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.DataText.setText(parkingList.get(position).getDescription() + "");

            Glide.with(MainActivity.this).load(parkingList.get(position).getImagePath()).into(viewHolder.cardImage);

            return rowView;
        }
    }

    public List<ParseUser> getCandidates()  {
        // local variables
        String gender = user.getString("gender");
        boolean men = user.getBoolean("men");
        boolean women = user.getBoolean("women");
        int age = (int)user.getNumber("age");
        int minAge = (int)user.getNumber("minAge");
        int maxAge = (int)user.getNumber("maxAge");
        int maxDist = (int)user.getNumber("maxDist");
        ParseGeoPoint location = user.getParseGeoPoint("location");

        // set up query
        ParseQuery<ParseUser> mainQuery = ParseUser.getQuery();
        if (men && women) {}
        else if (men) mainQuery.whereEqualTo("gender", "male");
        else if (women) mainQuery.whereEqualTo("gender", "female");
        mainQuery.whereNotEqualTo("objectId", user.getObjectId());
        mainQuery.whereGreaterThanOrEqualTo("age", minAge);
        mainQuery.whereLessThanOrEqualTo("age", maxAge);
        mainQuery.whereWithinMiles("location", location, maxDist);

        // further filter candidates
        List<ParseUser> candidates;
        try {
            candidates = mainQuery.find();
        } catch (Exception e) {
            return null;
        }
        Iterator<ParseUser> it = candidates.iterator();
        while (it.hasNext()) {
            ParseUser candidate = it.next();
            if (age < (int)candidate.getNumber("minAge") || age > (int)candidate.getNumber("maxAge") ||
                    location.distanceInMilesTo(candidate.getParseGeoPoint("location")) > (int)candidate.getNumber("maxDist") ||
                    (gender.equals("male") && !candidate.getBoolean("men")) ||
                    (gender.equals("female") && !candidate.getBoolean("women"))) {
                it.remove();
            }
        }

        // sort candidates
        Collections.sort(candidates, new Comparator<ParseUser>() {
            public int compare(ParseUser l, ParseUser r) {
                double lCount = 0, rCount = 0;
                List<String> interests = ParseUser.getCurrentUser().getList("interests");
                List<String> lInterests = l.getList("interests");
                List<String> rInterests = r.getList("interests");
                for (int i = 0; i < interests.size(); ++i) {
                    if (lInterests.contains(interests.get(i))) ++lCount;
                    if (rInterests.contains(interests.get(i))) ++rCount;
                }
                double lPoints = lCount / lInterests.size() + lCount / interests.size();
                double rPoints = rCount / rInterests.size() + rCount / interests.size();
                if (lPoints < rPoints) return 1;
                else if (lPoints > rPoints) return -1;
                else return 0;
            }
        });

        // remove likes, dislikes, matches
        List<String> likes = user.getList("likes");
        List<String> dislikes = user.getList("dislikes");
        List<String> matches = user.getList("matches");
        candidates.removeAll(likes);
        candidates.removeAll(dislikes);
        candidates.removeAll(matches);
        return candidates;
    }

    private class CardSwipeListener implements SwipeFlingAdapterView.onFlingListener{
        @Override
        public void removeFirstObjectInAdapter() {

        }

        @Override
        public void onLeftCardExit(Object dataObject) {
            al.remove(0);
            myAppAdapter.notifyDataSetChanged();
            List<ParseUser> dislikes = user.getList("dislikes");
            dislikes.add(candidates.get(currentCandidate++));
            user.put("dislikes", dislikes);
            user.saveInBackground();
        }

        @Override
        public void onRightCardExit(Object dataObject) {

            al.remove(0);
            myAppAdapter.notifyDataSetChanged();
            List<ParseUser> likes = user.getList("likes");
            likes.add(candidates.get(currentCandidate));
            List<ParseUser> targetlikes = candidates.get(currentCandidate).getList("likes");
            if (targetlikes.contains(user)) {
                List<ParseUser> matches = user.getList("matches");
                List<ParseUser> targetMatches = candidates.get(currentCandidate).getList("matches");
                matches.add(candidates.get(currentCandidate));
                targetMatches.add(user);
                user.put("matches", matches);
                candidates.get(currentCandidate).put("matches", targetMatches);
                user.saveInBackground();
                candidates.get(currentCandidate).saveInBackground();
            }
            ++currentCandidate;
        }

        @Override
        public void onAdapterAboutToEmpty(int itemsInAdapter) {

        }

        @Override
        public void onScroll(float scrollProgressPercent) {

            View view = flingContainer.getSelectedView();
            view.findViewById(R.id.background).setAlpha(0);
            view.findViewById(R.id.item_swipe_right_indicator).setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
            view.findViewById(R.id.item_swipe_left_indicator).setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        //pullCandidates();
    }
}
