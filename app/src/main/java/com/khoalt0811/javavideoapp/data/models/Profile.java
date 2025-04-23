package com.khoalt0811.javavideoapp.data.models; // Thay package name nếu khác
import com.google.gson.annotations.SerializedName;
public class Profile {
    @SerializedName("id") private String id; // UUID user (trùng với user_id trong bảng videos)
    @SerializedName("email") private String email; // Lấy từ bảng profiles
    @SerializedName("avatar_url") private String avatarUrl; // Lấy từ bảng profiles
    @SerializedName("full_name") private String fullName; // Lấy từ bảng profiles

    // Constructor mặc định (Gson cần)
    public Profile() {}

    // Getters
    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getFullName() { return fullName; }
}