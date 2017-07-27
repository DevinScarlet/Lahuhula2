package com.android.lahuhula.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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
import com.baidu.mapapi.map.Text;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tao.zhang on 2017/3/1.
 */

public class SelectImagePreviewActivity extends AppCompatActivity
        implements CheckBox.OnCheckedChangeListener, CheckBox.OnTouchListener,
        View.OnClickListener, ViewPager.OnPageChangeListener {

    private static final String TAG = SelectImagePreviewActivity.class.getSimpleName();

    private ArrayList<String> mAllImagePathList = new ArrayList<String>();
    private ArrayList<String> mSelectedImagePathList = new ArrayList<String>();
    private List<View> mAllPagerView = new ArrayList<View>();
    private int mClickIndex = 0;
    private int mLastSelectedImageCount = 0;
    private ViewPager mViewPager;
    private CheckBox mCheckBox;
    private Toast mToast;
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

        setContentView(R.layout.select_image_preview_activity);
        init();
    }

    private void init() {
        mAllImagePathList = getIntent().getStringArrayListExtra(ImageUtils.ALL_IMAGE_PATH_LIST);
        mSelectedImagePathList = getIntent().getStringArrayListExtra(ImageUtils.SELECTED_IMAGE_PATH_LIST);
        mLastSelectedImageCount = getIntent().getIntExtra(ImageUtils.SELECTED_IMAGE_COUNT, 0);
        mClickIndex = getIntent().getIntExtra(ImageUtils.CLICKED_IMAGE_INDEX, 0);
        mScrolledPosition = mClickIndex;

        mCheckBox = (CheckBox) findViewById(R.id.select_preview_checkbox);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        createPagerViews();
        PagerAdapter adapter = new MyPagerAdapter();
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(mScrolledPosition);
        mCheckBox.setOnCheckedChangeListener(this);
        mCheckBox.setOnTouchListener(this);

        TextView completeBtn = (TextView) findViewById(R.id.select_preview_complete);
        completeBtn.setOnClickListener(this);
    }

    private void createPagerViews() {
        if (mAllImagePathList.isEmpty()) {
            return;
        }
        mAllPagerView.clear();

        for (int i = 0; i < mAllImagePathList.size(); i ++) {
            View view = (View) LayoutInflater.from(this).inflate(R.layout.preview_image_layout, null);
            ImageView iv = (ImageView) view.findViewById(R.id.preview_image);
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            mAllPagerView.add(view);
        }
    }

    private void updateCheckBoxState(int position) {
        boolean isFindIt = false;
        for (int i = 0; i < mSelectedImagePathList.size(); i ++) {
            if (mAllImagePathList.get(position).equals(mSelectedImagePathList.get(i))) {
                isFindIt = true;
                break;
            }
        }
        if (isFindIt) {
            mCheckBox.setChecked(true);
        } else {
            mCheckBox.setChecked(false);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "onCheckedChanged.isChecked:" + isChecked);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        Log.d(TAG, "onPageSelected.position:" + position);
        mScrolledPosition = position;
        updateCheckBoxState(position);
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
    public void onPause() {
        super.onPause();
        if (mToast != null) {
            mToast.cancel();
        }
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
            case R.id.select_preview_complete:
                setResult(Activity.RESULT_OK, getResultDataIntent());
                finish();
                break;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        Log.d(TAG, "onTouch" );
        if (motionEvent.getAction() == KeyEvent.ACTION_UP) {
            String curPath = mAllImagePathList.get(mScrolledPosition);
            boolean isChecked = mCheckBox.isChecked();
            if ((mSelectedImagePathList.size() + mLastSelectedImageCount) >= ImageUtils.MAX_IMAGE_COUNT && !isChecked) {
                if (mToast != null) {
                    mToast.cancel();
                }
                mToast = Toast.makeText(this, getString(R.string.toast_max_image_is_added, ImageUtils.MAX_IMAGE_COUNT),
                        Toast.LENGTH_SHORT);
                mToast.setGravity(Gravity.CENTER, 0, 0);
                mToast.show();
                return true;
            }

            if (!isChecked) {
                if (!mSelectedImagePathList.contains(curPath)) {
                    mSelectedImagePathList.add(curPath);
                    mCheckBox.setChecked(true);
                    Log.d("zhangtao", "add ----- size:" + mSelectedImagePathList.size());
                }
            } else {
                if (mSelectedImagePathList.contains(curPath)) {
                    mSelectedImagePathList.remove(curPath);
                    mCheckBox.setChecked(false);
                    Log.d("zhangtao", "remove ----- size:" + mSelectedImagePathList.size());
                }
            }
        }
        return true;
    }

    public class MyPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mAllPagerView.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            // TODO Auto-generated method stub
            return arg0 == arg1;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView iv = (ImageView) mAllPagerView.get(position).findViewById(R.id.preview_image);
            Glide.with(SelectImagePreviewActivity.this).load(mAllImagePathList.get(position)).into(iv);
            container.addView(mAllPagerView.get(position));
            return mAllPagerView.get(position);
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            Glide.clear(mAllPagerView.get(position).findViewById(R.id.preview_image));
            container.removeView(mAllPagerView.get(position));
        }
    }
}
