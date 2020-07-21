package com.takpap.youngmap.utils;

public class httpConfig {
    public static boolean isDubug = true;
    // log日志开关
    public static boolean isLog = true;

    //开发环境

    /**
     *
     * 开发环境地址
     */
    public static final String DEBUG_BASE_URL ="http://192.168.3.218/";

    /**
     * 正式环境地址
     */
//    public static final String DEBUG_BASE_URL = "https://upyoung.xyz/";


    public static String getServerUrl() {
        return DEBUG_BASE_URL;
    }
}
