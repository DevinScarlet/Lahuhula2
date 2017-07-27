package com.android.lahuhula.adapter;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by lenovo on 2017/1/2.
 */

public class PictureViewPagerAdapter extends PagerAdapter {

    private ViewPager mViewPager;
    private ArrayList<View> mPagerViewList = new ArrayList<View>();

    public PictureViewPagerAdapter(ViewPager viewPager, ArrayList<View> pagerViewList) {
        mViewPager = viewPager;
        mPagerViewList = pagerViewList;
        //mViewPager.setAdapter(this);
        mViewPager.setOnPageChangeListener(mOnPageChangeListener);
    }

    @Override
    public int getCount() {
        return mPagerViewList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position,
                            Object object) {
        // TODO Auto-generated method stub
        container.removeView(mPagerViewList.get(position));
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // TODO Auto-generated method stub
        container.addView(mPagerViewList.get(position));
        return mPagerViewList.get(position);
    }

    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };
}
