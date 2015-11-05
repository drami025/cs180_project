package com.cs180.ucrtinder.ucrtinder.ui;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.cs180.ucrtinder.ucrtinder.Parse.ParseConstants;
import com.cs180.ucrtinder.ucrtinder.R;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends FragmentActivity {

    private LoginActivity mActivity;
    private Profile mProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button signInButton = (Button) findViewById(R.id.signInButton);
        mActivity = this;
        beginLogin();

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                beginLogin();
            }
        });
    }

    public void beginLogin(){
        mProfile = Profile.getCurrentProfile();

//        if(mProfile != null){
//            Intent intent = new Intent(mActivity, MainActivity.class);
//            startActivity(intent);
//            return;
//        }

        List<String> mPermissions = Arrays.asList("user_friends", "user_photos", "user_birthday", "email", "user_about_me", "user_photos" , "public_profile", "email");

        ParseFacebookUtils.logInWithReadPermissionsInBackground(mActivity, mPermissions, new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if(parseUser == null){
                    Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
                }
                else if(parseUser.isNew()){
                    Log.d("MyApp", "User signed up and logged in through Facebook!");
                    loginSuccessful(true);
                }
                else{
                    Log.d("MyApp", "User logged in through Facebook!");
                    loginSuccessful(false);
                }
            }
        });
    }

    public void loginSuccessful(final boolean isNewUser){

        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(final JSONObject jsonObject, GraphResponse graphResponse) {

                        Log.e("RESPONSE", jsonObject.toString());


                        //if(isNewUser) {
                            getUserDetailsFromFB(jsonObject);
                        //}

                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(mActivity, MainActivity.class);
                                intent.putExtra("user_data", jsonObject.toString());
                                startActivity(intent);
                            }
                        });
                    }
                }
        );

        Bundle params = new Bundle();
        params.putString("fields", "name,picture,birthday,id,gender,link,photos");
        request.setParameters(params);
        request.executeAsync();
    }

    public void getUserDetailsFromFB(JSONObject json){
        ParseUser user = ParseUser.getCurrentUser();
        mProfile = Profile.getCurrentProfile();

        Log.e("CHECK" , json.toString());

        try{
            String gender = json.getString("gender");
            String name = mProfile.getFirstName();
            String id = json.getString("id");
            String birthday = json.getString("birthday");

            String picture_url = json.getJSONObject("picture").getJSONObject("data").getString("url");
            String main_picture = mProfile.getProfilePictureUri(300, 300).toString();

            user.put("gender", gender);
            user.put(ParseConstants.KEY_NAME, name);
            user.put("facebookId", id);
            user.put("profile_picture_url", main_picture);
            user.put("birthday", birthday);

            boolean is_looking_for_men = (gender.equals("female"));

            user.put(ParseConstants.KEY_MEN, is_looking_for_men);
            user.put(ParseConstants.KEY_WOMEN, !is_looking_for_men);

            user.put(ParseConstants.KEY_SMALLESTAGE, 18);
            user.put(ParseConstants.KEY_LARGESTAGE, 30);

            user.put(ParseConstants.KEY_DISTANCE, 20);

            Log.e("FIELDS", gender + " " + name + " " + id + " " + picture_url + " " + birthday);

            user.saveInBackground();

            String photoIdOne = json.getJSONObject("photos").getJSONArray("data").getJSONObject(0).getString("id");
            String photoIdTwo = json.getJSONObject("photos").getJSONArray("data").getJSONObject(1).getString("id");

            getPhoto(photoIdOne, "secondary_photo");
            getPhoto(photoIdTwo, "tertiary_photo");

        }
        catch(JSONException e){
            e.printStackTrace();
        }


    }

    public void getPhoto(String photoId, final String key){
        GraphRequest request = new GraphRequest(AccessToken.getCurrentAccessToken(),
                "/" + photoId, null, HttpMethod.GET, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {
                ParseUser user = ParseUser.getCurrentUser();

                try{
                    String photoUrl = graphResponse.getJSONObject().getString("source");
                    Log.e("PHOTO URL", photoUrl);
                    user.put(key, photoUrl);
                    user.saveInBackground();
                }
                catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });

        Bundle params = new Bundle();
        params.putString("fields", "source");
        request.setParameters(params);
        request.executeAsync();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent mActivity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }
}
