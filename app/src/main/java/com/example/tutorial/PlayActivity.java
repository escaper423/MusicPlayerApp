package com.example.tutorial;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;


public class PlayActivity extends AppCompatActivity {
    private TextView musicTitle;
    private TextView musicArtist;
    private TextView musicAlbum;
    private TextView musicDuration;
    private ImageView musicImage;

    private MusicManager musicManager;
    private MediaPlayer mediaPlayer;
    private Intent serviceIntent;

    //Textviews to be displayed about duration, speed multiplication
    private TextView speedMultText;
    private TextView currentDurationText;

    //Play, shuffle, repeat buttons
    private ImageButton repeatButton;
    private ImageButton shuffleButton;
    int playButtonIcon, playButtonBackgroundColor;
    private FloatingActionButton playButton;

    private AppCompatSeekBar durationBar;
    private AppCompatSeekBar speedBar;


    AudioManager audiomManager;
    float actVolume, curVolume, maxVolume;

    Runnable runnable;
    Handler handler;

    private static final String TAG = "PlayActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate Called.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        musicImage = findViewById(R.id.imageView);
        musicTitle = findViewById(R.id.txt_title);
        musicArtist = findViewById(R.id.txt_artist);
        musicAlbum = findViewById(R.id.txt_album);
        musicDuration = findViewById(R.id.txt_song_total_duration);
        currentDurationText = findViewById(R.id.txt_song_current_duration);
        speedMultText = findViewById(R.id.speed_multiplier);

        repeatButton = findViewById(R.id.btn_repeat);
        playButton = findViewById(R.id.btn_play);
        shuffleButton = findViewById(R.id.btn_shuffle);

        durationBar = findViewById(R.id.song_progressbar);
        speedBar = findViewById(R.id.speed_multiplier_bar);
        handler = new Handler();
        musicManager = MusicManager.getInstance();
        mediaPlayer = musicManager.getMusicPlayer();

        Intent in = getIntent();
        musicManager.setCurrentIndex(in.getIntExtra("Index", -1));

        initMusicPlayer(musicManager.getCurrentIndex());
        initProgressBars();

        //Play Button Display
        playButtonIcon = R.drawable.ic_pause_black_24dp;
        playButtonBackgroundColor = R.color.playingColor;
        playButton.setImageDrawable(ContextCompat.getDrawable(getApplication(), playButtonIcon));
        playButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(playButtonBackgroundColor, null)));

        //Volume Control
        audiomManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        actVolume = (float) audiomManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume = (float) audiomManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        curVolume = actVolume / maxVolume;
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        //Shuffle and Repeat
        if (musicManager.isShuffling() == true)
            shuffleButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_dark, null)));
        else
            shuffleButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.white, null)));

        if (mediaPlayer.isLooping() != false) {
            repeatButton.setImageDrawable(ContextCompat.getDrawable(getApplication(), R.drawable.ic_repeat_one_black_24dp));
            repeatButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_dark, null)));
        } else {
            repeatButton.setImageDrawable(ContextCompat.getDrawable(getApplication(), R.drawable.ic_repeat_black_24dp));
            repeatButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.white, null)));
        }


        playMusic();
    }

    private void initMusicPlayer(int idx) {
        Music m = musicManager.getMusicByIndex(idx);
        if (idx >= 0 && idx < musicManager.getMusicSize()) {

            Glide.with(this)
                    .load(m.getMusicImage())
                    .placeholder(R.drawable.ic_music_basic)
                    .into(musicImage);

            musicTitle.setText(m.getMusicTitle());
            musicArtist.setText(m.getMusicArtist());
            musicAlbum.setText(m.getMusicAlbum());
            musicDuration.setText(musicInfoConverter.durationConvert(m.getMusicDuration()));
            durationBar.setProgress(0);
            durationBar.setMax(m.getMusicDuration());
            durationBar.setKeyProgressIncrement(1000);
            currentDurationText.setText(musicInfoConverter.durationConvert(0));
            speedBar.setProgress(musicInfoConverter.getProgressFormSpeedMult(musicManager.getPlaybackSpeed()));
            speedBar.setKeyProgressIncrement(1);
            speedMultText.setText(Float.toString(musicInfoConverter.getSpeedMultFromProgress(speedBar.getProgress())) + 'x');

        } else
            Log.d(TAG, "not found.");
    }

    private void initProgressBars() {
        durationBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekbar, int i, boolean b) {
                if (b) {
                    currentDurationText.setText(musicInfoConverter.durationConvert(i));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekbar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekbar) {
                mediaPlayer.seekTo(durationBar.getProgress());
            }
        });
        speedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    speedMultText.setText(Float.toString(musicInfoConverter.getSpeedMultFromProgress(progress)) + 'x');
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicManager.setPlaybackSpeed(musicInfoConverter.getSpeedMultFromProgress(speedBar.getProgress()));
            }
        });
    }

    public void doPlayService(String action) {
        serviceIntent = new Intent(this, PlayerService.class);
        serviceIntent.setAction(action);
        startService(serviceIntent);
    }

    private void playMusic() {
        doPlayService(Actions.ACTION_PLAY);
        playCycle();
    }

    private void pauseMusic() {
        setBackground();
        doPlayService(Actions.ACTION_PAUSE);
    }

    private void resumeMusic() {
        setBackground();
        doPlayService(Actions.ACTION_RESUME);
        playCycle();
    }

    private void playPrevMusic() {
        int ci = musicManager.getCurrentIndex();
        ci -= 1;
        if (ci == -1) ci = musicManager.getMusicSize() - 1;
        initMusicPlayer(ci);
        doPlayService(Actions.ACTION_PREV);

    }

    private void playNextMusic() {
        int ci = musicManager.getCurrentIndex();
        initMusicPlayer((ci + 1) % musicManager.getMusicSize());
        doPlayService(Actions.ACTION_NEXT);

    }

    private void setBackground() {
        if (mediaPlayer.isPlaying() == false) {
            playButtonIcon = R.drawable.ic_pause_black_24dp;
            playButtonBackgroundColor = R.color.playingColor;
        } else {
            playButtonIcon = R.drawable.ic_play_arrow_black_24dp;
            playButtonBackgroundColor = R.color.pausedColor;
        }
        playButton.setImageDrawable(ContextCompat.getDrawable(getApplication(), playButtonIcon));
        playButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(playButtonBackgroundColor, null)));
    }


    public void controlClick(View v) {
        int viewId = v.getId();
        switch (viewId) {
            case R.id.btn_prev:
                /* if the current elapsed duration is more than 5 seconds
                    then reset to 0
                    otherwise play the previous song
                 */
                if (mediaPlayer.getCurrentPosition() > 5000) {
                    durationBar.setProgress(0);
                    currentDurationText.setText(musicInfoConverter.durationConvert(0));
                    mediaPlayer.seekTo(0);
                } else {
                    playPrevMusic();
                }
                break;
            case R.id.btn_play:
                if (mediaPlayer.isPlaying()) {
                    pauseMusic();
                } else {
                    resumeMusic();
                }
                break;
            case R.id.btn_next:
                playNextMusic();
                break;
            case R.id.btn_repeat:
                if (mediaPlayer.isLooping() == false) {
                    mediaPlayer.setLooping(true);
                    repeatButton.setImageDrawable(ContextCompat.getDrawable(getApplication(), R.drawable.ic_repeat_one_black_24dp));
                    repeatButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_dark, null)));
                } else {
                    mediaPlayer.setLooping(false);
                    repeatButton.setImageDrawable(ContextCompat.getDrawable(getApplication(), R.drawable.ic_repeat_black_24dp));
                    repeatButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.white, null)));
                }
                break;
            case R.id.btn_shuffle:
                if (musicManager.isShuffling() == false) {
                    shuffleButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_dark, null)));
                    musicManager.shuffleList();
                    musicManager.setCurrentIndex(musicManager.getPositionByIdx(musicManager.getCurrentIndex()));
                } else {
                    shuffleButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.white, null)));
                    musicManager.setCurrentIndex(musicManager.getMusicByIndex(musicManager.getCurrentIndex()).getMusicIndex());
                    musicManager.shuffleList();
                }
                break;
        }
    }

    //TODO : Must solve some problems with this thread
    public void playCycle() {
        if (mediaPlayer == null)
            return;

        durationBar.setProgress(Math.min(durationBar.getMax(), mediaPlayer.getCurrentPosition()));
        currentDurationText.setText(musicInfoConverter.durationConvert(Math.min(durationBar.getMax(), mediaPlayer.getCurrentPosition())));

        runnable = new Runnable() {
            @Override
            public void run() {
                playCycle();
            }
        };
        handler.postDelayed(runnable, 250);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume Called");
        initMusicPlayer(musicManager.getCurrentIndex());
        super.onResume();

}

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}
