package com.android.lahuhula.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by lenovo on 2017/3/21.
 */

public class HuhuProvider extends ContentProvider {
    private static final String TAG = HuhuProvider.class.getSimpleName();

    private SQLiteDatabase mSqlDb = null;
    private DatabaseHelper mDbHelper = null;
    public static final String DATABASE_NAME = "lahuhula.db";
    public static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "FriendsCircleList";

    private static final int INFO_REQ = 1;
    private static final int INFO_REQ_ID = 2;
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URI_MATCHER.addURI(FriendsCircleInfo.AUTHORITY, FriendsCircleInfo.INFO, INFO_REQ);
        URI_MATCHER.addURI(FriendsCircleInfo.AUTHORITY, FriendsCircleInfo.INFO + "/#",
                INFO_REQ_ID);
    }

    class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.i(TAG, "DatabaseHelper.onCreate");
            db.execSQL(
                    "CREATE TABLE " + TABLE_NAME + "("
                            + FriendsCircleInfo.CircleInfo._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + FriendsCircleInfo.CircleInfo.CUSTOM_ID + " INTEGER UNIQUE,"
                            + FriendsCircleInfo.CircleInfo.BODY_DESCRIPTION + " TEXT,"
                            + FriendsCircleInfo.CircleInfo.FAVORITE + " INTEGER DEFAULT 0,"
                            + FriendsCircleInfo.CircleInfo.PICTURES + " TEXT,"
                            + FriendsCircleInfo.CircleInfo.COMMENTS + " TEXT,"
                            + FriendsCircleInfo.CircleInfo.TIMESTAMP + "TEXT,"
                            + FriendsCircleInfo.CircleInfo.TOTAL_NUM + "INTEGER DEFAULT 0,"
                            + FriendsCircleInfo.CircleInfo.UNIT_PRICE + "TEXT"
                            + ");"
            );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(TAG, "DatabaseHelper.onUpgrade, upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

    @Override
    public boolean onCreate() {
        Log.i(TAG, "onCreate");
        mDbHelper = new DatabaseHelper(getContext());
        Log.i(TAG, "onCreate.mDbHelper:" + mDbHelper);
        return (null == mDbHelper) ? false : true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.i(TAG, "query");
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        qb.setTables(TABLE_NAME);

        int match = URI_MATCHER.match(uri);

        /*if (INFO_REQ_ID == match) {
            qb.appendWhere("_id = " + uri.getPathSegments().get(1));
        }*/

        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        if (null != c) {
            c.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return c;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.i(TAG, "insert");
        Uri rowUri = null;
        mSqlDb = mDbHelper.getWritableDatabase();
        ContentValues v = new ContentValues(values);

        long rowId = mSqlDb.insert(TABLE_NAME, null, v);
        if (rowId <= 0) {
            Log.e(TAG, "insert, failed to insert row into " + uri);
        }
        rowUri = ContentUris.appendId(FriendsCircleInfo.CircleInfo.CONTENT_URI.buildUpon(), rowId)
                .build();
        getContext().getContentResolver().notifyChange(rowUri, null);
        return rowUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.i(TAG, "delete");
        int rows = 0;
        mSqlDb = mDbHelper.getWritableDatabase();
        rows = mSqlDb.delete(TABLE_NAME, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return rows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.i(TAG, "update");
        int rows = 0;
        mSqlDb = mDbHelper.getWritableDatabase();
        rows = mSqlDb.update(TABLE_NAME, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return rows;
    }
}
