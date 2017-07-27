package com.android.lahuhula.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.lahuhula.R;
import com.android.lahuhula.util.ImageUtils;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tao.zhang on 2017/3/1.
 */

public class SendImagePreviewEditActivity extends AppCompatActivity
        implements View.OnClickListener, ViewPager.OnPageChangeListener {

    private static final String TAG = SendImagePreviewEditActivity.class.getSimpleName();

    private ArrayList<String> mSelectedImagePathList = new ArrayList<String>();
    private List<View> mAllPagerView = new ArrayList<View>();
    private int mClickIndex = 0;
    private ViewPager mViewPager;
    private PagerAdapter mAdapter;
    private ImageView mDelete;
    private int mScrolledPosition = 0;
    private boolean mIsScrolled = false;
    private boolean mIsClicked = false;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        setContentView(R.layout.send_image_preview_edit_activity);
        init();
    }

    private void init() {
        mSelectedImagePathList = getIntent().getStringArrayListExtra(ImageUtils.SELECTED_IMAGE_PATH_LIST);
        mClickIndex = getIntent().getIntExtra(ImageUtils.CLICKED_IMAGE_INDEX, 0);
        mScrolledPosition = mClickIndex;

        mDelete = (ImageView) findViewById(R.id.send_preview_delete);
        mViewPager = (ViewPager) findViewById(R.id.send_viewpager);
        createPagerViews();
        mAdapter = new MyPagerAdapter();
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(mScrolledPosition);
        mDelete.setOnClickListener(this);

        TextView completeBtn = (TextView) findViewById(R.id.send_preview_edit_complete);
        completeBtn.setOnClickListener(this);
    }

    private void createPagerViews() {
        if (mSelectedImagePathList.isEmpty()) {
            return;
        }
        mAllPagerView.clear();

        for (int i = 0; i < mSelectedImagePathList.size(); i ++) {
            View view = (View) LayoutInflater.from(this).inflate(R.layout.preview_image_layout, null);
            ImageView iv = (ImageView) view.findViewById(R.id.preview_image);
            Glide.with(SendImagePreviewEditActivity.this).load(mSelectedImagePathList.get(i)).into(iv);
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            mAllPagerView.add(view);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        Log.d(TAG, "onPageSelected.position:" + position);
        mScrolledPosition = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private Intent getResultDataIntent() {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(ImageUtils.SELECTED_IMAGE_PATH_LIST, mSelectedImagePathList);
        return intent;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(Activity.RESULT_CANCELED);
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "onClick" );
        switch (view.getId()) {
            case R.id.send_preview_edit_complete:
                setResult(Activity.RESULT_OK, getResultDataIntent());
                finish();
                break;
            case R.id.send_preview_delete:
                updateCurrentViewPager();
                break;
        }
    }

    private void updateCurrentViewPager() {
        mSelectedImagePathList.remove(mScrolledPosition);
        mAllPagerView.remove(mScrolledPosition);
        if (mSelectedImagePathList.size() <= 0) {
            setResult(Activity.RESULT_OK, getResultDataIntent());
            finish();
            return;
        }

        mAdapter.notifyDataSetChanged();
    }

    public class MyPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mAllPagerView.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            // TODO Auto-generated method stub
            return arg0 == arg1;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Log.d(TAG, "instantiateItem.position:" + position);
            container.addView(mAllPagerView.get(position));
            return mAllPagerView.get(position);
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            Log.d(TAG, "destroyItem.position:" + position);
            View view = (View) object;
            container.removeView(view);
            view = null;
        }
    }
}
