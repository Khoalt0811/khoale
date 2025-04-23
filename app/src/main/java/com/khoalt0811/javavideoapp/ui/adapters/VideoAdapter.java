package com.khoalt0811.javavideoapp.ui.adapters; // Thay package name nếu khác

import android.content.Context; // Import Context
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Import Glide
import com.khoalt0811.javavideoapp.R; // Import R
import com.khoalt0811.javavideoapp.data.models.Profile; // Import Profile
import com.khoalt0811.javavideoapp.data.models.VideoMetadata;
import com.khoalt0811.javavideoapp.databinding.ItemVideoBinding; // Import Binding cho item

import java.util.List;
import java.util.Locale; // Import Locale

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private List<VideoMetadata> videoList;
    private final OnItemClickListener listener; // Interface để xử lý click

    // Interface định nghĩa các hành động click
    public interface OnItemClickListener {
        void onLikeClick(VideoMetadata video);
        void onDislikeClick(VideoMetadata video);
        void onUploaderClick(Profile profile); // Click vào avatar/tên người đăng
    }

    // Constructor
    public VideoAdapter(List<VideoMetadata> videoList, OnItemClickListener listener) {
        this.videoList = videoList;
        this.listener = listener;
    }

    // Tạo ViewHolder mới
    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout item bằng ViewBinding
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemVideoBinding binding = ItemVideoBinding.inflate(inflater, parent, false);
        return new VideoViewHolder(binding, listener);
    }

    // Gắn dữ liệu vào ViewHolder
    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoMetadata currentVideo = videoList.get(position);
        holder.bind(currentVideo); // Gọi hàm bind của ViewHolder
    }

    // Trả về số lượng item
    @Override
    public int getItemCount() {
        return videoList != null ? videoList.size() : 0;
    }

    // Hàm cập nhật dữ liệu cho Adapter
    public void updateVideos(List<VideoMetadata> newVideoList) {
        this.videoList = newVideoList;
        notifyDataSetChanged(); // Thông báo cho RecyclerView cập nhật (cách đơn giản nhất)
        // Cân nhắc dùng DiffUtil để tối ưu hiệu năng nếu danh sách lớn và thay đổi thường xuyên
    }

    // --- ViewHolder Class ---
    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        private final ItemVideoBinding binding; // ViewBinding cho item
        private final OnItemClickListener listener;
        private Context context; // Giữ context để dùng Glide

        public VideoViewHolder(@NonNull ItemVideoBinding binding, OnItemClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
            this.context = binding.getRoot().getContext(); // Lấy context từ view gốc
        }

        // Hàm gắn dữ liệu vào các view trong item layout
        public void bind(final VideoMetadata video) {
            // Load ảnh thumbnail video (hoặc hiển thị player nếu có)
            Glide.with(context)
                    .load(video.getVideoUrl()) // Hoặc URL thumbnail nếu có
                    .placeholder(R.drawable.ic_placeholder) // Ảnh chờ load
                    .error(R.drawable.ic_error)       // Ảnh khi lỗi
                    .into(binding.imageViewThumbnail);

            // Hiển thị tiêu đề video
            binding.textViewVideoTitle.setText(video.getTitle() != null ? video.getTitle() : "No Title");

            // Hiển thị thông tin người đăng (nếu có join bảng profiles)
            Profile uploader = video.getUploaderProfile();
            if (uploader != null) {
                binding.textViewUploaderEmail.setText(uploader.getFullName() != null ? uploader.getFullName() : (uploader.getEmail() !=null ? uploader.getEmail() : "Unknown Uploader")); // Ưu tiên tên, rồi đến email
                Glide.with(context)
                        .load(uploader.getAvatarUrl())
                        .placeholder(R.drawable.ic_profile) // Ảnh profile mặc định
                        .error(R.drawable.ic_profile)
                        .circleCrop() // Bo tròn ảnh avatar
                        .into(binding.imageViewUploaderAvatar);

                // Set click listener cho avatar và tên người đăng
                View.OnClickListener uploaderClickListener = v -> listener.onUploaderClick(uploader);
                binding.imageViewUploaderAvatar.setOnClickListener(uploaderClickListener);
                binding.textViewUploaderEmail.setOnClickListener(uploaderClickListener);

            } else {
                // Xử lý trường hợp không có thông tin người đăng
                binding.textViewUploaderEmail.setText("Unknown Uploader");
                binding.imageViewUploaderAvatar.setImageResource(R.drawable.ic_profile); // Ảnh mặc định
                binding.imageViewUploaderAvatar.setOnClickListener(null); // Xóa listener cũ
                binding.textViewUploaderEmail.setOnClickListener(null);
            }


            // Hiển thị số lượt like/dislike
            binding.textViewLikesCount.setText(String.format(Locale.getDefault(), "%d", video.getLikes()));
            binding.textViewDislikesCount.setText(String.format(Locale.getDefault(), "%d", video.getDislikes()));

            // Set click listener cho nút like và dislike
            binding.buttonLike.setOnClickListener(v -> listener.onLikeClick(video));
            binding.buttonDislike.setOnClickListener(v -> listener.onDislikeClick(video));
        }
    }
}