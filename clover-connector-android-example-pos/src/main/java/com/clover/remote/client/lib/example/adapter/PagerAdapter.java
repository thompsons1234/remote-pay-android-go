package com.clover.remote.client.lib.example.adapter;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by blakewilliams on 1/18/16.
 */
public class PagerAdapter extends FragmentPagerAdapter {
    List<Fragment> fragments = new ArrayList<>();

    public PagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    public void addFragment(Fragment fragment) {
        fragments.add(fragment);
    }

    @Override
    public Fragment getItem(int position) {
        if(position <0 || position > 3){
            throw new IllegalArgumentException("position must be 0-3");
        }
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
