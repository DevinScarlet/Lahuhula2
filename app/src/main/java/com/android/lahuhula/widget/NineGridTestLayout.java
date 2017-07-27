package com.android.lahuhula.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.AttributeSet;

import com.android.lahuhula.R;
import com.android.lahuhula.activity.PictureViewActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.ArrayList;

/**
 * 描述：
 * 作者：HMY
 * 时间：2016/5/12
 */
public class NineGridTestLayout extends NineGridLayout {

    protected static final int MAX_W_H_RATIO = 3;

    public NineGridTestLayout(Context context) {
        super(context);
    }

    public NineGridTestLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean displayOneImage(final RatioImageView imageView, String url, final int parentWidth) {
        //处理图片的加载
        Glide.with(mContext).load(url).asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                int w = resource.getWidth();
                int h = resource.getHeight();
                int newW;
                int newH;
                if (h > w * MAX_W_H_RATIO) {//h:w = 5:3
                    newW = parentWidth / 2;
                    newH = newW * 5 / 3;
                } else if (h < w) {//h:w = 2:3
                    newW = parentWidth * 2 / 3;
                    newH = newW * 2 / 3;
                } else {//newH:h = newW :w
                    newW = parentWidth / 2;
                    newH = h * newW / w;
                }
                setOneImageLayoutParams(imageView, newW, newH);
                imageView.setImageBitmap(resource);
            }
        });
        return false;
    }

    @Override
    protected void displayImage(RatioImageView imageView, String url) {
        Glide.with(getContext()).load(url).thumbnail(0.2f).centerCrop().error(R.drawable.login_icon).dontAnimate().
                into(imageView);
    }


    @Override
    protected void onClickImage(int i, String url, ArrayList<String> urlList, String mOwner, String price) {
        //进入图片页面
        mContext.startActivity(getPictureViewActivityIntent(i, urlList, mOwner, price));
    }

    private Intent getPictureViewActivityIntent(int index, ArrayList<String> urlList, String mOwner, String price) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.putStringArrayListExtra("pictures", urlList);
        intent.putExtra("vc_price", price);
        intent.putExtra("vc_owner", mOwner);
        intent.putExtra("cur_picture_index", index);
        intent.setClass(mContext, PictureViewActivity.class);
        return intent;
    }
}
