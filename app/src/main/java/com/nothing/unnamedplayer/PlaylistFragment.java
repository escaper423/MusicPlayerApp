package com.nothing.unnamedplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Map;

public class PlaylistFragment extends Fragment {
    private RecyclerView recyclerView;

    public PlaylistFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);
        recyclerView = view.findViewById(R.id.playlist_recycler_view);

        ArrayList<String> playListNames = new ArrayList<>();

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("playLists", Context.MODE_PRIVATE);
        Map<String, ?> playlistData = sharedPreferences.getAll();
        for(Map.Entry<String,?> entry: playlistData.entrySet()){
            playListNames.add(entry.getKey());
            Log.e("Playlist Add", entry.getKey());
        }

        PlaylistAdapter playListAdapter = new PlaylistAdapter(playListNames, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(playListAdapter);
        return view;
    }

    @Override
    public void onCreate (@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("PlaylistFragment","onCreate Called");
    }
}
