package com.android.lahuhula.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.android.lahuhula.R;
import com.android.lahuhula.service.LocationService;
import com.android.lahuhula.service.Utils;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapBaseIndoorMapInfo;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;

/**
 * Created by lenovo on 2017/2/16.
 */

public class OneVillageActivity extends AppCompatActivity
        implements BaiduMap.OnMapClickListener, BaiduMap.OnMarkerClickListener {
    private static final String MAP_TAG = "map_tag";

    private LocationService mLocationService;
    private LocationClient mLocClient;
    private MapView mMapView;
    private BaiduMap mBaiduMap;

    private BitmapDescriptor mCurrentMarker;
    private boolean mIsFirstLoc = true;
    //StripListView stripListView;
    //BaseStripAdapter mFloorListAdapter;
    private MapBaseIndoorMapInfo mMapBaseIndoorMapInfo = null;

    private ArrayList<MarkerOptions> mHuhuHelpStationMarkerList = new ArrayList<>();
    private static final LatLng[] DEFAULT_LATLNGS = new LatLng[]{
            new LatLng(31.12231, 121.31214),
            new LatLng(31.22242, 121.51311),
            new LatLng(31.32134, 121.21151),
            new LatLng(31.22452, 121.41234),
            new LatLng(31.24534, 121.11452),
    };


    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(MAP_TAG, "latLng:" + latLng);
    }

    @Override
    public boolean onMapPoiClick(MapPoi mapPoi) {
        return false;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        mBaiduMap.hideInfoWindow();
        if (marker != null) {
            TextView popupText = new TextView(this);
            popupText.setBackgroundResource(R.drawable.friends_circle_comment);
            popupText.setTextColor(0xFF000000);
            popupText.setText(marker.getExtraInfo().getString("des"));
            mBaiduMap.showInfoWindow(new InfoWindow(popupText, marker.getPosition(), -70));
            return true;
        }
        return false;
    }

    /**
     * 构造广播监听类，监听 SDK key 验证以及网络异常广播
     */
    public class SDKReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String tips = null;
            Log.d("zhangtao", "action: " + action);
            if (action.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
                tips = "key 验证出错! 错误码 :" + intent.getIntExtra
                        (SDKInitializer.SDK_BROADTCAST_INTENT_EXTRA_INFO_KEY_ERROR_CODE, 0)
                        + " ; 请在 AndroidManifest.xml 文件中检查 key 设置";
            } else if (action.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK)) {
                tips = "key 验证成功! 功能可以正常使用";
            } else if (action.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
                tips = "网络出错";
            }
            if (!TextUtils.isEmpty(tips)) {
                showToast(tips);
            }
        }
    }

    private SDKReceiver mReceiver;

    private void showToast(String text) {
        //Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.map_layout);
        init();
    }

    private void init() {
        mMapView = (MapView) findViewById(R.id.mapview);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap.setTrafficEnabled(false);
        //mBaiduMap.setBuildingsEnabled(true);
        mBaiduMap.setIndoorEnable(true);
        initHuhuHelpStationMarker();
        if (!mHuhuHelpStationMarkerList.isEmpty()) {
            MapStatus.Builder builder = new MapStatus.Builder().target(mHuhuHelpStationMarkerList.get(0).getPosition()).zoom(12.0f);
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        }

        CheckBox trafficBtn = (CheckBox) findViewById(R.id.traffic_btn);
        trafficBtn.setOnCheckedChangeListener(mTrafficOnCheckedListener);

        mBaiduMap.setOnMapClickListener(this);

        registerSDKReceiver();
        initLocationInfo();
    }

    private void initLocationInfo() {
        /*mLocationService = MyLocationService.getInstance(this).getLocationService();
        mLocationService.registerListener(mLocationListener);
        //注册监听
        int type = getIntent().getIntExtra("from", 0);
        if (type == 0) {
            mLocationService.setLocationOption(mLocationService.getDefaultLocationClientOption());
        } else if (type == 1) {
            mLocationService.setLocationOption(mLocationService.getOption());
        }
        mLocationService.start();*/
        // 定位初始化
        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(LocationMode.NORMAL, true,
                mCurrentMarker));
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(mLocationListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType(Utils.CoorType_BD09LL); // 设置坐标类型
        option.setScanSpan(3000);
        mLocClient.setLocOption(option);
        mLocClient.start();

        //stripListView = new StripListView(this);
        //layout.addView(stripListView);
        //setContentView(layout);
        //mFloorListAdapter = new BaseStripAdapter(IndoorLocationActivity.this);

        mBaiduMap.setOnBaseIndoorMapListener(new BaiduMap.OnBaseIndoorMapListener() {
            @Override
            public void onBaseIndoorMapMode(boolean b, MapBaseIndoorMapInfo mapBaseIndoorMapInfo) {
                if (b == false || mapBaseIndoorMapInfo == null) {
                    //stripListView.setVisibility(View.INVISIBLE);

                    return;
                }

                // mFloorListAdapter.setmFloorList(mapBaseIndoorMapInfo.getFloors());
                //stripListView.setVisibility(View.VISIBLE);
                //stripListView.setStripAdapter(mFloorListAdapter);
                mMapBaseIndoorMapInfo = mapBaseIndoorMapInfo;
            }
        });
    }

    private ArrayList<MarkerOptions> initHuhuHelpStationMarker() {
        mHuhuHelpStationMarkerList.clear();
        for (LatLng latLng : DEFAULT_LATLNGS) {
            MarkerOptions mo = createOneMarkerOptions(latLng);
            mHuhuHelpStationMarkerList.add(mo);
            mBaiduMap.addOverlay(mo);
            mBaiduMap.setOnMarkerClickListener(this);
        }
        return mHuhuHelpStationMarkerList;
    }

    private MarkerOptions createOneMarkerOptions(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.huhu_help_mark));
        markerOptions.title(getString(R.string.huhu_help_station));

        Bundle des = new Bundle();
        des.putString("des", getString(R.string.huhu_help_station));
        markerOptions.extraInfo(des);

        return markerOptions;
    }

    private OnCheckedChangeListener mTrafficOnCheckedListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            mBaiduMap.setTrafficEnabled(b);
        }
    };

    private void registerSDKReceiver() {
        IntentFilter SDKFilter = new IntentFilter();
        SDKFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK);
        SDKFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        SDKFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        mReceiver = new SDKReceiver();
        registerReceiver(mReceiver, SDKFilter);
    }

    private void unRegisterSDKReceiver() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterSDKReceiver();
        mLocClient.unRegisterLocationListener(mLocationListener); //注销掉监听
        mLocClient.stop(); //停止定位服务
        mMapView.onDestroy();
        mMapView = null;
    }


    /*****
     * 定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
     */
    private BDLocationListener mLocationListener = new BDLocationListener() {
        private String lastFloor = null;

        @Override
        public void onReceiveLocation(BDLocation location) {
            Log.i("indoor", "location:" + location + ", mMapView:" + mMapView);
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                Log.i("indoor", "location or mMapView is null");
                return;
            }
            String bid = location.getBuildingID();
            if (bid != null && mMapBaseIndoorMapInfo != null) {
                Log.i("indoor", "bid = " + bid + " mid = " + mMapBaseIndoorMapInfo.getID());
                if (bid.equals(mMapBaseIndoorMapInfo.getID())) {// 校验是否满足室内定位模式开启条件
                    // Log.i("indoor","bid = mMapBaseIndoorMapInfo.getID()");
                    String floor = location.getFloor().toUpperCase();// 楼层
                    Log.i("indoor", "floor = " + floor /*+ " position = " + mFloorListAdapter.getPosition(floor)*/);
                    Log.i("indoor", "radius = " + location.getRadius()/* + " type = " + location.getNetworkLocationType()*/);

                    boolean needUpdateFloor = true;
                    if (lastFloor == null) {
                        lastFloor = floor;
                    } else {
                        if (lastFloor.equals(floor)) {
                            needUpdateFloor = false;
                        } else {
                            lastFloor = floor;
                        }
                    }
                    if (needUpdateFloor) {// 切换楼层
                        mBaiduMap.switchBaseIndoorMapFloor(floor, mMapBaseIndoorMapInfo.getID());
                        //mFloorListAdapter.setSelectedPostion(mFloorListAdapter.getPosition(floor));
                        //mFloorListAdapter.notifyDataSetInvalidated();
                    }

                    if (!location.isIndoorLocMode()) {
                        mLocClient.startIndoorMode();// 开启室内定位模式，只有支持室内定位功能的定位SDK版本才能调用该接口
                        Log.i("indoor", "start indoormod");
                    }
                }
            }

            MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude()).longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (mIsFirstLoc) {
                mIsFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }

        public void onConnectHotSpotMessage(String s, int i) {

        }
    };
}
