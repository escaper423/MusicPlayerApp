package com.nothing.unnamedplayer;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CurrentPlaylistFragment extends Fragment {
    private MusicManager musicManager;
    private RecyclerView recyclerView;

    public CurrentPlaylistFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_current_playlist, container, false);
        recyclerView = view.findViewById(R.id.current_track_recycler_view);
        CurrentPlaylistAdapter currentPlaylistAdapter = new CurrentPlaylistAdapter(musicManager.getCurrentMusicList(), getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(currentPlaylistAdapter);
        return view;
    }

    @Override
    public void onCreate (@Nullable Bundle savedInstanceState) {
        musicManager = MusicManager.getInstance();
        super.onCreate(savedInstanceState);
    }
}
