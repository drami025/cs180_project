package com.cs180.ucrtinder.ucrtinder.ui;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.cs180.ucrtinder.ucrtinder.Parse.YouWhoApplication;
import com.cs180.ucrtinder.ucrtinder.R;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class IGWebActivity extends AppCompatActivity {

    private YouWhoApplication mApp;
    private String mRequestToken;
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_igweb);

        mWebView = (WebView) findViewById(R.id.ig_webview);
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setWebViewClient(new AuthWebViewClient());
        //mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(mApp.IG_AUTHURL);
        Log.e("HERE", "CHECK THIS");

    }

    public class AuthWebViewClient extends WebViewClient
    {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url)
        {
            if (url.startsWith(mApp.IG_CALLBACK_URL))
            {
                System.out.println(url);
                String parts[] = url.split("=");
                mRequestToken = parts[1];  //This is your request token.

                Log.e("TOKEN", mRequestToken);
                //InstagramLoginDialog.this.dismiss();

                new InstagramAsync().execute();

                return true;
            }
            return false;
        }
    }

    private class InstagramAsync extends AsyncTask<Object, Object, Object>{

        @Override
        protected Void doInBackground(Object...objs) {

            try
            {
                URL url = new URL(mApp.IG_TOKENURL);
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
                httpsURLConnection.setRequestMethod("POST");
                httpsURLConnection.setDoInput(true);
                httpsURLConnection.setDoOutput(true);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpsURLConnection.getOutputStream());
                outputStreamWriter.write("client_id="+ mApp.IG_CLIENT_ID +
                        "client_secret="+ mApp.IG_CLIENT_SECRET +
                        "grant_type=authorization_code" +
                        "redirect_uri="+ mApp.IG_CALLBACK_URL +
                        "code=" + mRequestToken);
                outputStreamWriter.flush();

                BufferedReader rd = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();

                String line = null;
                while((line = rd.readLine()) != null){
                    sb.append(line + "\n");
                }

                String response = sb.toString();
                JSONObject jsonObject = (JSONObject) new JSONTokener(response).nextValue();
                String accessTokenString = jsonObject.getString("access_token"); //Here is your ACCESS TOKEN
                String id = jsonObject.getJSONObject("user").getString("id");
                String username = jsonObject.getJSONObject("user").getString("username");

                ParseUser user = ParseUser.getCurrentUser();

                user.put("IG_ACCESSTOKEN", accessTokenString);
                user.put("IG_ID", id);
                user.put("IG_USERNAME", username);

                Log.e("JSON_IG", accessTokenString + " " + id + " " + username);
                user.saveInBackground();
                //This is how you can get the user info.
                //You can explore the JSON sent by Instagram as well to know what info you got in a response
            }catch (JSONException e)
            {
                Log.e("IG_EXCEPT", "JSON");
                e.printStackTrace();
            }
            catch(ProtocolException e){
                Log.e("IG_EXCEPT", "PROTOCOL");
                e.printStackTrace();
            }
            catch(MalformedURLException e){
                Log.e("IG_EXCEPT", "URL");
                e.printStackTrace();
            }
            catch(IOException e){
                Log.e("IG_EXCEPT", "IO");
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_igweb, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
