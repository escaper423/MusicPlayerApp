package com.nothing.unnamedplayer;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/* TODO :   impl handling bluetooth and phone calling receiver
            impl summarized progress bar
            impl fragment resetting
*/
public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";
    private static final int MY_PERMISSION_REQUEST = 1;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private MusicManager musicManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
    }

/*
    @Override
    public void onConfigurationChanged(Configuration newConfig) {

    }
*/
    protected void checkPermission() {
        //at least one of permission is not granted
        Log.e(TAG,"Checking Permission...");
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

            //asked once. but declined.
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Log.e(TAG,"shouldShowRequestPermissionRationale True");
                showConfirmDialog();
            }

            //first
            else{
                Log.e(TAG,"shouldShowRequestPermissionRationale False");
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            }
        }

        //all perms are all granted
        else{
            Log.e(TAG,"Loading main menu");
            initMenu();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        switch (requestCode) {
            case MY_PERMISSION_REQUEST:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission Granted.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG,"Granted");
                        initMenu();
                    }
                    else{
                        Toast.makeText(this, "Permission Denied.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG,"Denied");
                        showConfirmDialog();
                    }
                }
                break;
        }
    }

    protected void initMenu() {
        musicManager = MusicManager.getInstance();
        tabLayout = (TabLayout) findViewById(R.id.main_tablayout);
        viewPager = (ViewPager) findViewById(R.id.main_pager);
        ViewPagerAdapter adaptor = new ViewPagerAdapter(getSupportFragmentManager());

        adaptor.AddFragment(new TracklistFragment(), "Stored Tracks");
        adaptor.AddFragment(new PlaylistFragment(), "Playlists");
        adaptor.AddFragment(new CurrentPlaylistFragment(), "Current Playing");

        viewPager.setAdapter(adaptor);
        tabLayout.setupWithViewPager(viewPager);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
    }

    protected void showConfirmDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Required Permissions");
        builder.setMessage("Read External Storage");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                /*ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSION_REQUEST
                );*/
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", getPackageName(), null));
                startActivity(intent);
                finish();
            };
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                System.exit(3);
            };
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed(){
        Toast.makeText(getApplicationContext(), "Back Pressed.", Toast.LENGTH_SHORT).show();
        finish();
    }
}