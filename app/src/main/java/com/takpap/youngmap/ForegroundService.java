package com.takpap.youngmap;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;


public class ForegroundService extends Service {
    private final static String TAG = "logforegroundservice";
    private mBinder binder = new mBinder();
    private Notification.Builder builder;
    private NotificationManager mNM;
    PowerManager.WakeLock wakeLock = null;
    //Silent Music Keep Alive
    private MediaPlayer mMediaPlayer;
    String manufacturer = android.os.Build.MANUFACTURER;
    static class mBinder extends Binder {
        void getCount() {
            Log.d(TAG, "getcount");
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "ServiceOnCreate");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String notificationId = "callingChannelId";
            String notificationName = "callingChannelName";
            NotificationChannel channel = new NotificationChannel(notificationId, notificationName, NotificationManager.IMPORTANCE_HIGH);
            mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            assert mNM != null;
            mNM.createNotificationChannel(channel);
            builder = new Notification.Builder(this, notificationId);
        } else {
            builder = new Notification.Builder(this);
        }
        builder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0));
        builder.setAutoCancel(false);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setTicker("黔行 (司机端) 后台运行中...");
        builder.setContentTitle("黔行 (司机端)");
        builder.setContentText("正在后台运行...");
        Notification notification = builder.build();
//        notification.defaults = Notification.DEFAULT_SOUND;
        notification.sound = null;
        startForeground(1, notification);
        acquireWakeLock();
        //init MediaPlayer
//        mMediaPlayer = MediaPlayer.create(this, R.raw.no_kill);
//        mMediaPlayer.setLooping(true);
    }
    @Override
    public void onDestroy() {
        Log.d(TAG, "ServiceonDestroy");
        super.onDestroy();
        mMediaPlayer.pause();
        if(manufacturer.equals("OPPO")){
            stopPlaySong();
        }


        // 如果Service被杀死，干掉通知
//        NotificationManager mManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
//        mManager.cancel(1);
//        // 重启自己
//        Intent intent = new Intent(getApplicationContext(),ForegroundService.class);
//        startService(intent);
//        releaseWakeLock();
    }
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "ServiceOnBind");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "ServiceOnUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "ServiceOnStartCommand");
//        if(manufacturer.equals("OPPO")){
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    startPlaySong();
//                }
//            }).start();
//        }
        return START_STICKY;
    }

    //start player silent music
    //开始、暂停播放
    private void startPlaySong() {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(this, R.raw.no_kill);
            mMediaPlayer.start();
        } else {
            mMediaPlayer.start();
            Log.d(TAG,"startPlaySong");
        }
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        if (mMediaPlayer != null) {
//            mMediaPlayer.pause();
//            Log.d(TAG,"pausePlaySong");
//        }
    }
    //stop player silence music
    private void stopPlaySong() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            Log.d(TAG,"stopPlaySong");
        }
    }


    //获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
    @SuppressLint("InvalidWakeLockTag")
    private void acquireWakeLock()
    {
        if (null == wakeLock)
        {
            PowerManager pm = (PowerManager)this.getSystemService(POWER_SERVICE);
            assert pm != null;
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE, "PostLocationService");
            if (null != wakeLock)
            {
                Log.d(TAG,"acquireWakeLock");
                wakeLock.acquire();
            }
        }
    }

    //释放设备电源锁
    private void releaseWakeLock()
    {
        if (null != wakeLock)
        {
            Log.d(TAG,"releaseWakeLock");
            wakeLock.release();
            wakeLock = null;
        }
    }
}