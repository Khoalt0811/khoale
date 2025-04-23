package com.khoalt0811.javavideoapp.data.models; // Thay package name nếu khác
import com.google.gson.annotations.SerializedName;
public class AuthResponse {
    @SerializedName("access_token") private String accessToken;
    @SerializedName("token_type") private String tokenType; // Thường là "bearer"
    @SerializedName("expires_in") private Long expiresIn; // Thời gian hết hạn (giây)
    @SerializedName("refresh_token") private String refreshToken;
    @SerializedName("user") private SupabaseUser user;

    // Getters
    public String getAccessToken() { return accessToken; }
    public String getTokenType() { return tokenType; }
    public Long getExpiresIn() { return expiresIn; }
    public String getRefreshToken() { return refreshToken; }
    public SupabaseUser getUser() { return user; }
}