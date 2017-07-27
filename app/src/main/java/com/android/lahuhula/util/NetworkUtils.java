package com.android.lahuhula.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telecom.TelecomManager;
import android.widget.Toast;

/**
 * Created by lenovo on 2017/3/26.
 */

public class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            showInAvailableNetworkToast(context);
            return false;
        }

        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isAvailable()) {
            showInAvailableNetworkToast(context);
            return false;
        }
        return true;
    }

    private static void showInAvailableNetworkToast(Context context) {
        Toast.makeText(context, "当前网络不可用，请检查您的网络设置", Toast.LENGTH_SHORT).show();
    }
}
