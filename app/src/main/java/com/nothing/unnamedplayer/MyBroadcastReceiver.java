package com.nothing.unnamedplayer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.util.Log;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class MyBroadcastReceiver extends BroadcastReceiver {
    public MusicManager musicManager = MusicManager.getInstance();
    private final static String TAG = "MyBoradcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        String phoneState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        //MediaPlayer Actions.
        Log.e(TAG,"Received Notification Pending Event.");
        if (action.equals(Actions.ACTION_RESUME) ||
                action.equals(Actions.ACTION_END) ||
                action.equals(Actions.ACTION_NEXT) ||
                action.equals(Actions.ACTION_PLAY) ||
                action.equals(Actions.ACTION_PAUSE) ||
                action.equals(Actions.ACTION_PREV) ||
                action.equals(Actions.ACTION_VIEW)
        ) {
            sendActionToService(context, action);
        }

        //incoming or outgoing calling
        else if (phoneState.equals(TelephonyManager.EXTRA_STATE_RINGING) && musicManager.getMusicPlayer().isPlaying()){
            Log.e(TAG,"Incoming Call");
            musicManager.setInterruptState(true);
            Toast.makeText(context,"Incoming Call",Toast.LENGTH_SHORT).show();
            sendActionToService(context,Actions.ACTION_PAUSE);
        }


        else if (phoneState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK) && musicManager.getMusicPlayer().isPlaying()){
            Log.e(TAG,"Outgoing Call");
            musicManager.setInterruptState(true);
            Toast.makeText(context,"Outgoing Call",Toast.LENGTH_SHORT).show();
            sendActionToService(context,Actions.ACTION_PAUSE);
        }


        else if (phoneState.equals(TelephonyManager.EXTRA_STATE_IDLE))  {
            Log.e(TAG,"Call ended");
            if (musicManager.getInterruptState() == true && !musicManager.getMusicPlayer().isPlaying()){
                Toast.makeText(context,"Resumed",Toast.LENGTH_SHORT).show();
                sendActionToService(context,Actions.ACTION_RESUME);
            }
            musicManager.setInterruptState(false);
        }

    }

    private void sendActionToService(Context context, String action){
        Intent serviceIntent = new Intent(context, PlayerService.class);
        serviceIntent.setAction(action);
        context.startService(serviceIntent);
    }
}
