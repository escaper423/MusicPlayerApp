package com.nothing.unnamedplayer;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class CurrentPlaylistAdapter extends RecyclerView.Adapter<CurrentPlaylistAdapter.ViewHolder>{
    private MusicManager musicManager = MusicManager.getInstance();
    private static final String TAG = "CurrentPlaylistAdapter";

    private ArrayList<Music> musicList;
    private Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        //Music Info
        TextView musicTitle;
        TextView musicArtist;
        TextView musicDuration;
        RelativeLayout parentLayout;

        public ViewHolder(View itemView){
            super(itemView);
            musicTitle = itemView.findViewById(R.id.cmusicTitle);
            musicArtist = itemView.findViewById(R.id.cmusicArtist);
            musicDuration = itemView.findViewById(R.id.cmusicDuration);
            parentLayout = itemView.findViewById(R.id.ctrack_list);
        }
    }

    public CurrentPlaylistAdapter(ArrayList<Music> m, Context context) {
        musicList = m;
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.currenttrack_listitem, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        Log.d(TAG,"onBindViewHolder Called.");
        final Music m = musicList.get(i);

        if (musicManager.getCurrentIndex() == i){
            viewHolder.parentLayout.setBackgroundResource(R.drawable.background_green);
        }
        else{
            viewHolder.parentLayout.setBackgroundResource(R.drawable.background_red);
        }

        viewHolder.musicTitle.setText(m.getMusicTitle());
        viewHolder.musicArtist.setText(m.getMusicArtist());
        viewHolder.musicDuration.setText(musicInfoConverter.durationConvert(m.getMusicDuration()));
        viewHolder.parentLayout.setOnClickListener(view -> {
            //Play Music Function by clicking one of elements in the list
            musicManager.setCurrentIndex(i);

            //Switch Activity
            Intent in = new Intent(mContext,PlayActivity.class);
            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            mContext.startActivity(in);

            //Start Service
            Intent serviceIntent = new Intent(mContext, PlayerService.class);
            serviceIntent.setAction(Actions.ACTION_PLAY);
            mContext.startService(serviceIntent);

        });
    }

    @Override
    public int getItemCount() {
        return musicManager.getCurrentMusicList().size();
    }
}
