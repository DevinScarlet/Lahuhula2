package com.android.lahuhula.fragment;

import android.content.Intent;
//import android.app.Fragment;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.lahuhula.activity.OneVillageActivity;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;

import com.android.lahuhula.R;

/**
 * Created by lenovo on 2016/12/27.
 */

public class HuhuFragment extends Fragment {

    private ViewPager mViewPager;
    private LinearLayout mScrollViewLayout;
    private ArrayList<View> mPagerViewList = new ArrayList<View>();
    private static final int PAGER_NUMS = 4;
    private static final int COLUMN_NUMS = 3;
    private static final String[] mEntryTitle = new String[] {"一村", "一品", "一壶壶","一山", "一水",/* "一天下",
            "一村", "一品", "一壶壶","一村", "一品", "一壶壶","一村", "一品", "一壶壶","一村", "一品", "一壶壶",
            "一村", "一品", "一壶壶",*/};
    private static final int[] mEntryIconRes = new int[] {R.drawable.viliage, R.drawable.product, R.drawable.hu,
            R.drawable.mountain, R.drawable.water};

    public HuhuFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.home_page, container, false);
        initViewPager(rootView, inflater);
        initScrollView(rootView, inflater);
        return rootView;
    }

    private void initViewPager(View rootView, LayoutInflater inflater) {
        mViewPager = (ViewPager) rootView.findViewById(R.id.container);

        mPagerViewList.clear();
        for (int i = 0; i < PAGER_NUMS; i++) {
            View view = inflater.inflate(R.layout.fragment_main, null);
            int resId = getContext().getResources().getIdentifier("news_" + (i + 1),
                    "drawable", getContext().getPackageName());
            view.setBackgroundResource(resId);
            mPagerViewList.add(view);
        }
        mViewPager.setOnPageChangeListener(mOnPageChangeListener);
        mViewPager.setAdapter(mPagerAdapter);
        CirclePageIndicator cpi = (CirclePageIndicator) rootView.findViewById(R.id.indicator);
        cpi.setViewPager(mViewPager);
        cpi.setRadius(8);
    }

    private View.OnClickListener mItemClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            final Intent villageIntent = new Intent(getContext(), OneVillageActivity.class);
            getActivity().startActivity(villageIntent);
        }
    };

    private void initScrollView(View rootView, LayoutInflater inflater) {
        mScrollViewLayout = (LinearLayout) rootView.findViewById(R.id.home_page_scroll_view);
        LinearLayout layout = null;
        for (int index = 0; index < mEntryTitle.length; index += COLUMN_NUMS) {
            if (index % COLUMN_NUMS == 0) {
                layout = (LinearLayout) inflater.inflate(R.layout.scroll_view_layout, null);
            }
            if (layout != null) {
                for (int i = index; i < index + COLUMN_NUMS; i++) {
                    View view = inflater.inflate(R.layout.entry_list_cell, null);
                    TextView tv = (TextView) view.findViewById(R.id.entry_tv);
                    tv.setOnClickListener(mItemClickListener);
                    if (i < mEntryTitle.length) {
                        tv.setText(""/*mEntryTitle[i]*/);
                        tv.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(mEntryIconRes[i]), null, null);
                    } else {
                        tv.setVisibility(View.INVISIBLE);
                    }
                    layout.addView(view, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));
                }
                mScrollViewLayout.addView(layout);
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {
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

    private PagerAdapter mPagerAdapter = new PagerAdapter() {
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
    };
}
