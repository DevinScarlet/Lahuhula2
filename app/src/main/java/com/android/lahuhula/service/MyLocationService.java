package com.android.lahuhula.service;

import android.app.Application;
import android.content.Context;
import com.baidu.mapapi.SDKInitializer;

/**
 * Created by lenovo on 2017/2/22.
 */

public class MyLocationService {
    private static MyLocationService mMyLocationService;
    private LocationService mLocationService;

    public MyLocationService(Context context) {
        mLocationService = new LocationService(context);
        SDKInitializer.initialize(context);
    }

    public static MyLocationService getInstance(Context context) {
        if (mMyLocationService == null) {
            mMyLocationService = new MyLocationService(context);
        }
        return mMyLocationService;
    }

    public LocationService getLocationService() {
        return mLocationService;
    }
}
