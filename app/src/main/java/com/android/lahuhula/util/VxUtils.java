package com.android.lahuhula.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.provider.SyncStateContract;
import android.util.Log;
import android.util.Xml;

import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by lenovo on 2017/3/16.
 */

public class VxUtils {
    private static final String TAG = VxUtils.class.getSimpleName();

    public static final String WX_APP_ID = "wx0b1bd0c4d42bfdc5";
    public static final String PARTNER_ID = "1448740602";
    public static final String API_SIGN_KEY = "LlZtLhe39dgufpcLhu548485hulXzZcy";
    private Map<String,String> mResultunifiedorder;

    public static String genAppSign(List<NameValuePair> params) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < params.size(); i++) {
            sb.append(params.get(i).getName());
            sb.append('=');
            sb.append(params.get(i).getValue());
            sb.append('&');
        }
        sb.append("key=");
        sb.append(API_SIGN_KEY);

        //sb.append("sign str\n"+sb.toString()+"\n\n");
        String appSign = Utils.getMD5String(sb.toString()).toUpperCase();
        Log.e(TAG, "genAppSign.appSign:" + appSign);
        return appSign;
    }


    public static String genPackageSign(List<NameValuePair> params) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < params.size(); i++) {
            sb.append(params.get(i).getName());
            sb.append('=');
            sb.append(params.get(i).getValue());
            sb.append('&');
        }
        sb.append("key=");
        sb.append(API_SIGN_KEY);
        Log.e(TAG, "genPackageSign.sb.toString():" + sb.toString());
        String packageSign = Utils.getMD5String(sb.toString()).toUpperCase();
        Log.e(TAG, "genPackageSign.packageSign:" + packageSign);
        return packageSign;
    }

    public static String toXml(List<NameValuePair> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("<xml>");
        for (int i = 0; i < params.size(); i++) {
            sb.append("<"+ params.get(i).getName() + ">");


            sb.append(params.get(i).getValue());
            sb.append("</"+ params.get(i).getName() + ">");
        }
        sb.append("</xml>");

        Log.e(TAG, "toXml.sb:" + sb.toString());
        return sb.toString();
    }

    public static Map<String,String> decodeXml(String content) {

        try {
            Map<String, String> xml = new HashMap<String, String>();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(content));
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                String nodeName = parser.getName();
                switch (event) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if ("xml".equals(nodeName) == false) {
                            xml.put(nodeName,parser.nextText());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                event = parser.next();
            }
            return xml;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return null;

    }

    public static String genProductArgs(double value, String bodyTitle) {
        String price = String.valueOf(new DecimalFormat("#").format(value * 100));
        StringBuffer xml = new StringBuffer();
        try {
            xml.append("</xml>");
            List<NameValuePair> packageParams = new LinkedList<NameValuePair>();
            packageParams.add(new BasicNameValuePair("appid", WX_APP_ID));
            packageParams.add(new BasicNameValuePair("body", bodyTitle));
            packageParams.add(new BasicNameValuePair("mch_id", PARTNER_ID));
            packageParams.add(new BasicNameValuePair("nonce_str", getNonceStr()));
            packageParams.add(new BasicNameValuePair("notify_url", "http://www.weixin.qq.com/wxpay/pay.php"));
            packageParams.add(new BasicNameValuePair("out_trade_no",genTimeStamp()));
            packageParams.add(new BasicNameValuePair("spbill_create_ip","192.168.199.231"));
            packageParams.add(new BasicNameValuePair("total_fee", price));
            packageParams.add(new BasicNameValuePair("trade_type", "APP"));


            String sign = genPackageSign(packageParams);
            packageParams.add(new BasicNameValuePair("sign", sign));

            String xmlstring = toXml(packageParams);
            return xmlstring;

        } catch (Exception e) {
            Log.e(TAG, "genProductArgs fail, ex = " + e.getMessage());
            return null;
        }
    }

    public static String getNonceStr() {
        return Utils.getMD5String(String.valueOf(new Random().nextInt(10000)));
    }

    public static String genTimeStamp() {
        return String.valueOf(System.currentTimeMillis() / 1000);
    }

    public static boolean isVxAppInstalled(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo("com.tencent.mm", PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
