package com.example.tutorial;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

public class MyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (action.equals(Actions.ACTION_RESUME) ||
                action.equals(Actions.ACTION_END) ||
                action.equals(Actions.ACTION_NEXT) ||
                action.equals(Actions.ACTION_PLAY) ||
                action.equals(Actions.ACTION_PAUSE) ||
                action.equals(Actions.ACTION_PREV)) {
            Intent serviceIntent = new Intent(context, PlayerService.class);
            serviceIntent.setAction(action);
            context.startService(serviceIntent);
        }
    }
}
