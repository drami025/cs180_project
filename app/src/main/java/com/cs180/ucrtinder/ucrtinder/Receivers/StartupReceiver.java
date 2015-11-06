package com.cs180.ucrtinder.ucrtinder.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cs180.ucrtinder.ucrtinder.Services.GeoLocationService;

public class StartupReceiver extends BroadcastReceiver {
    public StartupReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        if (intent.getAction().equals("Action.MainActivity")) {
            context.startService(new Intent(context, GeoLocationService.class));
        }

    }
}
