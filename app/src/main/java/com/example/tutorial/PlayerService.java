package com.example.tutorial;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
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

    private MediaSessionCompat mediaSessionCompat;
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
        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_service);
        notificationLayout.setTextViewText(R.id.notification_title,m.getMusicTitle());
        notificationLayout.setTextViewText(R.id.notification_artist,m.getMusicArtist());
        notificationLayout.setImageViewBitmap(R.id.notification_image,m.getMusicImage());

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

    private void pauseMusic() {
        mediaPlayer.pause();
    }

    private void resumeMusic() {
        mediaPlayer.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand Called.");
        String actionToDo = intent.getAction();
        Music m = musicManager.getMusicByIndex(musicManager.getCurrentIndex());
        if (actionToDo.equals(Actions.ACTION_PLAY)) {
            playMusic(m);
        } else if (actionToDo.equals(Actions.ACTION_PREV)) {
            playPrevMusic();
        } else if (actionToDo.equals(Actions.ACTION_NEXT)) {
            playNextMusic();
        } else if (actionToDo.equals(Actions.ACTION_PAUSE)) {
            pauseMusic();
        } else if (actionToDo.equals(Actions.ACTION_RESUME)) {
            resumeMusic();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying())
                mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        stopSelf();
    }
}
