package com.sussman.ponzu;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

public class SwipeAdapter extends FragmentPagerAdapter {
    public SwipeAdapter(FragmentManager fm, int tabCount) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        Bundle bundle;
        switch (position) {
            case 0:
                fragment=new MainFragment();
                bundle = new Bundle();
                bundle.putInt("count", position+1);
                fragment.setArguments(bundle);
                return fragment;
            case 1:
                fragment=new SecondFragment();
                bundle = new Bundle();
                bundle.putInt("count", position+1);
                fragment.setArguments(bundle);
                return fragment;
            case 2:
                fragment=new ThirdFragment();
                bundle = new Bundle();
                bundle.putInt("count", position+1);
                fragment.setArguments(bundle);
                return fragment;
            default:
                bundle = new Bundle();
                bundle.putInt("count", position+1);
                fragment=new MainFragment();
                fragment.setArguments(bundle);
                return fragment;
        }
    }

    @Override
    public int getCount() {

        return 3;
    }
}
