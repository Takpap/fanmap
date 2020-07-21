package com.takpap.youngmap;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDNotifyListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MyLocationConfiguration;


public class LocationListenerInstance {
    private LocationClient mLocationClient = null;
    private LocationClientOption option = new LocationClientOption();
    private BitmapDescriptor currentMarker;
    private MyLocationConfiguration myLocationConfiguration;
    private BDNotifyListener bdNotifyListener;
    private Context context;
    private NotificationManager mNM;
    LocationListenerInstance(Context context, MylocationListener mylocationListener) {
        this.context = context;
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
//        option.setScanSpan(5000);
        option.setIsNeedAddress(true);
        option.setIsNeedLocationDescribe(true);
        option.setOpenAutoNotifyMode();
        mLocationClient = new LocationClient(context.getApplicationContext());
        mLocationClient.registerLocationListener(mylocationListener);
        mLocationClient.setLocOption(option);
        currentMarker = BitmapDescriptorFactory.fromResource(R.drawable.carer);
        myLocationConfiguration = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.COMPASS, true, currentMarker, 0x55AACFCF, 0xaa679B9B);
        //位置提醒只有三次 需要多次提醒就要自己改变 SetNotifyLocation 的参数
//        bdNotifyListener = new MyNotifyListener();
//        bdNotifyListener.SetNotifyLocation(26.889611,107.456568,20,"bd09ll");
//        mLocationClient.registerNotify(bdNotifyListener);
        //移除位置提醒
//        mLocationClient.removeNotifyEvent(bdNotifyListener);
    }

    public void start() {
        mLocationClient.start();
    }

    public void stop() {
        mLocationClient.stop();
    }

    public static class MylocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {

        }
    }

    public class MyNotifyListener extends BDNotifyListener {
        public void onNotify(BDLocation mlocation, float distance){
            //已到达设置监听位置附近
            Log.d("receivelocation:  ", "位置提醒范围");
        }
    }

    public MyLocationConfiguration getMyLocationConfiguration() {
        return myLocationConfiguration;
    }
    public void startForeService(){
        //开启前台定位服务：
        Notification.Builder builder = new Notification.Builder (context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String notificationId = "callingChannelId";
            String notificationName = "callingChannelName";
            NotificationChannel channel = new NotificationChannel(notificationId, notificationName, NotificationManager.IMPORTANCE_HIGH);
            mNM = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
            assert mNM != null;
            mNM.createNotificationChannel(channel);
            builder = new Notification.Builder(context, notificationId);
        } else {
            builder = new Notification.Builder(context);
        }
        //获取一个Notification构造器
        Intent nfIntent = new Intent(context, MainActivity.class);
        builder.setContentIntent(PendingIntent.getActivity(context, 0, nfIntent, 0)) // 设置PendingIntent
                .setContentTitle("正在进行后台定位") // 设置下拉列表里的标题
                .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
                .setContentText("后台定位通知") // 设置上下文内容
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间
        Notification notification = null;
        notification = builder.build();
        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
        mLocationClient.enableLocInForeground(1001, notification);// 调起前台定位
    }
    private void stopForeService(){
        //停止前台定位服务：
        mLocationClient.disableLocInForeground(true);// 关闭前台定位，同时移除通知栏
    }
}
