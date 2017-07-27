package com.android.lahuhula.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.lahuhula.R;
import com.android.lahuhula.adapter.PictureViewPagerAdapter;
import com.android.lahuhula.util.ImageUtils;
import com.bumptech.glide.Glide;
import com.viewpagerindicator.CirclePageIndicator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by lenovo on 2017/1/2.
 */

public class PictureViewActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.gallery_layout);
        hideStatusBarAndNavigationBar();
        init();
    }

    private void hideStatusBarAndNavigationBar() {
        findViewById(R.id.view_pager).setSystemUiVisibility(getHideNavigationBarFlag());
    }

    private int getHideNavigationBarFlag() {
        int flags = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        return flags;
    }

    private void init() {
        ArrayList<String> picturesList = getIntent().getStringArrayListExtra("pictures");
        String owner = getIntent().getStringExtra("vc_owner");
        final String price = getIntent().getStringExtra("vc_price");
        int curPictureIndex = getIntent().getIntExtra("cur_picture_index", 0);
        int totalCount = picturesList.size();


        Log.d("zhangtao", "totalCount:" + totalCount);
        Log.d("zhangtao", "owner:" + owner);
        Log.d("zhangtao", "curPictureIndex:" + curPictureIndex);

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        ArrayList<View> pagerViewList = new ArrayList<View>();
        for (int i = 0; i < totalCount; i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.picture_view_item, null);
            ImageView imageView = (ImageView) view.findViewById(R.id.image);

            Log.d("zhangtao", "picturesList.get(" + i + "):" + picturesList.get(i));

            try {
                Glide.with(this).load(new URL(ImageUtils.PICTURE_URL + owner + "/" + picturesList.get(i))).error(R.drawable.login_icon).into(imageView);
            } catch (MalformedURLException e) {
                //
            }
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    finish();
                    startBuyInfoComfirmActivity(price);
                    return true;
                }
            });
            //TextView num = (TextView) view.findViewById(R.id.pager_num) ;
            //num.setText((i + 1) + "/" + totalCount);
            pagerViewList.add(view);
        }
        PictureViewPagerAdapter adapter = new PictureViewPagerAdapter(viewPager, pagerViewList);
        viewPager.setAdapter(adapter);
        CirclePageIndicator cpi = (CirclePageIndicator) findViewById(R.id.indicator);
        cpi.setViewPager(viewPager, curPictureIndex);
        cpi.setRadius(8);
        TextView buyBtn = (TextView) findViewById(R.id.buy_btn);
        TextView buyPrice = (TextView) findViewById(R.id.product_unit_price);
        buyPrice.setText("￥ " + price);
        buyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startBuyInfoComfirmActivity(price);
            }
        });
    }

    private void startBuyInfoComfirmActivity(String price) {
        finish();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.putExtra("vc_price", price);
        intent.setClass(this, BuyInfoComfirmActivity.class);
        startActivity(intent);
    }
}
