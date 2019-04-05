package com.example.tutorial;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TrackFragment extends Fragment {
    private static final int MY_PERMISSION_REQUEST = 1;

    private RecyclerView recyclerView;
    private MusicManager musicManager;
    private Context mContext;

    public TrackFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        View view = inflater.inflate(R.layout.fragment_track, container, false);
        recyclerView = view.findViewById(R.id.track_recycler_view);
        TrackListAdapter trackListAdapter = new TrackListAdapter(musicManager.getMusicList(),getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(trackListAdapter);
        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(mContext, "granted.", Toast.LENGTH_SHORT).show();

                        //do something
                    } else {
                        Toast.makeText(mContext, "not granted.", Toast.LENGTH_SHORT).show();
                    }
                }
        }
    }

    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED){
            if(shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},MY_PERMISSION_REQUEST);
            }
            else{
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},MY_PERMISSION_REQUEST);
            }
        }
        else{
            //do something
        }
    }

    @Override
    public void onCreate (@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        musicManager = MusicManager.getInstance();
        mContext = getContext();
        checkPermission();
        musicManager.getMusic(getContext());
        Collections.sort(musicManager.getMusicList(),new Comparator<Music>(){
            @Override
            public int compare(Music m1, Music m2){
                return m1.getMusicTitle().toLowerCase().compareTo(m2.getMusicTitle().toLowerCase());
            }
        });
        Log.d("MusicList","Music List Size : "+musicManager.getMusicList().size());
    }

}