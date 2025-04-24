package com.khoalt0811.javavideoapp.data.models; // Thay package name nếu khác
import com.google.gson.annotations.SerializedName;
public class AuthResponse {
    @SerializedName("access_token") private String accessToken;
    @SerializedName("token_type") private String tokenType; // Thường là "bearer"
    @SerializedName("expires_in") private Long expiresIn; // Thời gian hết hạn (giây)
    @SerializedName("refresh_token") private String refreshToken;
    @SerializedName("user") private User user;

    // Getters
    public String getAccessToken() { return accessToken; }
    public String getTokenType() { return tokenType; }
    public Long getExpiresIn() { return expiresIn; }
    public String getRefreshToken() { return refreshToken; }
    public User getUser() { return user; }
    // Setters
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    // User class để chứa thông tin người dùng
    public static class User {
        private String id;
        private String email;

        // Getters
        public String getId() { return id; }
        public String getEmail() { return email; }

        // Setters
        public void setId(String id) { this.id = id; }
        public void setEmail(String email) { this.email = email; }
    }
    public void setUser(User user) { this.user = user; }

}