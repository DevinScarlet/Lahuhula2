package com.android.lahuhula.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.lahuhula.R;
import com.android.lahuhula.util.ImageUtils;
import com.android.lahuhula.util.JsonUtils;
import com.android.lahuhula.util.JsonUtils.JsonResultData;
import com.android.lahuhula.view.CircleInfo;
import com.android.lahuhula.view.UserInfo;
import com.android.lahuhula.widget.NineGridTestLayout;
import com.android.lahuhula.widget.PullToRefreshListView;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by lenovo on 2017/2/12.
 */

public class FriendsCircleActivity extends AppCompatActivity implements
        PullToRefreshListView.OnRefreshListener {

    private static final int MSG_UPDATE_CIRCLE_INFO = 0x22;

    private static final int REQUEST_CODE_SEND_NEW_MSG = 0x100;

    private PullToRefreshListView mCircleListView;
    private ArrayList<String> mData = new ArrayList<>();
    private ArrayList<Integer> mPictureData = new ArrayList<Integer>();
    private MyAdapter mAdapter;
    private GridView mPictureGridView;
    private PopupWindow mCommentsPopupWindow;
    private LinearLayout mCommentEditLayout;
    private EditText mCommentEditTextView;
    private Button mSendBtn;

    private ArrayList<Integer> mTotalLikeData = new ArrayList<>();
    private ArrayList<CommentsDisplayItem> mTotalCommentsDisplayList = new ArrayList<CommentsDisplayItem>();

    private ArrayList<CircleInfo> mCircleInfo = new ArrayList<CircleInfo>();

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_CIRCLE_INFO:
                    mAdapter.notifyDataSetChanged();
                    mCircleListView.refreshComplete();
                    break;
            }
        }
    };
    private int mOwnerIndex;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.friends_circle_activity_layout);
        init();
    }

    private void init() {
        initActionBar();
        initFriendsCircleListView();
    }

    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            return;
        }
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void initFriendsCircleListView() {
        //ListView
        mCircleListView = (PullToRefreshListView) findViewById(R.id.friends_circle_list);
        //获取焦点设置
        mCircleListView.setItemsCanFocus(true);
        //初始化图片数据
        initPictureData();
        //初始化评论数据
        initTotalLikeData();
        //初始化adapter
        mAdapter = new MyAdapter(this);
        //设置adapter
        mCircleListView.setAdapter(mAdapter);
        //设置下拉刷新监听
        mCircleListView.setOnRefreshListener(this);
        getNewCircleInfo();
    }

    private void initPictureData() {
        mPictureData.clear();
        for (int i = 0; i < 7; i++) {
            //获取资源id
            int resId = getResources().getIdentifier("apple_" + i, "drawable", getPackageName());
            mPictureData.add(resId);
        }
    }

    private void initTotalLikeData() {
        mTotalCommentsDisplayList.add(new CommentsDisplayItem("李磊", "", "请问包邮吗？可以顺便送点小礼物吗？"));
        mTotalCommentsDisplayList.add(new CommentsDisplayItem("张涛", "李磊", "当然包邮，还送您削苹果神器"));
    }

    @Override
    public void onRefresh() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 1获取最新数据
                getNewCircleInfo();
            }
        }, 2000);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SEND_NEW_MSG && resultCode == Activity.RESULT_OK) {
            getNewCircleInfo();
        }
    }

    public class MyAdapter extends BaseAdapter {
        private Context sContext;

        public MyAdapter(Context context) {
            sContext = context;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHold viewHold = null;
            final int viewPos = position;
            if (convertView == null) {
                convertView = LayoutInflater.from(sContext).inflate(R.layout.friends_circle_list_cell_layout, null);
                viewHold = new ViewHold();
                //图片展示控件
                viewHold.layout = (NineGridTestLayout) convertView.findViewById(R.id.multi_grid_layout);
                //用户头像
                viewHold.peopleImage = (ImageView) convertView.findViewById(R.id.people_portrait);
                //名字
                viewHold.name = (TextView) convertView.findViewById(R.id.people_name);
                //描述
                viewHold.describe = (TextView) convertView.findViewById(R.id.people_description);
                //发布时间
                viewHold.sentTime = (TextView) convertView.findViewById(R.id.people_sent_time);
                //评论按钮
                viewHold.commentsView = (ImageView) convertView.findViewById(R.id.people_comment);
                //获取评论输入框
                viewHold.editText = (EditText) convertView.findViewById(R.id.comment_edit_text);
                //喜欢
                viewHold.totalLikeView = (TextView) convertView.findViewById(R.id.total_like_display);
                //评论列表
                viewHold.totalCommentsDisplayLayout = (LinearLayout) convertView.findViewById(R.id.total_comments_display);
                convertView.setTag(viewHold);
            } else {
                viewHold = (ViewHold) convertView.getTag();
            }

            //设置用户头像
            try {
                Glide.with(FriendsCircleActivity.this).load(new URL(ImageUtils.PICTURE_URL
                        + mCircleInfo.get(position).mOwner + "/head.jpg")).centerCrop().error(R.drawable.login_icon).dontAnimate().into(viewHold.peopleImage);
            } catch (Exception e) {
                viewHold.peopleImage.setImageResource(R.drawable.login_icon);
            }
            //设置名称
            viewHold.name.setText(mCircleInfo.get(position).mItemName);
            //设置描述文字
            viewHold.describe.setText(mCircleInfo.get(position).mDescribe);
            //初始化喜欢控件
            viewHold.totalLikeView.setVisibility(View.GONE);
            //获取喜欢数据
            String likeNum = mCircleInfo.get(position).mLike;
            if (!TextUtils.isEmpty(likeNum)) {
                viewHold.totalLikeView.setText(likeNum);
                viewHold.totalLikeView.setVisibility(View.VISIBLE);
            }

            //获取评论数据
            String mComment = mCircleInfo.get(position).mComment;
            //设置评论数据
            setupTotalCommentsDisplay(viewHold.totalCommentsDisplayLayout);
            //设置发布时间
            viewHold.sentTime.setText(mCircleInfo.get(position).mOperdate);
            //给评论按钮设置tag
            viewHold.commentsView.setTag(position);
            //给评论按钮添加点击事件
            final ViewHold finalViewHold = viewHold;
            viewHold.commentsView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("zhangtao", "commentsView clicked");
                    //弹出pop
                    toggleCommentsPopupWindow(view, finalViewHold.totalLikeView);
                }
            });
            //判断是否有数据
            viewHold.layout.setUrlList(mCircleInfo.get(position).mPictureList, mCircleInfo.get(position).mOwner, mCircleInfo.get(position).mPrice);
          /*  //图片物品的点击事件
            viewHold.layout.setOnItemClickListener(new MultiGridLayout.OnItemClickListener() {
                @Override
                public void onItemClick(View v, int position) {
                    //进入图片页面
                    sContext.startActivity(getPictureViewActivityIntent(position, viewPos));
                }
            });*/
            return convertView;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public int getCount() {
            return mCircleInfo.size();
        }

        public boolean hasStableIds() {
            /*if (mAdapter != null) {
                return mAdapter.hasStableIds();
            }*/
            return false;
        }

        private void setupTotalCommentsDisplay(LinearLayout layout) {
            layout.setVisibility(View.GONE);

            if (layout.getChildCount() > 0) {
                layout.setVisibility(View.VISIBLE);
                return;
            }

            if (mTotalCommentsDisplayList.isEmpty()) {
                return;
            }

            for (CommentsDisplayItem item : mTotalCommentsDisplayList) {
                String from = item.getFrom();
                String to = item.getTo();
                String des = item.getDescription();
                SpannableString s;
                if (TextUtils.isEmpty(to)) {
                    s = new SpannableString(from + ":" + des);
                } else {
                    s = new SpannableString(from + CommentsDisplayItem.REPLY_TEXT + to + ":" + des);
                }
                s.setSpan(new ForegroundColorSpan(Color.RED), 0, from.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                s.setSpan(new ForegroundColorSpan(Color.RED), from.length() + CommentsDisplayItem.REPLY_TEXT.length(),
                        from.length() + CommentsDisplayItem.REPLY_TEXT.length() + to.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                TextView tv = new TextView(sContext);
                tv.setText(s);
                layout.addView(tv);
            }
            Log.i("comments", mTotalCommentsDisplayList.toString());
            layout.setVisibility(View.VISIBLE);
        }
    }

    public class ViewHold {
        NineGridTestLayout layout;
        ImageView peopleImage;
        TextView name;
        TextView describe;
        TextView sentTime;
        ImageView commentsView;
        EditText editText;
        TextView totalLikeView;
        LinearLayout totalCommentsDisplayLayout;
    }

    private void toggleCommentsPopupWindow(View parentView, final TextView totleLike) {
        int showPopupWindowWidth = getResources().getDimensionPixelSize(R.dimen.friends_circle_comment_width);
        int showPopupWindowHeight = getResources().getDimensionPixelSize(R.dimen.friends_circle_comment_height);
        if (mCommentsPopupWindow == null) {
            View content = LayoutInflater.from(this).inflate(R.layout.comments_layout, null, false);

            mCommentsPopupWindow = new PopupWindow(content, showPopupWindowWidth, showPopupWindowHeight);
            //mCommentsPopupWindow.setBackgroundDrawable(new BitmapDrawable());
            mCommentsPopupWindow.setOutsideTouchable(true);
            mCommentsPopupWindow.setTouchable(true);

            //content.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        }
        View parent = mCommentsPopupWindow.getContentView();

        TextView like = (TextView) parent.findViewById(R.id.like);
        TextView comment = (TextView) parent.findViewById(R.id.comment);
        like.setTag((int) parentView.getTag());
        comment.setTag((int) parentView.getTag());
        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = (int) view.getTag();
                Log.d("zhangtao", "like click position:" + position);
                mCommentsPopupWindow.dismiss();
                String lastValueString = mCircleInfo.get(position).mLike;
                mCircleInfo.get(position).mLike = String.valueOf(Integer.parseInt(lastValueString) + 1);
                totleLike.setText(mCircleInfo.get(position).mLike);
            }
        });
        comment.setOnClickListener(mCommentClickListener);
        if (mCommentsPopupWindow.isShowing()) {
            mCommentsPopupWindow.dismiss();
        } else {
            int heightMoreBtnView = parentView.getHeight();

            mCommentsPopupWindow.showAsDropDown(parentView, -showPopupWindowWidth,
                    -(showPopupWindowHeight + heightMoreBtnView) / 2);
        }
        TextView totalLikeDisplay = (TextView) findViewById(R.id.total_like_display);

    }

    private Intent getPictureViewActivityIntent(int index, int viewPos) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.putStringArrayListExtra("pictures", mCircleInfo.get(viewPos).mPictureList);
        intent.putExtra("vc_owner", mCircleInfo.get(viewPos).mOwner);
        intent.putExtra("cur_picture_index", index);
        intent.setClass(this, PictureViewActivity.class);
        return intent;
    }

    private View.OnClickListener mLikeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            //需要发送数据给服务器
        }
    };

    private View.OnClickListener mCommentClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int position = (int) view.getTag();
            Log.d("zhangtao", "comment click position:" + position);
            mCommentsPopupWindow.dismiss();
            showCommentEditLayout(position);
            showInputMethod();
        }
    };

    private void showCommentEditLayout(int index) {
        mCommentEditLayout = (LinearLayout) findViewById(R.id.friends_circle_comment_layout);
        mCommentEditLayout.setVisibility(View.VISIBLE);
        mCommentEditTextView = (EditText) findViewById(R.id.comment_edit_text);
        mCommentEditTextView.setFocusable(true);
        mCommentEditTextView.setFocusableInTouchMode(true);
        mCommentEditTextView.requestFocus();
        mCommentEditTextView.setText("");
        mCommentEditTextView.setHint(R.string.friend_circle_comment_label);
        mSendBtn = (Button) findViewById(R.id.friends_circle_comment_send);
        mOwnerIndex = index;
        mSendBtn.setOnClickListener(mSendClickLentenr);
    }

    private void showInputMethod() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mCommentEditTextView, InputMethodManager.SHOW_FORCED);
    }

    private void hideInputMethod() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mCommentEditTextView.getWindowToken(), 0);
    }

    private View.OnClickListener mSendClickLentenr = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String currentComment = mCommentEditTextView.getText().toString().trim();
            mCommentEditLayout.setVisibility(View.GONE);
            mCommentEditTextView.setText("");
            hideInputMethod();
            //刷新数据
            //上传服务器
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.friends_circle_menu, menu);
        if (UserInfo.isUserLogin(this)) {
            menu.findItem(R.id.action_add).setVisible(true);
        } else {
            menu.findItem(R.id.action_add).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {
            startActivityForResult(new Intent(getApplicationContext(), CircleAddActivity.class), REQUEST_CODE_SEND_NEW_MSG);
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public class CommentsDisplayItem {
        public static final String REPLY_TEXT = "回复";
        public String from;
        public String to;
        public String description;

        public CommentsDisplayItem(String arg1, String arg2, String arg3) {
            from = arg1;
            to = arg2;
            description = arg3;
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return "CommentsDisplayItem{" +
                    "from='" + from + '\'' +
                    ", to='" + to + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }
    }

    private void getNewCircleInfo() {
        new Thread() {
            @Override
            public void run() {
                try {
                    JSONObject object = new JSONObject();
                    object.put("vc_sellerno", "all");
                    JsonResultData result = JsonUtils.circleInfoHttpGet(JsonUtils.POST_TILE + object.toString());
                    if (result.isSuccess()) {
                        updateCircleInfo(result.getResultString());
                    }
                } catch (JSONException ex) {
                    return;
                }
            }
        }.start();
    }

    private ArrayList<String> getPictureDataList(JSONObject object) throws JSONException {
        ArrayList<String> picList = new ArrayList<String>();
        for (int i = 0; i < ImageUtils.MAX_IMAGE_COUNT; i++) {
            String picName = object.getString(CircleInfo.VC_PIC + (i + 1));
            if (!TextUtils.isEmpty(picName)) {
                picList.add(picName);
            }
        }
        return picList;
    }

    private void updateCircleInfo(String resultData) {
        Map<String, String> xml = JsonUtils.decodeJsonUtilXml(resultData);
        if (xml == null) {
            return;
        }
        String info = xml.get(JsonUtils.RESULT_TAG);
        Log.d("zhangtao", "updateCircleInfo.info:" + info);
        if (TextUtils.isEmpty(info)) {
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(info);
            JSONArray jsonArray = (JSONArray) jsonObject.get("orderList");
            Log.d("zhangtao", "updateCircleInfo.jsonArray:" + jsonArray.toString());
            Log.d("zhangtao", "updateCircleInfo.jsonArray.length:" + jsonArray.length());
            mCircleInfo.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                mCircleInfo.add(new CircleInfo(object.getString(CircleInfo.VC_ID),
                        object.getString(CircleInfo.VC_OWNER),
                        object.getString(CircleInfo.VC_ITEM_NO),
                        object.getString(CircleInfo.VC_ITEM_NAME),
                        object.getString(CircleInfo.VC_UNIT),
                        object.getString(CircleInfo.D_QUANITITY),
                        object.getString(CircleInfo.D_PRICE),
                        object.getString(CircleInfo.VC_DESCRIBE),
                        object.getString(CircleInfo.VC_LIKE),
                        object.getString(CircleInfo.VC_COMMENT),
                        object.getString(CircleInfo.DT_OPERDATE),
                        getPictureDataList(object)));
            }
            /*Collections.sort(mCircleInfo, new Comparator<CircleInfo>() {
                @Override
                public int compare(CircleInfo lhs, CircleInfo rhs) {
                    Date date1 = new Date(lhs.mOperdate);
                    Date date2 = new Date(rhs.mOperdate);
                    if (date1.before(date2)) {
                        return 1;
                    }
                    return -1;
                }
            });*/
            Log.d("zhangtao", mCircleInfo.toString());
            mHandler.sendEmptyMessageDelayed(MSG_UPDATE_CIRCLE_INFO, 100);
        } catch (JSONException je) {
            //Log.e(TAG, "saveUserInfo je:" + je);
        }
    }
}
