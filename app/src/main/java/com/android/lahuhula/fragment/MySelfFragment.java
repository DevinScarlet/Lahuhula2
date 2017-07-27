package com.android.lahuhula.fragment;

//import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.lahuhula.R;
import com.android.lahuhula.activity.AboutMeActivity;
import com.android.lahuhula.activity.MyAddressManagerActivity;
import com.android.lahuhula.activity.MyInfomationActivity;
import com.android.lahuhula.util.ImageUtils;
import com.bumptech.glide.Glide;

import java.io.File;

/**
 * Created by lenovo on 2016/12/27.
 */

public class MySelfFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "MySelfFragment";

    private RelativeLayout mMyInfo;
    private ImageView mMyIcon;

    public MySelfFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.me_fragment_layout, container, false);

        mMyInfo = (RelativeLayout) rootView.findViewById(R.id.my_info);
        mMyInfo.setOnClickListener(this);
        mMyIcon = (ImageView) rootView.findViewById(R.id.my_icon);

        rootView.findViewById(R.id.receive_address_manager).setOnClickListener(this);

        LinearLayout aboutMe = (LinearLayout) rootView.findViewById(R.id.about_me_layout);
        aboutMe.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        Bitmap bitmap = ImageUtils.getMyIconBitmap(getContext());
        if (bitmap != null) {
            mMyIcon.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.my_info:
                entryMyInfomationActivity();
                break;
            case R.id.receive_address_manager:
                entryMyAddressManagerActivity();
                break;
            case R.id.about_me_layout:
                startActivity(new Intent(view.getContext(), AboutMeActivity.class));
                break;
        }
    }

    private void entryMyInfomationActivity() {
        startActivity(new Intent(getContext(), MyInfomationActivity.class));
    }

    private void entryMyAddressManagerActivity() {
        startActivity(new Intent(getContext(), MyAddressManagerActivity.class));
    }
}
