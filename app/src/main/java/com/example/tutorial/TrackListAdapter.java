package com.example.tutorial;

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

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class TrackListAdapter extends RecyclerView.Adapter<TrackListAdapter.ViewHolder>{

    private static final String TAG = "TrackListAdapter";
    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        //Music Info
        CircleImageView musicImage;
        TextView musicTitle;
        TextView musicArtist;
        TextView musicDuration;
        RelativeLayout parent_layout;


        public ViewHolder(View itemView){
            super(itemView);
            musicImage = itemView.findViewById(R.id.musicImage);
            musicTitle = itemView.findViewById(R.id.musicTitle);
            musicArtist = itemView.findViewById(R.id.musicArtist);
            musicDuration = itemView.findViewById(R.id.musicDuration);
            parent_layout = itemView.findViewById(R.id.track_list);

        }
    }
    private static final String tag = "TrackListAdapter";

    private ArrayList<Music> musicList;
    private Context mContext;

    public TrackListAdapter(ArrayList<Music> musiclist, Context context) {
        musicList = musiclist;
        mContext = context;
    }
    public TrackListAdapter() {

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.track_listitem, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder,final int i) {
        Log.d(tag,"onBildViewholder Called.");
        Glide.with(mContext)
                .load(musicList.get(i).getMusicPath())
                .placeholder(R.drawable.ic_music_basic)
                .into(viewHolder.musicImage);

        viewHolder.musicTitle.setText(musicList.get(i).getMusicTitle());
        viewHolder.musicArtist.setText(musicList.get(i).getMusicArtist());
        viewHolder.musicDuration.setText(musicInfoConverter.durationConvert(musicList.get(i).getMusicDuration()));
        viewHolder.parent_layout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Play Music Function
                Intent in = new Intent(mContext,PlayActivity.class);
                in.putExtra("music_title",musicList.get(i).getMusicTitle());
                in.putExtra("music_artist",musicList.get(i).getMusicArtist());
                in.putExtra("music_album",musicList.get(i).getMusicAlbum());
                in.putExtra("music_duration",musicList.get(i).getMusicDuration());
                in.putExtra("music_path",musicList.get(i).getMusicPath());

                Log.d(TAG,"Title : " + in.getStringExtra("music_title"));
                Log.d(TAG,"Artist : " + in.getStringExtra("music_artist"));
                Log.d(TAG,"Album : " + in.getStringExtra("music_album"));
                Log.d(TAG,"Duration : " + in.getStringExtra("music_duration"));
                mContext.startActivity(in);
            }
        });
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }
}
