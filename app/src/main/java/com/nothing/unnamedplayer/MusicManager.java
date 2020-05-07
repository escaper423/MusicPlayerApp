package com.nothing.unnamedplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

//using singleton pattern to be used as global resource storage class (playlist)
class MusicManager {
    private static final String TAG = "MusicManager";
    private static MusicManager musicManager = null;

    //Music lists and MediaPlayer object.
    private ArrayList<Music> storedMusicList;   //stores all musics in the device.
    private ArrayList<Music> currentMusicList;  //currently using playlist.
    private MediaPlayer mediaPlayer = new MediaPlayer();

    //Playback speed, shuffle status
    private float playbackSpeed = 1.0f;
    public boolean isShuffling = false;
    public boolean isLooping = false;

    //Currently Playing Index and file path
    private int currentIndex;
    private String currentDirectory;

    public int getCurrentIndex(){
        return currentIndex;
    }
    public void setCurrentIndex(int i){
        currentIndex = i;
    }

    public String getCurrentDirectory(){ return currentDirectory; }
    public void setCurrentDirectory(String path) { currentDirectory = path; }

    public void setShuffling(boolean b) { isShuffling = b; }
    public void setPlaybackSpeed(float f) {
        playbackSpeed = f;
        mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(playbackSpeed));
    }


    public void setMediaPlayerLooping(boolean state){
        isLooping = state;
        if (mediaPlayer != null)
            mediaPlayer.setLooping(state);
    }
    public MediaPlayer createAndGetMusicPlayer(){
        if (mediaPlayer == null)
            mediaPlayer = new MediaPlayer();
        return mediaPlayer;
    }

    public float getPlaybackSpeed() {
        return playbackSpeed;
    }

    public int getMusicSize() {
        return currentMusicList.size();
    }

    private MusicManager() {
        storedMusicList = new ArrayList<>();
        currentMusicList = new ArrayList<>();
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
        return currentMusicList.get(i);
    }

    public ArrayList<Music> getCurrentMusicList() {
        return currentMusicList;
    }
    public ArrayList<Music> getStoredMusicList() {
        return storedMusicList;
    }

    //TODO : additional filter option needed
    //fills storedMusicList for initialization.
    public void initMusicList(Context context, @Nullable String[] projection, @Nullable String selection,@Nullable String[] selectionArgs) {
        if (storedMusicList.size() != 0)
            storedMusicList.clear();

        ContentResolver contentResolver = context.getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = contentResolver.query(songUri, projection, selection, selectionArgs, "LOWER(" + MediaStore.Audio.Media.TITLE + ") ASC");

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

                storedMusicList.add(new Music(currentTitle, currentArtist, currentAlbum, currentDuration, currentPath, bitmap, songCursor.getPosition()));

            } while (songCursor.moveToNext());
        }
    }

    //Resets current playlist. from stored music list
    public void resetMusicList(){
        currentMusicList.clear();
        for (Music m : storedMusicList){
            currentMusicList.add(m.clone());
        }
    }

    //shuffles the current list for shuffle feature.
    public void shuffleList() {
        if (isShuffling == true) {
            Collections.sort(currentMusicList, new Comparator<Music>() {
                @Override
                public int compare(Music m1, Music m2) {
                    return m1.getMusicIndex() > m2.getMusicIndex() ? 1 : -1;
                }
            });
            isShuffling = false;
        } else {
            Collections.shuffle(currentMusicList);
            isShuffling = true;
        }
    }
    public int getPositionByIdx(int idx){
        for(int i = 0; i < currentMusicList.size(); i++){
            if(idx == currentMusicList.get(i).getMusicIndex())
                return i;
        }
        //Some error occured
        return -1;
    }
}