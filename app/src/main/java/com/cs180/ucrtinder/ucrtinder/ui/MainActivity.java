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
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cs180.ucrtinder.ucrtinder.FragmentSupport.NavigationListener;
import com.cs180.ucrtinder.ucrtinder.FragmentSupport.OnCardsLoadedListener;
import com.cs180.ucrtinder.ucrtinder.Messenger.AtlasIdentityProvider;
import com.cs180.ucrtinder.ucrtinder.Parse.ParseConstants;
import com.cs180.ucrtinder.ucrtinder.tindercard.Data;
import com.cs180.ucrtinder.ucrtinder.FragmentSupport.AndroidDrawer;
import com.cs180.ucrtinder.ucrtinder.R;
import com.cs180.ucrtinder.ucrtinder.tindercard.FlingCardListener;
import com.cs180.ucrtinder.ucrtinder.tindercard.SwipeFlingAdapterView;
import com.layer.atlas.Atlas;
import com.parse.FindCallback;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

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
    MainActivity mActivity;
    private AtomicInteger mCounter = new AtomicInteger(0);
    private AtomicInteger mLimit;

    private OnCardsLoadedListener mCardsCompleteListener;

    private Button likebtn;
    private Button dislikebtn;

    public static final String CARD_BUNDLE = "cardBundle";
    public static final String CARD_USER = "cardUser";
    public static final String CARD_NAME = "cardName";
    public static final String CARD_ID = "cardID";

    public static void removeBackground() {


        viewHolder.background.setVisibility(View.GONE);
        myAppAdapter.notifyDataSetChanged();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mActivity = this;

        // Start Location when this activity is open
        Log.d(getClass().getSimpleName(), "Started the geolocation service");
        //startService(new Intent(this, GeoLocationService.class));

        // Creating an android drawer to slide in from the left side


//        ExecutorService es = Executors.newFixedThreadPool(1);
//        es.execute(new ProfileImageRunnable(mProfileImage));

        mAndroidDrawer = new AndroidDrawer(this, R.id.drawer_layout_main, R.id.left_drawer_main, R.id.main_profile_drawer_pic);

        //Set up toolbar
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.mipmap.ic_drawer);
        mToolbar.setNavigationOnClickListener(new NavigationListener(mAndroidDrawer));

        // Builds Fling card container
        flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frame);

        pullCandidates();
    }

    public void pullCandidates(){
        user = ParseUser.getCurrentUser();
        candidates = getCandidates();

        if(candidates == null){
            return;
        }

        mCardsCompleteListener = new OnCardsLoadedListener() {
            @Override
            public void onCardsLoaded() {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(mCounter.addAndGet(1) == mLimit.get()) {
                            myAppAdapter = new MyAppAdapter(al, MainActivity.this);
                            flingContainer.setAdapter(myAppAdapter);
                            setContainerListeners();
                            myAppAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        };

        al = new ArrayList<>();
        mLimit = new AtomicInteger(candidates.size());
        for (int i = 0; i < candidates.size(); ++i) {

            if(candidates.get(i).getString("facebookId") == null){
                mLimit.decrementAndGet();
            }

            al.add(new Data(candidates.get(i).getString(ParseConstants.KEY_PHOTO0), candidates.get(i).getString(ParseConstants.KEY_NAME) +
                    ", " + candidates.get(i).getNumber(ParseConstants.KEY_AGE), candidates.get(i).getObjectId(),
                    candidates.get(i).getString("facebookId"), mCardsCompleteListener, i));
        }
    }

    void setContainerListeners(){
        flingContainer.setFlingListener(new CardSwipeListener());

        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {

                View view = flingContainer.getSelectedView();
                view.findViewById(R.id.background).setAlpha(0);

                myAppAdapter.notifyDataSetChanged();
                //  Toast.makeText(getApplicationContext(), "Clicked card", Toast.LENGTH_SHORT).show();

                Intent cardProfileIntent = new Intent(getApplicationContext(), CardProfileActivity.class);
                Bundle b = new Bundle();

                // Get card user parse String
                b.putString(CARD_USER, al.get(itemPosition).getUserString());
                b.putString(CARD_ID, al.get(itemPosition).getID());
                cardProfileIntent.putExtra(CARD_BUNDLE, b);

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

    @Override
    public void onActionDownPerform() {
        Log.e("action", "bingo");
    }

    public static class ViewHolder {
        public static FrameLayout background;
        public TextView DataText;
        public ImageView cardImage;
        public TextView mutualFriendText;

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
                viewHolder.mutualFriendText = (TextView) rowView.findViewById(R.id.mutual_friend_count);

                rowView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.DataText.setText(parkingList.get(position).getDescription() + "");
            viewHolder.mutualFriendText.setText(viewHolder.mutualFriendText.getText().toString() + " " +
                    parkingList.get(position).getMutualFriendCount());

            Glide.with(MainActivity.this).load(parkingList.get(position).getImagePath()).into(viewHolder.cardImage);

            return rowView;
        }
    }

    public List<ParseUser> getCandidates()  {

        // local variables
        String gender = user.getString("gender");
        boolean men = user.getBoolean("men");
        boolean women = user.getBoolean("women");
        int age = (int) user.getNumber("age");
        int minAge = (int)user.getNumber("minAge");
        int maxAge = (int)user.getNumber("maxAge");
        int maxDist = (int)user.getNumber("maxDist");
        String id = user.getString("id");

        ParseGeoPoint location = user.getParseGeoPoint("location");

        // set up query
        ParseQuery<ParseUser> mainQuery = ParseUser.getQuery();
/*        if (men && women) {}
        else if (men) mainQuery.whereEqualTo("gender", "male");
        else if (women) mainQuery.whereEqualTo("gender", "female");
        mainQuery.whereNotEqualTo("objectId", user.getObjectId());
        mainQuery.whereGreaterThanOrEqualTo("age", minAge);
        mainQuery.whereLessThanOrEqualTo("age", maxAge);
        mainQuery.whereWithinMiles("location", location, maxDist);
*/
        // further filter candidates
        try {
            candidates = mainQuery.find();
        } catch (Exception e) {
            return null;
        }
 /*       Iterator<ParseUser> it = candidates.iterator();
        while (it.hasNext()) {
            ParseUser candidate = it.next();
            if (age < (int)candidate.getNumber("minAge") || age > (int)candidate.getNumber("maxAge") ||
                    location.distanceInMilesTo(candidate.getParseGeoPoint("location")) > (int)candidate.getNumber("maxDist") ||
                    (gender.equals("male") && !candidate.getBoolean("men")) ||
                    (gender.equals("female") && !candidate.getBoolean("women"))) {
                it.remove();
            }
        }
*/
        // sort candidates
  /*      Collections.sort(candidates, new Comparator<ParseUser>() {
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
*/
        if(candidates == null){
            candidates = new ArrayList<>();
        }

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
            int i = currentCandidate;
            if(i++ < candidates.size() ) {
                dislikes.add(candidates.get(currentCandidate++));
            }
            user.put("dislikes", dislikes);
            user.saveInBackground();
        }

        @Override
        public void onRightCardExit(Object dataObject) {

            al.remove(0);
            myAppAdapter.notifyDataSetChanged();
            List<ParseUser> likes = user.getList("likes");
            int i = currentCandidate;
            if(i++ < candidates.size() ) {
                likes.add(candidates.get(currentCandidate));
            }

            if(currentCandidate < candidates.size()) {
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

                    // Add this parseUser to the atlas message system list
                    /*
                    AtlasIdentityProvider atlasIdentityProvider = new AtlasIdentityProvider(getApplicationContext());
                    AtlasIdentityProvider.Participant participant = (AtlasIdentityProvider.Participant) atlasIdentityProvider.getParticipant(
                             candidates.get(currentCandidate).getString("atlasMessengerId"));

                    Map<String, Atlas.Participant> participants = new HashMap<>();
                    participants = atlasIdentityProvider.getParticipants(null, participants);
                    participants.put(participant.getId(), participant);

                    */
                    //atlasIdentityProvider.

                    // Create a notification message here




                }
                ++currentCandidate;
            }
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
