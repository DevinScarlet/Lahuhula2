package com.android.lahuhula.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.lahuhula.R;
import com.android.lahuhula.util.JsonUtils;
import com.android.lahuhula.util.Utils;
import com.baidu.mapapi.map.Text;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lenovo on 2017/2/21.
 */

public class RegisterInfoActivity extends AppCompatActivity
        implements View.OnClickListener, CheckBox.OnTouchListener {
    private static final String TAG = RegisterInfoActivity.class.getSimpleName();

    private EditText mPhoneNumber;
    private EditText mUserName;
    private EditText mPassword;
    private EditText mPasswordConfirm;
    private CheckBox mCheckBoxBuyer;
    private CheckBox mCheckBoxSaler;
    private EditText mAddress;
    private EditText mBankCardNumber;
    private LinearLayout mAddressLayout;
    private LinearLayout mBankCardLayout;
    private Toast mToast = null;

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);

        setContentView(R.layout.register_info_layout);
        initActionBar();
        setupView();
    }

    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void setupView() {
        Button registerBtn = (Button) findViewById(R.id.register_btn);
        registerBtn.setOnClickListener(this);

        mPhoneNumber = (EditText) findViewById(R.id.register_phone);
        mUserName = (EditText) findViewById(R.id.register_name);
        mPassword = (EditText) findViewById(R.id.register_password);
        mPasswordConfirm = (EditText) findViewById(R.id.register_password_comfirm);

        mCheckBoxBuyer = (CheckBox) findViewById(R.id.register_buyer);
        mCheckBoxBuyer.setChecked(true);
        mCheckBoxBuyer.setOnTouchListener(this);
        mCheckBoxSaler = (CheckBox) findViewById(R.id.register_saler);
        mCheckBoxSaler.setChecked(false);
        mCheckBoxSaler.setOnTouchListener(this);

        mAddressLayout = (LinearLayout) findViewById(R.id.register_receive_address_layout);
        mAddressLayout.setVisibility(View.VISIBLE);
        mAddress = (EditText) findViewById(R.id.register_receive_address);
        mBankCardLayout = (LinearLayout) findViewById(R.id.register_bank_number_layout);
        mBankCardLayout.setVisibility(View.GONE);
        mBankCardNumber = (EditText) findViewById(R.id.register_bank_number);
    }

    private void registerUserInfo() {
        if (TextUtils.isEmpty(mPhoneNumber.getText())) {
            showToast(this, "手机号码不能为空");
        } else if (TextUtils.isEmpty(mUserName.getText())) {
            showToast(this, "昵称不能为空");
        } else if (TextUtils.isEmpty(mPassword.getText())) {
            showToast(this, "密码不能为空");
        } else if (TextUtils.isEmpty(mPasswordConfirm.getText())) {
            showToast(this, "确认密码不能为空");
        } else if (!mPassword.getText().toString().equals(mPasswordConfirm.getText().toString())) {
            Log.d(TAG, "mPassword.getText():" + mPassword.getText());
            Log.d(TAG, "mPasswordConfirm.getText():" + mPasswordConfirm.getText());
            showToast(this, "两次输入的密码不一致，请重新输入");
        } else {
            new Thread() {
                @Override
                public void run() {
                    postRegisterInfo();
                }
            }.start();
        }
    }

    private void postRegisterInfo() {
        String registerInfo = getRegisterInfo();
        Log.d(TAG, "registerInfo:" + registerInfo);
        if (TextUtils.isEmpty(registerInfo)) {
            return;
        }
        JsonUtils.JsonResultData result = JsonUtils.registerInfoHttpPost(JsonUtils.USER_REGISTER_URI, JsonUtils.POST_TILE + registerInfo);
        if (result.isSuccess()) {
            finish();
        }
    }

    private String getRegisterInfo() {
        JSONObject object = new JSONObject();
        try {
            object.put("vc_nickname", mPhoneNumber.getText().toString().trim());
            object.put("vc_psd", mPassword.getText().toString());
            object.put("vc_phone", mPhoneNumber.getText().toString().trim());
            object.put("vc_name", mUserName.getText().toString().trim());
            object.put("vc_address", mAddress.getText().toString().trim());
            object.put("vc_seller_buyer", mCheckBoxBuyer.isChecked() ? "1" : "0");
            object.put("vc_head", "");
            object.put("vc_address1", TextUtils.isEmpty(mAddress.getText()) ? "" : mAddress.getText().toString());
            object.put("vc_address2", "");
            object.put("vc_address3", "");
            object.put("vc_address4", "");
            object.put("vc_address5", "");
        } catch (JSONException ex) {
            return null;
        }
        return object.toString();
    }

    private void showToast(Context context, String msg) {
        if (mToast != null) {
            mToast.cancel();
            mToast = null;
        }
        mToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        mToast.show();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.register_btn:
                registerUserInfo();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view == mCheckBoxBuyer && !mCheckBoxBuyer.isChecked()) {
            mCheckBoxBuyer.setChecked(true);
            mCheckBoxSaler.setChecked(false);
            mAddressLayout.setVisibility(View.VISIBLE);
            mBankCardLayout.setVisibility(View.GONE);
            return true;
        } else if (view == mCheckBoxSaler && !mCheckBoxSaler.isChecked()) {
            mCheckBoxSaler.setChecked(true);
            mCheckBoxBuyer.setChecked(false);
            mAddressLayout.setVisibility(View.GONE);
            mBankCardLayout.setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }
}
