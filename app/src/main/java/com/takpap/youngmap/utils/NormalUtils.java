package com.takpap.youngmap.utils;

import android.content.Context;

public class NormalUtils {

    public static String getTTSAppID() {
        return "19004809";
    }

    public static String getTTSAppKey() {
        return "99dMwmhk4CXvkKuuihjskrOXFnCFmBmI";
    }

    public static String getTTSsecretKey() {
        return "bc7ju1ZCDxMnPIhwsLqIoK6SYzGP5LG6";
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
