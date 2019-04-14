package com.example.tutorial;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.session.MediaSessionCompat;

import java.io.IOException;

public class PlayerService extends Service {
    private MusicManager musicManager;
    private MediaPlayer mediaPlayer;

    private MediaSessionCompat mediaSessionCompat;
    private NotificationManagerCompat notificationManagerCompat;

    @Override
    public void onCreate() {
        super.onCreate();
        musicManager = MusicManager.getInstance();
        mediaPlayer = musicManager.getMusicPlayer();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(musicManager.getPlaybackSpeed()));
                if (mediaPlayer.isPlaying())
                    mediaPlayer.start();
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playNextMusic();
            }
        });
    }

    private void playMusic(String path) {
        Uri uri = Uri.parse(path);
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
        playMusic(musicManager.getMusicByIndex(musicManager.getCurrentIndex()).getMusicPath());
    }

    private void playNextMusic() {
        musicManager.setCurrentIndex((musicManager.getCurrentIndex() + 1) % musicManager.getMusicSize());
        playMusic(musicManager.getMusicByIndex(musicManager.getCurrentIndex()).getMusicPath());
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
        String actionToDo = intent.getAction();
        Music m = musicManager.getMusicByIndex(musicManager.getCurrentIndex());
        if(actionToDo.equals(Actions.ACTION_PLAY)) {
            playMusic(m.getMusicPath());
        }
        else if (actionToDo.equals(Actions.ACTION_PREV)){
            playPrevMusic();
        }
        else if (actionToDo.equals(Actions.ACTION_NEXT)){
            playNextMusic();
        }
        else if (actionToDo.equals(Actions.ACTION_PAUSE)){
            pauseMusic();
        }
        else if (actionToDo.equals(Actions.ACTION_RESUME)){
            resumeMusic();
        }
        return super.onStartCommand(intent, flags, startId);
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
    }
}
