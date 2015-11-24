package com.cs180.ucrtinder.ucrtinder.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cs180.ucrtinder.ucrtinder.FragmentSupport.AndroidDrawer;
import com.cs180.ucrtinder.ucrtinder.FragmentSupport.NavigationListener;
import com.cs180.ucrtinder.ucrtinder.Parse.ParseConstants;
import com.cs180.ucrtinder.ucrtinder.R;
import com.cs180.ucrtinder.ucrtinder.tindercard.SwipePhotoAdapter;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.parse.Parse;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 *  Added by Aaron Peery
 *  10-24-2015
 */


public class CardProfileActivity extends AppCompatActivity {

    ParseUser currentUser;
    private List<ParseUser> profileUser;
    private ViewPager myPager;
    private LinearLayout mMutualFriendsView;

    public static final String KEY_CARDPROFILE = "cardprofile";

    ArrayList<String> array = new ArrayList<>();

    private String aboutYouText = "";
    private String userStringText = "";
    private String toolbarTitle = "";

    private Handler mHandler;
    final static int WAIT_TIME = 100;
    private SwipePhotoAdapter adapter;

    private ParseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_profile);

        try {
            user = ParseUser.getCurrentUser();
            if (user == null) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        } catch (NullPointerException n) {
            n.printStackTrace();
        }


        // Get intent with card user string data
        Intent intent = getIntent();
        Bundle b = intent.getBundleExtra(MainActivity.CARD_BUNDLE);
        final String userString = b.getString(MainActivity.CARD_USER, "");
        String profileId = b.getString(MainActivity.CARD_ID, "");

        Log.e("PROFILE ID", profileId);

        if(profileId.matches("-?\\d+(\\.\\d+)?")){
            Log.e("REGEX", "MATCHED");
            getMutualFriendIDs(profileId);
        }

        mMutualFriendsView = (LinearLayout) findViewById(R.id.card_profile_hsv_layout);

        AndroidDrawer drawer = new AndroidDrawer
                (this,R.id.drawer_layout_profile,R.id.left_drawer_card_profile, R.id.card_profile_drawer_pic);

        final Toolbar toolbar = (Toolbar)findViewById(R.id.my_toolbar);
        toolbar.setTitle(toolbarTitle);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_drawer);
        toolbar.setNavigationOnClickListener(new NavigationListener(drawer));
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException np) {
            np.printStackTrace();
        }

        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                currentUser = ParseUser.getCurrentUser();
                ParseQuery<ParseUser> mainQuery = ParseUser.getQuery();
                mainQuery.whereEqualTo(ParseConstants.KEY_OBJECTID, userString);

                try {
                    profileUser = mainQuery.find();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (profileUser != null) {
                    currentUser = profileUser.get(0);
                }

                toolbarTitle = currentUser.getString(ParseConstants.KEY_NAME);
                userStringText = currentUser.getString(ParseConstants.KEY_NAME) + ", " + currentUser.getInt(ParseConstants.KEY_AGE);
                aboutYouText = currentUser.getString(ParseConstants.KEY_ABOUTYOU);
                array = (ArrayList<String>) currentUser.get(ParseConstants.KEY_INTERESTS);

                CardProfileActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toolbar.setTitle(toolbarTitle);

                        TextView text;
                        text = (TextView) findViewById(R.id.name_textview);
                        text.setText(userStringText);
                        text = (TextView) findViewById(R.id.Aboutyou_textview);
                        text.setText(aboutYouText);
                        text = (TextView) findViewById(R.id.interests_textview);

                        String in = "";

                        if (array != null) {
                            for (int i = 0; i < array.size(); i++) {
                                in = in.concat(array.get(i));
                                in = in.concat(", ");
                            }
                        }
                        text.setText(in);
                    }
                });
            }
        });
        executor.shutdown();


        // Update photos on photoslider
        adapter = new SwipePhotoAdapter(KEY_CARDPROFILE, userString);
        adapter.pullPhotos();

        mHandler = new Handler();
    }

    @Override
    public void onStart() {
        super.onStart();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (adapter.getCandidateUserPhotoBool() || !adapter.getAllNullBool()) {
                    Log.e(getClass().getSimpleName(), "Updated images in the slider");
                    mHandler.removeCallbacks(this);


                    myPager = (ViewPager) findViewById(R.id.viewpager_layout);
                    myPager.setAdapter(adapter);
                    myPager.setCurrentItem(0);
                    adapter.notifyDataSetChanged();
                    myPager.destroyDrawingCache();

                    ViewTreeObserver viewTreeObserver = myPager.getViewTreeObserver();
                    final DisplayMetrics displayMetrics = CardProfileActivity.this.getResources().getDisplayMetrics();
                    viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {

                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 400);
                            float dpheight = (displayMetrics.heightPixels / 3) * 2;
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


    public void getMutualFriendIDs(final String userId){

        Bundle params = new Bundle();
        params.putString("fields", "context.fields(mutual_friends)");

        new GraphRequest(AccessToken.getCurrentAccessToken(), "/" + userId, params, HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse graphResponse) {

                        Log.e("RESPONSE", graphResponse.toString());

                        try {
                            ArrayList<String> names = new ArrayList<String>();
                            ArrayList<String> ids = new ArrayList<String>();

                            JSONObject contextObj = graphResponse.getJSONObject().getJSONObject("context");
                            JSONObject mutualFriendsObj = contextObj.getJSONObject("mutual_friends");
                            JSONArray friendData = mutualFriendsObj.getJSONArray("data");

                            for(int i = 0; i < friendData.length(); i++){
                                JSONObject obj = friendData.getJSONObject(i);
                                names.add(obj.getString("name"));
                                ids.add(obj.getString("id"));
                            }

                            getMutualFriendPictures(names, ids);
                        }
                        catch(JSONException e){
                            e.printStackTrace();
                        } catch (NullPointerException n) {
                            n.printStackTrace();
                        }
                    }
                }).executeAsync();
    }

    public void getMutualFriendPictures(ArrayList<String> names, ArrayList<String> ids){
        for(int i = 0; i < names.size() && i < 20; i++){
            mMutualFriendsView.addView(mutualFriendView(ids.get(i), names.get(i), mMutualFriendsView));
            Log.e("ADDING", "MUTUAL FRIEND");
        }
    }

    public View mutualFriendView(String id, String name, ViewGroup parent){
        LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.mutual_friend_card, parent, false);
        v.setBackgroundColor(0xfcf2fa);

        TextView friendName = (TextView) v.findViewById(R.id.mutual_friend_name);
        ImageView friendPic = (ImageView) v.findViewById(R.id.mutual_friend_card_pic);

        friendName.setText(name);

        ExecutorService ex = Executors.newFixedThreadPool(1);
        ex.execute(new PictureRunnable(friendPic, id));

        return v;
    }

    private class PictureRunnable implements Runnable{

        private ImageView mProfileImage;
        private String mId;

        public PictureRunnable(ImageView image, String id){
            mProfileImage = image;
            mId = id;
        }

        @Override
        public void run() {
            final Bitmap bmp;

            try {

                URL url = new URL("https://graph.facebook.com/" + mId + "/picture?type=large");
                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());

                CardProfileActivity.this.runOnUiThread(new Runnable() {
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
}
