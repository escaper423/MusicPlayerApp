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
    private String listName;
    private Gson gson;
    private SharedPreferences sp;


    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        //Music Info
        TextView musicTitle;
        TextView musicArtist;
        TextView musicDuration;
        ImageView musicOption;
        RelativeLayout parentLayout;


        public ViewHolder(View itemView){
            super(itemView);
            musicTitle = itemView.findViewById(R.id.musicTitle);
            musicArtist = itemView.findViewById(R.id.musicArtist);
            musicDuration = itemView.findViewById(R.id.musicDuration);
            parentLayout = itemView.findViewById(R.id.track_list);
            musicOption = itemView.findViewById(R.id.musicOption);
        }
    }


    public PlaylistTrackAdapter(String name, Context context) {
        listName = name;
        mContext = context;
        gson = new Gson();
        sp = mContext.getSharedPreferences("playLists",Context.MODE_PRIVATE);
        SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Log.e(TAG,"sharedPreferenceChanged.");
                notifyDataSetChanged();
            }
        };
        sp.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

        String playlistString = sp.getString(listName,"");

        TypeToken<Playlist> token = new TypeToken<Playlist>(){};
        playlist = gson.fromJson(playlistString, token.getType());
        musicList = playlist.getMusicList();
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


        //Option Menu
        viewHolder.musicOption.setOnClickListener(new View.OnClickListener(){
            public void onClick(final View view){
                PopupMenu popup = new PopupMenu(mContext,view);
                popup.getMenuInflater().inflate(R.menu.menu_playlist_trackitem,popup.getMenu());
                popup.show();
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item){
                        switch(item.getItemId()){
                            //Add to playlist
                            case R.id.playlist_trackitem_delete:
                                Toast.makeText(mContext.getApplicationContext(),"Deleted "+viewHolder.musicTitle.getText().toString()+" from playlist",Toast.LENGTH_SHORT).show();
                                musicList.remove(i);
                                musicManager.setCurrentMusicList(musicList);
                                SharedPreferences.Editor editor = sp.edit();

                                //Stop actual player
                                Intent serviceIntent = new Intent(Actions.ACTION_PLAYLISTTRACK_UPDATED);
                                mContext.sendBroadcast(serviceIntent);

                                if (musicList.size() == 0){
                                    editor.remove(listName);
                                    editor.apply();

                                    //All element deleted.
                                    Intent in = new Intent(mContext, MainActivity.class);
                                    in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    mContext.startActivity(in);

                                }
                                else {
                                    //deleted element. must rearrange index set
                                    for (int idx = 0; idx < musicList.size(); idx++) {
                                        musicList.get(idx).setMusicIndex(idx);
                                    }
                                    playlist.setCountTrack(musicList.size());
                                    String newListString = gson.toJson(playlist);
                                    editor.putString(listName, newListString);
                                    editor.apply();
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
