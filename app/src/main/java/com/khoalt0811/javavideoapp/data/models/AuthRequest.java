package com.khoalt0811.javavideoapp.data.models; // Thay package name nếu khác
import com.google.gson.annotations.SerializedName;
public class AuthRequest {
    @SerializedName("email") private String email;
    @SerializedName("password") private String password;
    public AuthRequest(String email, String password) { this.email = email; this.password = password; }
    // Getters
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}