package com.nothing.unnamedplayer;

import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TracklistFragment extends Fragment {

    private MusicManager musicManager;

    public TracklistFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_track, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.track_recycler_view);
        TracklistAdapter trackListAdapter = new TracklistAdapter(musicManager.getStoredMusicList(),getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(trackListAdapter);
        return view;
    }

    @Override
    public void onCreate (@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        musicManager = MusicManager.getInstance();
        musicManager.initMusicList(getContext(),null, MediaStore.Audio.Media.DURATION + ">= 20000" ,null);
        Log.d("MusicList","Music List Size : "+musicManager.getMusicListSize());
    }
}