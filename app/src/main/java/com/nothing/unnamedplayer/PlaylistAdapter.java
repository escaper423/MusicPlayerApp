package com.nothing.unnamedplayer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {
    private static final String TAG = "PlaylistAdapter";
    private ArrayList<String> playlistNames;
    private Context mContext;
    private SharedPreferences sharedPreferences;

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        //Playlist Info
        TextView listName;
        TextView countTrack;
        TextView speedMult;
        ImageView playlistOption;
        ConstraintLayout parentLayout;

        public ViewHolder(View itemView){
            super(itemView);
            listName = itemView.findViewById(R.id.playlist_name);
            countTrack = itemView.findViewById(R.id.playlist_count_track);
            speedMult = itemView.findViewById(R.id.playlist_speedmult);
            playlistOption = itemView.findViewById(R.id.playlist_menu_option);
            parentLayout = itemView.findViewById(R.id.playlist);
        }
    }

    public PlaylistAdapter(ArrayList<String> p, Context context){
        mContext = context;
        playlistNames = p;
        sharedPreferences = mContext.getSharedPreferences("playLists", Context.MODE_PRIVATE);
    }

    public PlaylistAdapter(){

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.playlist_item, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        Log.e(TAG, "onBindViewholder Called.");
        final String listName = playlistNames.get(i);
        String playlistString = sharedPreferences.getString(listName,"");
        Gson gson = new Gson();
        TypeToken<Playlist> token = new TypeToken<Playlist>(){};
        Playlist playlist = gson.fromJson(playlistString, token.getType());

        viewHolder.listName.setText(listName);
        viewHolder.countTrack.setText(String.format("%d Track(s)",playlist.getCountTrack()));
        viewHolder.speedMult.setText(String.format("%.2f x",playlist.getSpeedMult()));
        //TODO: Add onclick to get playlist activity
        viewHolder.parentLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PlaylistActivity.class);
                intent.putExtra("playListName",listName);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return playlistNames.size();
    }

}
