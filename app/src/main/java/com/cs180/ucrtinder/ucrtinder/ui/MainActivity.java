package com.cs180.ucrtinder.ucrtinder.ui;


import android.content.Context;
import android.content.Intent;
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
import com.bumptech.glide.Glide;
import com.cs180.ucrtinder.ucrtinder.FragmentSupport.NavigationListener;
import com.cs180.ucrtinder.ucrtinder.FragmentSupport.OnCardsLoadedListener;
import com.cs180.ucrtinder.ucrtinder.Messenger.AtlasMessagesScreen;
import com.cs180.ucrtinder.ucrtinder.Parse.ParseConstants;
import com.cs180.ucrtinder.ucrtinder.Services.GeoLocationService;
import com.cs180.ucrtinder.ucrtinder.tindercard.Data;
import com.cs180.ucrtinder.ucrtinder.FragmentSupport.AndroidDrawer;
import com.cs180.ucrtinder.ucrtinder.R;
import com.cs180.ucrtinder.ucrtinder.tindercard.FlingCardListener;
import com.cs180.ucrtinder.ucrtinder.tindercard.SwipeFlingAdapterView;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


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
    String newUserId = null;

    private OnCardsLoadedListener mCardsCompleteListener;

    private Button likebtn;
    private Button dislikebtn;

    public static final String CARD_BUNDLE = "cardBundle";
    public static final String CARD_USER = "cardUser";
    public static final String CARD_NAME = "cardName";
    public static final String CARD_ID = "cardID";

    public static final String CANDIDATE_ID = "candidateId";
    public static final String MATCHED_BUNDLE = "matchedBundle";

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
        startService(new Intent(this, GeoLocationService.class));

        // ExecutorService es = Executors.newFixedThreadPool(1);
        // es.execute(new ProfileImageRunnable(mProfileImage));

        mAndroidDrawer = new AndroidDrawer(this, R.id.drawer_layout_main, R.id.left_drawer_main, R.id.main_profile_drawer_pic);

        //Set up toolbar
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.mipmap.ic_drawer);
        mToolbar.setNavigationOnClickListener(new NavigationListener(mAndroidDrawer));
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException np) {
            np.printStackTrace();
        }

        // Builds Fling card container
        flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frame);

        pullCandidates();
    }


    @Override
    public void onResume(){
        super.onResume();
        Log.d(getClass().getSimpleName(), "Stopped the geolocation service");
        // Start geolocation update service
        startService(new Intent(this, GeoLocationService.class));
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(getClass().getSimpleName(), "Stopped the geolocation service");
        // Stop geolocation update service
        stopService(new Intent(this, GeoLocationService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(getClass().getSimpleName(), "Stopped the geolocation service");
        // Stop geolocation update service
        stopService(new Intent(this, GeoLocationService.class));
    }

    public void pullCandidates(){

        mCardsCompleteListener = new OnCardsLoadedListener() {
            @Override
            public void onCardsLoaded() {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(mCounter.addAndGet(1) == mLimit.get()) {
                            myAppAdapter = new MyAppAdapter(al, MainActivity.this);
                            flingContainer.setAdapter(myAppAdapter);
                            myAppAdapter.notifyDataSetChanged();
                            setContainerListeners();
                        }
                    }
                });
            }
        };



        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    user = ParseUser.getCurrentUser();
                } catch (NullPointerException n) {
                    n.printStackTrace();
                }

                al = new ArrayList<>();
                candidates = getCandidates();
                if (candidates == null) {
                    return;
                }

                mLimit = new AtomicInteger(candidates.size());
                for (int i = 0; i < candidates.size(); ++i) {

                    if (candidates.get(i).getString(ParseConstants.KEY_FACEBOOKID) == null) {
                        mLimit.decrementAndGet();
                    }

                    al.add(new Data(
                            candidates.get(i).getString(ParseConstants.KEY_PHOTO0),
                            candidates.get(i).getString(ParseConstants.KEY_NAME) + ", " + candidates.get(i).getNumber(ParseConstants.KEY_AGE),
                            candidates.get(i).getObjectId(),
                            candidates.get(i).getString(ParseConstants.KEY_FACEBOOKID),
                            mCardsCompleteListener,
                            i,
                            candidates.get(i).getString(ParseConstants.KEY_LAYERID)));
                }
            }
        });
        if (!executor.isShutdown()) {
            executor.shutdown();
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

                Intent cardProfileIntent = new Intent(MainActivity.this, CardProfileActivity.class);
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

            // Update texts
            String dataText = parkingList.get(position).getDescription() + "";
            String mutualText = viewHolder.mutualFriendText.getText().toString() + " " + parkingList.get(position).getMutualFriendCount();
            viewHolder.DataText.setText(dataText);
            viewHolder.mutualFriendText.setText(mutualText);

            Glide.with(MainActivity.this).load(parkingList.get(position).getImagePath()).into(viewHolder.cardImage);

            return rowView;
        }
    }

    public List<ParseUser> getCandidates()  {

        // local variables
        if (user != null) {
            String gender = user.getString(ParseConstants.KEY_GENDER);
            boolean men = user.getBoolean(ParseConstants.KEY_MEN);
            boolean women = user.getBoolean(ParseConstants.KEY_WOMEN);
            int age = (int) user.getNumber(ParseConstants.KEY_AGE);
            int minAge = (int) user.getNumber(ParseConstants.KEY_SMALLESTAGE);
            int maxAge = (int) user.getNumber(ParseConstants.KEY_LARGESTAGE);
            int maxDist = (int) user.getNumber(ParseConstants.KEY_DISTANCE);
            String id = user.getString(ParseConstants.KEY_ID);

            ParseGeoPoint location = user.getParseGeoPoint(ParseConstants.KEY_LOCATION);

            // set up query
            ParseQuery<ParseUser> mainQuery = ParseUser.getQuery();
            /*
            if (men && women) {}
            else if (men) mainQuery.whereEqualTo(ParseConstants.KEY_GENDER, "male");
            else if (women) mainQuery.whereEqualTo(ParseConstants.KEY_GENDER, "female");
            mainQuery.whereNotEqualTo(VParseConstants.KEY_OBJECTID, user.getObjectId());
            mainQuery.whereGreaterThanOrEqualTo(ParseConstants.KEY_AGE, minAge);
            mainQuery.whereLessThanOrEqualTo(ParseConstants.KEY_AGE, maxAge);
            mainQuery.whereWithinMiles(ParseConstants.KEY_LOCATION, location, maxDist);
            */
            // further filter candidates
            try {
                candidates = mainQuery.find();
            } catch (Exception e) {
                return null;
            }
            /*
            Iterator<ParseUser> it = candidates.iterator();
            while (it.hasNext()) {
                ParseUser candidate = it.next();
                if (age < (int)candidate.getNumber(ParseConstants.KEY_SMALLESTAGE) || age > (int)candidate.getNumber(ParseConstants.KEY_LARGESTAGE) ||
                        location.distanceInMilesTo(candidate.getParseGeoPoint(ParseConstants.KEY_LOCATION)) > (int)candidate.getNumber(ParseConstants.KEY_DISTANCE) ||
                        (gender.equals("male") && !candidate.getBoolean(ParseConstants.KEY_MEN)) ||
                        (gender.equals("female") && !candidate.getBoolean(ParseConstants.KEY_WOMEN))) {
                    it.remove();
                }
            }
            */
            // sort candidates
            /*
            Collections.sort(candidates, new Comparator<ParseUser>() {
                public int compare(ParseUser l, ParseUser r) {
                    double lCount = 0, rCount = 0;
                    List<String> interests = ParseUser.getCurrentUser().getList(ParseConstants.KEY_INTERESTS);
                    List<String> lInterests = l.getList(ParseConstants.KEY_INTERESTS);
                    List<String> rInterests = r.getList(ParseConstants.KEY_INTERESTS);
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
            List<String> likes = user.getList(ParseConstants.KEY_LIKES);
            List<String> dislikes = user.getList(ParseConstants.KEY_DISLIKES);
            List<String> matches = user.getList(ParseConstants.KEY_MATCHES);
            candidates.removeAll(likes);
            candidates.removeAll(dislikes);
            candidates.removeAll(matches);
            */
        }
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

            if (!al.isEmpty()) {
                al.remove(0);
                myAppAdapter.notifyDataSetChanged();

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<ParseUser> dislikes = user.getList(ParseConstants.KEY_DISLIKES);
                        int i = currentCandidate;
                        i += 1;
                        if (i < candidates.size()) {
                            dislikes.add(candidates.get(currentCandidate++));
                        }
                        user.put("dislikes", dislikes);

                        try {
                            user.saveInBackground();
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                });

                t.setPriority(Thread.MIN_PRIORITY);
                t.start();
            }
        }

        @Override
        public void onRightCardExit(final Object dataObject) {

            if (!al.isEmpty()) {
                final Data data = al.get(0);
                al.remove(0);
                myAppAdapter.notifyDataSetChanged();

                ExecutorService executor = Executors.newFixedThreadPool(1);
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        List<ParseUser> likes = user.getList(ParseConstants.KEY_LIKES);
                        int i = currentCandidate;
                        i += 1;
                        if (i < candidates.size()) {
                            likes.add(candidates.get(currentCandidate));
                        }

                        if (currentCandidate < candidates.size()) {
                            List<ParseUser> targetlikes = candidates.get(currentCandidate).getList(ParseConstants.KEY_LIKES);
                            if (targetlikes.contains(user)) {
                                // Get the current Candidate
                                Iterator<ParseUser> candidateIter = candidates.iterator();
                                int index = 0;
                                while (candidateIter.hasNext() && candidates.size() > currentCandidate) {
                                    if (index == currentCandidate) {
                                        break;
                                    }
                                    candidateIter.next();
                                    index++;
                                }
                                ParseUser currCandidate = candidateIter.next();

                                // Get matches list for current user and candidate
                                List<ParseUser> matches = user.getList(ParseConstants.KEY_MATCHES);
                                List<ParseUser> targetMatches = currCandidate.getList(ParseConstants.KEY_MATCHES);
                                //List<ParseUser> targetMatches = candidates.get(currentCandidate).getList("matches");

                                // Update each list for matched likes
                                matches.add(currCandidate);
                                //matches.add(candidates.get(currentCandidate));
                                targetMatches.add(user);
                                user.put(ParseConstants.KEY_MATCHES, matches);
                                currCandidate.put(ParseConstants.KEY_MATCHES, targetMatches);
                                //candidates.get(currentCandidate).put("matches", targetMatches);

                                // Save in the background
                                try {
                                    user.saveInBackground();
                                    currCandidate.saveInBackground();
                                }
                                catch (Exception e){
                                    e.printStackTrace();
                                }
                                //candidates.get(currentCandidate).saveInBackground();
                            }
                        }

                        ++currentCandidate;

                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Make a notification
                                Intent intent = new Intent(MainActivity.this, MatchedNotifcationActivity.class);
                                Bundle b = new Bundle();
                                // Get card user parse String
                                b.putString(CARD_USER, data.getImagePath());
                                //b.putString(CARD_USER, currCandidate.getString(ParseConstants.KEY_PHOTO0));
                                b.putString(CARD_ID, data.getID());
                                //b.putString(CARD_ID, currCandidate.getObjectId());
                                intent.putExtra(CARD_BUNDLE, b);
                                startActivity(intent);
                            }
                        });

                    }
                });
                if (!executor.isShutdown()) {
                    executor.shutdown();
                }

                //t.setPriority(Thread.MIN_PRIORITY);
                //t.start();

                // Add the current card if it matches
                addNewConversationWithThisCard();
            }
        }

        @Override
        public void onAdapterAboutToEmpty(int itemsInAdapter) {

        }

        @Override
        public void onScroll(float scrollProgressPercent) {

            View view = flingContainer.getSelectedView();
            try {
                if (view.findViewById(R.id.background) != null) {
                    view.findViewById(R.id.background).setAlpha(0);
                }
                if (view.findViewById(R.id.item_swipe_right_indicator) != null) {
                    view.findViewById(R.id.item_swipe_right_indicator).setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
                }
                if (view.findViewById(R.id.item_swipe_left_indicator) != null) {
                    view.findViewById(R.id.item_swipe_left_indicator).setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);
                }
            }
            catch (NullPointerException n) {
                n.printStackTrace();
            }
        }
    }

    public void addNewConversationWithThisCard() {

        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ParseUser currentUser = null;

                    try {
                        currentUser = ParseUser.getCurrentUser();
                        if (currentUser != null) {
                            Log.d(getClass().getSimpleName(), "Username: " + currentUser.get(ParseConstants.KEY_NAME));
                        }
                        newUserId = currentUser.getString(ParseConstants.KEY_LAYERID);
                    } catch (NullPointerException n) {
                        n.printStackTrace();
                    }


                    if (newUserId != null) {
                        Intent intent = new Intent(getApplicationContext(), AtlasMessagesScreen.class);
                        intent.putExtra(AtlasMessagesScreen.EXTRA_CONVERSATION_IS_NEW, true);
                        intent.putExtra(AtlasMessagesScreen.EXTRA_NEW_USER, newUserId);
                        startActivity(intent);
                    }
                } catch (NullPointerException n) {
                    n.printStackTrace();
                }
            }
        });
        if (!executor.isShutdown()) {
            executor.shutdown();
        }

        //t.setPriority(Thread.MIN_PRIORITY);
        //t.start();
    }

}
