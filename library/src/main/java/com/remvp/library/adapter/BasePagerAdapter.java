package com.remvp.library.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public abstract class BasePagerAdapter extends PagerAdapter {

    @Override
    public abstract int getCount();

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    @Override
    public abstract Object instantiateItem(ViewGroup container, int position);

    @Override
    public void destroyItem(View container, int arg1, Object object) {
        ((ViewGroup) container).removeView((View) object);
    }


}
