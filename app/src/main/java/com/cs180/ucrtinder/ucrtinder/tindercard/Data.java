package com.cs180.ucrtinder.ucrtinder.tindercard;

import android.os.Bundle;
import android.util.Log;

import com.cs180.ucrtinder.ucrtinder.FragmentSupport.OnCardsLoadedListener;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nirav on 05/10/15.
 */
public class Data {

    private String description;
    private String imagePath;
    private String UserString;
    private String id;
    private String mutualFriends;
    private OnCardsLoadedListener mCallback;
    private int mPosition;
    private boolean hasCount = false;

    public Data(String imagePath, String description, String userString, String id, OnCardsLoadedListener listener, int position) {
        this.imagePath = imagePath;
        this.description = description;
        this.UserString = userString;
        this.id = id;
        this.mutualFriends = "0";
        mCallback = listener;
        mPosition = position;

        if(id != null && !hasCount) {
            getMutualFriends();
        }
    }

    public String getDescription() {
        return description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getUserString() {
        return UserString;
    }

    public void getMutualFriends(){
        Bundle params = new Bundle();
        params.putString("fields", "context.fields(mutual_friends)");

        new GraphRequest(AccessToken.getCurrentAccessToken(), "/" + id, params, HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse graphResponse) {

                        Log.e("RESPONSE", graphResponse.toString());

                        try {
                            JSONObject contextObj = graphResponse.getJSONObject().getJSONObject("context");
                            JSONObject mutualFriendsObj = contextObj.getJSONObject("mutual_friends");
                            JSONObject summaryObj = mutualFriendsObj.getJSONObject("summary");
                            mutualFriends = summaryObj.getInt("total_count") + "";
                        }
                        catch(JSONException e){
                            e.printStackTrace();
                        }

                        mCallback.onCardsLoaded();
                        hasCount = true;
                    }
                }).executeAsync();
    }

    public String getMutualFriendCount(){
        return mutualFriends;
    }

    public String getID(){
        return id;
    }
}
