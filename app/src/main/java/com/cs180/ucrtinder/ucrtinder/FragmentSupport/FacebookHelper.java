package com.cs180.ucrtinder.ucrtinder.FragmentSupport;

import android.os.Bundle;
import android.util.Log;
import android.util.StringBuilderPrinter;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by daniel on 11/8/15.
 */
public class FacebookHelper {

    String mUserId;

    public FacebookHelper(String id){
        mUserId = id;
    }

    public void getMutualFriendIDs(){

        Bundle params = new Bundle();
        params.putString("fields", "context.fields(mutual_friends)");

        new GraphRequest(AccessToken.getCurrentAccessToken(), "/" + mUserId, params, HttpMethod.GET,
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

