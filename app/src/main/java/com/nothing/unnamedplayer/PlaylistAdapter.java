package com.nothing.unnamedplayer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {
    private static final String TAG = "PlaylistAdapter";
    private ArrayList<String> playlistNames;
    private Context mContext;
    private SharedPreferences sharedPreferences;
    private EditText newPlaylistNameText;
    private Gson gson;

    private TextView playbackSpeedMultText;
    private Button plusButton;
    private Button plusFiveButton;
    private Button minusButton;
    private Button minusFiveButton;

    private Context rootContext;
    private AlertDialog.Builder builder;
    private View v;

    private MusicManager musicManager;

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
        musicManager = MusicManager.getInstance();
        playlistNames = p;
        Log.e(TAG,"Constructor Called");
        sharedPreferences = mContext.getSharedPreferences("playLists", Context.MODE_PRIVATE);
        SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = (sharedPreferences, key) -> {
            Log.e(TAG,"sharedPreferenceChanged.");
            notifyDataSetChanged();
        };
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
        gson = new Gson();
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

        TypeToken<Playlist> token = new TypeToken<Playlist>(){};
        final Playlist playlist = gson.fromJson(playlistString, token.getType());

        if (playlist != null) {
            viewHolder.listName.setText(listName);
            viewHolder.countTrack.setText(String.format("%d Track(s)", playlist.getCountTrack()));
            viewHolder.speedMult.setText(String.format("%.2f x", playlist.getSpeedMult()));

            viewHolder.parentLayout.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, PlaylistActivity.class);
                intent.putExtra("playListName", listName);
                mContext.startActivity(intent);
            });

            viewHolder.playlistOption.setOnClickListener(view -> {
                PopupMenu popup = new PopupMenu(mContext, view);
                popup.getMenuInflater().inflate(R.menu.menu_playlist, popup.getMenu());
                popup.show();
                popup.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        //Delete Playlist
                        case R.id.playlist_menu_delete:
                            //Stop Player
                            musicManager.dispatchMusicDelete(mContext);

                            //Playlist modification
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            playlistNames.remove(i);
                            editor.remove(listName);
                            editor.commit();

                            Toast.makeText(mContext.getApplicationContext(), "Deleted Playlist " + listName, Toast.LENGTH_SHORT).show();
                            sendUpdateBoradcast(view);
                            break;
                        //Rename Playlist
                        case R.id.playlist_menu_rename:
                            rootContext = view.getRootView().getContext();
                            builder = new AlertDialog.Builder(rootContext);
                            v = LayoutInflater.from(rootContext).inflate(R.layout.rename_playlist_input, null);

                            builder.setView(v)
                                    .setTitle("Rename Playlist")
                                    .setNegativeButton("cancel",
                                            (dialog, which) -> {
                                            })
                                    .setPositiveButton("ok",
                                            (dialog, which) -> {
                                                try {
                                                    String newName = newPlaylistNameText.getText().toString();
                                                    String playlistString1 = gson.toJson(playlist);
                                                    SharedPreferences.Editor editor1 = sharedPreferences.edit();

                                                    playlistNames.remove(i);
                                                    playlistNames.add(i, newName);

                                                    editor1.remove(listName);
                                                    editor1.putString(newName, playlistString1);
                                                    editor1.commit();
                                                    sendUpdateBoradcast(v);
                                                    Toast.makeText(mContext.getApplicationContext(),
                                                            "Changed playlist name from " + listName + " to " + newName, Toast.LENGTH_SHORT)
                                                            .show();
                                                }
                                                catch (Exception e){
                                                    e.printStackTrace();
                                                }
                                            });
                            newPlaylistNameText = v.findViewById(R.id.rename_playlist_name);
                            builder.show();
                            break;
                        case R.id.playlist_menu_multchange:
                            rootContext = view.getRootView().getContext();
                            builder = new AlertDialog.Builder(rootContext);
                            v = LayoutInflater.from(rootContext).inflate(R.layout.change_speedmult_input, null);

                            builder.setView(v)
                                    .setTitle("Change Playback Speedmult")
                                    .setNegativeButton("cancel",
                                            (dialog, which) -> {
                                            })
                                    .setPositiveButton("ok",
                                            (dialog, which) -> {
                                                try {
                                                    playlist.setSpeedMult(Float.parseFloat(playbackSpeedMultText.getText().toString()));
                                                    String playlistString12 = gson.toJson(playlist);
                                                    SharedPreferences.Editor editor12 = sharedPreferences.edit();
                                                    editor12.putString(listName, playlistString12);
                                                    editor12.commit();
                                                    sendUpdateBoradcast(v);
                                                    Toast.makeText(mContext.getApplicationContext(),
                                                            "Changed playback speedmult to " + playbackSpeedMultText.getText().toString(), Toast.LENGTH_SHORT)
                                                            .show();
                                                }
                                                catch(Exception e){
                                                    e.printStackTrace();
                                                }
                                            });
                            playbackSpeedMultText = v.findViewById(R.id.change_speedmult_multiplier);
                            playbackSpeedMultText.setText(String.format("%.2f", playlist.getSpeedMult()));

                            plusButton = v.findViewById(R.id.change_speedmult_plus);
                            plusFiveButton = v.findViewById(R.id.change_speedmult_plusfive);
                            minusButton = v.findViewById(R.id.change_speedmult_minus);
                            minusFiveButton = v.findViewById(R.id.change_speedmult_minusfive);

                            plusButton.setOnClickListener(v -> {
                                float mult = Float.parseFloat(playbackSpeedMultText.getText().toString());
                                mult += 0.01f;
                                if (mult > 2.00f)
                                    mult = 2.00f;
                                playbackSpeedMultText.setText(String.format("%.2f", mult));
                            });

                            plusFiveButton.setOnClickListener(v -> {
                                float mult = Float.parseFloat(playbackSpeedMultText.getText().toString());
                                mult += 0.05f;
                                if (mult > 2.00f)
                                    mult = 2.00f;
                                playbackSpeedMultText.setText(String.format("%.2f", mult));
                            });

                            minusButton.setOnClickListener(v -> {
                                float mult = Float.parseFloat(playbackSpeedMultText.getText().toString());
                                mult -= 0.01f;
                                if (mult < 0.25f)
                                    mult = 0.25f;
                                playbackSpeedMultText.setText(String.format("%.2f", mult));
                            });

                            minusFiveButton.setOnClickListener(v -> {
                                float mult = Float.parseFloat(playbackSpeedMultText.getText().toString());
                                mult -= 0.05f;
                                if (mult < 0.25f)
                                    mult = 0.25f;
                                playbackSpeedMultText.setText(String.format("%.2f", mult));
                            });
                            builder.show();
                            break;
                        default:
                            break;
                    }
                    return true;
                });
            });
        }
    }

    private void sendUpdateBoradcast(View v) {
        Intent updateIntent = new Intent(Actions.ACTION_BOOKMARK_UPDATED);
        v.getContext().sendBroadcast(updateIntent);
    }
    @Override
    public int getItemCount() {
        return playlistNames.size();
    }

}
