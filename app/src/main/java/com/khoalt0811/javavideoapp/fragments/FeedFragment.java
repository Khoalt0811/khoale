package com.khoalt0811.javavideoapp.fragments; // Thay package name nếu khác

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView; // Import TextView
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout; // *** IMPORT ĐÚNG ***

import com.khoalt0811.javavideoapp.R;
import com.khoalt0811.javavideoapp.ui.adapters.VideoAdapter;
import com.khoalt0811.javavideoapp.data.models.Profile;
import com.khoalt0811.javavideoapp.data.models.VideoMetadata;
import com.khoalt0811.javavideoapp.databinding.FragmentFeedBinding; // Đảm bảo tên layout đúng
import com.khoalt0811.javavideoapp.viewmodels.VideoViewModel;

import java.util.ArrayList;

public class FeedFragment extends Fragment {

    private static final String TAG = "FeedFragment";
    private FragmentFeedBinding binding; // Biến Binding cho fragment_feed.xml
    private VideoViewModel videoViewModel;
    private VideoAdapter videoAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout bằng ViewBinding
        binding = FragmentFeedBinding.inflate(inflater, container, false);
        Log.d(TAG, "onCreateView: Layout inflated.");
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: View created.");

        // Khởi tạo ViewModel bằng Factory
        VideoViewModel.VideoViewModelFactory factory = new VideoViewModel.VideoViewModelFactory(requireActivity().getApplication());
        videoViewModel = new ViewModelProvider(this, factory).get(VideoViewModel.class);

        setupRecyclerView();
        setupSwipeRefresh(); // Thiết lập SwipeRefreshLayout
        observeViewModel();

        // Lấy dữ liệu lần đầu nếu adapter rỗng
        if (videoAdapter.getItemCount() == 0) {
            Log.d(TAG, "onViewCreated: Adapter empty, fetching initial videos...");
            fetchVideos(false); // Không hiển thị indicator swipe cho lần load đầu
        }
    }

    private void setupRecyclerView() {
        videoAdapter = new VideoAdapter(new ArrayList<>(), new VideoAdapter.OnItemClickListener() {
            @Override
            public void onLikeClick(VideoMetadata video) {
                Log.d(TAG, "Like clicked for video ID: " + video.getId());
                if (video.getId() != null) {
                    videoViewModel.likeVideo(video.getId());
                    // TODO: Cập nhật UI tức thì (tùy chọn)
                }
            }

            @Override
            public void onDislikeClick(VideoMetadata video) {
                Log.d(TAG, "Dislike clicked for video ID: " + video.getId());
                if (video.getId() != null) {
                    videoViewModel.dislikeVideo(video.getId());
                    // TODO: Cập nhật UI tức thì (tùy chọn)
                }
            }

            @Override
            public void onUploaderClick(Profile profile) {
                if (profile != null && profile.getId() != null) {
                    Log.d(TAG, "Uploader clicked: " + profile.getFullName() + " (ID: " + profile.getId() + ")");
                    // Điều hướng đến ProfileFragment, truyền user ID
                    navigateToProfile(profile.getId());
                } else {
                    Log.w(TAG, "Uploader profile or ID is null, cannot navigate.");
                    Toast.makeText(getContext(), "Cannot view uploader profile", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.recyclerViewFeed.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewFeed.setAdapter(videoAdapter);
        Log.d(TAG, "setupRecyclerView: RecyclerView setup complete.");
    }

    private void setupSwipeRefresh() {
        // Lấy tham chiếu đến SwipeRefreshLayout từ binding
        SwipeRefreshLayout swipeLayout = binding.swipeRefreshLayout; // *** TRUY CẬP QUA BINDING ***
        if (swipeLayout != null) {
            // Thiết lập màu sắc indicator
            swipeLayout.setColorSchemeResources(R.color.purple_500, R.color.teal_200); // *** SỬ DỤNG PHƯƠNG THỨC ĐÚNG ***
            // Thiết lập listener khi người dùng kéo xuống
            swipeLayout.setOnRefreshListener(() -> { // *** SỬ DỤNG PHƯƠNG THỨC ĐÚNG ***
                Log.d(TAG, "Swipe to refresh triggered.");
                fetchVideos(true); // Fetch lại video và báo là do swipe
            });
        } else {
            // Ghi log cảnh báo nếu không tìm thấy ID trong layout
            Log.w(TAG, "swipeRefreshLayout ID not found in fragment_feed.xml binding. Cannot set OnRefreshListener.");
        }
    }


    private void observeViewModel() {
        // Quan sát trạng thái loading
        videoViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            Log.d(TAG, "isLoading changed: " + isLoading);
            SwipeRefreshLayout swipeLayout = binding.swipeRefreshLayout; // *** TRUY CẬP QUA BINDING ***
            // Chỉ hiển thị ProgressBar nếu không phải đang refresh bằng SwipeRefreshLayout
            if (swipeLayout == null || !swipeLayout.isRefreshing()) { // *** SỬ DỤNG PHƯƠNG THỨC ĐÚNG ***
                binding.progressBarFeed.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
            // Dừng indicator của SwipeRefreshLayout khi loading kết thúc
            if (!isLoading && swipeLayout != null) {
                swipeLayout.setRefreshing(false); // *** SỬ DỤNG PHƯƠNG THỨC ĐÚNG ***
            }
        });

        // Quan sát lỗi
        videoViewModel.error.observe(getViewLifecycleOwner(), error -> {
            // Chỉ hiển thị toast nếu có lỗi mới
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "Error observed: " + error);
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                // Đảm bảo các indicator loading ẩn đi khi có lỗi
                binding.progressBarFeed.setVisibility(View.GONE);
                SwipeRefreshLayout swipeLayout = binding.swipeRefreshLayout; // *** TRUY CẬP QUA BINDING ***
                if (swipeLayout != null) {
                    swipeLayout.setRefreshing(false); // *** SỬ DỤNG PHƯƠNG THỨC ĐÚNG ***
                }
                // QUAN TRỌNG: Xóa lỗi trong ViewModel sau khi hiển thị để tránh hiển thị lại khi xoay màn hình
                videoViewModel.clearError();
            }
        });

        // Quan sát cập nhật danh sách video
        videoViewModel.videos.observe(getViewLifecycleOwner(), videos -> {
            TextView emptyTextView = binding.textViewEmptyFeed; // *** TRUY CẬP QUA BINDING ***
            if (videos != null) {
                Log.d(TAG, "Videos updated: " + videos.size() + " items.");
                videoAdapter.updateVideos(videos); // Cập nhật dữ liệu adapter
                // Hiển thị/ẩn thông báo rỗng
                if (emptyTextView != null) {
                    emptyTextView.setVisibility(videos.isEmpty() ? View.VISIBLE : View.GONE);
                }
            } else {
                Log.w(TAG, "Videos list received is null. Clearing adapter.");
                videoAdapter.updateVideos(new ArrayList<>()); // Xóa adapter
                if (emptyTextView != null) {
                    emptyTextView.setVisibility(View.VISIBLE); // Hiển thị thông báo rỗng
                }
            }
        });
        Log.d(TAG, "observeViewModel: Observers set.");
    }

    // Hàm tập trung để fetch video, xử lý việc hiển thị indicator swipe
    private void fetchVideos(boolean isSwipeRefresh) {
        SwipeRefreshLayout swipeLayout = binding.swipeRefreshLayout; // *** TRUY CẬP QUA BINDING ***
        TextView emptyTextView = binding.textViewEmptyFeed; // *** TRUY CẬP QUA BINDING ***

        // Chỉ hiển thị indicator swipe nếu được kích hoạt bởi swipe
        if (isSwipeRefresh && swipeLayout != null) {
            swipeLayout.setRefreshing(true); // *** SỬ DỤNG PHƯƠNG THỨC ĐÚNG ***
        } else {
            // Hiển thị progress bar cho các lần load không phải swipe
            binding.progressBarFeed.setVisibility(View.VISIBLE);
        }
        // Ẩn thông báo rỗng khi đang load
        if (emptyTextView != null) {
            emptyTextView.setVisibility(View.GONE);
        }
        videoViewModel.fetchVideos();
    }

    private void navigateToProfile(String userId) {
        Fragment profileFragment = ProfileFragment.newInstance(userId); // Sử dụng factory method
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, profileFragment) // Thay thế nội dung trong container của MainActivity
                .addToBackStack(null) // Cho phép người dùng nhấn back để quay lại Feed
                .commit();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Dọn dẹp binding và tham chiếu adapter khi view bị hủy
        if (binding != null) {
            binding.recyclerViewFeed.setAdapter(null); // Quan trọng để tránh leak memory với RecyclerView
        }
        binding = null; // Giải phóng binding
        Log.d(TAG, "onDestroyView: Binding and RecyclerView adapter set to null.");
    }
}