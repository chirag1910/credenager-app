package com.credenager.utils;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Globals {
    public static final String AUTH_FILE_NAME = "USER_INFO";
    public static final String SETTINGS_FILE_NAME = "USER_SETTINGS";
    public static final String DATA_FILE_NAME = "USER_DATA";
    public static final String JWT_KEY = "JWT_TOKEN";
    public static final String KEY_KEY = "USER_ENCRYPTION_KEY";
    public static final String OFFLINE_KEY = "OFFLINE_MODE";
    public static final String DATA_KEY = "USER_OFFLINE_DATA";
    public static final String LOGIN_FRAGMENT_TAG = "LOGIN_FRAG";
    public static final String SIGNUP_FRAGMENT_TAG = "SIGNUP_FRAG";
    public static final String RESET_PASS_FRAGMENT_TAG = "RESET_PASS_FRAG";
    public static final String RESET_KEY_FRAGMENT_TAG = "RESET_KEY_FRAG";
    public static final String GROUP_FRAGMENT_TAG = "GROUP_TAG";
    public static final String SEARCH_FRAGMENT_TAG = "SEARCH_TAG";
    public static final String FROM_SEARCH_FRAGMENT_TAG = "FROM_SEARCH_FRAGMENT";
    public static final int GOOGLE_REQUEST_CODE = 69420;
    public static String JWT_TOKEN;
    public static String USER_EMAIL;
    public static String KEY;
    public static boolean APP_OFFLINE_MODE = false;

    public static boolean isValidEmail(String email){
        Pattern pattern = Pattern.compile("^(.+)@(.+)$");
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static float dpToPx(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void setUserState(String email, String token){
        JWT_TOKEN = token;
        USER_EMAIL = email;
    }

    public static void setToken(Context context, String token){
        context.getSharedPreferences(AUTH_FILE_NAME, Context.MODE_PRIVATE).edit().putString(JWT_KEY, token).apply();
    }

    public static String getToken(Context context){
        return context.getSharedPreferences(AUTH_FILE_NAME, Context.MODE_PRIVATE).getString(JWT_KEY, null);
    }

    public static void setKey(Context context, String key){
        context.getSharedPreferences(AUTH_FILE_NAME, Context.MODE_PRIVATE).edit().putString(KEY_KEY, key).apply();
    }

    public static String getKey(Context context){
        return context.getSharedPreferences(AUTH_FILE_NAME, Context.MODE_PRIVATE).getString(KEY_KEY, null);
    }

    public static void setOfflineSetting(Context context, boolean offline) {
        context.getSharedPreferences(SETTINGS_FILE_NAME, Context.MODE_PRIVATE).edit().putBoolean(OFFLINE_KEY, offline).apply();
    }

    public static boolean getOfflineSetting(Context context) {
        return context.getSharedPreferences(SETTINGS_FILE_NAME, Context.MODE_PRIVATE).getBoolean(OFFLINE_KEY, false);
    }

    public static void setData(Context context, String data){
        context.getSharedPreferences(DATA_FILE_NAME, Context.MODE_PRIVATE).edit().putString(DATA_KEY, data).apply();
    }

    public static String getData(Context context) {
        return context.getSharedPreferences(DATA_FILE_NAME, Context.MODE_PRIVATE).getString(DATA_KEY, null);
    }
}
