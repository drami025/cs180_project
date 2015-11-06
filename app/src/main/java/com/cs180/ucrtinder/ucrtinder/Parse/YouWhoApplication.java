package com.cs180.ucrtinder.ucrtinder.Parse;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.util.Log;
import android.widget.Toast;

import com.cs180.ucrtinder.ucrtinder.Messenger.AtlasIdentityProvider;
import com.layer.atlas.Atlas;
import com.layer.sdk.LayerClient;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

/**
 * Created by bananapanda on 10/22/15.
 */
public class YouWhoApplication extends Application {

    //==============================================================================================
    // LAYER CONFIGURATION
    //==============================================================================================

    // 1. Set your Layer App ID from the Developer Console to bypass the QR code flow
    public static final String LAYER_APP_ID = "layer:///apps/staging/46a55db8-7e70-11e5-9763-b381840a00ad";

    // 2. Optionally replace the Google Cloud Messaging Sender ID (in the Developer Console too)
    private static final String GCM_SENDER_ID = "748607264448"; // Set your GCM Sender ID

    //==============================================================================================

    private static final String TAG = YouWhoApplication.class.getSimpleName();
    private static final boolean debug = false;

    private LayerClient layerClient;
    private AtlasIdentityProvider identityProvider;
    private String appId = LAYER_APP_ID;



    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "o66AszQHjZRyH1nY7XMeHL9t9oAKeSe5hWDfRYEH", "9lo4BUYpKJGmNTN6jtoPD8LoYN4R1m7TKHIaDxYG");

        ParseFacebookUtils.initialize(this);

        // Messaging system through Layer.com
        LayerClient.enableLogging(this);
        LayerClient.applicationCreated(this);
        if (this.appId == null) {
            this.appId = loadAppId();
        }
        this.identityProvider = new AtlasIdentityProvider(this);

    }


    public interface keys {
        String CONVERSATION_URI = "conversation.uri";
    }

    public LayerClient getLayerClient() {
        return layerClient;
    }

    /**
     * Initializes a new LayerClient if needed, or returns the already-initialized LayerClient.
     *
     * @param appIdString Layer App ID to initialize a LayerClient with
     * @return The newly initialized LayerClient, or the existing LayerClient
     */
    public LayerClient initLayerClient(final String appIdString) {
        if (layerClient != null) return layerClient;

        String appId = appIdString;
        if (appIdString.startsWith("layer:///")) {
            identityProvider.setAppId(UUID.fromString(appId.substring(appId.lastIndexOf("/") + 1)).toString());
        } else {
            identityProvider.setAppId(appIdString);
            appId = "layer:///apps/staging/" + UUID.fromString(appIdString).toString();
        }

        final LayerClient client = LayerClient.newInstance(this, appId, new LayerClient.Options()
                .broadcastPushInForeground(false)
                .googleCloudMessagingSenderId(GCM_SENDER_ID));
        if (debug) Log.w(TAG, "onCreate() client created");

        setAppId(appIdString);
        layerClient = client;

        if (!client.isAuthenticated()) client.authenticate();
        else if (!client.isConnected()) client.connect();
        if (debug) Log.w(TAG, "onCreate() Layer launched");

        identityProvider.requestRefresh();

        return layerClient;
    }

    public Atlas.ParticipantProvider getParticipantProvider() {
        return identityProvider;
    }

    public AtlasIdentityProvider getIdentityProvider() {
        return identityProvider;
    }

    public void setAppId(String appId) {
        this.appId = appId;
        getSharedPreferences("app", MODE_PRIVATE).edit().putString("appId", appId).commit();
    }

    private String loadAppId() {
        return getSharedPreferences("app", MODE_PRIVATE).getString("appId", null);
    }

    public String getAppId() {
        return appId;
    }

    /**
     * Converts a Bundle to the human readable string.
     *
     * @param bundle the collection for example, {@link java.util.ArrayList}, {@link java.util.HashSet} etc.
     * @return the converted string
     */
    public static String toString(Bundle bundle) {
        return toString(bundle, ", ", "");
    }

    public static String toString(Bundle bundle, String separator, String firstSeparator) {
        if (bundle == null) return "null";
        StringBuilder sb = new StringBuilder("[");
        int i = 0;
        for (Iterator<String> itKey = bundle.keySet().iterator(); itKey.hasNext(); i++) {
            String key = itKey.next();
            sb.append(i == 0 ? firstSeparator : separator).append(i).append(": ");
            sb.append(key).append(" : ").append(bundle.get(key));
        }
        sb.append("]");
        return sb.toString();
    }

    //==============================================================================================
    private static final String EXTRA_PARAM_ID = "YouWhoApplication.param.id";
    private static int nextParamId = 0;
    private final HashMap<Integer, Object> paramMap = new HashMap<Integer, Object>();

    public void setParam(Intent to, Object what) {
        synchronized (paramMap) {
            int paramId = nextParamId++;
            paramMap.put(paramId, what);
            to.putExtra(EXTRA_PARAM_ID, paramId);
        }
    }
    public Object getParam(Intent from) {
        synchronized (paramMap) {
            int paramId = from.getIntExtra(EXTRA_PARAM_ID, -1);
            Object result = paramId != -1 ? paramMap.get(Integer.valueOf(paramId)) : null;
            return result;
        }
    }

    @Override
    protected void attachBaseContext(Context base){
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
