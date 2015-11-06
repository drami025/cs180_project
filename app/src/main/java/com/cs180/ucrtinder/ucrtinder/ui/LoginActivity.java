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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        if(mProfile != null){
            Intent intent = new Intent(mActivity, MainActivity.class);
            startActivity(intent);
            return;
        }

        Log.e("LOGIN", "RETRIEVING INFO FROM FB");

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

                        getUserDetailsFromFB(jsonObject);

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
        params.putString("fields", "name,picture,birthday,id,gender,link,photos,albums");
        request.setParameters(params);
        request.executeAsync();
    }

    public void getUserDetailsFromFB(JSONObject json){
        ParseUser user = ParseUser.getCurrentUser();
        //mProfile = Profile.getCurrentProfile();

        Log.e("CHECK" , json.toString());

        try{
            String gender = json.getString("gender");
            String name = json.getString("name");
            String firstName = name.substring(0, name.indexOf(' '));
            String id = json.getString("id");
            String birthday = json.getString("birthday");

            user.put("gender", gender);
            user.put(ParseConstants.KEY_NAME, firstName);
            user.put("facebookId", id);
            user.put("birthday", birthday);

            int age = getAge(birthday);
            Log.e("AGE", age + "");
            user.put("age", age);

            boolean is_looking_for_men = (gender.equals("female"));

            user.put(ParseConstants.KEY_MEN, is_looking_for_men);
            user.put(ParseConstants.KEY_WOMEN, !is_looking_for_men);

            user.put(ParseConstants.KEY_SMALLESTAGE, 18);
            user.put(ParseConstants.KEY_LARGESTAGE, 30);

            user.put(ParseConstants.KEY_DISTANCE, 20);

            Log.e("FIELDS", gender + " " + firstName + " " + id + " " + birthday);

            user.saveInBackground();

            JSONArray profileArray = json.getJSONObject("albums").getJSONArray("data");

            String profileAlbumID = "";

            for(int i = 0; i < profileArray.length(); i++) {
                JSONObject obj = profileArray.getJSONObject(i);

                if (obj.getString("name").toLowerCase().equals("profile pictures")) {
                    profileAlbumID = obj.getString("id");
                    break;
                }
            }

            Log.e("ALBUM ID" , profileAlbumID);
            getPhotosFromAlbum(profileAlbumID);

        }
        catch(JSONException e){
            e.printStackTrace();
        }
        catch(java.text.ParseException e){
            e.printStackTrace();
        }
    }

    public int getAge(String birthday) throws java.text.ParseException{
        DateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
        Date birthDate = format.parse(birthday);

        Date today = new Date();

        long diffAsLong = today.getTime() - birthDate.getTime();
        Calendar diffAsCalendar = Calendar.getInstance();
        diffAsCalendar.setTimeInMillis(diffAsLong);
        return (diffAsCalendar.get(Calendar.YEAR) - 1970);
    }

    public void getPhotosFromAlbum(String id){
        new GraphRequest(AccessToken.getCurrentAccessToken(),
                "/" + id + "/photos", null, HttpMethod.GET, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {
                Log.e("ALBUM RESPONSE", graphResponse.toString());

                try {
                    JSONObject jsonObject = graphResponse.getJSONObject();
                    JSONArray jsonArray = jsonObject.getJSONArray("data");

                    for(int i = 0; i < jsonArray.length() && i < 3; i++){
                        String photoId = jsonArray.getJSONObject(i).getString("id");
                        getPhoto(photoId, "photo" + i);
                    }
                }
                catch(JSONException e){
                    e.printStackTrace();
                }
            }
        }).executeAsync();
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
