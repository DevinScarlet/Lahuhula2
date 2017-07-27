package com.android.lahuhula.wxapi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.android.lahuhula.util.VxUtils;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * Created by lenovo on 2017/3/15.
 */

public class WXPayEntryActivity extends AppCompatActivity implements IWXAPIEventHandler {
    private static final String TAG = WXPayEntryActivity.class.getSimpleName();
    private IWXAPI api;
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        api = WXAPIFactory.createWXAPI(this, VxUtils.WX_APP_ID);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq baseReq) {
        Log.d(TAG, "baseReq:" + baseReq);
    }

    @Override
    public void onResp(BaseResp baseResp) {
        Log.d(TAG, "baseResp.type:" + baseResp.getType() + "baseResp.errCode:" + baseResp.errCode);

        int resqType = baseResp.getType();
        switch (resqType) {
            case ConstantsAPI.COMMAND_PAY_BY_WX:
                if (baseResp.errCode == BaseResp.ErrCode.ERR_OK) {
                    Toast.makeText(this, "支付成功", Toast.LENGTH_SHORT).show();
                } else if (baseResp.errCode == BaseResp.ErrCode.ERR_USER_CANCEL) {
                    Toast.makeText(this, "取消支付", Toast.LENGTH_SHORT).show();
                }
                finish();
                break;
        }
    }
}
