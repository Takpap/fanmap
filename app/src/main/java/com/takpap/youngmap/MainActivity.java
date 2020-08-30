package com.takpap.youngmap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.takpap.youngmap.Fragment.DialogFragmentDataCallback;
import com.takpap.youngmap.Fragment.PassengerDialogFragment;
import com.takpap.youngmap.utils.UtilLog;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.entity.UMessage;
import com.umeng.message.inapp.IUmengInAppMsgCloseCallback;
import com.umeng.message.inapp.InAppMessageManager;
//导航类

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.internal.Util;


public class MainActivity extends AppCompatActivity implements DialogFragmentDataCallback {
    private SeekBar seekBar;
    private static final String TAG = "YMmainactivity";
    //引入地图
    private MapView mMapView = null;
    private BaiduMap baiduMap;
    private LocationListenerInstance locationListenerInstance;
    private MyLocationData myLocationData;
    private boolean isFirstLoc = true;
    private Double longitude;
    private Double langitude;
    private int accuracy;
    private float direction;
    private Double preLongitude;
    private MyTrace myTrace;
    private String permissionInfo;
    private final int SDK_PERMISSION_REQUEST = 127;
    private MyOrientationListener myOrientationListener; //方向传感器
    private int i = 1;
    private float mCurrentx;
    private float lastX = (float) 0.0;
    private MyApplication myApplication;
    //前台服务
    private TestConnection testConnection = new TestConnection();
    private ForegroundService.mBinder binder;
    private Vibrator vb;
    private InAppMessageManager inAppMessageManager;
    private Bundle passengerBundle;
    private PassengerDialogFragment passengerDialogFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PushAgent.getInstance(this).onAppStart();
        myApplication = (MyApplication) getApplication();
        //显示地图
        mMapView = findViewById(R.id.bmapView);
        seekBar = findViewById(R.id.online_seekbar);
        baiduMap = mMapView.getMap();
        initLocation();
        LatLng ll = new LatLng(26.893189, 107.461637);
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(ll, 16);
        baiduMap.setMapStatus(u);
        baiduMap.setMyLocationEnabled(true);
        baiduMap.setMyLocationConfiguration(locationListenerInstance.getMyLocationConfiguration());
        getPersimmions();
        initOrientation();
        PushAgent.getInstance(this).setPushCheck(true);
        passengerDialogFragment = new PassengerDialogFragment();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (seekBar.getProgress() == seekBar.getMax()) {
                    vb = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
                    Toast.makeText(MainActivity.this, "上线成功, 祝您发车愉快 !", Toast.LENGTH_LONG).show();
                    vb.vibrate(1000);
                    seekBar.setVisibility(View.GONE);
                    locationListenerInstance.start();
                    myOrientationListener.start();
                }
                Log.d("receivelocation", String.valueOf(seekBar.getProgress()));
            }
        });
        //展示插屏消息
        if (savedInstanceState == null) {
            showCardMessage();
        }


    }

    @SuppressLint("LongLogTag")
    private void showCardMessage() {
        InAppMessageManager.getInstance(MainActivity.this).showCardMessage(MainActivity.this, "main", new IUmengInAppMsgCloseCallback() {
            @Override
            public void onClose() {
                Log.i(TAG, "card message close");
            }
        });
    }


    private void getPolyline() {
        List<LatLng> latLngList = new ArrayList<LatLng>();
        latLngList.add(new LatLng(26.889553, 107.45704));
        latLngList.add(new LatLng(26.889565, 107.45704));
        OverlayOptions mOverlayOptions = new PolylineOptions()
                .width(10)
                .color(0xAAFF0000)
                .points(latLngList);
        Overlay mPolyline = baiduMap.addOverlay(mOverlayOptions);
        Log.d("receivelocation:  ", "轨迹添加");
    }

    private void initTrace() {
        //鹰眼轨迹相关
        myTrace = new MyTrace(MainActivity.this);
    }

    private void initLocation() {


        locationListenerInstance = new LocationListenerInstance(this, new LocationListenerInstance.MylocationListener() {
            private MyOkhttp myOkhttp;

            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                accuracy = (int) bdLocation.getRadius();
                longitude = bdLocation.getLongitude();
                langitude = bdLocation.getLatitude();
                myLocationData = new MyLocationData.Builder()
                        .accuracy(accuracy)
                        .direction(mCurrentx)
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .latitude(langitude)
                        .longitude(longitude).build();
                baiduMap.setMyLocationData(myLocationData);
                if (isFirstLoc) {
                    isFirstLoc = false;
                    preLongitude = longitude;
                    LatLng lll = new LatLng(langitude, longitude);
                    MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(lll, 19);
                    baiduMap.animateMapStatus(u);
                }
                myOkhttp = new MyOkhttp(accuracy, longitude, langitude, (double) mCurrentx, myApplication.getTel());
                myOkhttp.SendAndRespon();
//                final Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (accuracy <= 40) {
//                            if ((Double.doubleToLongBits(preLongitude) != Double.doubleToLongBits(longitude)) && Math.abs(preLongitude - longitude) >= 8e-6) {
//                                Log.d("receivelocation:  ", "上传第" + i++ + "次");
//                                preLongitude = longitude;
//                                myOkhttp = new MyOkhttp(accuracy, longitude, langitude, (double) mCurrentx, myApplication.getDeviceUUID());
//                                myOkhttp.SendAndRespon();
//                            }
//                        }
//                        handler.postDelayed(this, 5000);
//                    }
//                }, 5000);
                Log.d("receivelocation:  ", "经度：" + bdLocation.getLongitude() + "\t纬度：" + bdLocation.getLatitude() + "\t精度：" + bdLocation.getRadius() + "\t位置描述：" + bdLocation.getLocationDescribe() + bdLocation.getLocTypeDescription());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
        checkGpsEnabled();
        passengerBundle = getIntent().getExtras();
        if (passengerBundle != null && !passengerDialogFragment.isAdded()) {
            passengerDialogFragment.show(getSupportFragmentManager(), "这是啥玩意儿");
        }
    }




    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("logforegroundservice", "ActivityOnPause");
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
//        bindService(new Intent(this, ForegroundService.class), testConnection, BIND_AUTO_CREATE);
        mMapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("logforegroundservice", "ActivityOnStop");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, ForegroundService.class));
        } else {
            startService(new Intent(this, ForegroundService.class));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("logforegroundservice", "ActivityOnDestroy");
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        stopService(new Intent(this, ForegroundService.class));
        baiduMap.setMyLocationEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 100, 1, "开始定位");
        menu.add(Menu.NONE, 101, 1, "停止定位");
        menu.add(Menu.NONE, 102, 1, "离线地图");
        menu.add(Menu.NONE, 103, 1, "开启服务");
        menu.add(Menu.NONE, 104, 1, "关闭服务");
        menu.add(Menu.NONE, 105, 2, "创建地理围栏");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case 100:
                Log.d("receiveloction", "start");
                locationListenerInstance.start();
                myOrientationListener.start();
                break;
            case 101:
                Log.d("receiveloction", "stop");
                locationListenerInstance.stop();
                myOrientationListener.stop();
                isFirstLoc = true;
                break;
            case 102:
                startActivity(new Intent(MainActivity.this, MyMKOfflineMap.class));
                break;
            case 103:
//                bindService(new Intent(this, ForegroundService.class),testConnection,BIND_AUTO_CREATE);
//                startService(new Intent(this, ForegroundService.class));
                inAppMessageManager.getInstance(this).showCardMessage(MainActivity.this, "main",
                        new IUmengInAppMsgCloseCallback() {
                            @Override
                            public void onClose() {
                                Log.d(TAG, "card message close");

                            }
                        });
                Log.d(TAG, "card message close");

                break;
            case 104:
                stopService(new Intent(this, ForegroundService.class));
                break;
            case 105:
                Log.d("testtracefences", "创建地理围栏");
                myTrace.initFences();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @TargetApi(23)
    private void getPersimmions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<String>();
            /***
             * 定位权限为必须权限，用户如果禁止，则每次进入都会申请
             */
            // 定位精确位置
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.CALL_PHONE);
            }
            if (permissions.size() > 0) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), SDK_PERMISSION_REQUEST);
            }
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    public void letIgnoreBetterOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            Log.d("receivelocation", String.valueOf(powerManager.isIgnoringBatteryOptimizations(this.getPackageName())));
            assert powerManager != null;
            if (!powerManager.isIgnoringBatteryOptimizations(this.getPackageName())) {
                Log.d("receivelocation", "letIgnoreBetterOptimizationdddddddddddddddddddddd");
                @SuppressLint("BatteryLife") Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                startActivity(intent);
            }
        }

    }

    // Check whether the GPS is enabled
    public void checkGpsEnabled() {
        //得到系统的位置服务，判断GPS是否激活
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean ok = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (ok) {
//            Toast.makeText(this, "GPS IS ON", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "请先开启GPS定位服务", Toast.LENGTH_LONG).show();
            Intent intent = new Intent();
            intent.setAction(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    private void initOrientation() {
        //传感器
        myOrientationListener = new MyOrientationListener(this);
        myOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                if (langitude != null) {
                    if (Math.abs(x - lastX) > 1.0) {
                        mCurrentx = x;
//                        Log.d("receivelocation:  ", String.valueOf(mCurrentx));
                        baiduMap.setMyLocationData(new MyLocationData.Builder().accuracy(accuracy).direction(x).latitude(langitude).longitude(longitude).build());
//                        Log.d("receivelocation:  ", "位置提醒范围");

                    }
                    lastX = x;
                }

            }
        });
    }

    private final class TestConnection implements ServiceConnection {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            binder = null;
            Log.d("logforegroundservice", "onServiceConnectedFailed");
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("logforegroundservice", "onServiceConnected");
            binder = (ForegroundService.mBinder) service;
            binder.getCount();
        }
    }

    /**
     * 返回键退回到登陆页面
     **/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            exitDialog();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 退出App对话框
     */
    private void exitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("您真的想要退出么?");
        builder.setTitle("提醒");
        builder.setPositiveButton("真的退出", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.setNegativeButton("点错了", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public String getOrderId() {
        return passengerBundle.getString("orderId");
    }

    /**
     * 实现 DialogFragmentDataCallback 接口
     **/
    @Override
    public String getPassengerName() {
        return passengerBundle.getString("name");
    }

    @Override
    public String getPassengerCount() {
        return passengerBundle.getString("count");
    }

    @Override
    public String getPassengerTel() {
        return passengerBundle.getString("tel");
    }

    @Override
    public String getPassengerStart() {
        return passengerBundle.getString("start");
    }

    @Override
    public Double getPassengerStartLat() {
        return Double.valueOf(Objects.requireNonNull(passengerBundle.getString("startLat")));
    }

    @Override
    public Double getPassengerStartLon() {
        return Double.valueOf(Objects.requireNonNull(passengerBundle.getString("startLon")));
    }

    @Override
    public String getPassengerDest() {
        return passengerBundle.getString("dest");
    }

    @Override
    public Double getPassengerDestLat() {
        return Double.valueOf(Objects.requireNonNull(passengerBundle.getString("destLat")));
    }

    @Override
    public Double getPassengerDestLon() {
        return Double.valueOf(Objects.requireNonNull(passengerBundle.getString("destLon")));
    }

    @Override
    public Double getDriverLat() {
        return langitude;
    }

    @Override
    public Double getDriverLon() {
        return longitude;
    }
}
