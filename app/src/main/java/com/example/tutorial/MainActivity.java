package com.example.tutorial;

import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tabLayout = (TabLayout) findViewById(R.id.main_tablayout);
        viewPager = (ViewPager) findViewById(R.id.main_pager);
        ViewPagerAdapter adaptor = new ViewPagerAdapter(getSupportFragmentManager());

        adaptor.AddFragment(new TrackFragment(), "Tracks");
        adaptor.AddFragment(new BlankFragment(), "Artists");
        adaptor.AddFragment(new BlankFragment(), "Bookmarks");
        adaptor.AddFragment(new BlankFragment(), "Folder");

        viewPager.setAdapter(adaptor);
        tabLayout.setupWithViewPager(viewPager);
    }

    public void onClick(View v)
    {

    }
}
