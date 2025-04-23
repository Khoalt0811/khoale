package com.khoalt0811.javavideoapp.utils; // Thay package name nếu khác

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class TokenManager {

    private static final String TAG = "TokenManager";
    private static final String PREF_NAME = "AuthPrefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id"; // Key để lưu User ID

    private static TokenManager instance;
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    // Constructor riêng tư để đảm bảo là Singleton
    private TokenManager(Context context) {
        // Sử dụng Application Context để tránh memory leak
        sharedPreferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Phương thức để lấy instance Singleton
    public static synchronized TokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new TokenManager(context);
        }
        return instance;
    }

    // Lưu Access Token và Refresh Token
    public void saveTokens(String accessToken, String refreshToken) {
        Log.d(TAG, "Saving tokens...");
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.apply(); // apply() chạy bất đồng bộ, commit() chạy đồng bộ
    }

    // Lưu User ID
    public void saveUserId(String userId) {
        Log.d(TAG, "Saving User ID: " + userId);
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }

    // Lấy Access Token
    public String getAccessToken() {
        String token = sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
        // Log.d(TAG, "Retrieved Access Token: " + (token != null ? "Exists" : "Null")); // Gỡ log này khi release
        return token;
    }

    // Lấy Refresh Token
    public String getRefreshToken() {
        String token = sharedPreferences.getString(KEY_REFRESH_TOKEN, null);
        // Log.d(TAG, "Retrieved Refresh Token: " + (token != null ? "Exists" : "Null"));
        return token;
    }

    // Lấy User ID
    public String getUserId() {
        String userId = sharedPreferences.getString(KEY_USER_ID, null);
        Log.d(TAG, "Retrieved User ID: " + userId);
        return userId;
    }


    // Xóa tất cả token và User ID (khi logout)
    public void clearTokens() {
        Log.d(TAG, "Clearing all tokens and User ID.");
        editor.remove(KEY_ACCESS_TOKEN);
        editor.remove(KEY_REFRESH_TOKEN);
        editor.remove(KEY_USER_ID);
        editor.apply();
    }
}