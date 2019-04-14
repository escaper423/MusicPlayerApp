package com.example.tutorial;

import android.app.Application;

public class App extends Application {
    public static final String Channel_ID = "MediaPlayer";

    @Override
    public void onCreate(){
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel(){
        
    }
}
