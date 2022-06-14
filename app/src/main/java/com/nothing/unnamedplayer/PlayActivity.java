package com.nothing.unnamedplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
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
import android.widget.Toast;

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

    Runnable runnable;
    Handler handler;
    BroadcastReceiver bReceiver;
    boolean currentState = false;
    private static final String TAG = "PlayActivity";


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.e(TAG,"onConfigChanged Called.");
        super.onConfigurationChanged(newConfig);

        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(getApplicationContext(), "Landscape", Toast.LENGTH_SHORT).show();
            setContentView(R.layout.activity_play);
            Log.e(TAG,"layoutID: "+R.layout.activity_play);
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(getApplicationContext(),"Portrait",Toast.LENGTH_SHORT).show();
            setContentView(R.layout.activity_play);
            Log.e(TAG,"layoutID: "+R.layout.activity_play);
        }
        refreshNotificationLayout();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        Log.e(TAG,"onSaveInstanceState Called");
        outState.putCharSequence("title",musicTitle.getText());
        outState.putCharSequence("artist",musicArtist.getText());
        outState.putCharSequence("album",musicAlbum.getText());
        outState.putCharSequence("current_duration",currentDurationText.getText());
        outState.putCharSequence("total_duration",musicDuration.getText());
        outState.putCharSequence("speedmult",speedMultText.getText());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        Log.e(TAG,"onRestoreInstanceState Called");
        savedInstanceState.getCharSequence("title");
        savedInstanceState.getCharSequence("artist");
        savedInstanceState.getCharSequence("album");
        savedInstanceState.getCharSequence("current_duration");
        savedInstanceState.getCharSequence("total_duration");
        savedInstanceState.getCharSequence("speedmult");
    }

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

        registerReceiver();
        updateDisplay(musicManager.getCurrentIndex());
        initProgressBars();

        currentState = (mediaPlayer != null);
        if (currentState)
            playCycle();
    }

    /*
        Updates all Acitivity Displays
        idx : Current Music Index
        isFirst : when it starts onCreate (first update) : 1
                  not first (is playing and pause or resume) : 0
     */

    private void updateDisplay(int idx) {
        Log.d(TAG, "updateDisplay Called.");
        Log.d(TAG, "updateDisplay index : " + idx);
        Music m = musicManager.getMusicByIndex(idx);
        if (idx >= 0 && idx < musicManager.getMusicListSize()) {

            Glide.with(this)
                    .load(musicManager.getBitmapFromMusicPath(m.getMusicPath()))
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

            if (musicManager.getMusicPlayer() == null)
                mediaPlayer = musicManager.createAndGetMusicPlayer();
            else
                mediaPlayer = musicManager.getMusicPlayer();
            setPlayButtonBackground();
            /*Shuffle and Repeat
            in this case, i just display current state of repeat/shuffle using toggleButtonDisplay function
            */
            if (musicManager.isShuffling == true)
                toggleButtonDisplay(1,"Shuffle");
                //shuffleButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_dark, null)));
            else
                toggleButtonDisplay(0,"Shuffle");
                //shuffleButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.white, null)));

            if (musicManager.isLooping != false) {
                toggleButtonDisplay(1,"Repeat");
                //repeatButton.setImageDrawable(ContextCompat.getDrawable(getApplication(), R.drawable.ic_repeat_one_black_24dp));
                //repeatButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_dark, null)));
            } else {
                toggleButtonDisplay(0,"Repeat");
                //repeatButton.setImageDrawable(ContextCompat.getDrawable(getApplication(), R.drawable.ic_repeat_black_24dp));
                //repeatButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.white, null)));
            }

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

    private void refreshNotificationLayout(){
        doPlayService(Actions.ACTION_ORIENTATION_CHANGED);
    }

    private void pauseMusic() {
        currentState = false;
        setPlayButtonBackground();
        doPlayService(Actions.ACTION_PAUSE);

    }

    private void resumeMusic() {
        currentState = true;
        setPlayButtonBackground();
        doPlayService(Actions.ACTION_RESUME);
        playCycle();
    }

    private void playPrevMusic() {
        int ci = musicManager.getCurrentIndex();
        ci -= 1;
        if (ci == -1) ci = musicManager.getMusicListSize() - 1;
        updateDisplay(ci);
        doPlayService(Actions.ACTION_PREV);

    }

    private void playNextMusic() {
        int ci = musicManager.getCurrentIndex();
        updateDisplay((ci + 1) % musicManager.getMusicListSize());
        doPlayService(Actions.ACTION_NEXT);

    }

    private void setPlayButtonBackground() {
        if (mediaPlayer.isPlaying()) {
            playButtonIcon = R.drawable.ic_pause_black_24dp;
            playButtonBackgroundColor = R.color.playingColor;
        } else {
            playButtonIcon = R.drawable.ic_play_arrow_black_24dp;
            playButtonBackgroundColor = R.color.pausedColor;
        }
        playButton.setImageDrawable(ContextCompat.getDrawable(getApplication(), playButtonIcon));
        playButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(playButtonBackgroundColor, null)));
    }

    /*  state : expected next state
        buttonType
            Repeat
            Shuffleg
    */

    private void toggleButtonDisplay(int state, String buttonType){
        if (buttonType.equals("Repeat")){
            if(state == 0){
                repeatButton.setImageDrawable(ContextCompat.getDrawable(getApplication(), R.drawable.ic_repeat_black_24dp));
                repeatButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.white, null)));
            }
            else{
                repeatButton.setImageDrawable(ContextCompat.getDrawable(getApplication(), R.drawable.ic_repeat_one_black_24dp));
                repeatButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_dark, null)));
            }
        }
        else if (buttonType.equals("Shuffle")){
            if(state == 0){
                shuffleButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.white, null)));
            }
            else{
                shuffleButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_dark, null)));

            }
        }
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
                if (musicManager.isLooping == false) {
                    musicManager.setMediaPlayerLooping(true);
                    toggleButtonDisplay(1,"Repeat");
                    //repeatButton.setImageDrawable(ContextCompat.getDrawable(getApplication(), R.drawable.ic_repeat_one_black_24dp));
                    //repeatButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_dark, null)));
                    Toast.makeText(getApplicationContext(), "Repeat Enabled.", Toast.LENGTH_SHORT).show();
                } else {
                    musicManager.setMediaPlayerLooping(false);
                    toggleButtonDisplay(0,"Repeat");
                    //repeatButton.setImageDrawable(ContextCompat.getDrawable(getApplication(), R.drawable.ic_repeat_black_24dp));
                    //repeatButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.white, null)));
                    Toast.makeText(getApplicationContext(), "Repeat Disabled.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_shuffle:
                if (musicManager.isShuffling == false) {
                    //shuffleButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_dark, null)));
                    toggleButtonDisplay(1,"Shuffle");
                    musicManager.shuffleList();
                    musicManager.setCurrentIndex(musicManager.getPositionByIdx(musicManager.getCurrentIndex()));
                    Toast.makeText(getApplicationContext(), "Shuffle Enabled.", Toast.LENGTH_SHORT).show();
                } else {
                    //shuffleButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.white, null)));
                    toggleButtonDisplay(0,"Shuffle");
                    musicManager.setCurrentIndex(musicManager.getMusicByIndex(musicManager.getCurrentIndex()).getMusicIndex());
                    musicManager.shuffleList();
                    Toast.makeText(getApplicationContext(), "Shuffle Disabled.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void playCycle() {
        if (mediaPlayer == null){
            return;
        }
        runnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    durationBar.setProgress(Math.min(durationBar.getMax(), mediaPlayer.getCurrentPosition()));
                    currentDurationText.setText(musicInfoConverter.durationConvert(Math.min(durationBar.getMax(), mediaPlayer.getCurrentPosition())));
                    playCycle();
                }
            }
        };
        handler.postDelayed(runnable, 250);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume Called");
        updateDisplay(musicManager.getCurrentIndex());
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
        unregisterReceiver(bReceiver);
    }

    private void registerReceiver() {
        bReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String statusToUpdate = intent.getStringExtra("Status");
                if (statusToUpdate.equals("Play"))
                    currentState = true;
                else if (statusToUpdate.equals("Pause"))
                    currentState = false;

                updateDisplay(musicManager.getCurrentIndex());
            }
        };
        registerReceiver(bReceiver, new IntentFilter(Actions.ACTION_UPDATE));
    }
}
