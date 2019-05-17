package com.example.tutorial;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

public class MyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent){
        Intent serviceIntent = new Intent(context,PlayerService.class);
        String action = intent.getAction();
        serviceIntent.setAction(action);
        context.startService(serviceIntent);
    }
}
