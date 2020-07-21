package com.takpap.youngmap;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.umeng.message.inapp.IUmengInAppMsgCloseCallback;
import com.umeng.message.inapp.InAppMessageManager;
import com.umeng.message.inapp.UmengSplashMessageActivity;

public class SplashUmenActivity extends UmengSplashMessageActivity {
    @Override
    public boolean onCustomPretreatment() {
        InAppMessageManager mInAppMessageManager = InAppMessageManager.getInstance(SplashUmenActivity.this);
        //设置应用内消息为Debug模式
        mInAppMessageManager.setInAppMsgDebugMode(true);
        //参数为Activity的完整包路径，下面仅是示例代码，请按实际需求填写
        mInAppMessageManager.setMainActivityPath("com.takpap.youngmap.ActivityLogin");
        return super.onCustomPretreatment();
    }
}