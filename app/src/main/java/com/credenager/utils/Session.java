package com.credenager.utils;

public class Session {
    public static String JWT_TOKEN;
    public static String USER_EMAIL;
    public static String USER_KEY;

    public static boolean APP_OFFLINE_MODE = false;


    public static void setUserState(String email, String token){
        JWT_TOKEN = token;
        USER_EMAIL = email;
    }

    public static void setKey(String key){
        USER_KEY = key;
    }

    public static void setAppOfflineMode(boolean isRunningInOfflineMode){
        APP_OFFLINE_MODE = isRunningInOfflineMode;
    }
}
