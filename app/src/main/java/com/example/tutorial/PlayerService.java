package com.example.tutorial;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.IOException;

import static com.example.tutorial.App.CHANNEL_ID;

public class PlayerService extends Service {
    private final static String TAG = "PlayerService";
    private MusicManager musicManager;
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    float actVolume, curVolume, maxVolume;

    private NotificationManagerCompat notificationManagerCompat;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManagerCompat = NotificationManagerCompat.from(this);
        musicManager = MusicManager.getInstance();
        mediaPlayer = musicManager.getMusicPlayer();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(musicManager.getPlaybackSpeed()));
                mp.start();
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mp.isLooping() == false) {
                    playNextMusic();
                }

            }
        });
    }

    private void playMusic(Music m) {
        //Volume Control
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        actVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        curVolume = actVolume / maxVolume;

        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_service);

        Intent pauseIntent = new Intent(Actions.ACTION_PAUSE);
        PendingIntent pPause = PendingIntent.getBroadcast(this,0,pauseIntent,0);

        Intent nextIntent = new Intent(Actions.ACTION_NEXT);
        PendingIntent pNext = PendingIntent.getBroadcast(this,0,nextIntent,0);

        Intent prevIntent = new Intent(Actions.ACTION_PREV);
        PendingIntent pPrev = PendingIntent.getBroadcast(this,0,prevIntent,0);

        Intent endIntent = new Intent(Actions.ACTION_END);
        PendingIntent pEnd = PendingIntent.getBroadcast(this,0,endIntent,0);

        notificationLayout.setTextViewText(R.id.notification_title,m.getMusicTitle());
        notificationLayout.setTextViewText(R.id.notification_artist,m.getMusicArtist());
        notificationLayout.setImageViewBitmap(R.id.notification_image,m.getMusicImage());
        notificationLayout.setImageViewResource(R.id.notification_play,R.drawable.ic_pause_black_24dp);
        notificationLayout.setOnClickPendingIntent(R.id.notification_prev, pPrev);
        notificationLayout.setOnClickPendingIntent(R.id.notification_next, pNext);
        notificationLayout.setOnClickPendingIntent(R.id.notification_play, pPause);
        notificationLayout.setOnClickPendingIntent(R.id.notification_end, pEnd);

        Notification channel = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music_video_black_24dp)
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayout)
                .build();

        startForeground(1,channel);
        Uri uri = Uri.parse(m.getMusicPath());
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(this, uri);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playPrevMusic() {
        int ci = musicManager.getCurrentIndex();
        ci -= 1;
        if (ci == -1) {
            ci = musicManager.getMusicSize() - 1;
        }
        musicManager.setCurrentIndex(ci);

        if (mediaPlayer.isLooping() == true)
            mediaPlayer.setLooping(false);

            playMusic(musicManager.getMusicByIndex(musicManager.getCurrentIndex()));

    }

    private void playNextMusic() {
        musicManager.setCurrentIndex((musicManager.getCurrentIndex() + 1) % musicManager.getMusicSize());

        if (mediaPlayer.isLooping() == true)
            mediaPlayer.setLooping(false);

            playMusic(musicManager.getMusicByIndex(musicManager.getCurrentIndex()));
    }

    @Override
    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }

    //same code repeats, and inefficient. so need to do some replace or merge
    private void pauseMusic(Music m) {
        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_service);

        Intent resumeIntent = new Intent(Actions.ACTION_RESUME);
        PendingIntent pResume = PendingIntent.getBroadcast(this,0,resumeIntent,0);

        Intent nextIntent = new Intent(Actions.ACTION_NEXT);
        PendingIntent pNext = PendingIntent.getBroadcast(this,0,nextIntent,0);

        Intent prevIntent = new Intent(Actions.ACTION_PREV);
        PendingIntent pPrev = PendingIntent.getBroadcast(this,0,prevIntent,0);

        Intent endIntent = new Intent(Actions.ACTION_END);
        PendingIntent pEnd = PendingIntent.getBroadcast(this,0,endIntent,0);

        notificationLayout.setTextViewText(R.id.notification_title,m.getMusicTitle());
        notificationLayout.setTextViewText(R.id.notification_artist,m.getMusicArtist());
        notificationLayout.setImageViewBitmap(R.id.notification_image,m.getMusicImage());
        notificationLayout.setImageViewResource(R.id.notification_play,R.drawable.ic_play_arrow_black_24dp);
        notificationLayout.setOnClickPendingIntent(R.id.notification_prev, pPrev);
        notificationLayout.setOnClickPendingIntent(R.id.notification_next, pNext);
        notificationLayout.setOnClickPendingIntent(R.id.notification_play, pResume);
        notificationLayout.setOnClickPendingIntent(R.id.notification_end, pEnd);

        Notification channel = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music_video_black_24dp)
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayout)
                .build();

        startForeground(1,channel);
        mediaPlayer.pause();
    }

    private void resumeMusic(Music m) {
        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_service);

        Intent pauseIntent = new Intent(Actions.ACTION_PAUSE);
        PendingIntent pPause = PendingIntent.getBroadcast(this,0,pauseIntent,0);

        Intent nextIntent = new Intent(Actions.ACTION_NEXT);
        PendingIntent pNext = PendingIntent.getBroadcast(this,0,nextIntent,0);

        Intent prevIntent = new Intent(Actions.ACTION_PREV);
        PendingIntent pPrev = PendingIntent.getBroadcast(this,0,prevIntent,0);

        Intent endIntent = new Intent(Actions.ACTION_END);
        PendingIntent pEnd = PendingIntent.getBroadcast(this,0,endIntent,0);

        notificationLayout.setTextViewText(R.id.notification_title,m.getMusicTitle());
        notificationLayout.setTextViewText(R.id.notification_artist,m.getMusicArtist());
        notificationLayout.setImageViewBitmap(R.id.notification_image,m.getMusicImage());
        notificationLayout.setImageViewResource(R.id.notification_play,R.drawable.ic_pause_black_24dp);
        notificationLayout.setOnClickPendingIntent(R.id.notification_prev, pPrev);
        notificationLayout.setOnClickPendingIntent(R.id.notification_next, pNext);
        notificationLayout.setOnClickPendingIntent(R.id.notification_play, pPause);
        notificationLayout.setOnClickPendingIntent(R.id.notification_end, pEnd);

        Notification channel = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music_video_black_24dp)
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayout)
                .build();

        startForeground(1,channel);
        mediaPlayer.start();
    }

    /**
     *turn the service and mediaplayer off
     */

    public void stopPlayer(){
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying())
                mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        stopSelf();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand Called.");
        Log.d(TAG,"onStartCommand index : "+musicManager.getCurrentIndex());
        String actionToDo = intent.getAction();
        Music m = musicManager.getMusicByIndex(musicManager.getCurrentIndex());
        if (actionToDo.equals(Actions.ACTION_PLAY)) {
            playMusic(m);
        } else if (actionToDo.equals(Actions.ACTION_PREV)) {
            playPrevMusic();
        } else if (actionToDo.equals(Actions.ACTION_NEXT)) {
            playNextMusic();
        } else if (actionToDo.equals(Actions.ACTION_PAUSE)) {
            pauseMusic(m);
        } else if (actionToDo.equals(Actions.ACTION_RESUME)) {
            resumeMusic(m);
        } else if (actionToDo.equals(Actions.ACTION_END)){
            stopPlayer();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPlayer();
    }
}
