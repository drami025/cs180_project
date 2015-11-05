/*
 * Copyright (c) 2015 Layer. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.layer.atlas.messenger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.layer.atlas.Atlas;

/**
 * @author Oleg Orlov
 * @since  17 Jul 2015
 */
public class AtlasIdentityProvider implements Atlas.ParticipantProvider {
    private final static String TAG = AtlasIdentityProvider.class.getSimpleName();
    private static final boolean debug = false;

    private static final int REFRESH_TIMEOUT_MILLIS = 60 * 1000;
    
    private final Map<String, Participant> participantsMap = new HashMap<String, Participant>();
    
    private String appId;
    private final Context context;
    
    private Thread refresher;
    private boolean refreshRequired = false;
    private final Object refreshLock = new Object();
    private long lastRefreshMs = System.currentTimeMillis();
    
    public AtlasIdentityProvider(Context context) {
        this.context = context;
        load();
        
        // refreshThread
        refresher = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    synchronized (refreshLock) {
                        long waitMillis = 0;
                        while ( appId == null || ( ! refreshRequired && (waitMillis = lastRefreshMs + REFRESH_TIMEOUT_MILLIS - System.currentTimeMillis()) > 0)) {
                            try {
                                refreshLock.wait(waitMillis);
                            } catch (InterruptedException e) {
                                Log.e(TAG, "refresh() interrupted ", e);
                            }
                        }
                        refreshContacts(false, null, null);
                        lastRefreshMs = System.currentTimeMillis();
                        refreshRequired = false;
                    }
                }
            }
        }, "atlas-contacts-refresher");
        refresher.setDaemon(true);
        refresher.start();
        
    }
    
    @Override
    public Map<String, Atlas.Participant> getParticipants(String filter, Map<String, Atlas.Participant> result) {
        if (result == null) {
            result = new HashMap<String, Atlas.Participant>();
        }

        // With no filter, return all Participants
        if (filter == null) {
            result.putAll(participantsMap);
            return result;
        }

        // Filter participants by substring matching first- and last- names
        for (Participant p : participantsMap.values()) {
            boolean matches = false;
            if (p.firstName != null && p.firstName.toLowerCase().contains(filter)) matches = true;
            if (!matches && p.lastName != null && p.lastName.toLowerCase().contains(filter)) matches = true;
            if (matches) {
                result.put(p.getId(), p);
            } else {
                result.remove(p.getId());
            }
        }
        return result;
    }

    @Override
    public Atlas.Participant getParticipant(String userId) {
        Participant participant = participantsMap.get(userId);
        if (participant == null && appId != null) {
            requestRefresh();
        }
        return participant;
    }

    /** @return String[] { indentityToken (may be null), status/error description } */
    public String[] getIdentityToken(String nonce, String userName) {
        if (appId == null) return new String[] {null, "App ID is not set!"};
        return refreshContacts(true, nonce, userName);
    }
    
    public void requestRefresh() {
        synchronized (refreshLock) {
            refreshRequired = true;
            refreshLock.notifyAll();
        }
    }
    
    public void setAppId(String appId) {
        try {
            UUID.fromString(appId);
        } catch (IllegalArgumentException e){
            throw new IllegalArgumentException("appId must be valid UUID value. appId: " + appId, e);
        }
        synchronized (refreshLock) {
            this.appId = appId;
            refreshRequired = true;
            refreshLock.notifyAll();
        }
    }
    
    private String[] refreshContacts(boolean requestIdentityToken, String nonce, String userName) {
        try {
            String url = "https://layer-identity-provider.herokuapp.com/apps/" + appId + "/atlas_identities";
            HttpPost post = new HttpPost(url);
            post.setHeader("Content-Type", "application/json");
            post.setHeader("Accept", "application/json");
            post.setHeader("X_LAYER_APP_ID", appId);
            
            JSONObject rootObject = new JSONObject();
            if (requestIdentityToken) {
                rootObject.put("nonce", nonce);
                rootObject.put("name", userName);
            } else {
                rootObject.put("name", "Web");  // name must be specified to make entiry valid
            }
            StringEntity entity = new StringEntity(rootObject.toString(), "UTF-8");
            entity.setContentType("application/json");
            post.setEntity(entity);
            
            HttpResponse response = (new DefaultHttpClient()).execute(post);
            if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode() && HttpStatus.SC_CREATED != response.getStatusLine().getStatusCode()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Got status ").append(response.getStatusLine().getStatusCode()).append(" [").append(response.getStatusLine())
                    .append("] when logging in. Request: ").append(url);
                if (requestIdentityToken) sb.append(" login: ").append(userName).append(", nonce: ").append(nonce);
                Log.e(TAG, sb.toString());
                return new String[] {null, sb.toString()};
            }

            String responseString = EntityUtils.toString(response.getEntity());
            JSONObject jsonResp = new JSONObject(responseString);
            
            JSONArray atlasIdentities = jsonResp.getJSONArray("atlas_identities");
            List<Participant> participants = new ArrayList<Participant>(atlasIdentities.length());
            for (int i = 0; i < atlasIdentities.length(); i++) {
                JSONObject identity = atlasIdentities.getJSONObject(i);
                Participant participant = new Participant();
                participant.firstName = identity.getString("name");
                participant.userId = identity.getString("id");
                participants.add(participant);
            }
            if (participants.size() > 0) {
                setParticipants(participants);
                save();
                if (debug) Log.d(TAG, "refreshContacts() contacts: " + atlasIdentities);
            }
            
            if (requestIdentityToken) {
                String error = jsonResp.optString("error", null);
                String identityToken = jsonResp.optString("identity_token");
                return new String[] {identityToken, error};
            }
            return new String[] {null, "Refreshed " + participants.size() + " contacts"};
        } catch (Exception e) {
            Log.e(TAG, "Error when fetching identity token", e);
            return new String[] {null, "Cannot obtain identity token. " + e};
        }
    }
    
    /**
     * Overwrites the current list of Contacts with the provided list.
     *
     * @param participants New list of Contacts to apply.
     */
    private void setParticipants(List<Participant> participants) {
        synchronized (participantsMap) {
            participantsMap.clear();
            for (Participant participant : participants) {
                participantsMap.put(participant.userId, participant);
            }
        }
    }

    private boolean load() {
        String jsonString = context.getSharedPreferences("contacts", Context.MODE_PRIVATE).getString("json", null);
        if (jsonString == null) return false;

        List<Participant> participants;
        try {
            JSONArray contactsJson = new JSONArray(jsonString);
            participants = new ArrayList<Participant>(contactsJson.length());
            for (int i = 0; i < contactsJson.length(); i++) {
                JSONObject contactJson = contactsJson.getJSONObject(i);
                Participant participant = new Participant();
                participant.userId = contactJson.optString("id");
                participant.firstName = contactJson.optString("first_name");
                participant.lastName = contactJson.optString("last_name");
                participants.add(participant);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error while saving", e);
            return false;
        }

        setParticipants(participants);

        return true;
    }

    private boolean save() {
        Collection<Participant> participants;
        synchronized (participantsMap) {
            participants = participantsMap.values();
        }

        JSONArray contactsJson;
        try {
            contactsJson = new JSONArray();
            for (Participant participant : participants) {
                JSONObject contactJson = new JSONObject();
                contactJson.put("id", participant.userId);
                contactJson.put("first_name", participant.firstName);
                contactJson.put("last_name", participant.lastName);
                contactsJson.put(contactJson);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error while saving", e);
            return false;
        }

        SharedPreferences.Editor editor = context.getSharedPreferences("contacts", Context.MODE_PRIVATE).edit();
        editor.putString("json", contactsJson.toString());
        editor.commit();

        return true;
    }
    
    public class Participant implements Atlas.Participant {
        public String userId;
        public String firstName;
        public String lastName;
        
        public String getId() {
            return userId;
        }

        @Override
        public String getFirstName() {
            return firstName;
        }

        @Override
        public String getLastName() {
            return lastName;
        }
        
        @Override
        public Drawable getAvatarDrawable() {
            return null;
        }
        
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Contact [userId: ").append(userId).append(", firstName: ").append(firstName).append(", lastName: ").append(lastName).append("]");
            return builder.toString();
        }
    }

}
