package com.takpap.youngmap.utils;

import android.util.Log;

public class UtilLog {
    public static boolean sIsShow = true;
    public static String sLogName = "Log";

    /**
     *
     * @param TAG       類名
     * @param content   log的内容
     */
    public static void d(String TAG, String content) {
        if (sIsShow) {
            Log.d(sLogName + TAG, content);
        }
    }

    public static void v(String TAG, String content) {
        if (sIsShow) {
            Log.v(sLogName + TAG, content);
        }
    }

    public static void w(String TAG, String content) {
        if (sIsShow) {
            Log.w(sLogName + TAG, content);
        }
    }

    public static void e(String TAG, String content) {
        if (sIsShow) {
            Log.e(sLogName + TAG, content);
        }
    }

    public static void i(String TAG, String content) {
        if (sIsShow) {
            Log.i(sLogName + TAG, content);
        }
    }

}
