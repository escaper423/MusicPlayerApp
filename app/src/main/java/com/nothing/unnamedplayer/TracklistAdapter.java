package com.nothing.unnamedplayer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class TracklistAdapter extends RecyclerView.Adapter<TracklistAdapter.ViewHolder>{
    private MusicManager musicManager = MusicManager.getInstance();
    private static final String TAG = "TrackListAdapter";

    private Button plusButton;
    private Button plusFiveButton;
    private Button minusButton;
    private Button minusFiveButton;
    private EditText playlistText;
    private TextView multText;

    private ArrayList<Music> musicList;
    private Context mContext;

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


    public TracklistAdapter(ArrayList<Music> musiclist, Context context) {
        musicList = musiclist;
        mContext = context;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.track_listitem, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    private void deleteCascade(String uri){
        Gson gson = new Gson();

        //Loading all bookmark
        SharedPreferences sp = mContext.getSharedPreferences("playLists", Context.MODE_PRIVATE);
        Map<String, ?> playlistData = sp.getAll();
        if (playlistData != null){
            Log.e(TAG,"Modifying bookmark lists...");
            for(Map.Entry<String,?> entry: playlistData.entrySet()){
                String bookmarkName = entry.getKey();

                TypeToken<Playlist> token = new TypeToken<Playlist>() {};
                Playlist playList = gson.fromJson(entry.getValue().toString(), token.getType());
                ArrayList<Music> tracks = playList.getMusicList();

                /*  delete cascading music
                    due to ConcurrentModificationException, i used iterator to remove   */
                Iterator<Music> it = tracks.iterator();
                while(it.hasNext()) {
                    if (it.next().getMusicPath().equals(uri)) {
                        it.remove();
                    }
                }

                //Putting modified list to the bookmark back
                playList.setCountTrack(tracks.size());
                SharedPreferences.Editor editor = sp.edit();

                if (tracks.size() == 0){
                    editor.remove(bookmarkName);
                }
                else {
                    String newBookmark = gson.toJson(playList);
                    editor.putString(bookmarkName, newBookmark);
                    Log.e(TAG,"Bookmark has emptied. so deleted it");
                }
                editor.apply();
                Log.e(TAG,"Modification done.");
            }
        }
        else{
            Log.e(TAG,"The bookmarks are not exist.");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        final Music m = musicList.get(i);
        viewHolder.musicTitle.setText(m.getMusicTitle());
        viewHolder.musicArtist.setText(m.getMusicArtist());
        viewHolder.musicDuration.setText(musicInfoConverter.durationConvert(m.getMusicDuration()));
        viewHolder.parentLayout.setOnClickListener(view -> {
            //Play Music Function by clicking one of elements in the list
            musicManager.resetMusicList();
            musicManager.setShuffling(false);
            musicManager.preparePlaybackSpeed(1.0f);
            musicManager.setCurrentIndex(i);

            //Switch Activity
            Intent in = new Intent(mContext,PlayActivity.class);
            mContext.startActivity(in);

            //Start Service
            Intent serviceIntent = new Intent(mContext, PlayerService.class);
            serviceIntent.setAction(Actions.ACTION_PLAY);
            mContext.startService(serviceIntent);

        });

        //Option Menu
        viewHolder.musicOption.setOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(mContext,view);
            popup.getMenuInflater().inflate(R.menu.menu_trackitem,popup.getMenu());
            popup.show();
            popup.setOnMenuItemClickListener(item -> {
                switch(item.getItemId()){
                    //Add to playlist
                    case R.id.trackitem_menu_addlist:
                        Toast.makeText(mContext.getApplicationContext(),"Clicked: 'Add to playlist'",Toast.LENGTH_SHORT).show();
                        openDialog(view, i);
                        break;

                    case R.id.trackitem_menu_delete:
                        Toast.makeText(mContext.getApplicationContext(),"Clicked: 'Delete'",Toast.LENGTH_SHORT).show();
                        Log.d(TAG,"Delete item clicked.");
                        File to_delete = new File(m.getMusicPath());
                        if (to_delete.exists()){
                            //Stop actual player
                            musicManager.dispatchMusicDelete(mContext);

                            Toast.makeText(mContext.getApplicationContext(),"Deleted: "+m.getMusicTitle(),Toast.LENGTH_SHORT).show();
                            to_delete.delete();
                            deleteCascade(m.getMusicPath());

                            Log.d(TAG, "Path: "+m.getMusicPath());
                            Log.d(TAG,Integer.toString(musicList.size()));
                        }
                        break;

                    default:
                        break;
                }
                return true;
            });
        });


    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    private void openDialog(final View v, final int i){
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getRootView().getContext());
        View view = LayoutInflater.from(v.getRootView().getContext()).inflate(R.layout.addlist_input, null);

        builder.setView(view)
                .setTitle("Add Playlist")
                .setNegativeButton("cancel", (dialog, which) -> {

                })
                .setPositiveButton("ok",
                        (dialog, which) -> {

                        });

        plusButton = view.findViewById(R.id.addlist_multplus);
        plusFiveButton = view.findViewById(R.id.addlist_multplusfive);
        minusButton = view.findViewById(R.id.addlist_multminus);
        minusFiveButton = view.findViewById(R.id.addlist_multminusfive);
        multText = view.findViewById(R.id.addlist_multiplier);
        playlistText = view.findViewById(R.id.addlist_name);

        plusButton.setOnClickListener(v1 -> {
            float mult = Float.parseFloat(multText.getText().toString());
            mult += 0.01f;
            if (mult > 2.00f)
                mult = 2.00f;
            multText.setText(String.format("%.2f",mult));
        });

        plusFiveButton.setOnClickListener(v12 -> {
            float mult = Float.parseFloat(multText.getText().toString());
            mult += 0.05f;
            if (mult > 2.00f)
                mult = 2.00f;
            multText.setText(String.format("%.2f",mult));
        });

        minusButton.setOnClickListener(v13 -> {
            float mult = Float.parseFloat(multText.getText().toString());
            mult -= 0.01f;
            if (mult < 0.25f)
                mult = 0.25f;
            multText.setText(String.format("%.2f",mult));
        });

        minusFiveButton.setOnClickListener(v14 -> {
            float mult = Float.parseFloat(multText.getText().toString());
            mult -= 0.05f;
            if (mult < 0.25f)
                mult = 0.25f;
            multText.setText(String.format("%.2f",mult));
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v15 -> {
            if (playlistText.getText().toString().length() == 0){
                Toast.makeText(mContext.getApplicationContext(), "Playlist name must be at least 1 character.", Toast.LENGTH_SHORT).show();
            }
            else{
                SharedPreferences sharedPreferences = mContext.getSharedPreferences("playLists", Context.MODE_PRIVATE);
                Gson gson = new Gson();
                String listName = playlistText.getText().toString();
                String listMult = multText.getText().toString();

                Toast.makeText(mContext.getApplicationContext(),"Added Listname: "+listName+" SpeedMult: "+listMult,Toast.LENGTH_SHORT).show();

                //Loading playlist and append to it
                Playlist playList;
                if (sharedPreferences.contains(listName)) {
                    String tracks = sharedPreferences.getString(listName, "");
                    TypeToken<Playlist> token = new TypeToken<Playlist>() {};
                    playList = gson.fromJson(tracks, token.getType());
                }
                else{
                    playList = new Playlist();
                }

                //Adding a music to playlist object
                Music musicToAdd = musicManager.getStoredMusicByIndex(i);
                musicToAdd.setMusicIndex(playList.getCountTrack());
                playList.getMusicList().add(musicToAdd);

                //Setting playlist metadata
                playList.setSpeedMult(Float.parseFloat(listMult));
                playList.setCountTrack(playList.getMusicList().size());

                //Actual playlist adding (from playlist object)
                String listToAdd = gson.toJson(playList);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(listName,listToAdd);
                editor.apply();

                //Sending playlist modification broadcast
                Intent addIntent = new Intent(Actions.ACTION_PLAYLIST_TRACK_UPDATED);
                v15.getContext().sendBroadcast(addIntent);

                dialog.dismiss();

            }
        });
    }
}
