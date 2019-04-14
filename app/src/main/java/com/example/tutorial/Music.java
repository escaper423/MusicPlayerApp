package com.example.tutorial;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

public class Music {
    private String musicTitle;
    private String musicAlbum;
    private String musicArtist;
    private int musicDuration;
    private String musicPath;
    private Bitmap musicImage;
    private int musicIndex; //Original Index of music (used for shuffling)

    public Music(String title, @Nullable String artist, @Nullable String album, int duration, String path, @Nullable Bitmap image, int originalIdx) {
        musicTitle = title;
        musicAlbum = album;
        musicArtist = artist;
        musicDuration = duration;
        musicPath = path;
        musicImage = image;
        musicIndex = originalIdx;
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

    public Bitmap getMusicImage() {
        return musicImage;
    }

}
