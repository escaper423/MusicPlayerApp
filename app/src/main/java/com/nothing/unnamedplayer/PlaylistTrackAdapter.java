package com.nothing.unnamedplayer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.hdodenhof.circleimageview.CircleImageView;

public class PlaylistTrackAdapter extends RecyclerView.Adapter<PlaylistTrackAdapter.ViewHolder>{
    private MusicManager musicManager = MusicManager.getInstance();
    private static final String TAG = "PlaylistTrackAdapter";

    private Playlist playlist;
    private ArrayList<Music> musicList;
    private Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        //Music Info
        CircleImageView musicImage;
        TextView musicTitle;
        TextView musicArtist;
        TextView musicDuration;
        ImageView musicOption;
        RelativeLayout parentLayout;


        public ViewHolder(View itemView){
            super(itemView);
            musicImage = itemView.findViewById(R.id.musicImage);
            musicTitle = itemView.findViewById(R.id.musicTitle);
            musicArtist = itemView.findViewById(R.id.musicArtist);
            musicDuration = itemView.findViewById(R.id.musicDuration);
            parentLayout = itemView.findViewById(R.id.track_list);
            musicOption = itemView.findViewById(R.id.musicOption);
        }
    }


    public PlaylistTrackAdapter(Playlist p, Context context) {
        playlist = p;
        mContext = context;
        musicList = p.getMusicList();
    }

    public PlaylistTrackAdapter() {

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.track_listitem, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        Log.e(TAG,"onBildViewholder Called.");
        final Music m = musicList.get(i);
        Glide.with(mContext)
                .load(musicInfoConverter.getBitmapFromString(musicList.get(i).getMusicImage()))
                .placeholder(R.drawable.ic_music_basic)
                .into(viewHolder.musicImage);

        viewHolder.musicTitle.setText(m.getMusicTitle());
        viewHolder.musicArtist.setText(m.getMusicArtist());
        viewHolder.musicDuration.setText(musicInfoConverter.durationConvert(m.getMusicDuration()));
        viewHolder.parentLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Play Music Function by clicking one of elements in the list
                musicManager.setCurrentMusicList(musicList);
                musicManager.setShuffling(false);
                musicManager.preparePlaybackSpeed(playlist.getSpeedMult());
                musicManager.setCurrentIndex(i);
                //Switch Activity
                Intent in = new Intent(mContext,PlayActivity.class);
                mContext.startActivity(in);

                //Start Service
                Intent serviceIntent = new Intent(mContext, PlayerService.class);
                serviceIntent.setAction(Actions.ACTION_PLAY);
                mContext.startService(serviceIntent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

}
