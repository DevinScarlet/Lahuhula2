package com.android.lahuhula.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.lahuhula.R;
import com.android.lahuhula.util.ImageUtils;
import com.android.lahuhula.util.JsonUtils;
import com.android.lahuhula.util.Utils;
import com.android.lahuhula.view.UserInfo;
import com.baidu.mapapi.map.Text;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.android.lahuhula.util.ImageUtils.bytesToBase64String;

/**
 * Created by lenovo on 2017/2/28.
 */

public class CircleAddActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = CircleAddActivity.class.getSimpleName();

    private static final int REQUEST_CODE_ADD_IMAGE = 1001;
    private static final int REQUEST_CODE_PREVIEW_EDIT_IMAGE = 1002;
    private static final int MAX_IMAGE = 9;
    private static final int COLUMNS = 4;
    private static final int GAP = 5;
    private int mLcmWidth;
    private int mUnitSize;
    private LinearLayout mAddImageLayout;
    private EditText mAddDescriptionTextBtn;
    private EditText mSetNumBtn;
    private EditText mSetUnitPrice;
    private EditText mSetProductName;
    private static ArrayList<String> mImageList = new ArrayList<String>();

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        setContentView(R.layout.circle_add_activity);
        initActionBar();
        initView();
    }

    private void initActionBar() {
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setDisplayShowTitleEnabled(false);
    }

    private void initView() {
        mImageList.clear();
        mAddDescriptionTextBtn = (EditText) findViewById(R.id.add_description_btn);
        mAddImageLayout = (LinearLayout) findViewById(R.id.add_image_layout);
        mLcmWidth = getResources().getDisplayMetrics().widthPixels;
        int addImageLayoutMargin = getResources().getDimensionPixelSize(R.dimen.friends_circle_add_image_margin);
        mUnitSize = (mLcmWidth - addImageLayoutMargin * 2) / COLUMNS;
        initImageLayout();
        mSetProductName = (EditText) findViewById(R.id.set_product_name);
        mSetNumBtn = (EditText) findViewById(R.id.set_num);
        mSetNumBtn.addTextChangedListener(mSetNumTextWatcherListener);
        mSetUnitPrice = (EditText) findViewById(R.id.set_unit_price);
        mSetUnitPrice.addTextChangedListener(mSetUnitPriceTextWatcherListener);
    }

    private void initImageLayout() {
        LinearLayout layout = new LinearLayout(this);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                mUnitSize));
        for (int i = 0; i < COLUMNS; i ++) {
            ImageView iv = getAddImageView();
            layout.addView(iv);
            if (i != 0) {
                iv.setVisibility(View.INVISIBLE);
            }
        }
        mAddImageLayout.addView(layout);
    }

    private void updateImageLayout() {
        mAddImageLayout.removeAllViews();

        LinearLayout columnLayout;
        View btn;
        int rows = mImageList.size() / COLUMNS + 1;
        rows = Math.min(rows, 3);
        boolean isAddImageAdded = false;
        for (int i = 0; i < rows; i++) {
            columnLayout = new LinearLayout(this);
            columnLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    mUnitSize));
            for (int j = 0; j < COLUMNS; j++) {
                int itemIndex = i * COLUMNS + j;
                btn = getImageView(itemIndex);
                if (itemIndex == mImageList.size() && mImageList.size() == ImageUtils.MAX_IMAGE_COUNT) {
                    isAddImageAdded = true;
                }
                if (btn.getVisibility() == View.INVISIBLE && !isAddImageAdded) {
                    btn = getAddImageView();
                    isAddImageAdded = true;
                }
                columnLayout.addView(btn);
            }
            mAddImageLayout.addView(columnLayout);
            if (isAddImageAdded) {
                break;
            }
        }
    }

    private ImageView getAddImageView() {
        ImageView iv = new ImageView(this);
        iv.setId(R.id.add_image_btn);
        iv.setImageResource(R.drawable.ic_add_image);
        iv.setOnClickListener(this);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
        lp.setMargins(GAP, GAP, GAP, GAP);
        iv.setLayoutParams(lp);
        return iv;
    }


    private View getImageView(int itemIndex) {
        View layout = (View) LayoutInflater.from(this).inflate(R.layout.circle_send_image_item, null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
        lp.setMargins(GAP, GAP, GAP, GAP);
        layout.setLayoutParams(lp);

        if (itemIndex >= mImageList.size()) {
            layout.setVisibility(View.INVISIBLE);
            return layout;
        }

        ImageView iv = (ImageView) layout.findViewById(R.id.image_item);
        ImageView deleteImage = (ImageView) layout.findViewById(R.id.delete_item);
        iv.setTag(R.id.image_item, itemIndex);
        iv.setOnClickListener(mImageClickListener);
        Glide.with(this).load(mImageList.get(itemIndex)).into(iv);
        deleteImage.setOnClickListener(mImageDeleteClickListener);
        deleteImage.setTag(itemIndex);
        return layout;
    }

    private void entrySelectImageActivity() {
        Intent intent = new Intent(this, SelectImageActivity.class);
        intent.putExtra(ImageUtils.SELECTED_IMAGE_COUNT, mImageList.size());
        startActivityForResult(intent, REQUEST_CODE_ADD_IMAGE);
    }

    private void entryImagePreviewEditActivity(int position) {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(ImageUtils.SELECTED_IMAGE_PATH_LIST, mImageList);
        intent.putExtra(ImageUtils.CLICKED_IMAGE_INDEX, position);
        intent.setClass(this, SendImagePreviewEditActivity.class);
        startActivityForResult(intent, REQUEST_CODE_PREVIEW_EDIT_IMAGE);
    }

    private View.OnClickListener mImageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.image_item);
            entryImagePreviewEditActivity(position);
        }
    };

    private View.OnClickListener mImageDeleteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag();
            removeChildView(position);
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_image_btn:
                entrySelectImageActivity();
                break;
        }
    }

    private boolean isAddImageBtn(int id) {
        if (id > 0 && id == R.id.add_image_btn) {
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_ADD_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    final ArrayList<String> pathList = data.getStringArrayListExtra(ImageUtils.SELECTED_IMAGE_PATH_LIST);
                    mImageList.addAll(pathList);
                    updateImageLayout();
                }
                break;
            case REQUEST_CODE_PREVIEW_EDIT_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    mImageList = data.getStringArrayListExtra(ImageUtils.SELECTED_IMAGE_PATH_LIST);
                    updateImageLayout();
                }
                break;
        }
    }

    private void removeChildView(int position) {
        int row = position / COLUMNS;
        int col = position % COLUMNS;
        ViewGroup curGroup = (ViewGroup) mAddImageLayout.getChildAt(row);
        curGroup.removeViewAt(col);
        mAddImageLayout.invalidate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.circle_add_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_send:
                sendCircleAddInfo();
                finish();
                return true;
            case android.R.id.home:
                setResult(Activity.RESULT_CANCELED);
                finish();
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private TextWatcher mSetNumTextWatcherListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private TextWatcher mSetUnitPriceTextWatcherListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().contains(".")) {
                if (s.length() - 1 - s.toString().indexOf(".") > 2) {
                    String value = s.toString().substring(0, s.toString().indexOf(".") + 3);
                    mSetUnitPrice.setText(value);
                    mSetUnitPrice.setSelection(value.length());
                }
            }
            if (s.toString().trim().substring(0).equals(".")) {
                String value = "0" + s;
                mSetUnitPrice.setText(s);
                mSetUnitPrice.setSelection(s.length());
            }

            if (s.toString().startsWith("0")
                    && s.toString().trim().length() > 1) {
                if (!s.toString().substring(1, 2).equals(".")) {
                    mSetUnitPrice.setText(s.subSequence(0, 1));
                    mSetUnitPrice.setSelection(1);
                }
            }
        }
    };


    private void sendCircleAddInfo() {
        new Thread() {
            @Override
            public void run() {
                postCircleAddInfo();
            }
        }.start();
    }

    private void postCircleAddInfo() {
        String curTime = Utils.getCurrentUnsignedSystemTime();
        ArrayList<String> pictureInfoList = getCircleAddPictureInfo(curTime);
        String circleStringInfo = getCircleTextInfo(curTime);
        if (pictureInfoList != null && pictureInfoList.size() > 0) {
            JsonUtils.JsonResultData result = JsonUtils.sendCircleHttpPost(pictureInfoList, JsonUtils.POST_TILE + circleStringInfo);
            if (result.isSuccess()) {
                setResult(Activity.RESULT_OK);
            }
        } else {
            JsonUtils.JsonResultData resultText = JsonUtils.sendCircleHttpPost(null, JsonUtils.POST_TILE + circleStringInfo);
            if (resultText.isSuccess()) {
                setResult(Activity.RESULT_OK);
            }
        }
    }

    private String getCircleTextInfo(String curTime) {
        JSONObject object = new JSONObject();
        try {
            object.put("vc_ower", UserInfo.getUserPhoneNumber(CircleAddActivity.this));
            object.put("vc_id", "123");
            object.put("vc_itemno", "");
            object.put("vc_unit", "");
            object.put("vc_thumbs", "0");
            object.put("vc_comment", "");
            object.put("vc_describe", !TextUtils.isEmpty(mAddDescriptionTextBtn.getText()) ? mAddDescriptionTextBtn.getText().toString().trim() : "");
            object.put("vc_itemname", !TextUtils.isEmpty(mSetProductName.getText()) ? mSetProductName.getText().toString().trim() : "");
            object.put("d_quantity", !TextUtils.isEmpty(mSetNumBtn.getText()) ? mSetNumBtn.getText().toString().trim() : "");
            object.put("d_price", !TextUtils.isEmpty(mSetUnitPrice.getText()) ? mSetUnitPrice.getText().toString().trim() : "");
            for (int i = 1; i <= ImageUtils.MAX_IMAGE_COUNT; i ++) {
                if (i <= mImageList.size()) {
                    object.put("vc_pic" + i, curTime + i + ".jpg");
                } else {
                    object.put("vc_pic" + i, "");
                }
            }
            object.put("dt_operdate", Utils.getCircleSystemTimeSeconds());
        } catch (JSONException ex) {
            return null;
        }
        return object.toString();
    }

    private ArrayList<String> getCircleAddPictureInfo(String curTime) {
        ArrayList<String> pictureInfoList = new ArrayList<String>();
        String imageString = null;
        String owner = UserInfo.getUserPhoneNumber(this);
        if (TextUtils.isEmpty(owner)) {
            return null;
        }

        for (int i = 1; i <= mImageList.size(); i ++) {
            imageString = ImageUtils.bitmapToBase64String(this, mImageList.get(i - 1));
            String Base64StrData = imageString.replace("+", "%2B");
            String pictureData = "owner=" + owner + "&fileName=" + curTime + i + ".jpg&" + JsonUtils.POST_TILE + Base64StrData;
            pictureInfoList.add(pictureData);
        }
        return pictureInfoList;
    }
}
