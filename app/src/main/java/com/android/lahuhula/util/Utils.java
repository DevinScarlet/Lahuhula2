package com.android.lahuhula.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.lahuhula.bean.MyAddress;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by lenovo on 2017/1/8.
 */

public class Utils {
    private static final String TAG = "Utils";

    public static String getCurrentUnsignedSystemTime() {
        long time = System.currentTimeMillis();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date(time);
        String timeString = format.format(date);
        Log.d(TAG, timeString);
        return timeString;
    }

    public static String getCurrentSystemDate() {
        long time = System.currentTimeMillis();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(time);
        String timeString = format.format(date);
        Log.d(TAG, timeString);
        return timeString;
    }

    public static String getCurrentSystemTime() {
        long time = System.currentTimeMillis();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date(time);
        String timeString = format.format(date);
        Log.d(TAG, timeString);
        return timeString;
    }

    public static String getCircleSystemTimeSeconds() {
        long time = System.currentTimeMillis();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(time);
        String timeString = format.format(date);
        Log.d(TAG, timeString);
        return timeString;
    }

    public static Date stringToDate(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return format.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String getMD5String(String info) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(info.getBytes("UTF-8"));
            byte[] encryption = md5.digest();

            StringBuffer strBuf = new StringBuffer();
            for (int i = 0; i < encryption.length; i++) {
                if (Integer.toHexString(0xff & encryption[i]).length() == 1) {
                    strBuf.append("0").append(Integer.toHexString(0xff & encryption[i]));
                } else {
                    strBuf.append(Integer.toHexString(0xff & encryption[i]));
                }
            }
            return strBuf.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    public static String StringFormatToUTF8(String entity) {
        try {
            return new String(entity.getBytes("utf-8"), "iso8859-1");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static double getTotalPriceValue(double value) {
        return Double.parseDouble(new DecimalFormat("#.00").format(value));
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static MyAddress getDefaultAddress() {
        ArrayList<MyAddress> list = PreferenceUtils.getCachedAddress();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getDefaultAddress().equals("true")) {
                return list.get(i);
            }
        }
        return null;
    }

}
