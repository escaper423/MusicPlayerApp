package com.example.tutorial;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;

//using singleton pattern to be used as global resource class (playlist)
class MusicManager {
    private static final String TAG = "MusicManager";
    private static MusicManager musicManager = null;
    ArrayList<Music> musicList;

    private MusicManager(){
        musicList = new ArrayList<>();
    }
    public static MusicManager getInstance(){
        if(musicManager == null){
            musicManager = new MusicManager();
        }
        return musicManager;
    }
    public ArrayList<Music> getMusicList(){
        return musicList;
    }

    //Cover Image to be added.
    //additional filter need.
    public void getMusic(Context context){
        if(musicList.size() != 0)
            musicList.clear();

        ContentResolver contentResolver = context.getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = contentResolver.query(songUri, null, null ,null ,null);

        if(songCursor != null && songCursor.moveToFirst()){
            int titleIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int albumIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int durationIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int pathIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);

            do{
                String currentTitle = songCursor.getString(titleIndex);
                String currentArtist = songCursor.getString(artistIndex);
                String currentAlbum = songCursor.getString(albumIndex);
                int currentDuration = songCursor.getInt(durationIndex); //milliseconds
                String currentPath = songCursor.getString(pathIndex);

                musicList.add(new Music(currentTitle,currentArtist,currentAlbum,currentDuration,currentPath));

            } while(songCursor.moveToNext());
        }
    }
}
