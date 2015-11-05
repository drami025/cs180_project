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
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.layer.sdk.LayerClient;
import com.layer.sdk.exceptions.LayerException;
import com.layer.sdk.listeners.LayerAuthenticationListener;

/**
 * @author Oleg Orlov
 * @since 24 Apr 2015
 */
public class AtlasLoginScreen extends Activity {
    private static final String TAG = AtlasLoginScreen.class.getSimpleName();
    private static final boolean debug = false;
    
    private volatile boolean inProgress = false;
    private EditText loginText;
    private View goButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.atlas_screen_login);
        
        loginText = (EditText) findViewById(R.id.atlas_screen_login_username);
        goButton = findViewById(R.id.atlas_screen_login_go_btn);

        final TextView.OnEditorActionListener doneListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    login();
                    return true;
                }
                return false;
            }
        };
        loginText.setOnEditorActionListener(doneListener);
        
        goButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                login();
            }
        });

        loginText.requestFocus();
        
    }

    private void updateValues() {
        if (inProgress) {
            if (goButton.getVisibility() == View.VISIBLE) {
                goButton.setEnabled(false);
            }
        } else {
            if (goButton.getVisibility() == View.VISIBLE) {
                goButton.setEnabled(true);
            }
        }
    }

    private void login() {
        final MessengerApp app = (MessengerApp)getApplication();
        final LayerClient layerClient = app.getLayerClient();
        final AtlasIdentityProvider identityProvider = app.getIdentityProvider();
        
        final String userName = loginText.getText().toString().trim();
        
        if (userName.isEmpty()) return;
        inProgress = true;

        layerClient.registerAuthenticationListener(new LayerAuthenticationListener() {
            public void onAuthenticationChallenge(final LayerClient client, final String nonce) {
                if (debug) Log.w(TAG, "onAuthenticationChallenge() nonce: " + nonce);
                new Thread(new Runnable() {
                    public void run() {
                        String[] tokenAndError = new String[] {null, null};
                        try {
                            tokenAndError = identityProvider.getIdentityToken(nonce, userName);
                        } catch (Exception e) {
                            tokenAndError[1] = "Unexpected error, " + e;
                            Log.e(TAG, "onAuthenticationChallenge() Unexpected error. ", e);
                        }
                        final String token = tokenAndError[0];
                        final String error = tokenAndError[1];
                        if (error != null || token == null) {
                            inProgress = false;
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    updateValues();
                                    Toast.makeText(AtlasLoginScreen.this, error, Toast.LENGTH_LONG).show();
                                }
                            });
                            return;
                        }
                        layerClient.answerAuthenticationChallenge(token);
                    }
                }).start();
            }

            public void onAuthenticated(LayerClient client, String userId) {
                if (debug) Log.w(TAG, "onAuthenticated() userID: " + userId);
                inProgress = false;
                updateValues();
                setResult(RESULT_OK);
                client.unregisterAuthenticationListener(this);
                finish();
            }

            public void onDeauthenticated(LayerClient client) {
                if (debug) Log.e(TAG, "onDeauthenticated() ");
            }

            public void onAuthenticationError(LayerClient client, final LayerException exception) {
                Log.e(TAG, "onAuthenticationError() ", exception);
                inProgress = false;
                updateValues();
                client.unregisterAuthenticationListener(this);
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(AtlasLoginScreen.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        layerClient.authenticate();
        updateValues();
    }
}
