package com.android.lahuhula.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.lahuhula.R;
import com.android.lahuhula.util.ImageUtils;
import com.android.lahuhula.view.UserInfo;
import com.baidu.mapapi.map.Text;
import com.bumptech.glide.Glide;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static com.android.lahuhula.util.ImageUtils.HEAD_ICON_NAME;

/**
 * Created by lenovo on 2017/3/19.
 */

public class MyInfomationActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int REQUEST_CODE_GET_IMAGE = 0x101;
    private ImageView mIconView;
    private OnMyIconChangedListener mMyIconChangedListener = null;

    public interface OnMyIconChangedListener {
        public void onIconChanged(Bitmap bitmap);
    }

    public void setMyIconChangedListener(OnMyIconChangedListener listener) {
        mMyIconChangedListener = listener;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        setContentView(R.layout.my_infomation_activity);
        setupView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void setupView() {
        TextView personalName = (TextView) findViewById(R.id.personal_name);
        personalName.setText(UserInfo.getUserPersonalName(this));

        Button quit = (Button) findViewById(R.id.account_quit);
        quit.setOnClickListener(this);

        LinearLayout headPortrait = (LinearLayout) findViewById(R.id.head_portrait_layout);
        headPortrait.setOnClickListener(this);
        mIconView = (ImageView) findViewById(R.id.head_portrait_iv);
        mIconView.setOnClickListener(this);
        Bitmap bitmap = ImageUtils.getMyIconBitmap(this);
        if (bitmap != null) {
            mIconView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.head_portrait_layout:
                entryPickImageToHeadPortrait();
                break;
            case R.id.account_quit:
                UserInfo.clearUserLogin(this);
                finish();
                break;
        }
    }

    private void entryPickImageToHeadPortrait() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra("crop", true);
        startActivityForResult(intent, REQUEST_CODE_GET_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_GET_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();
                    Log.d("zhangtao", "uri:" + uri);
                    if (uri != null) {
                        //Glide.with(this).load(uri).into(mIconView);
                        new SaveImageTask().execute(uri);
                    }
                } else {
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    class SaveImageTask extends AsyncTask<Uri, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Uri... uri) {
            Bitmap bm = ImageUtils.saveMyIcon(MyInfomationActivity.this, uri[0]);
            return bm;
        }

        @Override
        protected void onPostExecute(Bitmap bm) {
            if (bm != null) {
                mIconView.setImageBitmap(bm);
            }
        }
    }
}
