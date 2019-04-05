package com.example.tutorial;

import android.support.annotation.Nullable;

public class Music {
    private String musicTitle;
    private String musicAlbum;
    private String musicArtist;
    private int musicDuration;
    private String musicPath;

    public Music(String title, @Nullable String artist, String album, int duration, String path)
    {
        musicTitle = title;
        musicAlbum = album;
        musicArtist = artist;
        musicDuration = duration;
        musicPath = path;
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
}
