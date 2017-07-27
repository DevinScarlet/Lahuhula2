package com.android.lahuhula.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import java.io.ByteArrayOutputStream;

/**
 * Created by lenovo on 2017/3/21.
 */

public class FriendsCircleInfo {
    public static final String AUTHORITY = "com.android.lahuhula";
    public static final String INFO = "info";

    public static final String[] COLUMNS = new String[] {
            CircleInfo._ID,
            CircleInfo.CUSTOM_ID,
            CircleInfo.BODY_DESCRIPTION,
            CircleInfo.PICTURES,
            CircleInfo.FAVORITE,
            CircleInfo.COMMENTS,
            CircleInfo.TIMESTAMP,
            CircleInfo.TOTAL_NUM,
            CircleInfo.UNIT_PRICE,
    };

    public static final class CircleInfo implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + INFO);

        public static final String CUSTOM_ID = "custom_id";
        public static final String BODY_DESCRIPTION = "body_description";
        public static final String PICTURES = "pictures";
        public static final String FAVORITE = "favorite";
        public static final String COMMENTS = "comments";
        public static final String TIMESTAMP = "timestamp";
        public static final String TOTAL_NUM = "total_num";
        public static final String UNIT_PRICE = "unit_price";
    }

    public static void insertInfoToDb(
            Context context, int customId, String description, int fav, String comments) {
        Log.d("TestProvider", "insertInfoToDb...start");
        ContentValues values = new ContentValues(4);
        values.put(CircleInfo.CUSTOM_ID, customId);
        values.put(CircleInfo.BODY_DESCRIPTION, description);
        values.put(CircleInfo.FAVORITE, fav);
        values.put(CircleInfo.COMMENTS, comments);
        context.getContentResolver().insert(CircleInfo.CONTENT_URI, values);
        Log.d("TestProvider", "insertInfoToDb...end");
    }

    public static void deleteStationInDb(Context context, int customId) {
        context.getContentResolver().delete(
                CircleInfo.CONTENT_URI,
                CircleInfo.CUSTOM_ID + "=?",
                new String[] { String.valueOf(customId)});
    }

    public static void updateStationToDb(Context context, int customId, String description) {
        final int size = 1;
        ContentValues values = new ContentValues(size);
        values.put(CircleInfo.BODY_DESCRIPTION, description);
        context.getContentResolver().update(
                CircleInfo.CONTENT_URI,
                values,
                CircleInfo.CUSTOM_ID + "=?",
                new String[] { String.valueOf(customId)});
    }

    public static void queryDb(Context context) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    CircleInfo.CONTENT_URI,
                    null,
                    null,
                    null,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                Log.d("TestProvider", "cursor.getCount():" + cursor.getCount());
                do {
                    int customId = cursor.getInt(cursor.getColumnIndex(CircleInfo.CUSTOM_ID));
                    String description = cursor.getString(cursor.getColumnIndex(CircleInfo.BODY_DESCRIPTION));
                    int fav = cursor.getInt(cursor.getColumnIndex(CircleInfo.FAVORITE));
                    String comments = cursor.getString(cursor.getColumnIndex(CircleInfo.COMMENTS));
                    Log.d("TestProvider", "customId:" + customId);
                    Log.d("TestProvider", "description:" + description);
                    Log.d("TestProvider", "fav:" + fav);
                    Log.d("TestProvider", "comments:" + comments);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static byte[] getPicture(Drawable drawable) {
        if(drawable == null) {
            return null;
        }
        BitmapDrawable bd = (BitmapDrawable) drawable;
        Bitmap bitmap = bd.getBitmap();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
        return os.toByteArray();
    }
}
