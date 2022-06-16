package com.nothing.unnamedplayer;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.Toast;
import android.util.Log;

import java.util.ArrayList;


/* TODO :   impl handling bluetooth and phone calling receiver
            impl summarized progress bar
            impl fragment resetting
*/
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int MY_PERMISSION_REQUEST = 1;
    private static final String[] permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    };
    private TabLayout tabLayout;
    private ViewPager viewPager;

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
        ArrayList<String> perms = new ArrayList<String>();

        for (String p: permissions){
            if (ContextCompat.checkSelfPermission(getApplicationContext(), p) != PackageManager.PERMISSION_GRANTED){
                perms.add(p);
            }
        }

        if (perms.size() > PackageManager.PERMISSION_GRANTED){
            //shouldShowRequestPermissionRationale
            //True: asked once. but declined.
            //False: first, or did check "Do not ask again"
            boolean shouldShow = false;
            for(String p: perms) {
                shouldShow |= shouldShowRequestPermissionRationale(p);
            }

            if (shouldShow) {
                Log.e(TAG,"shouldShowRequestPermissionRationale True");
                showConfirmDialog();
            }

            else{
                Log.e(TAG,"shouldShowRequestPermissionRationale False");
                ActivityCompat.requestPermissions(MainActivity.this, perms.toArray(new String[perms.size()]), MY_PERMISSION_REQUEST);
            }
        }

        //all perms are already granted
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
                    int res = 0;
                    for(int i = 0; i < grantResults.length; i++){
                        res += grantResults[i];
                    }

                    if (res == PackageManager.PERMISSION_GRANTED) {
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
        builder.setMessage("Read External Storage, Read Phone State");
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