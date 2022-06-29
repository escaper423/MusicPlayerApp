package com.nothing.unnamedplayer;

public class Actions {
    //Actions used in PlayerService
    public static final String ACTION_PREV = "com.nothing.unnamedplayer.prev";
    public static final String ACTION_PLAY = "com.nothing.unnamedplayer.play";
    public static final String ACTION_PAUSE = "com.nothing.unnamedplayer.pause";
    public static final String ACTION_RESUME = "com.nothing.unnamedplayer.resume";
    public static final String ACTION_NEXT = "com.nothing.unnamedplayer.next";
    public static final String ACTION_END = "com.nothing.unnamedplayer.end";
    public static final String ACTION_VIEW = "com.nothing.unnamedplayer.view";

    //Actions used in PlayActivity
    public static final String ACTION_UPDATE = "com.nothing.unnamedplayer.update";
    public static final String ACTION_ORIENTATION_CHANGED = "com.nothing.unnamedplayer.orientation_changed";

    //Playlist detail changed. (adding or deleting an item of the playlist)
    public static final String ACTION_PLAYLIST_TRACK_UPDATED = "com.nothing.unnamedplayer.playlist_track_updated";

    //Current playlist changed.
    public static final String ACTION_CURRENT_PLAYLIST_CHANGED = "com.nothing.unnamedplayer.current_playlst_changed";

    //Need to refresh tabs
    public static final String ACTION_BOOKMARK_UPDATED = "com.nothing.unnamedplayer.bookmark_updated";
    public static final String ACTION_DELETE = "com.nothing.unnamedplayer.delete";
}
