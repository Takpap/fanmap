package com.takpap.youngmap;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UTrack;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.entity.UMessage;
import com.umeng.message.inapp.InAppMessageManager;

public class MyApplication extends Application {
    private String deviceUUID;
    private String tel;

    public String getDeviceUUID() {
        return deviceUUID;
    }
    public String getTel(){
        SharedPreferences sharedPreferences = getSharedPreferences("userData", MODE_PRIVATE);
        String tel = sharedPreferences.getString("tel","");
        if(!tel.isEmpty()){
            return tel;
        }
        return "";
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(this);
        //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.setCoordType(CoordType.BD09LL);
        InAppMessageManager.getInstance(getApplicationContext()).setInAppMsgDebugMode(true);
        UMConfigure.init(this, "5e9c09b1978eea083f0c7a57", "UMENG_CHANNEL", UMConfigure.DEVICE_TYPE_PHONE, "5bed6ec96f431761362f81eaa2bc47e7");
        PushAgent mPushAgent = PushAgent.getInstance(getApplicationContext());
        mPushAgent.setPushCheck(true);
        mPushAgent.register(new IUmengRegisterCallback() {
            @Override
            public void onSuccess(String s) {
                Log.i("umenPush", "注册成功：deviceToken：-------->  " + s);
                deviceUUID = s;
            }
            @Override
            public void onFailure(String s, String s1) {
                Log.i("umenPush", "注册失败：deviceToken：-------->  " + "s:" + s + ",s1:" + s1);
            }
        });
        UmengMessageHandler messageHandler = new UmengMessageHandler() {

            @Override
            public void dealWithCustomMessage(final Context context, final UMessage msg) {
                new Handler(getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        // 对于自定义消息，PushSDK默认只统计送达。若开发者需要统计点击和忽略，则需手动调用统计方法。
                        boolean isClickOrDismissed = true;
                        if (isClickOrDismissed) {
                            //自定义消息的点击统计
                            UTrack.getInstance(getApplicationContext()).trackMsgClick(msg);
                        } else {
                            //自定义消息的忽略统计
                            UTrack.getInstance(getApplicationContext()).trackMsgDismissed(msg);
                        }
                        Toast.makeText(context, msg.custom, Toast.LENGTH_LONG).show();
                    }
                });
            }
        };
        mPushAgent.setMessageHandler(messageHandler);

    }

}
