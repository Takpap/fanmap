package com.takpap.youngmap;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.baidu.trace.Trace;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.api.fence.AddMonitoredPersonResponse;
import com.baidu.trace.api.fence.CreateFenceRequest;
import com.baidu.trace.api.fence.CreateFenceResponse;
import com.baidu.trace.api.fence.DeleteFenceResponse;
import com.baidu.trace.api.fence.DeleteMonitoredPersonResponse;
import com.baidu.trace.api.fence.FenceListResponse;
import com.baidu.trace.api.fence.HistoryAlarmResponse;
import com.baidu.trace.api.fence.ListMonitoredPersonResponse;
import com.baidu.trace.api.fence.MonitoredStatusByLocationResponse;
import com.baidu.trace.api.fence.MonitoredStatusResponse;
import com.baidu.trace.api.fence.OnFenceListener;
import com.baidu.trace.api.fence.UpdateFenceResponse;
import com.baidu.trace.model.CoordType;
import com.baidu.trace.model.OnTraceListener;
import com.baidu.trace.model.PushMessage;

public class MyTrace {

    private final long serviceId;       // 设备标识
    private final String entityName;       // 设备名字
    private final boolean isNeedObjectStorage;// 是否需要对象存储服务，默认为：false，关闭对象存储服务。注：鹰眼 Android SDK v3.0以上版本支持随轨迹上传图像等对象数据，若需使用此功能，该参数需设为 true，且需导入bos-android-sdk-1.0.2.jar。
    private final Trace mTrace;       // 初始化轨迹服务
    private final LBSTraceClient mTraceClient;        // 初始化轨迹服务客户端
    private final int gatherInterval;        // 定位周期(单位:秒)
    private final int packInterval;        // 打包回传周期(单位:秒)
    private final OnTraceListener mTraceListener;

    MyTrace(Context context) {
        serviceId = 220212;
        entityName = Build.MODEL;
        isNeedObjectStorage = false;
        mTrace = new Trace(serviceId, entityName, isNeedObjectStorage);
        mTraceClient = new LBSTraceClient(context);
        gatherInterval = 10;
        packInterval = 15;
        // 设置定位和打包周期
        mTraceClient.setInterval(gatherInterval, packInterval);
        mTraceListener = new OnTraceListener() {
            @Override
            public void onBindServiceCallback(int i, String s) {
                Log.d("testtrace", "onBindServiceCallback");

            }

            @Override
            public void onStartTraceCallback(int i, String s) {
                if (i == 0) {
                    mTraceClient.startGather(mTraceListener);
                }
                Log.d("testtrace", "onStartTraceCallback"+i+s);
            }

            @Override
            public void onStopTraceCallback(int i, String s) {
                mTraceClient.stopGather(mTraceListener);
                Log.d("testtrace", "onStopTraceCallback");

            }

            @Override
            public void onStartGatherCallback(int i, String s) {
                Log.d("testtrace", "onStartGatherCallback");

            }

            @Override
            public void onStopGatherCallback(int i, String s) {
                Log.d("testtrace", "onStopGatherCallback");

            }

            @Override
            public void onPushCallback(byte b, PushMessage pushMessage) {
                Log.d("testtrace", "onPushCallback");

            }

            @Override
            public void onInitBOSCallback(int i, String s) {
                Log.d("testtrace", "onInitBOSCallback");

            }
        };
    }

    public void initFences() {
        // 请求标识
        int tag = 3;
        // 围栏名称
        String fenceName = "local_circle";
        // 监控对象
        String monitoredPerson = Build.MODEL;
        // 围栏圆心
        com.baidu.trace.model.LatLng center = new com.baidu.trace.model.LatLng(26.889724, 107.456539);
        // 围栏半径（单位 : 米）
        double radius = 50;
        // 去噪精度
        int denoise = 200;
        // 坐标类型
        CoordType coordType = CoordType.bd09ll;
        // 创建本地圆形围栏请求实例
        CreateFenceRequest localCircleFenceRequest = CreateFenceRequest.buildLocalCircleRequest(tag, serviceId, fenceName, monitoredPerson, center, radius, denoise, coordType);


// 初始化围栏监听器
        OnFenceListener mFenceListener = new OnFenceListener() {
            @Override
            public void onCreateFenceCallback(CreateFenceResponse createFenceResponse) {
                Log.d("testtracefences", "onCreateFenceCallback");

            }

            @Override
            public void onUpdateFenceCallback(UpdateFenceResponse updateFenceResponse) {
                Log.d("testtracefences", "onUpdateFenceCallback");

            }

            @Override
            public void onDeleteFenceCallback(DeleteFenceResponse deleteFenceResponse) {
                Log.d("testtracefences", "onDeleteFenceCallback");

            }

            @Override
            public void onFenceListCallback(FenceListResponse fenceListResponse) {
                Log.d("testtracefences", "onFenceListCallback");

            }

            @Override
            public void onMonitoredStatusCallback(MonitoredStatusResponse monitoredStatusResponse) {
                Log.d("testtracefences", "onMonitoredStatusCallback");

            }

            @Override
            public void onMonitoredStatusByLocationCallback(MonitoredStatusByLocationResponse monitoredStatusByLocationResponse) {
                Log.d("testtracefences", "onMonitoredStatusByLocationCallback");

            }

            @Override
            public void onHistoryAlarmCallback(HistoryAlarmResponse historyAlarmResponse) {
                Log.d("testtracefences", "onHistoryAlarmCallback");

            }

            @Override
            public void onAddMonitoredPersonCallback(AddMonitoredPersonResponse addMonitoredPersonResponse) {
                Log.d("testtracefences", "onAddMonitoredPersonCallback");

            }

            @Override
            public void onDeleteMonitoredPersonCallback(DeleteMonitoredPersonResponse deleteMonitoredPersonResponse) {
                Log.d("testtracefences", "onDeleteMonitoredPersonCallback");

            }

            @Override
            public void onListMonitoredPersonCallback(ListMonitoredPersonResponse listMonitoredPersonResponse) {
                Log.d("testtracefences", "onListMonitoredPersonCallback");

            }
        };
        mTraceClient.createFence(localCircleFenceRequest, mFenceListener);
    }

    public void start() {
        mTraceClient.startTrace(mTrace, mTraceListener);
    }

    public void stop() {
        mTraceClient.stopTrace(mTrace, mTraceListener);
    }


}
