package com.remvp.library.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * ViewPager+Fragment的Adapter
 * <p>
 * {@link FragmentPagerAdapter}适用Fragment少的情况，每一个Fragment保存在内存中
 * {@link android.support.v4.app.FragmentStatePagerAdapter}适用于Fragment多的情况，
 * 切换不同的Fragment时会保存Bundle，切换回来时恢复
 */
public class FragmentAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragments;
    private List<String> titles;

    public FragmentAdapter(FragmentManager fm, List<Fragment> fragments, List<String> titles) {
        super(fm);
        this.fragments = fragments;
        this.titles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }
}
