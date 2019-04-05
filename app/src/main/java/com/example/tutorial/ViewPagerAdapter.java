package com.example.tutorial;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentPagerAdapter {
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
