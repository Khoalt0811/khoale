package com.khoalt0811.javavideoapp.data.models;

import com.google.gson.annotations.SerializedName;

public class VideoMetadata {
    @SerializedName("id")
    private long id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("video_url")
    private String videoUrl;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("like_count")
    private int likeCount = 0;

    @SerializedName("dislike_count")
    private int dislikeCount = 0;

    @SerializedName("view_count")
    private int viewCount = 0;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("profiles")
    private Profile profiles;

    // Constructor rỗng cần cho Gson deserialization
    public VideoMetadata() {
    }

    // Constructor với các tham số cần thiết cho upload video
    public VideoMetadata(String userId, String videoUrl, String title, String description) {
        this.userId = userId;
        this.videoUrl = videoUrl;
        this.title = title;
        this.description = description;
    }

    // Getters
    public long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public int getDislikeCount() {
        return dislikeCount;
    }

    public int getViewCount() {
        return viewCount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public Profile getProfiles() {
        return profiles;
    }

    // Setters
    public void setId(long id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public void setDislikeCount(int dislikeCount) {
        this.dislikeCount = dislikeCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setProfiles(Profile profiles) {
        this.profiles = profiles;
    }
    public Profile getUploaderProfile() {
        return getProfiles();
    }

    public int getLikes() {
        return getLikeCount();
    }


    public int getDislikes() {
        return getDislikeCount();
    }
}