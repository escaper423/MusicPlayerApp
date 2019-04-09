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


    AudioManager audiomManager;
    float actVolume, curVolume, maxVolume;
    private int currentIndex;
    private boolean isShuffling = false;
    private CurrentStatus currentStatus;

    Runnable runnable;
    Handler handler;


    private static final String TAG = "PlayActivity";

    enum CurrentStatus {
        PAUSE,
        PLAYING
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");
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

        Intent in = getIntent();
        currentIndex = in.getIntExtra("Index",-1);

        audiomManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        actVolume = (float) audiomManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume = (float) audiomManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        curVolume = actVolume / maxVolume;
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        currentStatus = CurrentStatus.PLAYING;
        initMusicPlayer();
    }

    private void initMusicPlayer() {
        Music m = musicManager.getMusicByIndex(currentIndex);
        Log.d(TAG,"Music Index : "+m.getMusicIndex());
        if (currentIndex >= 0 && currentIndex < musicManager.getMusicSize()) {

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
            speedMultText.setText(Float.toString(musicInfoConverter.getSpeedMultFromProgress(speedBar.getProgress()))+'x');
            loadMusic(m.getMusicPath());
        } else
            Log.d(TAG, "not found.");

    }

    private void loadMusic(String path) {
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
                mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(musicManager.getPlaybackSpeed()));
                Log.d(TAG,"Speed : "+musicManager.getPlaybackSpeed());
            }
        });
        Uri uri = Uri.parse(path);
        if(mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, uri);
            mediaPlayer.start();
        }
        else{
            try{
                mediaPlayer.reset();
                mediaPlayer.setDataSource(this,uri);
                mediaPlayer.prepareAsync();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

        mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(musicManager.getPlaybackSpeed()));
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (currentStatus == CurrentStatus.PLAYING)
                    playMusic();
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                setBackground(CurrentStatus.PLAYING);
                if (mediaPlayer.isLooping() == false)
                    playNextMusic();
            }
        });
    }

    private void playMusic() {
        setBackground(currentStatus);
        currentStatus = CurrentStatus.PLAYING;
        mediaPlayer.start();
        playCycle();
    }

    private void playPrevMusic(){
        currentIndex = currentIndex - 1;
        if(currentIndex == -1)
            currentIndex = musicManager.getMusicSize() - 1;
        initMusicPlayer();
    }

    private void playNextMusic(){
        currentIndex = (currentIndex + 1)%musicManager.getMusicSize();
        initMusicPlayer();
    }

    private void setBackground(CurrentStatus curStatus) {
        switch (curStatus) {
            case PLAYING:
                playButtonIcon = R.drawable.ic_pause_black_24dp;
                playButtonBackgroundColor = R.color.playingColor;
                break;
            case PAUSE:
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
                if (mediaPlayer.getCurrentPosition() > 5000) {
                    durationBar.setProgress(0);
                    currentDurationText.setText(musicInfoConverter.durationConvert(0));
                    mediaPlayer.seekTo(0);
                }
                else {
                    playPrevMusic();
                }
                break;
            case R.id.btn_play:
                if (currentStatus == CurrentStatus.PLAYING) {
                    currentStatus = CurrentStatus.PAUSE;
                    setBackground(currentStatus);
                    mediaPlayer.pause();
                } else {
                    currentStatus = CurrentStatus.PLAYING;
                    playMusic();
                }
                break;
            case R.id.btn_next:
                playNextMusic();
                break;
            case R.id.btn_repeat:
                if (mediaPlayer.isLooping() == false) {
                    mediaPlayer.setLooping(true);
                    repeatButton.setImageDrawable(ContextCompat.getDrawable(getApplication(), R.drawable.ic_repeat_one_black_24dp));
                    repeatButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_dark,null)));
                } else {
                    mediaPlayer.setLooping(false);
                    repeatButton.setImageDrawable(ContextCompat.getDrawable(getApplication(), R.drawable.ic_repeat_black_24dp));
                    repeatButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.white,null)));
                }
                break;
            case R.id.btn_shuffle:
                if(musicManager.isShuffling() == false){
                    shuffleButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_dark,null)));
                    musicManager.shuffleList();
                    Log.d(TAG,"Music Index before Shuffle : "+currentIndex);
                    currentIndex = musicManager.getPositionByIdx(currentIndex);
                    Log.d(TAG,"Music Index after Shuffle : "+currentIndex);

                }
                else{
                    shuffleButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.white,null)));
                    currentIndex = musicManager.getMusicByIndex(currentIndex).getMusicIndex();
                    Log.d(TAG, "Music Index before Disable Shuffle : "+currentIndex);
                    musicManager.shuffleList();
                    Log.d(TAG,"Music Index after Disable Shuffle : "+currentIndex);

                }
                break;
        }
    }

    //TODO : Must solve some problems with this thread
    public void playCycle() {
        if(mediaPlayer != null) {
            durationBar.setProgress(Math.min(durationBar.getMax(), mediaPlayer.getCurrentPosition()));
            currentDurationText.setText(musicInfoConverter.durationConvert(Math.min(durationBar.getMax(), mediaPlayer.getCurrentPosition())));

            if (currentStatus == CurrentStatus.PLAYING) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        playCycle();
                    }
                };
                handler.postDelayed(runnable, 100);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /*
    @Override
    public void onStop(){
        super.onStop();
        mediaPlayer.release();
        mediaPlayer = null;
    }
    */
    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handler.removeCallbacks(runnable);
        mediaPlayer.stop();
        mediaPlayer.release();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        mediaPlayer.release();
    }
}
