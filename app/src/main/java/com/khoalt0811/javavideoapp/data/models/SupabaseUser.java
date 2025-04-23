package com.khoalt0811.javavideoapp.data.models; // Thay package name nếu khác
import com.google.gson.annotations.SerializedName;
import java.util.Map;
public class SupabaseUser {
    @SerializedName("id") private String id; // UUID
    @SerializedName("aud") private String aud; // Audience
    @SerializedName("role") private String role;
    @SerializedName("email") private String email;
    @SerializedName("created_at") private String createdAt;
    @SerializedName("last_sign_in_at") private String lastSignInAt;
    @SerializedName("user_metadata") private Map<String, Object> userMetadata; // Chứa avatar_url, full_name...

    // Getters
    public String getId() { return id; }
    public String getAud() { return aud; }
    public String getRole() { return role; }
    public String getEmail() { return email; }
    public String getCreatedAt() { return createdAt; }
    public String getLastSignInAt() { return lastSignInAt; }
    public Map<String, Object> getUserMetadata() { return userMetadata; }

    // Hàm tiện ích để lấy avatar_url từ metadata
    public String getAvatarUrl() {
        if (userMetadata != null && userMetadata.containsKey("avatar_url")) {
            // Cẩn thận kiểu dữ liệu trả về từ JSON, có thể cần ép kiểu an toàn
            Object url = userMetadata.get("avatar_url");
            return (url instanceof String) ? (String) url : null;
        }
        return null;
    }
    // Hàm tiện ích để lấy full_name từ metadata
    public String getFullName() {
        if (userMetadata != null && userMetadata.containsKey("full_name")) {
            Object name = userMetadata.get("full_name");
            return (name instanceof String) ? (String) name : null;
        }
        return null;
    }
}