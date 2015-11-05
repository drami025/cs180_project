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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.layer.atlas.Atlas.Tools;
import com.layer.atlas.AtlasConversationsList;
import com.layer.atlas.AtlasConversationsList.ConversationClickListener;
import com.layer.atlas.AtlasConversationsList.ConversationLongClickListener;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.query.Predicate;
import com.layer.sdk.query.Query;
import com.layer.sdk.query.SortDescriptor;

/**
 * @author Oleg Orlov
 * @since 14 Apr 2015
 */
public class AtlasConversationsScreen extends Activity {
    private static final String TAG = AtlasConversationsScreen.class.getSimpleName();
    private static final boolean debug = false;

    private static final int REQUEST_CODE_LOGIN_SCREEN = 191;
    private static final int REQUEST_CODE_SETTINGS_SCREEN = 192;
    
    /** Switch it to <code>true</code> to see {@link #AtlasConversationsScreen} Query support in action */
    private static final boolean USE_QUERY = false;

    private MessengerApp app;

    private View btnNewConversation;
    private AtlasConversationsList conversationsList;
    private boolean isInitialized = false;
    private boolean forceLogout = false;
    private boolean showSplash = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.atlas_screen_conversations);
        this.app = (MessengerApp) getApplication();
    }

    private synchronized void initializeViews() {
        if (app.getLayerClient() == null) {
            return;
        }

        if (!isInitialized) {
            this.conversationsList = (AtlasConversationsList) findViewById(R.id.atlas_screen_conversations_conversations_list);
            this.conversationsList.init(app.getLayerClient(), app.getParticipantProvider());
            conversationsList.setClickListener(new ConversationClickListener() {
                public void onItemClick(Conversation conversation) {
                    openChatScreen(conversation, false);
                }
            });
            conversationsList.setLongClickListener(new ConversationLongClickListener() {
                public void onItemLongClick(Conversation conversation) {
//                    conversation.delete(DeletionMode.ALL_PARTICIPANTS);
//                    updateValues();
//                    Toast.makeText(AtlasConversationsScreen.this, "Deleted: " + conversation, Toast.LENGTH_SHORT).show();
                }
            });
            if (USE_QUERY) {
                long monthBeforeMs = System.currentTimeMillis() - (1L * 30 * 24 * 3600 * 1000);
                Query<Conversation> query = Query.builder(Conversation.class)
                        .predicate(new Predicate(Conversation.Property.LAST_MESSAGE_RECEIVED_AT, Predicate.Operator.GREATER_THAN, monthBeforeMs))
                        .sortDescriptor(new SortDescriptor(Conversation.Property.LAST_MESSAGE_RECEIVED_AT, SortDescriptor.Order.DESCENDING))
                        .build();
                conversationsList.setQuery(query);
            } else {
                conversationsList.setQuery(null);
            }

            btnNewConversation = findViewById(R.id.atlas_conversation_screen_new_conversation);
            btnNewConversation.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), AtlasMessagesScreen.class);
                    intent.putExtra(AtlasMessagesScreen.EXTRA_CONVERSATION_IS_NEW, true);
                    startActivity(intent);
                    return;
                }
            });

            prepareActionBar();
            isInitialized = true;
        }
        app.getLayerClient().registerEventListener(conversationsList);
        updateValues();
    }
    
    private void updateValues() {
        conversationsList.updateValues();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_LOGIN_SCREEN && resultCode != RESULT_OK) {
            finish(); // no login - no app
            return;
        }
        if (requestCode == REQUEST_CODE_SETTINGS_SCREEN && resultCode == RESULT_OK) {
            forceLogout = data.getBooleanExtra(AtlasSettingsScreen.EXTRA_FORCE_LOGOUT, false);  // user logged out
            return;
        }
        if (requestCode == IntentIntegrator.REQUEST_CODE && resultCode == RESULT_OK) {
            String qrCodeAppId = IntentIntegrator.parseActivityResult(requestCode, resultCode, data).getContents();
            Log.w(TAG, "Captured App ID: " + qrCodeAppId);
            try {
                app.initLayerClient(qrCodeAppId);
                initializeViews();
            } catch (IllegalArgumentException e) {
                if (debug) Log.w(TAG, "Not a valid Layer QR code app ID: " + qrCodeAppId);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (debug) Log.w(TAG, "onResume()");

        // Logging out?
        if (forceLogout) {
            forceLogout = false;
            Intent intent = new Intent(this, AtlasLoginScreen.class);
            startActivityForResult(intent, REQUEST_CODE_LOGIN_SCREEN);
            return;
        }

        // Initialize the LayerClient
        if ((app.getAppId() != null) && app.getLayerClient() == null) {
            app.initLayerClient(app.getAppId());
        }

        // Can we continue in this Activity?
        if ((app.getAppId() != null) && (app.getLayerClient() != null) && app.getLayerClient().isAuthenticated()) {
            findViewById(R.id.atlas_screen_login_splash).setVisibility(View.GONE);
            initializeViews();
            return;
        }
        
        // Must route.
        if (showSplash) {
            showSplash = false;
            new Handler(Looper.getMainLooper()).postDelayed(
                    new Runnable() {
                        public void run() {
                            route();
                        }
                    }, 3000);
        } else {
            findViewById(R.id.atlas_screen_login_splash).setVisibility(View.GONE);
            route();
        }
    }

    private void route() {
        // Initialize a LayerClient with an App ID
        if (app.getAppId() == null) {
            // Launch QR code activity to capture App ID
            IntentIntegrator integrator = new IntentIntegrator(this)
                    .setCaptureActivity(AtlasQRCaptureScreen.class)
                    .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES)
                    .setPrompt(getResources().getString(R.string.atlas_screen_qr_prompt))
                    .setOrientationLocked(true);
            integrator.initiateScan();
            return;
        } else if (app.getLayerClient() == null) {
            // Use provided App ID to initialize new client
            app.initLayerClient(app.getAppId());
        }
        
        // Optionally launch the login screen
        if ((app.getLayerClient() != null) && (!app.getLayerClient().isAuthenticated() || forceLogout)) {
            forceLogout = false;
            Intent intent = new Intent(this, AtlasLoginScreen.class);
            startActivityForResult(intent, REQUEST_CODE_LOGIN_SCREEN);
            return;
        }
        findViewById(R.id.atlas_screen_login_splash).setVisibility(View.GONE);
        initializeViews();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (app.getLayerClient() != null) {
            app.getLayerClient().unregisterEventListener(conversationsList);
        }
    }

    public void openChatScreen(Conversation conv, boolean newConversation) {
        Context context = this;
        Intent intent = new Intent(context, AtlasMessagesScreen.class);
        intent.putExtra(AtlasMessagesScreen.EXTRA_CONVERSATION_URI, conv.getId().toString());
        startActivity(intent);
    }

    private void prepareActionBar() {
        ((TextView)findViewById(R.id.atlas_actionbar_title_text)).setText("Conversations");
        ImageView menuBtn = (ImageView) findViewById(R.id.atlas_actionbar_left_btn);
        menuBtn.setImageResource(R.drawable.atlas_ctl_btn_menu);
        menuBtn.setVisibility(View.VISIBLE);
        menuBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), AtlasSettingsScreen.class);
                startActivityForResult(intent, REQUEST_CODE_SETTINGS_SCREEN);
            }
        });

        ImageView searchBtn = (ImageView) findViewById(R.id.atlas_actionbar_right_btn);
        searchBtn.setImageResource(R.drawable.atlas_ctl_btn_search);
        searchBtn.setVisibility(View.GONE);
        searchBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Title should be replaced by edit text here...", Toast.LENGTH_LONG).show();
            }
        });
        Tools.setStatusBarColor(getWindow(), getResources().getColor(R.color.atlas_background_blue_dark));
    }
}