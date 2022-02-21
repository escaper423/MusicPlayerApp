package com.nothing.unnamedplayer;

import static com.nothing.unnamedplayer.musicInfoConverter.durationConvert;
import static com.nothing.unnamedplayer.musicInfoConverter.getBitmapFromString;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class PlaylistActivity extends AppCompatActivity {
    private static final String TAG = "PlayListActivity";
    private Playlist playlist;
    private TextView title;
    private TextView speedMult;
    private TextView duration;
    private TextView numTrack;
    private ImageView image;

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedinstanceState){
        Log.e(TAG,"onCreate Called");
        super.onCreate(savedinstanceState);
        setContentView(R.layout.activity_playlist);
        Intent in = getIntent();
        String listName = in.getStringExtra("playListName");
        if (listName.isEmpty()){
            Toast.makeText(this, "Playlist does not exists.", Toast.LENGTH_SHORT).show();
            finish();
        }

        SharedPreferences sp = getSharedPreferences("playLists",MODE_PRIVATE);
        Gson gson = new Gson();

        if (sp.contains(listName)) {
            String tracks = sp.getString(listName, "");
            TypeToken<Playlist> token = new TypeToken<Playlist>() {};
            playlist = gson.fromJson(tracks, token.getType());
        }

        title = findViewById(R.id.pa_title);
        duration = findViewById(R.id.pa_duration);
        speedMult = findViewById(R.id.pa_speedmult);
        numTrack = findViewById(R.id.pa_numtrack);
        image = findViewById(R.id.pa_image);

        title.setText(listName);
        speedMult.setText(String.format("%.2fx",playlist.getSpeedMult()));
        int sum_duration = 0;
        String encoded_img = "";
        for(Music m : playlist.getMusicList()){
            sum_duration += m.getMusicDuration();
            sum_duration -= sum_duration%1000;
            if (!(m.getMusicImage().isEmpty())){
                encoded_img = m.getMusicImage();
            }
        }
        if (encoded_img.length() > 1){
            image.setImageBitmap(getBitmapFromString(encoded_img));
        }
        duration.setText(durationConvert(sum_duration));
        numTrack.setText(String.format("%d Track(s)",playlist.getCountTrack()));

        recyclerView = findViewById(R.id.pa_tracklist);
        PlaylistTrackAdapter playlistTrackAdapter = new PlaylistTrackAdapter(playlist,this);
        recyclerView.setAdapter(playlistTrackAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }
}
