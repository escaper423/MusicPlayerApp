package com.nothing.unnamedplayer;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;


import com.nothing.unnamedplayer.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class TrackListAdapter extends RecyclerView.Adapter<TrackListAdapter.ViewHolder>{
    private MusicManager musicManager = MusicManager.getInstance();
    private static final String TAG = "TrackListAdapter";

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        //Music Info
        CircleImageView musicImage;
        TextView musicTitle;
        TextView musicArtist;
        TextView musicDuration;
        ImageView musicOption;
        RelativeLayout parent_layout;


        public ViewHolder(View itemView){
            super(itemView);
            musicImage = itemView.findViewById(R.id.musicImage);
            musicTitle = itemView.findViewById(R.id.musicTitle);
            musicArtist = itemView.findViewById(R.id.musicArtist);
            musicDuration = itemView.findViewById(R.id.musicDuration);
            parent_layout = itemView.findViewById(R.id.track_list);
            musicOption = itemView.findViewById(R.id.musicOption);


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
        final Music m = musicList.get(i);
        Glide.with(mContext)
                .load(musicList.get(i).getMusicImage())
                .placeholder(R.drawable.ic_music_basic)
                .into(viewHolder.musicImage);

        viewHolder.musicTitle.setText(m.getMusicTitle());
        viewHolder.musicArtist.setText(m.getMusicArtist());
        viewHolder.musicDuration.setText(musicInfoConverter.durationConvert(m.getMusicDuration()));
        viewHolder.parent_layout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Play Music Function by clicking one of elements in the list
                musicManager.resetMusicList();
                musicManager.setShuffling(false);
                Intent in = new Intent(mContext,PlayActivity.class);
                in.putExtra("Index",i);
                mContext.startActivity(in);
            }
        });

        //Option Menu
        viewHolder.musicOption.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                PopupMenu popup = new PopupMenu(mContext,view);
                popup.getMenuInflater().inflate(R.menu.menu_trackitem,popup.getMenu());
                popup.show();
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item){
                        switch(item.getItemId()){
                            //Add to bookmark list
                            case R.id.trackitem_menu_addlist:
                                Toast.makeText(mContext.getApplicationContext(),"Clicked: 'Add to bookmark'",Toast.LENGTH_SHORT);
                                Log.d("Popup Menu","Add to bookmark item clicked.");
                                break;
                            case R.id.trackitem_menu_delete:
                                Toast.makeText(mContext.getApplicationContext(),"Clicked: 'Delete'",Toast.LENGTH_SHORT);
                                Log.d("Popup Menu","Delete item clicked.");
                                File to_delete = new File(m.getMusicPath());
                                if (to_delete.exists()){
                                    //to_delete.delete();
                                    musicList.remove(i);
                                    Toast.makeText(mContext.getApplicationContext(),"Deleted: "+m.getMusicTitle(),Toast.LENGTH_SHORT);
                                    notifyDataSetChanged();
                                    Log.d("Delete File", "Path: "+m.getMusicPath());
                                    Log.d("Delete File","Deleting...");
                                    Log.d("Music List:",Integer.toString(musicList.size()));
                                }
                                break;

                            default:
                                break;
                        }
                        return true;
                    }
                });
            }
        });


    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }
}
