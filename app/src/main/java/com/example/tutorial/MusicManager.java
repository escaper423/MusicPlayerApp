package com.example.tutorial;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.bumptech.glide.load.model.stream.MediaStoreImageThumbLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

//using singleton pattern to be used as global resource class (playlist)
class MusicManager {
    private static final String TAG = "MusicManager";
    private static MusicManager musicManager = null;

    private ArrayList<Music> musicList;
    private MediaPlayer mediaPlayer = new MediaPlayer();

    //Playback speed, shuffle status
    private float playbackSpeed = 1.0f;
    private boolean isShuffling = false;

    //Currently Playing Index
    private int currentIndex;

    public int getCurrentIndex(){
        return currentIndex;
    }
    public void setCurrentIndex(int i){
        currentIndex = i;
    }

    public boolean isShuffling(){
        return isShuffling;
    }

    public void setPlaybackSpeed(float f) {
        playbackSpeed = f;
        mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(playbackSpeed));
    }

    public float getPlaybackSpeed() {
        return playbackSpeed;
    }

    public int getMusicSize() {
        return musicList.size();
    }

    private MusicManager() {
        musicList = new ArrayList<>();
    }

    public static MusicManager getInstance() {
        if (musicManager == null) {
            musicManager = new MusicManager();
        }
        return musicManager;
    }

    public MediaPlayer getMusicPlayer(){
        return mediaPlayer;
    }
    public Music getMusicByIndex(int i) {
        return musicList.get(i);
    }

    public ArrayList<Music> getMusicList() {
        return musicList;
    }

    //TODO : additional filter option needed
    public void initMusicList(Context context) {
        if (musicList.size() != 0)
            musicList.clear();

        ContentResolver contentResolver = context.getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = contentResolver.query(songUri, null, null, null, "LOWER(" + MediaStore.Audio.Media.TITLE + ") ASC");

        if (songCursor != null && songCursor.moveToFirst()) {
            int titleIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int albumIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int durationIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int pathIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);

            do {
                Log.d(TAG, "Current Cursor Index : " + songCursor.getPosition());
                String currentTitle = songCursor.getString(titleIndex);
                String currentArtist = songCursor.getString(artistIndex);
                String currentAlbum = songCursor.getString(albumIndex);
                int currentDuration = songCursor.getInt(durationIndex); //milliseconds
                String currentPath = songCursor.getString(pathIndex);

                //Extract the cover image
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(currentPath);

                byte[] data = mmr.getEmbeddedPicture();
                Bitmap bitmap = null;
                if (data != null) {
                    bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                }

                musicList.add(new Music(currentTitle, currentArtist, currentAlbum, currentDuration, currentPath, bitmap, songCursor.getPosition()));

            } while (songCursor.moveToNext());
        }
    }

    public void shuffleList() {
        if (isShuffling == true) {
            Collections.sort(musicList, new Comparator<Music>() {
                @Override
                public int compare(Music m1, Music m2) {
                    return m1.getMusicIndex() > m2.getMusicIndex() ? 1 : -1;
                }
            });
            isShuffling = false;
        } else {
            Collections.shuffle(musicList);
            isShuffling = true;
        }
    }
    public int getPositionByIdx(int idx){
        for(int i = 0; i < musicList.size(); i++){
            if(idx == musicList.get(i).getMusicIndex())
                return i;
        }
        //Some error occured
        return -1;
    }
}