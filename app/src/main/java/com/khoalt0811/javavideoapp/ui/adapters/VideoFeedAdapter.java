package com.khoalt0811.javavideoapp.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.khoalt0811.javavideoapp.R;
import com.khoalt0811.javavideoapp.data.models.VideoMetadata;

import java.util.List;

public class VideoFeedAdapter extends RecyclerView.Adapter<VideoFeedAdapter.VideoViewHolder> {

    private List<VideoMetadata> videoList;
    private final OnVideoInteractionListener listener;

    public interface OnVideoInteractionListener {
        void onLikeClicked(VideoMetadata video);
        void onDislikeClicked(VideoMetadata video);
        void onShareClicked(VideoMetadata video);
        void onProfileClicked(VideoMetadata video);
    }

    public VideoFeedAdapter(List<VideoMetadata> videoList, OnVideoInteractionListener listener) {
        this.videoList = videoList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_feed, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoMetadata video = videoList.get(position);
        holder.bind(video);
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public void updateData(List<VideoMetadata> newVideos) {
        this.videoList = newVideos;
        notifyDataSetChanged();
    }

    class VideoViewHolder extends RecyclerView.ViewHolder {

        private final VideoView videoView;
        private final ImageView imagePreview;
        private final TextView textUsername;
        private final TextView textTitle;
        private final TextView textDescription;
        private final ImageView imageUserAvatar;
        private final ImageButton buttonLike;
        private final TextView textLikeCount;
        private final ImageButton buttonDislike;
        private final TextView textDislikeCount;
        private final ImageButton buttonShare;
        private final Button buttonPlayPause;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);

            videoView = itemView.findViewById(R.id.videoView);
            imagePreview = itemView.findViewById(R.id.imagePreview);
            textUsername = itemView.findViewById(R.id.textUsername);
            textTitle = itemView.findViewById(R.id.textTitle);
            textDescription = itemView.findViewById(R.id.textDescription);
            imageUserAvatar = itemView.findViewById(R.id.imageUserAvatar);
            buttonLike = itemView.findViewById(R.id.buttonLike);
            textLikeCount = itemView.findViewById(R.id.textLikeCount);
            buttonDislike = itemView.findViewById(R.id.buttonDislike);
            textDislikeCount = itemView.findViewById(R.id.textDislikeCount);
            buttonShare = itemView.findViewById(R.id.buttonShare);
            buttonPlayPause = itemView.findViewById(R.id.buttonPlayPause);

            // Set up listeners
            buttonLike.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onLikeClicked(videoList.get(position));
                }
            });

            buttonDislike.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDislikeClicked(videoList.get(position));
                }
            });

            buttonShare.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onShareClicked(videoList.get(position));
                }
            });

            imageUserAvatar.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onProfileClicked(videoList.get(position));
                }
            });

            textUsername.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onProfileClicked(videoList.get(position));
                }
            });

            buttonPlayPause.setOnClickListener(v -> toggleVideoPlayback());
        }

        void bind(VideoMetadata video) {
            textTitle.setText(video.getTitle());
            textDescription.setText(video.getDescription());
            textLikeCount.setText(String.valueOf(video.getLikeCount()));
            textDislikeCount.setText(String.valueOf(video.getDislikeCount()));

            // Hiển thị tên người dùng và avatar nếu có profiles
            if (video.getProfiles() != null) {
                textUsername.setText(video.getProfiles().getFullName() != null
                        ? video.getProfiles().getFullName() : "User");

                // Load avatar nếu có
                if (video.getProfiles().getAvatarUrl() != null) {
                    Glide.with(imageUserAvatar.getContext())
                            .load(video.getProfiles().getAvatarUrl())
                            .placeholder(R.drawable.default_avatar)
                            .error(R.drawable.default_avatar)
                            .circleCrop()
                            .into(imageUserAvatar);
                } else {
                    imageUserAvatar.setImageResource(R.drawable.default_avatar);
                }
            } else {
                textUsername.setText("User");
                imageUserAvatar.setImageResource(R.drawable.default_avatar);
            }

            // Thiết lập video
            // Lưu ý: trong triển khai thực tế, bạn có thể muốn lazy load video
            // hoặc sử dụng ExoPlayer thay vì VideoView
            if (video.getVideoUrl() != null) {
                videoView.setVideoPath(video.getVideoUrl());
                videoView.setOnPreparedListener(mp -> {
                    mp.setVolume(0f, 0f); // Tắt âm thanh ban đầu
                    imagePreview.setVisibility(View.GONE);
                    videoView.start();
                });

                // Hiển thị thumbnail hoặc khung đen cho đến khi video sẵn sàng
                Glide.with(imagePreview.getContext())
                        .load(video.getVideoUrl())
                        .placeholder(R.drawable.video_placeholder)
                        .error(R.drawable.video_placeholder)
                        .into(imagePreview);
            }
        }

        void toggleVideoPlayback() {
            if (videoView.isPlaying()) {
                videoView.pause();
                buttonPlayPause.setText("Phát");
            } else {
                videoView.start();
                buttonPlayPause.setText("Tạm dừng");
            }
        }
    }
}