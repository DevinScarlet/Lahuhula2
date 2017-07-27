package com.android.lahuhula.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.lahuhula.R;
import com.android.lahuhula.util.ImageUtils;
import com.bumptech.glide.Glide;
import com.tencent.mm.opensdk.constants.Build;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tao.zhang on 2017/2/27.
 */

public class SelectImageActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE_SELECT_IMAGE = 100;
    private static final int REQUEST_CODE_SELECT_IMAGE_FROM_PREVIEW = 0x20;
    private static final int COLUMNS = 4;

    private ScrollView mScrollView;
    private LinearLayout mMultiRowLayout;
    private TextView mCompleteBtn;
    private Toast mToast;
    private Handler mHandler = new Handler();
    private ArrayList<ImageList> mImageList = new ArrayList<ImageList>();
    private ArrayList<String> mAllImagePathList = new ArrayList<String>();
    private ArrayList<String> mSelectedImagePathList = new ArrayList<String>();
    private int mLastSelectedImageCount = 0;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        setContentView(R.layout.select_image_activity_layout);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int readExtStorage = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            int writeExtStorage = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            List<String> mPermissionStrings = new ArrayList<String>();
            boolean mRequest = false;

            if (readExtStorage != PackageManager.PERMISSION_GRANTED) {
                mPermissionStrings.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                mRequest = true;
            }
            if (writeExtStorage != PackageManager.PERMISSION_GRANTED) {
                mPermissionStrings.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                mRequest = true;
            }
            if (mRequest == true) {
                String[] mPermissionList = new String[mPermissionStrings.size()];
                mPermissionList = mPermissionStrings.toArray(mPermissionList);
                requestPermissions(mPermissionList, PERMISSION_REQUEST_CODE_SELECT_IMAGE);
                return;
            }
        }

        init();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mToast != null) {
            mToast.cancel();
        }
    }

    private void init() {
        mLastSelectedImageCount = getIntent().getIntExtra(ImageUtils.SELECTED_IMAGE_COUNT, 0);
        initData();
        initImageView();
    }

    private void initImageView() {
        mScrollView = (ScrollView)findViewById(R.id.image_scroll_view);
        mMultiRowLayout = (LinearLayout)findViewById(R.id.image_multi_row);
        TextView cancelTv = (TextView) findViewById(R.id.add_image_cancel_btn);
        mCompleteBtn = (TextView) findViewById(R.id.add_image_complete_btn);
        cancelTv.setOnClickListener(mActionClickListener);
        mCompleteBtn.setOnClickListener(mActionClickListener);
        mCompleteBtn.setEnabled(false);

        LinearLayout columnLayout;
        View btn;
        int testItemRowCount = COLUMNS;
        int itemButtonLayoutHeight = getResources().getDisplayMetrics().widthPixels / testItemRowCount;
        int columns = mImageList.size() / testItemRowCount;
        int lastColumnItemCount = mImageList.size() % testItemRowCount;
        if (lastColumnItemCount != 0) {
            columns += 1;
        }
        for (int i = 0; i < columns; i++) {
            columnLayout = new LinearLayout(this);
            columnLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    itemButtonLayoutHeight));
            for (int j = 0; j < testItemRowCount; j++) {
                btn = getImageView(i, j, testItemRowCount);
                columnLayout.addView(btn);
            }
            mMultiRowLayout.addView(columnLayout);
        }
        scrollToBottom();
    }

    private void scrollToBottom() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        }, 50);
    }

    private View getImageView(int columnIndex, int rowIndex, int rowCount) {
        int itemIndex = columnIndex * rowCount + rowIndex;

        View layout = (View) LayoutInflater.from(this).inflate(R.layout.circle_add_image_item, null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
        lp.setMargins(5, 5, 5, 5);
        layout.setLayoutParams(lp);

        if (itemIndex > mImageList.size() - 1) {
            layout.setVisibility(View.INVISIBLE);
            return layout;
        }

        ImageView iv = (ImageView) layout.findViewById(R.id.image_item);
        CheckBox cb = (CheckBox) layout.findViewById(R.id.checkbox_item);
        //cb.setOnCheckedChangeListener(mCheckBoxChangedListener);
        cb.setOnTouchListener(mCheckBoxTouchListener);
        Glide.with(this).load(mImageList.get(itemIndex).getPath()).into(iv);
        iv.setOnClickListener(mImageClickListener);
        iv.setTag(R.id.image_item, itemIndex);
        cb.setTag(itemIndex);
        return layout;
    }

    private void initData() {
        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        mImageList.clear();
        if (cursor == null) {
            return;
        }
        while (cursor.moveToNext()) {
            //获取图片的名称
            String name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
            //获取图片的生成日期
            byte[] data = cursor.getBlob(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            //获取图片的详细信息
            String desc = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DESCRIPTION));
            String mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE));
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            mImageList.add(new ImageList(name, mimeType, path));
            mAllImagePathList.add(path);
        }
        cursor.close();
    }

    private CheckBox.OnCheckedChangeListener mCheckBoxChangedListener = new CheckBox.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int index = (int) buttonView.getTag();
            final String path = mImageList.get(index).getPath();
            Log.d("zhangtao", "add or remove index:" + index);
            if (isChecked) {
                if (!mSelectedImagePathList.contains(path)) {
                    mSelectedImagePathList.add(path);
                }
            } else {
                if (mSelectedImagePathList.contains(path)) {
                    mSelectedImagePathList.remove(path);
                }
            }
            Log.d("zhangtao", "mSelectedImagePathList.size():" + mSelectedImagePathList.size());
            mCompleteBtn.setEnabled(mSelectedImagePathList.size() > 0);
        }
    };

    private View.OnTouchListener mCheckBoxTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == KeyEvent.ACTION_UP) {
                int index = (int) view.getTag();
                final String path = mImageList.get(index).getPath();
                CheckBox checkBox = (CheckBox)view;
                boolean isChecked = checkBox.isChecked();

                if ((mSelectedImagePathList.size() + mLastSelectedImageCount) >= ImageUtils.MAX_IMAGE_COUNT && !isChecked) {
                    if (mToast != null) {
                        mToast.cancel();
                    }
                    mToast = Toast.makeText(SelectImageActivity.this, getString(R.string.toast_max_image_is_added, ImageUtils.MAX_IMAGE_COUNT),
                            Toast.LENGTH_SHORT);
                    mToast.setGravity(Gravity.CENTER, 0, 0);
                    mToast.show();
                    return true;
                }

                if (!isChecked) {
                    if (!mSelectedImagePathList.contains(path)) {
                        mSelectedImagePathList.add(path);
                        checkBox.setChecked(true);
                    }
                } else {
                    if (mSelectedImagePathList.contains(path)) {
                        mSelectedImagePathList.remove(path);
                        checkBox.setChecked(false);
                    }
                }
                Log.d("zhangtao", "mSelectedImagePathList.size():" + mSelectedImagePathList.size());
                mCompleteBtn.setEnabled(mSelectedImagePathList.size() > 0);
            }
            return true;
        }
    };


    private View.OnClickListener mImageClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            int index = (int) v.getTag(R.id.image_item);
            Log.d("zhangtao", "mImageClickListener.index:" + index);
            startActivityForResult(getPreviewActivityIntent(index), REQUEST_CODE_SELECT_IMAGE_FROM_PREVIEW);
        }
    };

    private View.OnClickListener mActionClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.add_image_cancel_btn:
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                    break;
                case R.id.add_image_complete_btn:
                    setResult(Activity.RESULT_OK, getResultDataIntent());
                    finish();
                    break;
            }
        }
    };

    private Intent getResultDataIntent() {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(ImageUtils.SELECTED_IMAGE_PATH_LIST, mSelectedImagePathList);
        return intent;
    }

    private Intent getPreviewActivityIntent(int index) {
        Intent intent = new Intent();
        intent.setClass(this, SelectImagePreviewActivity.class);
        intent.putStringArrayListExtra(ImageUtils.ALL_IMAGE_PATH_LIST, mAllImagePathList);
        intent.putStringArrayListExtra(ImageUtils.SELECTED_IMAGE_PATH_LIST, mSelectedImagePathList);
        intent.putExtra(ImageUtils.CLICKED_IMAGE_INDEX, index);
        intent.putExtra(ImageUtils.SELECTED_IMAGE_COUNT, mLastSelectedImageCount);
        return intent;
    }

    private void updateSelectedImageCheckBoxState() {
        ArrayList<Integer> positionList = getSelectedImagePositionList();
        int count = mMultiRowLayout.getChildCount();
        for (int i = 0; i < count; i ++) {
            LinearLayout layout = (LinearLayout) mMultiRowLayout.getChildAt(i);
            int num = layout.getChildCount();
            for (int j = 0; j < num; j ++) {
                View view = layout.getChildAt(j);
                CheckBox cb = (CheckBox) view.findViewById(R.id.checkbox_item);
                cb.setChecked(false);
                for (int n = 0; n < positionList.size(); n ++) {
                    if (cb != null && cb.getTag() != null && (int) cb.getTag() == positionList.get(n)) {
                        cb.setChecked(true);
                    }
                }
            }
        }
    }

    private ArrayList<Integer> getSelectedImagePositionList() {
        ArrayList<Integer> positionList = new ArrayList<Integer>();
        for (int i = 0; i < mAllImagePathList.size(); i ++) {
            for (int j = 0; j < mSelectedImagePathList.size(); j ++) {
                if (mAllImagePathList.get(i).equals(mSelectedImagePathList.get(j))) {
                    positionList.add(i);
                }
            }
        }
        return positionList;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_SELECT_IMAGE_FROM_PREVIEW:
                if (resultCode == Activity.RESULT_OK) {
                    mSelectedImagePathList.clear();
                    mSelectedImagePathList = data.getStringArrayListExtra(ImageUtils.SELECTED_IMAGE_PATH_LIST);
                    updateSelectedImageCheckBoxState();
                    mCompleteBtn.setEnabled(mSelectedImagePathList.size() > 0);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(Activity.RESULT_CANCELED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE_SELECT_IMAGE) {
            boolean permissionGranted = false;
            for (int counter = 0; counter < permissions.length; counter++) {
                permissionGranted = (grantResults[counter] ==
                        PackageManager.PERMISSION_GRANTED);
                if (permissionGranted) {
                    break;
                }
            }
            if (permissionGranted) {
                init();
            }
        }
    }

    public class ImageList {
        private String name;
        private String mimeType;
        private String path;

        public ImageList(String name, String type, String path) {
            this.name = name;
            this.mimeType = type;
            this.path = path;
        }

        public String getName() {
            return this.name;
        }

        public String getMimeType() {
            return this.mimeType;
        }

        public String getPath() {
            return this.path;
        }
    }
}
