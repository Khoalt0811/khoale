package com.khoalt0811.javavideoapp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.khoalt0811.javavideoapp.R;
import com.khoalt0811.javavideoapp.ui.adapters.ProfileVideosAdapter;
import com.khoalt0811.javavideoapp.data.models.Profile;
import com.khoalt0811.javavideoapp.data.models.VideoMetadata;
import com.khoalt0811.javavideoapp.utils.TokenManager;
import com.khoalt0811.javavideoapp.viewmodels.VideoViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProfileFragment extends Fragment implements ProfileVideosAdapter.OnVideoClickListener {
    private static final String TAG = "ProfileFragment";
    private static final String ARG_USER_ID = "userId";

    private VideoViewModel videoViewModel;
    private TokenManager tokenManager;
    private ProfileVideosAdapter videosAdapter;

    private ImageView imageViewProfile;
    private TextView textViewFullName;
    private TextView textViewEmail;
    private TextView textViewVideoCount;
    private Button buttonEditProfile;
    private RecyclerView recyclerViewVideos;
    private ProgressBar progressBar;

    private String userId;
    private boolean isCurrentUserProfile;

    public static ProfileFragment newInstance(String userId) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Lấy userId từ arguments
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID);
        }

        // Sử dụng userId của người dùng hiện tại nếu không có userId
        if (userId == null || userId.isEmpty()) {
            tokenManager = TokenManager.getInstance(requireContext());
            userId = tokenManager.getUserId();
        } else {
            tokenManager = TokenManager.getInstance(requireContext());
        }

        // Kiểm tra xem đây có phải là profile của người dùng hiện tại
        isCurrentUserProfile = userId != null && userId.equals(tokenManager.getUserId());

        // Khởi tạo ViewModel
        videoViewModel = new ViewModelProvider(requireActivity(),
                new VideoViewModel.VideoViewModelFactory(requireActivity().getApplication()))
                .get(VideoViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Khởi tạo views
        imageViewProfile = view.findViewById(R.id.imageViewProfile);
        textViewFullName = view.findViewById(R.id.textViewFullName);
        textViewEmail = view.findViewById(R.id.textViewEmail);
        textViewVideoCount = view.findViewById(R.id.textViewVideoCount);
        buttonEditProfile = view.findViewById(R.id.buttonEditProfile);
        recyclerViewVideos = view.findViewById(R.id.recyclerViewProfileVideos);
        progressBar = view.findViewById(R.id.progressBar);

        // Thiết lập RecyclerView với GridLayoutManager
        recyclerViewVideos.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        videosAdapter = new ProfileVideosAdapter(new ArrayList<>(), this);
        recyclerViewVideos.setAdapter(videosAdapter);

        // Hiển thị hoặc ẩn nút Edit Profile dựa vào việc có phải profile người dùng hiện tại
        buttonEditProfile.setVisibility(isCurrentUserProfile ? View.VISIBLE : View.GONE);

        // Thiết lập sự kiện click cho nút Edit Profile
        buttonEditProfile.setOnClickListener(v -> {
            // TODO: Chuyển tới EditProfileFragment hoặc mở EditProfileActivity
            Toast.makeText(requireContext(), "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
        });

        // Thiết lập observers
        setupObservers();

        // Tải profile và videos
        loadProfileData();

        return view;
    }

    private void setupObservers() {
        // Quan sát thông tin profile
        videoViewModel.profileDetails.observe(getViewLifecycleOwner(), this::updateProfileUI);

        // Quan sát danh sách video
        videoViewModel.videos.observe(getViewLifecycleOwner(), this::filterUserVideos);

        // Quan sát trạng thái loading
        videoViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Quan sát lỗi
        videoViewModel.error.observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProfileData() {
        if (userId != null) {
            videoViewModel.fetchProfileDetails(userId);
            videoViewModel.fetchVideos(); // Sẽ lọc ra video của người dùng sau
        } else {
            Toast.makeText(requireContext(), "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProfileUI(Profile profile) {
        if (profile == null) {
            Log.d(TAG, "Profile null");
            return;
        }

        // Hiển thị thông tin profile
        textViewFullName.setText(profile.getFullName() != null ? profile.getFullName() : "User");
        textViewEmail.setText(profile.getEmail());

        // Hiển thị avatar nếu có
        if (profile.getAvatarUrl() != null && !profile.getAvatarUrl().isEmpty()) {
            Glide.with(this)
                    .load(profile.getAvatarUrl())
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .circleCrop()
                    .into(imageViewProfile);
        } else {
            // Sử dụng avatar mặc định
            imageViewProfile.setImageResource(R.drawable.default_avatar);
        }
    }

    private void filterUserVideos(List<VideoMetadata> allVideos) {
        if (allVideos == null || allVideos.isEmpty()) {
            textViewVideoCount.setText("0 video");
            videosAdapter.updateData(new ArrayList<>());
            return;
        }

        // Lọc video của người dùng hiện tại
        List<VideoMetadata> userVideos = allVideos.stream()
                .filter(video -> userId.equals(video.getUserId()))
                .collect(Collectors.toList());

        // Cập nhật UI
        textViewVideoCount.setText(userVideos.size() + " video");
        videosAdapter.updateData(userVideos);
    }

    @Override
    public void onVideoClicked(VideoMetadata video) {
        // TODO: Mở video để xem chi tiết hoặc phát
        Toast.makeText(requireContext(), "Đang mở video...", Toast.LENGTH_SHORT).show();
        // Implement mở video
    }
}