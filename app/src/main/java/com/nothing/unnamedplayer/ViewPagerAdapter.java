package com.nothing.unnamedplayer;

import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    private final List<Fragment> fragmentList = new ArrayList<>();
    private final List<String> fragmentListTitles = new ArrayList<>();


    public ViewPagerAdapter(FragmentManager fm)
    {
        super(fm);
    }

    public Fragment getItem(int idx) {
        return fragmentList.get(idx);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @Override
    public int getItemPosition(Object obj) {return POSITION_NONE;}

    @Override
    public CharSequence getPageTitle(int idx)
    {
        return fragmentListTitles.get(idx);
    }


    public void AddFragment(Fragment fragment, String name)
    {
        fragmentList.add(fragment);
        fragmentListTitles.add(name);
    }

}
