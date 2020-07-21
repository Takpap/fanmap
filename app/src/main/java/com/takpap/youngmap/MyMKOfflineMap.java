package com.takpap.youngmap;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;

public class MyMKOfflineMap extends AppCompatActivity implements View.OnClickListener {

    private Button mbt1;
    private Button mbt2;
    private Button mbt3;
    private Button mbt4;
    private EditText met;
    private ProgressBar progressBar;
    //    private final int cityId = 343;
    private final int cityId = 306;
    private MKOfflineMap mOffline;
    private Handler handler;
    private MKOfflineMapListener mkOfflineMapListener;
    //    private MKOfflineMapListener mkOfflineMapListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_m_k_offline_map);
        mbt1 = findViewById(R.id.button);
        mbt2 = findViewById(R.id.button4);
        mbt3 = findViewById(R.id.button2);
        mbt4 = findViewById(R.id.button3);
        met = findViewById(R.id.editText2);
        progressBar = findViewById(R.id.progressBar);
        mbt1.setOnClickListener(this);
        mbt2.setOnClickListener(this);
        mbt3.setOnClickListener(this);
        mbt4.setOnClickListener(this);
        InitOfflineMap();
//        mkOfflineMapListener = new MKOfflineMapListener() {
//            @RequiresApi(api = Build.VERSION_CODES.N)
//            @Override
//            public void onGetOfflineMapState(int type, int state) {
//                switch (type) {
//                    case MKOfflineMap.TYPE_DOWNLOAD_UPDATE:
//                        MKOLUpdateElement update = mOffline.getUpdateInfo(state);
//                        // 处理下载进度更新提示
//                        if (update != null) {
//                            met.setText(update.cityName);
//                            progressBar.setProgress(update.ratio, true);
//                            Log.d("mkofflinemapdownloadcit", String.valueOf(update.ratio));
//                            if (progressBar.getProgress() == 100) {
//                                Toast.makeText(MyMKOfflineMap.this, "download successful!", Toast.LENGTH_LONG).show();
//                            }
//                        }
//                        break;
//                    case MKOfflineMap.TYPE_NEW_OFFLINE:
//                        // 有新离线地图安装
//                        Log.d("mkofflinemapdownloadcit", String.format("add offlinemap num:%d", state));
//                        break;
//                    case MKOfflineMap.TYPE_VER_UPDATE:
//                        // 版本更新提示
//                        // MKOLUpdateElement e = mOffline.getUpdateInfo(state);
//                        break;
//                    default:
//                        break;
//                }
//            }
//        };


    }

    private void InitOfflineMap() {
        mOffline = new MKOfflineMap();
        mOffline.update(cityId);
        mkOfflineMapListener = new MKOfflineMapListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onGetOfflineMapState(int type, int state) {
                switch (type) {
                    case MKOfflineMap.TYPE_DOWNLOAD_UPDATE:
                        MKOLUpdateElement update = mOffline.getUpdateInfo(state);
                        // 处理下载进度更新提示
                        if (update != null) {
                            met.setText(update.size+update.cityName);
                            progressBar.setProgress(update.ratio, true);
                            Log.d("mkofflinemapdownloadcit", String.valueOf(update.ratio)+"\t"+update.status+"\t"+update.update);
                            if (progressBar.getProgress() == 100) {
                                Toast.makeText(MyMKOfflineMap.this, "download successful!", Toast.LENGTH_LONG).show();
                            }
                        }
                        break;
                    case MKOfflineMap.TYPE_NEW_OFFLINE:
                        // 有新离线地图安装
                        Log.d("mkofflinemapdownloadcit", String.format("have new offlineMap", state));
                        break;
                    case MKOfflineMap.TYPE_VER_UPDATE:
                        // 版本更新提示
                        MKOLUpdateElement e = mOffline.getUpdateInfo(state);
                        if(e.status != 4){
                            Toast.makeText(MyMKOfflineMap.this, "离线地图没有下载！", Toast.LENGTH_LONG).show();
                        }
                        Log.d("mkofflinemapdownloadcit", "MKOfflineMap.TYPE_NEW_OFFLINE" + "\t" + e.update+e.status);
                        break;
                    default:
                        break;
                }
            }
        };
        mOffline.init(mkOfflineMapListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                mOffline.start(cityId);
                Log.d("mkofflinemapdownloadcit", "开始下载" + cityId);
                break;
            case R.id.button2:
                mOffline.pause(cityId);
//                Log.d("mkofflinemapdownloadcit", "暂停下载" + cityId);
//                testThread();
                break;
            case R.id.button3:
                mOffline.remove(cityId);
                Log.d("mkofflinemapdownloadcit", "删除下载" + cityId);
                break;
            case R.id.button4:
                mOffline.update(cityId);
                Log.d("mkofflinemapdownloadcit", "更新下载" + cityId);
                break;
            default:
                break;
        }
    }


    @SuppressLint("HandlerLeak")
    private void testThread() {
        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == 1) {
                    Toast.makeText(MyMKOfflineMap.this, "download successful!", Toast.LENGTH_LONG).show();
                }
                super.handleMessage(msg);
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                message.obj = "nihao";
                handler.sendMessage(message);
                Log.d("mkofflinemapdownloadcit", "runable");

            }
        }).start();
    }
}
