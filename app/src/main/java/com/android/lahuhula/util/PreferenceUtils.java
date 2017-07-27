package com.android.lahuhula.util;

/**
 * Created by Devin on 2017/6/18.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.android.lahuhula.activity.HuHuApplication;
import com.android.lahuhula.bean.MyAddress;

import java.util.ArrayList;

public class PreferenceUtils {
    public static final String KEY_DEFAULT_PREFERENCE = "com.lahuhula.utils";

    private static SharedPreferences settingPref = HuHuApplication.getApp()
            .getSharedPreferences(KEY_DEFAULT_PREFERENCE, Context.MODE_PRIVATE);
    private static SharedPreferences.Editor editor = settingPref.edit();


    public static void init(Context context) {
        if (null == settingPref) {
            settingPref = android.preference.PreferenceManager
                    .getDefaultSharedPreferences(context);
        }
    }

    /**
     * 获取缓存中的频道ID,名称
     *
     * @return
     */
    public static ArrayList<MyAddress> getCachedAddress() {
        String cachedValue = settingPref.getString("myAddress", "");
        if (!TextUtils.isEmpty(cachedValue)) {
            ArrayList<MyAddress> result = new ArrayList<MyAddress>();

            String[] arr_1 = cachedValue.split(":");
            for (String itemPair : arr_1) {
                String[] arr_2 = itemPair.split("~");
                MyAddress channel = new MyAddress(arr_2[0], arr_2[1],
                        arr_2[2], arr_2[3]);
                result.add(channel);
            }
            return result;
        } else {
            return new ArrayList<MyAddress>();
        }
    }

    /**
     * 保存新闻频道ID和名称的组合，供联网获取失败时使用
     */
    public static void setCachedAddress(ArrayList<MyAddress> maddress) {
        StringBuilder sb = new StringBuilder();
        Log.i("zhangtt", "setCachedAddress" + maddress);
        if (null == maddress) {
            editor.putString("myAddress", "");
            editor.commit();
            Log.i("zhangtt", "null" + PreferenceUtils.getCachedAddress().toString());
        } else {
            for (MyAddress nc : maddress) {
                String channelItem = nc.getUserName() + "~" + nc.getUserNum() + "~"
                        + nc.getDefaultAddress() + "~" + nc.getAddress();
                sb.append(channelItem).append(":");
            }

            if (sb.length() > 0) {
                String result = sb.substring(0, sb.length() - 1);
                editor.putString("myAddress", result);
                editor.commit();
            }
            Log.i("zhangtt", "非空" + PreferenceUtils.getCachedAddress().toString());
        }

    }

}
