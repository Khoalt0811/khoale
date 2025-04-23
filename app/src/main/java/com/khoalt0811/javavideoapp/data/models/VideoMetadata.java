package com.khoalt0811.javavideoapp.data.models; // Thay package name nếu khác
import com.google.gson.annotations.SerializedName;
public class VideoMetadata {
    // Dùng @SerializedName nếu tên biến khác tên cột JSON trả về từ API
    @SerializedName("id") private Long id; // ID của video trong DB
    @SerializedName("created_at") private String createdAt; // Thời gian tạo
    @SerializedName("user_id") private String userId; // UUID của người đăng
    @SerializedName("video_url") private String videoUrl; // URL trên Storage
    @SerializedName("title") private String title;
    @SerializedName("description") private String description;
    @SerializedName("likes") private int likes;
    @SerializedName("dislikes") private int dislikes;

    // Trường này sẽ có dữ liệu nếu bạn dùng select query để join bảng 'profiles'
    @SerializedName("profiles") private Profile uploaderProfile;

    // Constructor để tạo đối tượng gửi đi khi insert (không cần id, createdAt, uploaderProfile)
    public VideoMetadata(String userId, String videoUrl, String title, String description) {
        this.userId = userId;
        this.videoUrl = videoUrl;
        this.title = title;
        this.description = description;
        // likes, dislikes sẽ được DB đặt giá trị default hoặc là 0
    }

    // Constructor mặc định (Gson cần) hoặc constructor đầy đủ nếu cần
    public VideoMetadata() {}

    // Getters (và Setters nếu cần thiết)
    public Long getId() { return id; }
    public String getCreatedAt() { return createdAt; }
    public String getUserId() { return userId; }
    public String getVideoUrl() { return videoUrl; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getLikes() { return likes; }
    public int getDislikes() { return dislikes; }
    public Profile getUploaderProfile() { return uploaderProfile; } // Lấy thông tin người đăng đã join
}