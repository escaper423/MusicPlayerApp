package com.nothing.unnamedplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.Html;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.nothing.unnamedplayer.R;

import java.io.IOException;



public class PlayerService extends Service {
    private final static String TAG = "PlayerService";

    private MusicManager musicManager;
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private NotificationManagerCompat notificationManager;
    private EarphoneBroadcastReceiver earphoneBroadcastReceiver;

    float actVolume, curVolume, maxVolume;

    @Override
    public void onCreate() {
        Log.e(TAG,"onCreate");
        super.onCreate();
        notificationManager = NotificationManagerCompat.from(this);
        musicManager = MusicManager.getInstance();
        mediaPlayer = musicManager.getMusicPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (!mp.isLooping()) {
                    playNextMusic();
                }
            }
        });

        earphoneBroadcastReceiver = new EarphoneBroadcastReceiver();

        IntentFilter earphoneFilter = new IntentFilter();
        earphoneFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        earphoneFilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiver(earphoneBroadcastReceiver, earphoneFilter);

    }



    private class EarphoneBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            AudioDeviceInfo[] deviceInfo = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
            Log.e(TAG, "Bluetooth or Earphone state");
            boolean hasPlugged = false;

            String action = intent.getAction();
            //Wired Headset Plug State Changed.
            for(AudioDeviceInfo device: deviceInfo){
                if (device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADSET){
                    hasPlugged = true;
                    break;
                }
            }

            //Bluetooth Connection State Changed.
            if (!hasPlugged && action.equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)){
                int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE,0);
                if (state == BluetoothHeadset.STATE_CONNECTED || state == BluetoothHeadset.STATE_CONNECTING) {
                    hasPlugged = true;
                }
            }

            //both type of earphones are disconnected then pause.
            if (!hasPlugged && mediaPlayer.isPlaying()){
                pauseMusic(musicManager.getMusicByIndex(musicManager.getCurrentIndex()));
                Log.e(TAG, "Earphone plugged off");
            }
        }
    }

    private void playMusic(final Music m) {
        //Volume Control
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        actVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        curVolume = actVolume / maxVolume;

        Uri uri = Uri.parse(m.getMusicPath());
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(this, uri);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(musicManager.getPlaybackSpeed()));
                    mp.start();
                    Log.e(TAG,"musicPlayer Really Plays");
                    setDisplay(m);
                    sendDisplayUpdate("Play");
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendDisplayUpdate(final String status){
        Intent intent = new Intent(Actions.ACTION_UPDATE);
        intent.putExtra("Status",status);
        sendBroadcast(intent);
    }

    private void playPrevMusic() {
        int ci = musicManager.getCurrentIndex();
        ci -= 1;
        if (ci == -1) {
            ci = musicManager.getMusicListSize() - 1;
        }
        musicManager.setCurrentIndex(ci);

        if (mediaPlayer.isLooping()){
            musicManager.setMediaPlayerLooping(false);
        }
        playMusic(musicManager.getMusicByIndex(musicManager.getCurrentIndex()));
    }

    private void playNextMusic() {
        musicManager.setCurrentIndex((musicManager.getCurrentIndex() + 1) % musicManager.getMusicListSize());

        if (mediaPlayer.isLooping())
            musicManager.setMediaPlayerLooping(false);

        playMusic(musicManager.getMusicByIndex(musicManager.getCurrentIndex()));
    }
    private Notification createNotificationWithStatus(Music m) {
        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_service);

        Intent pauseIntent, resumeIntent, nextIntent, prevIntent, endIntent, viewIntent;
        PendingIntent pPause, pNext, pPrev, pEnd, pResume, pView;
        Log.e(TAG,"Creating Notification");
        //Resume player
        if (mediaPlayer.isPlaying()) {
            pauseIntent = new Intent(this, MyBroadcastReceiver.class).setAction(Actions.ACTION_PAUSE);
            pPause = PendingIntent.getBroadcast(this, 0, pauseIntent, 0);

            nextIntent = new Intent(this, MyBroadcastReceiver.class).setAction(Actions.ACTION_NEXT);
            pNext = PendingIntent.getBroadcast(this, 0, nextIntent, 0);

            prevIntent = new Intent(this, MyBroadcastReceiver.class).setAction(Actions.ACTION_PREV);
            pPrev = PendingIntent.getBroadcast(this, 0, prevIntent, 0);

            endIntent = new Intent(this, MyBroadcastReceiver.class).setAction(Actions.ACTION_END);
            pEnd = PendingIntent.getBroadcast(this, 0, endIntent, 0);

            viewIntent = new Intent(this, MyBroadcastReceiver.class).setAction(Actions.ACTION_VIEW);
            pView = PendingIntent.getBroadcast(this,0,viewIntent,0);

            notificationLayout.setTextViewText(R.id.notification_title, m.getMusicTitle());
            notificationLayout.setTextViewText(R.id.notification_artist, m.getMusicArtist());
            notificationLayout.setImageViewBitmap(R.id.notification_image, musicManager.getBitmapFromMusicPath(m.getMusicPath()));
            notificationLayout.setImageViewResource(R.id.notification_play, R.drawable.ic_pause_black_24dp);
            notificationLayout.setOnClickPendingIntent(R.id.notification_prev, pPrev);
            notificationLayout.setOnClickPendingIntent(R.id.notification_next, pNext);
            notificationLayout.setOnClickPendingIntent(R.id.notification_play, pPause);
            notificationLayout.setOnClickPendingIntent(R.id.notification_end, pEnd);
            notificationLayout.setOnClickPendingIntent(R.id.notification_image, pView);
        }
        //Pause player
        else {
            resumeIntent = new Intent(this, MyBroadcastReceiver.class).setAction(Actions.ACTION_RESUME);
            pResume = PendingIntent.getBroadcast(this, 0, resumeIntent, 0);

            nextIntent = new Intent(this, MyBroadcastReceiver.class).setAction(Actions.ACTION_NEXT);
            pNext = PendingIntent.getBroadcast(this, 0, nextIntent, 0);

            prevIntent = new Intent(this, MyBroadcastReceiver.class).setAction(Actions.ACTION_PREV);
            pPrev = PendingIntent.getBroadcast(this, 0, prevIntent, 0);

            endIntent = new Intent(this, MyBroadcastReceiver.class).setAction(Actions.ACTION_END);
            pEnd = PendingIntent.getBroadcast(this, 0, endIntent, 0);

            viewIntent = new Intent(this, MyBroadcastReceiver.class).setAction(Actions.ACTION_VIEW);
            pView = PendingIntent.getBroadcast(this,0,viewIntent,0);

            notificationLayout.setTextViewText(R.id.notification_title, m.getMusicTitle());
            notificationLayout.setTextViewText(R.id.notification_artist, m.getMusicArtist());
            notificationLayout.setImageViewBitmap(R.id.notification_image, musicManager.getBitmapFromMusicPath(m.getMusicPath()));
            notificationLayout.setImageViewResource(R.id.notification_play, R.drawable.ic_play_arrow_black_24dp);
            notificationLayout.setOnClickPendingIntent(R.id.notification_prev, pPrev);
            notificationLayout.setOnClickPendingIntent(R.id.notification_next, pNext);
            notificationLayout.setOnClickPendingIntent(R.id.notification_play, pResume);
            notificationLayout.setOnClickPendingIntent(R.id.notification_end, pEnd);
            notificationLayout.setOnClickPendingIntent(R.id.notification_image, pView);
        }

        return new NotificationCompat.Builder(getApplicationContext(), App.CHANNEL_ID)
                .setCustomContentView(notificationLayout)
                .setSmallIcon(R.drawable.ic_music_basic)
                .setShowWhen(false)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    @Override
    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }

    //same code repeats, and inefficient. so need to do some replace or merge
    private void pauseMusic(Music m) {
        mediaPlayer.pause();
        setDisplay(m);
    }

    private void setDisplay(Music m){
        if (mediaPlayer.isPlaying()) {
            Notification notification = createNotificationWithStatus(m);
            startForeground(1,notification);
            sendDisplayUpdate("Pause");
        }
        else
        {
            Notification notification = createNotificationWithStatus(m);
            startForeground(1,notification);
            sendDisplayUpdate("Play");
        }
    }

    private void resumeMusic(Music m) {

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        actVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        curVolume = actVolume / maxVolume;

        mediaPlayer.start();
        setDisplay(m);
    }

    /**
     * turn the service and mediaplayer off
     */

    public void stopPlayer() {
        if (mediaPlayer == null) {
            stopSelf();
        }
        else {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand Called.");
        String actionToDo = intent.getAction();

        if (actionToDo.equals(Actions.ACTION_PLAY)) {
            Log.e(TAG, "onStartCommand index : " + musicManager.getCurrentIndex());
            Music m = musicManager.getMusicByIndex(musicManager.getCurrentIndex());
            playMusic(m);
        } else if (actionToDo.equals(Actions.ACTION_PREV)) {
            playPrevMusic();
        } else if (actionToDo.equals(Actions.ACTION_NEXT)) {
            playNextMusic();
        } else if (actionToDo.equals(Actions.ACTION_PAUSE)) {
            Log.e(TAG, "onStartCommand index : " + musicManager.getCurrentIndex());
            Music m = musicManager.getMusicByIndex(musicManager.getCurrentIndex());
            pauseMusic(m);
        } else if (actionToDo.equals(Actions.ACTION_RESUME)) {
            Log.e(TAG, "onStartCommand index : " + musicManager.getCurrentIndex());
            Music m = musicManager.getMusicByIndex(musicManager.getCurrentIndex());
            resumeMusic(m);
        } else if (actionToDo.equals(Actions.ACTION_END)) {
            stopPlayer();
        }
        else if (actionToDo.equals(Actions.ACTION_VIEW)){
            Intent in = new Intent(this, MainActivity.class);
            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(in);
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG,"onDestroy");
        notificationManager.cancel(1);
        unregisterReceiver(earphoneBroadcastReceiver);
        sendDisplayUpdate("Pause");
    }
}
