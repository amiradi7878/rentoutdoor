package com.tanjungdev.rentoutdoor.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import java.util.ArrayList;

/**
 * Created by everjn on 4/19/16.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter{


        ArrayList<Fragment> fragments = new ArrayList<>();
        ArrayList<String> tabTitles = new ArrayList<>();

        public void addFragments (Fragment fragments, String tabTitles)
        {
            this.fragments.add(fragments);
            this.tabTitles.add(tabTitles);
        }

        public ViewPagerAdapter (FragmentManager fm) {
            super(fm);


        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }


    }