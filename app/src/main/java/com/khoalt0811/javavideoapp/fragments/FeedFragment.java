package com.khoalt0811.javavideoapp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.khoalt0811.javavideoapp.R;
import com.khoalt0811.javavideoapp.ui.adapters.VideoFeedAdapter;
import com.khoalt0811.javavideoapp.data.models.VideoMetadata;
import com.khoalt0811.javavideoapp.viewmodels.VideoViewModel;

import java.util.ArrayList;
import java.util.List;

public class FeedFragment extends Fragment implements VideoFeedAdapter.OnVideoInteractionListener {
    private static final String TAG = "FeedFragment";

    private VideoViewModel videoViewModel;
    private VideoFeedAdapter videoAdapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;

    public static FeedFragment newInstance() {
        return new FeedFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo ViewModel
        videoViewModel = new ViewModelProvider(requireActivity(),
                new VideoViewModel.VideoViewModelFactory(requireActivity().getApplication()))
                .get(VideoViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        // Khởi tạo các view
        recyclerView = view.findViewById(R.id.recyclerViewVideos);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressBar = view.findViewById(R.id.progressBar);

        // Thiết lập RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        videoAdapter = new VideoFeedAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(videoAdapter);

        // Thiết lập SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::refreshVideos);

        // Thiết lập observers
        setupObservers();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tải video khi fragment được hiển thị
        videoViewModel.fetchVideos();
    }

    private void setupObservers() {
        // Quan sát danh sách video
        videoViewModel.videos.observe(getViewLifecycleOwner(), this::updateVideos);

        // Quan sát trạng thái loading
        videoViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (!isLoading) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        // Quan sát lỗi
        videoViewModel.error.observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void updateVideos(List<VideoMetadata> videos) {
        if (videos != null) {
            videoAdapter.updateData(videos);
            Log.d(TAG, "Đã cập nhật " + videos.size() + " video");
        } else {
            Log.d(TAG, "Danh sách video null");
        }
    }

    private void refreshVideos() {
        videoViewModel.fetchVideos();
    }

    // Implement từ OnVideoInteractionListener
    @Override
    public void onLikeClicked(VideoMetadata video) {
        if (video != null) {
            videoViewModel.likeVideo(video.getId());
        }
    }

    @Override
    public void onDislikeClicked(VideoMetadata video) {
        if (video != null) {
            videoViewModel.dislikeVideo(video.getId());
        }
    }

    @Override
    public void onShareClicked(VideoMetadata video) {
        // Xử lý chia sẻ video
        if (video != null) {
            Toast.makeText(requireContext(), "Đang chia sẻ video...", Toast.LENGTH_SHORT).show();
            // Implement chia sẻ nếu cần
        }
    }

    @Override
    public void onProfileClicked(VideoMetadata video) {
        // Chuyển đến trang profile của người đăng video
        if (video != null && video.getUserId() != null) {
            Bundle args = new Bundle();
            args.putString("userId", video.getUserId());

            // Chuyển đến ProfileFragment với userId
            ProfileFragment profileFragment = new ProfileFragment();
            profileFragment.setArguments(args);

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, profileFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}