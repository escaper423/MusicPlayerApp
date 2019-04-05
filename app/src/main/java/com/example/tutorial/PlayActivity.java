package com.example.tutorial;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.math.BigDecimal;

public class PlayActivity extends AppCompatActivity {
    private TextView musicTitle;
    private TextView musicArtist;
    private TextView musicAlbum;
    private TextView musicDuration;

    //Textviews to be displayed about duration, speed multiplication
    private TextView speedMultText;
    private TextView currentDurationText;

    //Play, shuffle, repeat buttons
    private ImageButton repeatButton;
    private ImageButton shuffleButton;
    int playButtonIcon, playButtonBackgroundColor;
    private FloatingActionButton playButton;

    private MediaPlayer mediaPlayer;

    private AppCompatSeekBar durationBar;
    private AppCompatSeekBar speedBar;
    private float speedMult;


    AudioManager audiomManager;
    float actVolume, curVolume, maxVolume;

    Runnable runnable;
    Handler handler;


    private static final String TAG = "PlayActivity";

    enum CurrentStatus {
        PAUSE,
        PLAYING
    };

    //should be merged

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
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
        getIncomingIntent();
    }

    private void getIncomingIntent() {
        Log.d(TAG, "getIncomingIntent : checking...");
        Intent in = getIntent();
        if (in.hasExtra("music_title") && in.hasExtra("music_artist") && in.hasExtra("music_album") && in.hasExtra("music_duration")) {
            Log.d(TAG, "found.");
            musicTitle.setText(in.getStringExtra("music_title"));
            musicArtist.setText(in.getStringExtra("music_artist"));
            musicAlbum.setText(in.getStringExtra("music_album"));
            musicDuration.setText(musicInfoConverter.durationConvert(in.getIntExtra("music_duration",0)));

            durationBar.setProgress(0);
            durationBar.setMax(in.getIntExtra("music_duration",0));
            durationBar.setKeyProgressIncrement(1000);
            speedBar.setProgress(75);
            speedBar.setKeyProgressIncrement(1);
            speedMult =  musicInfoConverter.getSpeedMultFromProgress(speedBar.getProgress());
            Log.d(TAG,"Speed : "+speedMult);

            if (in.hasExtra("music_path"))
                loadMusic(in.getStringExtra("music_path"));
        } else
            Log.d(TAG, "not found.");

    }

    private void loadMusic(String path) {
        audiomManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        actVolume = (float) audiomManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume = (float) audiomManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        curVolume = actVolume / maxVolume;
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        durationBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekbar, int i, boolean b) {
                if(b) {
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
            if(fromUser){
                speedMultText.setText(Float.toString(musicInfoConverter.getSpeedMultFromProgress(progress))+'x');
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            speedMult =  musicInfoConverter.getSpeedMultFromProgress(speedBar.getProgress());
            Log.d(TAG,"Speed : "+speedMult);
            mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speedMult));
        }
    });

    Uri uri = Uri.parse(path);
    mediaPlayer = MediaPlayer.create(this,uri);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speedMult));
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                setBackground(CurrentStatus.PLAYING);
            }
        });
        setBackground(CurrentStatus.PAUSE);
        mediaPlayer.start();
        playCycle();

    }

    private void setBackground(CurrentStatus curStatus) {
        switch(curStatus) {
            case PAUSE:
                playButtonIcon = R.drawable.ic_pause_black_24dp;
                playButtonBackgroundColor = R.color.playingColor;
                break;
            case PLAYING:
                playButtonIcon = R.drawable.ic_play_arrow_black_24dp;
                playButtonBackgroundColor = R.color.pausedColor;
                break;
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
                if (mediaPlayer.getDuration() > 5000){
                    durationBar.setProgress(0);
                    mediaPlayer.seekTo(0);
                }
                else{

                }
                break;
            case R.id.btn_play:
                if (mediaPlayer.isPlaying() == true) {
                    setBackground(CurrentStatus.PLAYING);
                    mediaPlayer.pause();
                } else {
                    setBackground(CurrentStatus.PAUSE);
                    mediaPlayer.start();
                    playCycle();
                }
                break;
            case R.id.btn_next:
                break;
            case R.id.btn_repeat:
                if (mediaPlayer.isLooping() == false){
                    mediaPlayer.setLooping(true);
                    repeatButton.setImageDrawable(ContextCompat.getDrawable(getApplication(),R.drawable.ic_repeat_one_black_24dp));
                }
                else{
                    mediaPlayer.setLooping(false);
                    repeatButton.setImageDrawable(ContextCompat.getDrawable(getApplication(),R.drawable.ic_repeat_black_24dp));
                }

                break;
        }
    }
    public void playCycle(){
        durationBar.setProgress(Math.min(durationBar.getMax(),mediaPlayer.getCurrentPosition()));
        currentDurationText.setText(musicInfoConverter.durationConvert(Math.min(durationBar.getMax(),mediaPlayer.getCurrentPosition())));

        if(mediaPlayer.isPlaying()){
            runnable = new Runnable(){
                @Override
                public void run(){
                    playCycle();
                }
            };
            handler.postDelayed(runnable, 100);
        }
    }
    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();

    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        if(mediaPlayer.isPlaying())
            mediaPlayer.stop();
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}
