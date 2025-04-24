package com.khoalt0811.javavideoapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id"; // Thêm key mới để lưu User ID

    private static TokenManager instance;
    private SharedPreferences preferences;

    private TokenManager(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized TokenManager getInstance(Context context) {
        if (instance == null && context != null) {
            instance = new TokenManager(context);
        }
        return instance;
    }

    public void saveToken(String accessToken) {
        preferences.edit().putString(KEY_ACCESS_TOKEN, accessToken).apply();
    }

    public void saveRefreshToken(String refreshToken) {
        preferences.edit().putString(KEY_REFRESH_TOKEN, refreshToken).apply();
    }

    // Thêm phương thức để lưu User ID
    public void saveUserId(String userId) {
        preferences.edit().putString(KEY_USER_ID, userId).apply();
    }

    public String getAccessToken() {
        return preferences.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return preferences.getString(KEY_REFRESH_TOKEN, null);
    }

    // Thêm phương thức để lấy User ID
    public String getUserId() {
        return preferences.getString(KEY_USER_ID, null);
    }

    public void clearTokens() {
        preferences.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .remove(KEY_USER_ID) // Đồng thời xoá cả User ID khi đăng xuất
                .apply();
    }

    public boolean hasAccessToken() {
        return getAccessToken() != null;
    }

    public String getAuthorizationHeader() {
        String token = getAccessToken();
        return token != null ? "Bearer " + token : null;
    }
}