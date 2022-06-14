package com.nothing.unnamedplayer;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

public class Music {
    private String musicTitle;
    private String musicAlbum;
    private String musicArtist;
    private int musicDuration;
    private String musicPath;
    private int musicIndex; //Original Index of music (used for shuffling)

    public Music( String title, @Nullable String artist, @Nullable String album, int duration, String path, int originalIdx) {
        musicTitle = title;
        musicAlbum = album;
        musicArtist = artist;
        musicDuration = duration;
        musicPath = path;
        musicIndex = originalIdx;
    }
    public void setMusicIndex(int idx){
        musicIndex = idx;
    }
    public String getMusicAlbum() {
        return musicAlbum;
    }

    public String getMusicTitle() {
        return musicTitle;
    }

    public String getMusicArtist() {
        return musicArtist;
    }

    public int getMusicDuration() {
        return musicDuration;
    }

    public String getMusicPath() {
        return musicPath;
    }

    public int getMusicIndex() { return musicIndex; }

    @Override
    public Music clone(){
        String mt = this.musicTitle;
        String mal = this.musicAlbum;
        String mat = this.musicArtist;
        int md = this.musicDuration;
        String mp = this.musicPath;
        int midx = this.musicIndex;
        return new Music(mt,mat,mal,md,mp,midx);
    }
}
