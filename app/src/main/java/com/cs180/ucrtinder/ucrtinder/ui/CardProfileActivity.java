package com.cs180.ucrtinder.ucrtinder.ui;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cs180.ucrtinder.ucrtinder.FragmentSupport.AndroidDrawer;
import com.cs180.ucrtinder.ucrtinder.Parse.ParseConstants;
import com.cs180.ucrtinder.ucrtinder.R;
import com.cs180.ucrtinder.ucrtinder.tindercard.SwipePhotoAdapter;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 *  Added by Aaron Peery
 *  10-24-2015
 */


public class CardProfileActivity extends AppCompatActivity {

    ParseUser currentUser = ParseUser.getCurrentUser();
    private List<ParseUser> profileUser;
    private ViewPager myPager;

    public static final String KEY_CARDPROFILE = "cardprofile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_card_profile);


        // Get intent with card user string data
        Intent intent = getIntent();
        Bundle b = intent.getBundleExtra(MainActivity.CARD_BUNDLE);
        String userString = b.getString(MainActivity.CARD_USER, "");
        String profileId = b.getString(MainActivity.CARD_ID, "");

        Log.e("PROFILE ID", profileId);

        if(profileId.contains("[")){
            profileId = "";
        }

        getMutualFriendIDs(profileId);

        ParseQuery<ParseUser> mainQuery = ParseUser.getQuery();
        mainQuery.whereEqualTo(ParseConstants.KEY_OBJECTID, userString);

        try {
            profileUser = mainQuery.find();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(profileUser != null) {
            currentUser = profileUser.get(0);
        }

        Toolbar toolbar = (Toolbar)findViewById(R.id.my_toolbar);
        toolbar.setTitle(currentUser.getString(ParseConstants.KEY_NAME));
        setSupportActionBar(toolbar);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException np) {
            np.printStackTrace();
        }

        AndroidDrawer drawer = new AndroidDrawer
                (this,R.id.drawer_layout_profile,R.id.left_drawer_card_profile, R.id.card_profile_drawer_pic);

        TextView text;
        text = (TextView) this.findViewById(R.id.name_textview);
        text.setText(currentUser.getString(ParseConstants.KEY_NAME) + ", " + currentUser.getInt(ParseConstants.KEY_AGE));
        text = (TextView) this.findViewById(R.id.Aboutyou_textview);
        text.setText(currentUser.getString(ParseConstants.KEY_ABOUTYOU));
        text = (TextView) this.findViewById(R.id.interests_textview);
        ArrayList<String> array = (ArrayList<String>)currentUser.get(ParseConstants.KEY_INTERESTS);
        String in = "";

        if(array != null) {
            for (int i = 0; i < array.size(); i++) {
                in = in.concat(array.get(i));
                in = in.concat(", ");
            }
        }
        text.setText(in);

        // Update photos on photoslider
        SwipePhotoAdapter adapter = new SwipePhotoAdapter(KEY_CARDPROFILE, userString);
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
                        }
                    }
                }).executeAsync();
    }

    public void getMutualFriendPictures(ArrayList<String> names, ArrayList<String> ids){

    }
}
