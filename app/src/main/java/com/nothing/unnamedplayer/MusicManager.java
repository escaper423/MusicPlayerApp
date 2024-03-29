package com.nothing.unnamedplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
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

//using singleton pattern to be used as global resource storage class (playlist)
class MusicManager {
    private static final String TAG = "MusicManager";
    private static MusicManager musicManager = null;

    //Music lists and MediaPlayer object.
    private final ArrayList<Music> storedMusicList;   //stores all musics in the device.
    private final ArrayList<Music> currentMusicList;  //currently using playlist.
    private MediaPlayer mediaPlayer;
    private boolean hasInterrupted = false;

    //Playback speed, shuffle status
    private float playbackSpeed = 1.0f;

    public boolean isShuffling = false;
    public boolean isLooping = false;

    //Currently Playing Index and file path
    private int currentIndex = -1;
    private String currentDirectory;

    public void setInterruptState(boolean state) { hasInterrupted = state; }
    public boolean getInterruptState() {return hasInterrupted; }

    public String getCurrentMusicTitle(){
        return currentMusicList.get(currentIndex).getMusicTitle();
    }

    public String getCurrentMusicArtist(){
        return currentMusicList.get(currentIndex).getMusicArtist();
    }

    public String getCurrentMusicPath(){
        return currentMusicList.get(currentIndex).getMusicPath();
    }

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
        boolean oldPlayerStatus = mediaPlayer.isPlaying();

        //MediaPlayer automatically resume after setting playback speed by calling setPlaybackParams function.
        //so save current mediaplayer status before calling it and restore the status.
        mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(playbackSpeed));
        if (!oldPlayerStatus) {
            mediaPlayer.pause();
        }
    }
    public void preparePlaybackSpeed(float f){
        playbackSpeed = f;
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

    public int getMusicListSize() {
        return currentMusicList.size();
    }

    private MusicManager() {
        mediaPlayer = new MediaPlayer();
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
    public Music getStoredMusicByIndex(int i){
        return storedMusicList.get(i);
    }

    public ArrayList<Music> getCurrentMusicList() {
        return currentMusicList;
    }
    public ArrayList<Music> getStoredMusicList() {
        return storedMusicList;
    }
    public void setCurrentMusicList(Context context, ArrayList<Music> m){
        currentMusicList.clear();
        for(int i = 0; i < m.size(); i++){
            currentMusicList.add(m.get(i));
        }
        //Send musiclist update broadcast
        Intent playlistChangedBroadcast = new Intent(Actions.ACTION_CURRENT_PLAYLIST_CHANGED);
        context.sendBroadcast(playlistChangedBroadcast);
    }

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

                storedMusicList.add(new Music(currentTitle, currentArtist, currentAlbum, currentDuration, currentPath, songCursor.getPosition()));

            } while (songCursor.moveToNext());
        }
        try{
            songCursor.close();
        }
        catch(Exception e){
            e.printStackTrace();
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
            Collections.sort(currentMusicList, (m1, m2) -> m1.getMusicIndex() > m2.getMusicIndex() ? 1 : -1);
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

    public Bitmap getBitmapFromMusicPath(String path) {
        //Extract the cover image
        android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(path);
        byte[] data = mmr.getEmbeddedPicture();
        Bitmap bitmap = null;
        if (data != null) {
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        return bitmap;
    }

    public void dispatchMusicDelete(Context context){
        musicManager.setCurrentIndex(-1);
        currentMusicList.clear();

        Intent serviceIntent = new Intent(context, PlayerService.class);
        serviceIntent.setAction(Actions.ACTION_END);
        context.startService(serviceIntent);

        context.sendBroadcast(new Intent(Actions.ACTION_DELETE));
    }
}