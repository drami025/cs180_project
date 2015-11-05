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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.layer.atlas.Atlas;
import com.layer.atlas.Atlas.Participant;
import com.layer.atlas.Atlas.Tools;
import com.layer.sdk.messaging.Conversation;

/**
 * @author Oleg Orlov
 * @since 23 Apr 2015
 */
public class AtlasConversationSettingsScreen extends Activity {
    private static final String TAG = AtlasConversationSettingsScreen.class.getSimpleName();
    private static final boolean debug = false;

    private static final int REQUEST_CODE_ADD_PARTICIPANT = 999;
    
    public static Conversation conv;
    private ViewGroup namesList;
    
    private View btnLeaveGroup;
    private EditText textGroupName;
    
    private Bitmap maskSingleBmp;
    private Paint avatarPaint = new Paint();
    private Paint maskPaint = new Paint();
    private int avatarBackgroundColor;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.atlas_screen_conversation_settings);
        setupPaints();
        
        btnLeaveGroup = findViewById(R.id.atlas_screen_conversation_settings_leave_group);
        textGroupName = (EditText) findViewById(R.id.atlas_screen_conversation_settings_groupname_text);
        
        View btnAddParticipant = findViewById(R.id.atlas_screen_conversation_settings_add_participant);
        btnAddParticipant.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(AtlasConversationSettingsScreen.this, AtlasParticipantPickersScreen.class);
                final String[] skipUserIds = conv.getParticipants().toArray(new String[0]);
                intent.putExtra(AtlasParticipantPickersScreen.EXTRA_KEY_USERIDS_SKIP, skipUserIds);
                startActivityForResult(intent, REQUEST_CODE_ADD_PARTICIPANT);
            }
        });
        
        this.namesList = (ViewGroup) findViewById(R.id.atlas_screen_conversation_settings_participants_list);
        
        prepareActionBar();
    }
    
    private void setupPaints() {
        maskSingleBmp = Bitmap.createBitmap((int)Tools.getPxFromDp(32, this), (int)Tools.getPxFromDp(32, this), Config.ARGB_8888);
        
        avatarPaint.setAntiAlias(true);
        avatarPaint.setDither(true);
        
        maskPaint.setAntiAlias(true);
        maskPaint.setDither(true);
        maskPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
        
        Paint paintCircle = new Paint();
        paintCircle.setStyle(Style.FILL_AND_STROKE);
        paintCircle.setColor(Color.CYAN);
        paintCircle.setAntiAlias(true);
        
        Canvas maskSingleCanvas = new Canvas(maskSingleBmp);
        maskSingleCanvas.drawCircle(0.5f * maskSingleBmp.getWidth(), 0.5f * maskSingleBmp.getHeight(), 0.5f * maskSingleBmp.getWidth(), paintCircle);
        avatarBackgroundColor = getResources().getColor(R.color.atlas_shape_avatar_gray);
    }

    private void updateValues() {
        
        MessengerApp app101 = (MessengerApp) getApplication();
        
        String conversationTitle = Atlas.getTitle(conv);
        if (conversationTitle != null && conversationTitle.length() > 0) {
            textGroupName.setText(conversationTitle.trim());
        } else {
            textGroupName.setText("");
        }
        
        // refresh names screen
        namesList.removeAllViews();
        
        HashSet<String> userIds = new HashSet<String>(conv.getParticipants());
        userIds.remove(app101.getLayerClient().getAuthenticatedUserId());
        ArrayList<ParticipantEntry> entries = new ArrayList<ParticipantEntry>(userIds.size());
        for (String userId : userIds) {
            Participant participant = app101.getParticipantProvider().getParticipant(userId);
            if (participant == null) continue;
            entries.add(new ParticipantEntry(userId, participant));
        }
        
        Collections.sort(entries, new Comparator<ParticipantEntry>() {
            public int compare(ParticipantEntry lhs, ParticipantEntry rhs) {
                return Participant.COMPARATOR.compare(lhs.participant, rhs.participant);
            }
        });
        
        for (int iContact = 0; iContact < entries.size(); iContact++) {
            View convert = getLayoutInflater().inflate(R.layout.atlas_screen_conversation_settings_participant_convert, namesList, false);
            
            Participant participant = entries.get(iContact).participant;
            
            ImageView avatarImgView = (ImageView) convert.findViewById(R.id.atlas_screen_conversation_settings_convert_avatar_img);
            TextView avaText = (TextView) convert.findViewById(R.id.atlas_screen_conversation_settings_convert_ava);
            Bitmap avatarBmp = Bitmap.createBitmap(maskSingleBmp.getWidth(), maskSingleBmp.getHeight(), Config.ARGB_8888);
            Canvas avatarCanvas = new Canvas(avatarBmp);
            avatarCanvas.drawColor(avatarBackgroundColor);
            Drawable avatarDrawable = participant.getAvatarDrawable();
            if (avatarDrawable != null) {
                avatarDrawable.setBounds(0, 0, avatarBmp.getWidth(), avatarBmp.getHeight());
                avatarDrawable.draw(avatarCanvas);
                avaText.setVisibility(View.GONE);
            } else {
                avaText.setText(Atlas.getInitials(participant));
            }
            avatarCanvas.drawBitmap(maskSingleBmp, 0, 0, maskPaint);
            avatarImgView.setImageBitmap(avatarBmp);
            
            TextView nameText = (TextView) convert.findViewById(R.id.atlas_screen_conversation_settings_convert_name);
            nameText.setText(Atlas.getFullName(participant));
            
            convert.setTag(entries.get(iContact));
            convert.setOnLongClickListener(contactLongClickListener);
            
            namesList.addView(convert);
        }
        
        if (userIds.size() == 1) { // one-on-one
            btnLeaveGroup.setVisibility(View.GONE);
        } else {                        // multi
            btnLeaveGroup.setVisibility(View.VISIBLE);
        }

    }
    
    private static class ParticipantEntry {
        private final String userId;
        private final Participant participant;
        public ParticipantEntry(String userId, Participant participant) {
            if (userId == null || participant == null) throw new IllegalArgumentException("userId and participant cannot be null. userId: " + userId + ", participant: " + participant);
            this.userId = userId;
            this.participant = participant;
        }
    }
    
    private OnLongClickListener contactLongClickListener = new OnLongClickListener() {
        public boolean onLongClick(View v) {
            ParticipantEntry entry = (ParticipantEntry) v.getTag();
            conv.removeParticipants(entry.userId);
            Toast.makeText(v.getContext(), "Removing " + Atlas.getFullName(entry.participant), Toast.LENGTH_LONG).show();
            updateValues();
            return true;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_ADD_PARTICIPANT && resultCode == RESULT_OK) {
            String[] addedParticipants = data.getStringArrayExtra(AtlasParticipantPickersScreen.EXTRA_KEY_USERIDS_SELECTED);
            conv.addParticipants(addedParticipants);
            updateValues();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateValues();
    }
    
    protected void onPause() {
        super.onPause();
        Atlas.setTitle(conv, textGroupName.getText().toString().trim());
    }
    
    private void prepareActionBar() {
        ImageView menuBtn = (ImageView) findViewById(R.id.atlas_actionbar_left_btn);
        menuBtn.setImageResource(R.drawable.atlas_ctl_btn_back);
        menuBtn.setVisibility(View.VISIBLE);
        menuBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
        
        ((TextView)findViewById(R.id.atlas_actionbar_title_text)).setText("Details");
        Tools.setStatusBarColor(getWindow(), getResources().getColor(R.color.atlas_background_blue_dark));
    }

}
