package com.example.application.musicplayer.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.application.musicplayer.Model.CreateNotification;

public class NotificationActionService extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Intent newIntent = new Intent("TRACKS_TRACKS");
        if (action.equals(CreateNotification.ACTION_SEEK)) {
            newIntent.putExtra("pos", intent.getExtras().getLong("pos"));
        }
        newIntent.putExtra("actionname", action);
        context.sendBroadcast(newIntent);
    }

}
