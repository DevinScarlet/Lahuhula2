package com.android.lahuhula.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.android.lahuhula.util.JsonUtils;
import com.baidu.mapapi.map.Text;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by lenovo on 2017/4/9.
 */

public class UserInfo {
    private static final String TAG = "UserInfo";
    public static final String USER_INFO_PREF = "user_info_pref";

    public static SharedPreferences getUserInfoSharedPreferences(Context context) {
        return context.getSharedPreferences(USER_INFO_PREF, Context.MODE_PRIVATE);
    }

    public static boolean isUserLogin(Context context) {
        SharedPreferences prefs = getUserInfoSharedPreferences(context);
        String phone = prefs.getString("vc_phone", null);

        if (!TextUtils.isEmpty(phone)) {
            return true;
        }
        return false;
    }

    public static void clearUserLogin(Context context) {
        SharedPreferences prefs = getUserInfoSharedPreferences(context);
        prefs.edit().clear().commit();
    }

    public static String getUserPhoneNumber(Context context) {
        SharedPreferences prefs = getUserInfoSharedPreferences(context);
        return prefs.getString("vc_phone", "");
    }

    public static String getUserPersonalName(Context context) {
        SharedPreferences prefs = getUserInfoSharedPreferences(context);
        return prefs.getString("vc_name", "");
    }

    public static void saveUserInfoToSharedPreferences(Context context, JSONObject jsonObject) {
        SharedPreferences prefs = getUserInfoSharedPreferences(context);
        try {
            final String headIconName = jsonObject.getString("vc_head");
            final String owner = jsonObject.getString("vc_phone");
            final String name = jsonObject.getString("vc_name");
            prefs.edit().putString("int_id", jsonObject.getString("int_id"))
                    .putString("vc_nickname", jsonObject.getString("vc_nickname"))
                    .putString("vc_phone", owner)
                    .putString("vc_name", !TextUtils.isEmpty(name) ? name : owner)
                    .putString("vc_seller_buyer", jsonObject.getString("vc_seller_buyer"))
                    .putString("vc_head", headIconName)
                    .putString("vc_address1", jsonObject.getString("vc_address1"))
                    .putString("vc_address2", jsonObject.getString("vc_address2"))
                    .putString("vc_address3", jsonObject.getString("vc_address3"))
                    .putString("vc_address4", jsonObject.getString("vc_address4"))
                    .putString("vc_address5", jsonObject.getString("vc_address5"))
                    .commit();

            /*if (!TextUtils.isEmpty(headIconName)) {
                new Thread() {
                    @Override
                    public void run() {
                        String imageUrl = "http://sxfarm.cn/upload/" + owner + "/" + headIconName;
                    }
                }.start();
            }*/
        } catch (JSONException je) {
            Log.e(TAG, "save user info to sharedpreferences failed");
        }
    }
}
