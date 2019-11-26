package com.nothing.unnamedplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.nothing.unnamedplayer.R;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSION_REQUEST = 1;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private MusicManager musicManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        musicManager = MusicManager.getInstance();
        checkPermission();
        tabLayout = (TabLayout) findViewById(R.id.main_tablayout);
        viewPager = (ViewPager) findViewById(R.id.main_pager);
        ViewPagerAdapter adaptor = new ViewPagerAdapter(getSupportFragmentManager());

        adaptor.AddFragment(new TrackFragment(), "Stored Tracks");
        adaptor.AddFragment(new BlankFragment(), "Current Tracks");
        adaptor.AddFragment(new BlankFragment(), "Playlists");

        viewPager.setAdapter(adaptor);
        tabLayout.setupWithViewPager(viewPager);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getApplicationContext(), "granted.", Toast.LENGTH_SHORT).show();

                        //do something
                    } else {
                        Toast.makeText(getApplicationContext(), "not granted.", Toast.LENGTH_SHORT).show();
                    }
                }
        }
    }

    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED){
            if(shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},MY_PERMISSION_REQUEST);
            }
            else{
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},MY_PERMISSION_REQUEST);
            }
        }
        else{
            //do something
        }
    }
    public void onClick(View v)
    {

    }
}
