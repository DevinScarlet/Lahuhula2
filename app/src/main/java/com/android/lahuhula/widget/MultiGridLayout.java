package com.android.lahuhula.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.lahuhula.util.ImageUtils;
import com.bumptech.glide.Glide;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by lenovo on 2017/1/3.
 */

public class MultiGridLayout extends LinearLayout {
    private static final String TAG = MultiGridLayout.class.getSimpleName();

    private static final int DEFAULT_MAX_COLUMNS = 3;
    private static final int DEFAULT_GAP = 10;
    private int mGap = DEFAULT_GAP;
    ;
    private int mMaxColumns = DEFAULT_MAX_COLUMNS;
    private int mRows = 1;
    private int mColumns = 2;
    private String mOwner;
    private ArrayList<String> mImageList = new ArrayList<String>();

    private OnItemClickListener mItemClickListener = null;

    public interface OnItemClickListener {
        public void onItemClick(View v, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    public MultiGridLayout(Context context) {
        super(context);
    }

    public MultiGridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MultiGridLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MultiGridLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setMaxColumns(int maxColumns) {
        mMaxColumns = maxColumns;
    }

    public void setDataList(ArrayList<String> resList, String owner) {
        mOwner = owner;
        mImageList.clear();
        mImageList.addAll(resList);
        setupView();
        calculateColumnRows();
    }

    private void setupView() {
        for (int i = 0; i < mImageList.size() && i < Math.pow(mMaxColumns, 2); i++) {
            ImageView iv = new ImageView(getContext());
            iv.setId(i);
            try {
                Glide.with(getContext()).load(new URL(ImageUtils.PICTURE_URL + mOwner + "/" + mImageList.get(i))).thumbnail(0.2f).centerCrop().into(iv);
            } catch (MalformedURLException e) {
                //
            }
            iv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClick(view, view.getId());
                    }
                }
            });
            addView(iv, generateDefaultLayoutParams());
        }
    }

    public int getDataListSize() {
        return mImageList.size();
    }

    private void calculateColumnRows() {
        int length = getDataListSize();
        if (length <= 0) {
            return;
        }
        //低于2张图片
        if (length <= 2) {
            //两列
            mColumns = length;
            //1行
            mRows = 1;
        } else {
            //大于两张图片
            for (int i = 2; i <= mMaxColumns; i++) {

                if (Math.pow(i, 2) == length) {
                    mColumns = mRows = i;
                    break;
                } else if (Math.pow(i, 2) < length) {
                    mColumns = i;
                    mRows = length / i + (length % i > 0 ? 1 : 0);
                    break;
                } else {
                    if (i == mMaxColumns) {
                        mColumns = mRows = i;
                        break;
                    }
                }
            }
        }
        Log.d(TAG, "length:" + length + ", mColumns:" + mColumns + ", mRows:" + mRows);
    }

    private int[] findPosition(int childNum) {
        int[] position = new int[2];
        for (int i = 0; i < mRows; i++) {
            for (int j = 0; j < mColumns; j++) {
                if ((i * mColumns + j) == childNum) {
                    position[0] = i;
                    position[1] = j;
                    break;
                }
            }
        }
        return position;
    }

    private void layoutChildrenView(int width) {
        int childrenCount = getChildCount();
        Log.d(TAG, "layoutChildrenView.childrenCount:" + childrenCount);
        if (childrenCount <= 0) {
            return;
        }

        int singleWidth = (width - mGap * (mColumns - 1)) / mColumns;
        int singleHeight = singleWidth;

        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = singleHeight * mRows + mGap * (mRows - 1);
        setLayoutParams(params);
        for (int i = 0; i < childrenCount; i++) {
            ImageView childrenView = (ImageView) getChildAt(i);
            int[] position = findPosition(i);
            int left = (singleWidth + mGap) * position[1];
            int top = (singleHeight + mGap) * position[0];
            int right = left + singleWidth;
            int bottom = top + singleHeight;
            childrenView.layout(left, top, right, bottom);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d(TAG, "onLayout->left:" + l + ", top:" + t + ", right:" + r + ", bottom:" + b);
        Log.d(TAG, "onLayout.getChildCount():" + getChildCount());
        int width = r - l;
        layoutChildrenView(width);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.d(TAG, "onMeasure.getChildCount():" + getChildCount());
        final int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int realWidth = getChildCount() > 1 ? width : width / 2;
        setMeasuredDimension(realWidth, realWidth);
    }
}
