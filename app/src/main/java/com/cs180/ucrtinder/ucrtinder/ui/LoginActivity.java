package com.cs180.ucrtinder.ucrtinder.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cs180.ucrtinder.ucrtinder.R;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestBatch;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.google.gson.Gson;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoginActivity extends FragmentActivity {

    private EditText mUser;
    private EditText mPass;
    private LoginActivity mActivity;
    private Profile mProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button signInButton = (Button) findViewById(R.id.signInButton);
        mUser = (EditText) findViewById(R.id.emailEditText);
        mPass = (EditText) findViewById(R.id.passwordEditText);
        mActivity = this;

        mProfile = Profile.getCurrentProfile();

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<String> mPermissions = Arrays.asList("user_photos", "public_profile", "email");

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
        });
    }

    public void loginSuccessful(final boolean isNewUser){

        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(final JSONObject jsonObject, GraphResponse graphResponse) {

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
        params.putString("fields", "name,picture,birthday,id,gender,link");
        request.setParameters(params);
        request.executeAsync();

    }

    public void getUserDetailsFromFB(JSONObject json){
        ParseUser user = ParseUser.getCurrentUser();

        Log.e("CHECK" , json.toString());

        try{
            String gender = json.getString("gender");
            String name = json.getString("name");
            String id = json.getString("id");

            String picture_url = json.getJSONObject("picture").getJSONObject("data").getString("url");

            user.put("gender", gender);
            user.put("name", name);
            user.put("id", id);
            user.put("profile_picture_url", picture_url);

            Log.e("FIELDS", gender + " " + name + " " + id + " " + picture_url);

            user.saveInBackground();
        }
        catch(JSONException e){
            e.printStackTrace();
        }


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

    class ProfilePhotoAsync extends AsyncTask<String, String, String> {
        Profile profile;
        public Bitmap bitmap;

        public ProfilePhotoAsync(Profile profile) {
            this.profile = profile;
        }

        @Override
        protected String doInBackground(String... params) {
            // Fetching data from URI and storing in bitmap
            bitmap = DownloadImageBitmap(profile.getProfilePictureUri(200, 200).toString());

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //mProfileImage.setImageBitmap(bitmap);
        }
    }

    public static Bitmap DownloadImageBitmap(String url) {
        Bitmap bm = null;
        try {
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        } catch (IOException e) {
            Log.e("IMAGE", "Error getting bitmap", e);
        }
        return bm;
    }
}
