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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.layer.atlas.Atlas;
import com.layer.atlas.messenger.MessengerApp.keys;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Conversation;

/**
 * @author Oleg Orlov
 */
public class MessengerPushReceiver extends BroadcastReceiver {
    
    private static final String TAG = MessengerPushReceiver.class.getSimpleName();
    private static final boolean debug = false;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (debug) Log.w(TAG, "onReceive() action: " + intent.getAction() + ", extras: " + MessengerApp.toString(intent.getExtras(), "\n", "\n"));
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            if (debug) Log.w(TAG, "onReceive() Waking Up! due to action: "  + intent.getAction());
            return;
        }
        
        NotificationManager notificationService = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        String text = intent.getStringExtra("layer-push-message");
        Uri conversationId = (Uri) intent.getExtras().get("layer-conversation-id");
        String title = getTitle(context, conversationId);
        if (title == null) title = context.getResources().getString(R.string.app_name);

        Notification.Builder bld = new Notification.Builder(context);
        bld.setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher)
            .setAutoCancel(true)
            .setLights(Color.rgb(0, 255, 0), 100, 1900)
            .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE)
            ;
        
        Intent chatIntent = new Intent(context, AtlasMessagesScreen.class);
        chatIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        chatIntent.putExtra(keys.CONVERSATION_URI, conversationId.toString());

        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                context, 0, chatIntent, PendingIntent.FLAG_ONE_SHOT);
        
        bld.setContentIntent(resultPendingIntent);
        
        final Notification notification = bld.getNotification();
        
        try {
            // Group notifications by Conversation
            notificationService.notify(conversationId.hashCode(), notification);
        } catch (SecurityException ignored) { 
            // 4.1.2 device required VIBRATE permission when in Vibrate mode. 
            // Fixed in 4.2.1 https://android.googlesource.com/platform/frameworks/base/+/cc2e849
        }
    }

    private String getTitle(Context context, final Uri conversationId) {
        Context appContext = context.getApplicationContext();
        if (!(appContext instanceof MessengerApp)) return null;

        MessengerApp app = (MessengerApp)appContext;
        final LayerClient client = app.initLayerClient(app.getAppId());
        if (client == null || !client.isAuthenticated()) return null;
        
        Conversation conversation = client.getConversation(conversationId);
        if (conversation != null) return Atlas.getTitle(conversation, app.getParticipantProvider(), client.getAuthenticatedUserId());
        
        return null;
    }
}
