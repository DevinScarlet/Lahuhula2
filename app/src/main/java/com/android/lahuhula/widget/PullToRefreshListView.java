package com.android.lahuhula.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.lahuhula.R;

/**
 * 下拉刷新ListView
 *
 * @author wkl
 *
 */
public class PullToRefreshListView extends ListView implements OnScrollListener {
    private static final String TAG = PullToRefreshListView.class.getSimpleName();

    // 文字提示
    private TextView tip;
    // 箭头
    private ImageView arrow;
    // 圆形进度条
    private ProgressBar progress;
    // 普通状态
    private static final int NORMAL = 0;
    // 下拉状态
    private static final int PULL = 1;
    // 提示释放
    private static final int RELEASE = 2;
    // 正在刷新
    private static final int REFRESHING = 3;
    // 当前状态
    private int state = NORMAL;
    private LayoutInflater inflater;
    // header布局
    private View header;

    // header的高度
    private int headerHeight;
    // 第一个可见的item
    private int firstVisibleItem;
    // 逆时针旋转动画
    private RotateAnimation animNi;
    // 顺时针旋转动画
    private RotateAnimation animShun;
    // 滚动状态
    private int scrollState;
    // 手指按下标记
    private boolean flag = false;
    // 手指按下起始点
    private int startY;
    private OnRefreshListener listener;
    // 标记
    private int count = 0;

    public PullToRefreshListView(Context context) {
        super(context);
        init(context);
    }

    public PullToRefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PullToRefreshListView(Context context, AttributeSet attrs,
                                 int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    /**
     * 初始化
     *
     * @param context
     */
    private void init(Context context) {
        animNi = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF,
                0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        animNi.setFillAfter(true);
        animNi.setDuration(250);
        animShun = new RotateAnimation(-180, 0,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        animShun.setFillAfter(true);
        animShun.setDuration(250);

        inflater = LayoutInflater.from(context);
        header = inflater.inflate(R.layout.friends_circle_header_layout, this, false);

        tip = (TextView) header.findViewById(R.id.tip);
        arrow = (ImageView) header.findViewById(R.id.arrow);
        progress = (ProgressBar) header.findViewById(R.id.progress);

        addHeaderView(header);
        measureView(header);
        headerHeight = header.getMeasuredHeight();
        setTopPadding(-headerHeight);
        setOnScrollListener(this);
    }

    /**
     * 设置header的TopPadding
     *
     * @param topPadding
     */
    private void setTopPadding(int topPadding) {
        header.setPadding(header.getPaddingLeft(), topPadding,
                header.getPaddingRight(), header.getPaddingBottom());
    }

    /**
     * 通知父布局占用的空间
     *
     * @param child
     */
    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
                    MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0,
                    MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "ACTION_DOWN");
                if (firstVisibleItem == 0) {
                    flag = true;
                    startY = (int) ev.getY();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "ACTION_MOVE");
                onMove(ev);
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "ACTION_UP");
                count = 0;
                if (state == RELEASE) {
                    state = REFRESHING;
                    changeViewByState();
                    // 预留加载数据
                    Log.d(TAG, "ACTION_UP->listener:" + listener);
                    if (listener != null) {
                        Log.d(TAG, "ACTION_UP->onRefresh");
                        listener.onRefresh();
                    }
                    // 预留加载数据
                } else if (state == PULL) {
                    Log.d(TAG, "ACTION_UP->PULL");
                    state = NORMAL;
                    flag = false;
                    changeViewByState();
                    if (listener != null) {
                        Log.d(TAG, "ACTION_UP->onRefresh");
                        listener.onRefresh();
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 根据state改变header
     */
    private void changeViewByState() {
        switch (state) {
            case NORMAL:
                arrow.clearAnimation();
                setTopPadding(-headerHeight);
                tip.setText("下拉可以刷新");
                break;
            case PULL:
                arrow.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                if (count != 0) {
                    arrow.clearAnimation();
                    arrow.startAnimation(animShun);
                    tip.setText("下拉可以刷新");
                }
                break;
            case RELEASE:
                //arrow.setImageResource(R.drawable.ic_circle_header_release);
                arrow.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                arrow.clearAnimation();
                count = 1;
                arrow.startAnimation(animNi);
                tip.setText("释放可以刷新");
                break;
            case REFRESHING:
                setTopPadding(10);
                arrow.clearAnimation();
                arrow.setVisibility(View.GONE);
                progress.setVisibility(View.VISIBLE);
                tip.setText("正在加载...");
                break;
        }
    }

    /**
     * 移动事件
     *
     * @param ev
     */
    private void onMove(MotionEvent ev) {
        /*if (!flag) {
            return;
        }*/
        int tempY = (int) ev.getY();
        int space = tempY - startY;
        int topPadding = space - headerHeight;

        switch (state) {
            case NORMAL:
                if (space > 0) {
                    state = PULL;
                    changeViewByState();
                }
                break;
            case PULL:
                setTopPadding(topPadding);
                if (space > headerHeight + 20
                        && scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    state = RELEASE;
                    changeViewByState();
                }
                break;
            case RELEASE:
                setTopPadding(topPadding);
                if (space < headerHeight + 20) {
                    state = PULL;
                    changeViewByState();
                } else if (space <= 0) {
                    state = NORMAL;
                    flag = false;
                    changeViewByState();
                }
                break;
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        this.scrollState = scrollState;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        this.firstVisibleItem = firstVisibleItem;
    }

    /**
     * 刷新数据完成
     */
    public void refreshComplete() {
        state = NORMAL;
        flag = false;
        changeViewByState();
    }

    /**
     * 设置数据更新回调
     */
    public void setOnRefreshListener(OnRefreshListener listener) {
        this.listener = listener;
    }

    /**
     * 刷新数据回调接口
     *
     * @author wkl
     *
     */
    public interface OnRefreshListener {
        /**
         * 通知activity进行更新数据
         */
        public void onRefresh();
    }
}
