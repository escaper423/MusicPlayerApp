package com.nothing.unnamedplayer;

import java.util.ArrayList;

public class Playlist {
    private ArrayList<Music> musicList;
    private float speedMult = 1.0f;
    private int numTrack = 0;

    public Playlist(){
        musicList = new ArrayList<Music>();
    }
    public ArrayList<Music> getMusicList(){
        return musicList;
    }

    public void setMusicList(ArrayList<Music> m){
        musicList = m;
    }

    public float getSpeedMult(){
        return speedMult;
    }

    public void setSpeedMult(float f){
        speedMult = f;
    }

    public int getCountTrack(){
        return numTrack;
    }

    public void setCountTrack(int i){
        numTrack = i;
    }

}
