package com.android.lahuhula.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.lahuhula.R;
import com.android.lahuhula.bean.MyAddress;
import com.android.lahuhula.util.JsonUtils;
import com.android.lahuhula.util.Utils;
import com.android.lahuhula.util.VxUtils;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by lenovo on 2017/3/11.
 */

public class BuyInfoComfirmActivity extends AppCompatActivity
        implements ImageView.OnClickListener {
    private static final String TAG = BuyInfoComfirmActivity.class.getSimpleName();
    public static final int REQUEST_CODE = 200;
    public static final int RESULT_CODE = 300;
    private static final String PRICE_UNIT = "￥";
    private TextView mProductNameTv;
    private String mProductName;
    private EditText mBuyNumber;
    private ImageView mBuyNumMinus;
    private ImageView mBuyNumPlus;
    private TextView mTotalPrice;
    private Button mPayBtn;
    private double mUnitPrice;
    private int mBuyNumberValue = 1;
    private IWXAPI mVxApi = WXAPIFactory.createWXAPI(this, VxUtils.WX_APP_ID, true);

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        mVxApi.registerApp(VxUtils.WX_APP_ID);
        setContentView(R.layout.buy_info_comfirm_activity);
        setActionBar();
        setupView();
    }

    private void setActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            return;
        }

        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void setupView() {
        MyAddress myAddress = Utils.getDefaultAddress();
        ImageView addArroe = (ImageView) findViewById(R.id.iv_add_address);
        TextView receiverName = (TextView) findViewById(R.id.buy_info_receiver_name);
        TextView receiverAddress = (TextView) findViewById(R.id.buy_info_receiver_address);
        TextView receiverPhone = (TextView) findViewById(R.id.buy_info_receiver_phone_num);
        if (null != myAddress) {
            receiverName.setText(getString(R.string.buy_info_receiver_name, myAddress.getUserName()));
            receiverAddress.setText("收货地址：" + myAddress.getAddress());
            receiverPhone.setText(myAddress.getUserNum());
        } else {
            receiverName.setText(getString(R.string.buy_info_receiver_name, "请添加收货人信息"));
            receiverAddress.setText("收货地址：" + "请添加收货地址");
            receiverPhone.setText("请添加收货人联系方式");
        }

        String price = getIntent().getStringExtra("vc_price");
        mProductNameTv = (TextView) findViewById(R.id.buy_info_description);
        mProductName = mProductNameTv.getText().toString();
        mBuyNumber = (EditText) findViewById(R.id.buy_info_num);
        mBuyNumberValue = 1;
        mBuyNumber.setText(String.valueOf(mBuyNumberValue));
        mBuyNumber.setSelection(mBuyNumber.getText().length());
        mBuyNumber.addTextChangedListener(new EditChangedListener());
        mBuyNumMinus = (ImageView) findViewById(R.id.buy_info_num_minus);
        mBuyNumPlus = (ImageView) findViewById(R.id.buy_info_num_plus);
        mBuyNumMinus.setEnabled(false);
        mBuyNumMinus.setOnClickListener(this);
        mBuyNumPlus.setOnClickListener(this);
        mTotalPrice = (TextView) findViewById(R.id.buy_info_total_price);
        mUnitPrice = Double.parseDouble(price);
        updateTotalPrice();
        mPayBtn = (Button) findViewById(R.id.buy_info_pay_btn);
        mPayBtn.setOnClickListener(this);
        addArroe.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        hideInputMethod();
    }

    private void hideInputMethod() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mBuyNumber.getWindowToken(), 0);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.buy_info_num_minus:
                updateBuyNumber(false);
                break;
            case R.id.buy_info_num_plus:
                updateBuyNumber(true);
                break;
            case R.id.buy_info_pay_btn:
                payStart();
                break;
            case R.id.iv_add_address:
                //startActivityForResult(new Intent(this, MyAddressManagerActivity.class), REQUEST_CODE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_CODE) {

            }
        }
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    private void payStart() {
        Log.d("zhangtao", "payStart...");
        if (!VxUtils.isVxAppInstalled(this)) {
            Toast.makeText(this, "please install Vx frist!", Toast.LENGTH_SHORT).show();
            return;
        }
        new GetPrepayIdTask().execute();
    }

    private void genPayReq(Map<String, String> result) {
        PayReq req = new PayReq();
        req.appId = VxUtils.WX_APP_ID;
        req.partnerId = VxUtils.PARTNER_ID;
        req.prepayId = result.get("prepay_id");
        req.packageValue = "Sign=WXPay";
        req.nonceStr = VxUtils.getNonceStr();
        req.timeStamp = VxUtils.genTimeStamp();


        List<NameValuePair> signParams = new LinkedList<NameValuePair>();
        signParams.add(new BasicNameValuePair("appid", req.appId));
        signParams.add(new BasicNameValuePair("noncestr", req.nonceStr));
        signParams.add(new BasicNameValuePair("package", req.packageValue));
        signParams.add(new BasicNameValuePair("partnerid", req.partnerId));
        signParams.add(new BasicNameValuePair("prepayid", req.prepayId));
        signParams.add(new BasicNameValuePair("timestamp", req.timeStamp));

        req.sign = VxUtils.genAppSign(signParams);

        Log.e(TAG, signParams.toString());
        boolean reqResult = mVxApi.sendReq(req);
        Log.e(TAG, "reqResult:" + reqResult);
        finish();
    }

    private class GetPrepayIdTask extends AsyncTask<Void, Void, Map<String, String>> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Map<String, String> result) {
            if (result != null) {
                genPayReq(result);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected Map<String, String> doInBackground(Void... params) {
            String url = String.format("https://api.mch.weixin.qq.com/pay/unifiedorder");
            String entity = VxUtils.genProductArgs(Utils.getTotalPriceValue(mUnitPrice * mBuyNumberValue), mProductName);
            String entityString;
            try {
                entityString = new String(entity.getBytes("utf-8"), "iso8859-1");
            } catch (UnsupportedEncodingException e) {
                return null;
            }
            Log.e(TAG, entity);
            byte[] buf = JsonUtils.httpPost(url, entityString);
            String content = new String(buf);
            Log.e(TAG, content);
            Map<String, String> xml = VxUtils.decodeXml(content);
            return xml;
        }
    }


    private void updateBuyNumber(boolean isPlus) {
        if (isPlus) {
            mBuyNumberValue++;
        } else {
            mBuyNumberValue--;
        }
        String number = String.valueOf(mBuyNumberValue);
        mBuyNumber.setText(number);
        mBuyNumber.setSelection(number.length());
        mBuyNumMinus.setEnabled(mBuyNumberValue > 1);
        updateTotalPrice();
        mPayBtn.setEnabled(true);
    }

    private void updateTotalPrice() {
        String price = PRICE_UNIT + String.valueOf(Utils.getTotalPriceValue(mUnitPrice * mBuyNumberValue));
        mTotalPrice.setText(getString(R.string.buy_info_total_price) + price);
    }

    private class EditChangedListener implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            Log.d("zhangtao", "beforeTextChanged.s:" + s);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Log.d("zhangtao", "onTextChanged.s:" + s);
            if (TextUtils.isEmpty(s)) {
                mBuyNumberValue = 0;
                mBuyNumMinus.setEnabled(false);
                mPayBtn.setEnabled(false);
                updateTotalPrice();
                return;
            }

            mBuyNumberValue = Integer.parseInt(s.toString().trim());
            mBuyNumber.setSelection(s.toString().trim().length());
            mBuyNumMinus.setEnabled(mBuyNumberValue > 1);
            mPayBtn.setEnabled(mBuyNumberValue >= 1);
            updateTotalPrice();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    ;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case android.R.id.home:
                finish();
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
