package com.khoalt0811.javavideoapp.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.khoalt0811.javavideoapp.R;
import com.khoalt0811.javavideoapp.data.models.VideoMetadata;

import java.util.List;

public class ProfileVideosAdapter extends RecyclerView.Adapter<ProfileVideosAdapter.VideoGridViewHolder> {

    private List<VideoMetadata> videoList;
    private final OnVideoClickListener listener;

    public interface OnVideoClickListener {
        void onVideoClicked(VideoMetadata video);
    }

    public ProfileVideosAdapter(List<VideoMetadata> videoList, OnVideoClickListener listener) {
        this.videoList = videoList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VideoGridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile_video, parent, false);
        return new VideoGridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoGridViewHolder holder, int position) {
        holder.bind(videoList.get(position));
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public void updateData(List<VideoMetadata> newVideos) {
        this.videoList = newVideos;
        notifyDataSetChanged();
    }

    class VideoGridViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageVideoThumbnail;
        private final TextView textViewVideoStats;

        public VideoGridViewHolder(@NonNull View itemView) {
            super(itemView);
            imageVideoThumbnail = itemView.findViewById(R.id.imageVideoThumbnail);
            textViewVideoStats = itemView.findViewById(R.id.textViewVideoStats);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onVideoClicked(videoList.get(position));
                }
            });
        }

        void bind(VideoMetadata video) {
            // Hiển thị thumbnail
            if (video.getVideoUrl() != null) {
                Glide.with(itemView.getContext())
                        .load(video.getVideoUrl())
                        .placeholder(R.drawable.video_placeholder)
                        .centerCrop()
                        .into(imageVideoThumbnail);
            }

            // Hiển thị số lượt thích
            String stats = video.getLikeCount() + " ❤️ · " + video.getViewCount() + " 👁";
            textViewVideoStats.setText(stats);
        }
    }
}