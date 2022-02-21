package com.nothing.unnamedplayer;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

public class Music {
    private String musicTitle;
    private String musicAlbum;
    private String musicArtist;
    private int musicDuration;
    private String musicPath;
    private String musicImage;
    private int musicIndex; //Original Index of music (used for shuffling)

    public Music( String title, @Nullable String artist, @Nullable String album, int duration, String path, @Nullable String image, int originalIdx) {
        musicTitle = title;
        musicAlbum = album;
        musicArtist = artist;
        musicDuration = duration;
        musicPath = path;
        musicImage = image;
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

    public String getMusicImage() {
        return musicImage;
    }

    @Override
    public Music clone(){
        String mt = this.musicTitle;
        String mal = this.musicAlbum;
        String mat = this.musicArtist;
        int md = this.musicDuration;
        String mp = this.musicPath;
        String mimg = this.musicImage;
        int midx = this.musicIndex;
        return new Music(mt,mat,mal,md,mp,mimg,midx);
    }
}
