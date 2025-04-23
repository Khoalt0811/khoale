package com.khoalt0811.javavideoapp.fragments; // Thay package name nếu khác

import android.app.Activity; // Import Activity
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore; // Import MediaStore
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult; // Import ActivityResult
import androidx.activity.result.ActivityResultCallback; // Import ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher; // Import ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts; // Import ActivityResultContracts
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide; // Glide để hiển thị preview
import com.khoalt0811.javavideoapp.R;
import com.khoalt0811.javavideoapp.databinding.FragmentUploadBinding; // Cần tạo layout fragment_upload.xml
import com.khoalt0811.javavideoapp.viewmodels.VideoViewModel;

public class UploadFragment extends Fragment {

    private static final String TAG = "UploadFragment";
    private FragmentUploadBinding binding;
    private VideoViewModel videoViewModel;
    private Uri selectedVideoUri = null; // Lưu Uri của video đã chọn

    // ActivityResultLauncher để xử lý kết quả chọn video
    private final ActivityResultLauncher<Intent> videoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedVideoUri = result.getData().getData();
                        if (selectedVideoUri != null) {
                            Log.d(TAG, "Video selected: " + selectedVideoUri.toString());
                            // Hiển thị preview video (dùng Glide hoặc VideoView)
                            Glide.with(UploadFragment.this)
                                    .load(selectedVideoUri)
                                    .placeholder(R.drawable.ic_placeholder)
                                    .error(R.drawable.ic_error)
                                    .into(binding.imageViewVideoPreview); // Giả sử có ImageView tên imageViewVideoPreview
                            binding.buttonSelectVideo.setText("Change Video"); // Đổi text nút
                            binding.buttonUpload.setEnabled(true); // Bật nút Upload
                        } else {
                            Log.e(TAG, "Failed to get URI from selected video.");
                            Toast.makeText(getContext(), "Failed to select video", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d(TAG, "Video selection cancelled or failed.");
                    }
                }
            });


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentUploadBinding.inflate(inflater, container, false);
        Log.d(TAG, "onCreateView: Layout inflated.");
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: View created.");

        // Lấy ViewModel
        // Cần Factory cho VideoViewModel
        VideoViewModel.VideoViewModelFactory factory = new VideoViewModel.VideoViewModelFactory(requireActivity().getApplication());
        videoViewModel = new ViewModelProvider(this, factory).get(VideoViewModel.class);

        // Thiết lập nút chọn video
        binding.buttonSelectVideo.setOnClickListener(v -> {
            Log.d(TAG, "Select video button clicked.");
            openVideoPicker();
        });

        // Thiết lập nút Upload (ban đầu bị vô hiệu hóa)
        binding.buttonUpload.setEnabled(false);
        binding.buttonUpload.setOnClickListener(v -> {
            Log.d(TAG, "Upload button clicked.");
            handleUpload();
        });

        // Observe trạng thái từ ViewModel
        observeViewModel();
    }

    // Mở trình chọn video của hệ thống
    private void openVideoPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        // Hoặc dùng Intent.ACTION_GET_CONTENT nếu muốn trình chọn file chung chung hơn
        // Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // intent.setType("video/*");
        videoPickerLauncher.launch(intent); // Khởi chạy ActivityResultLauncher
    }

    // Xử lý logic khi nhấn nút Upload
    private void handleUpload() {
        String title = binding.editTextVideoTitle.getText().toString().trim();
        String description = binding.editTextVideoDescription.getText().toString().trim();

        if (selectedVideoUri == null) {
            Toast.makeText(getContext(), "Please select a video first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(title)) {
            binding.editTextVideoTitle.setError("Title cannot be empty");
            binding.editTextVideoTitle.requestFocus();
            return;
        }

        // Gọi hàm upload trong ViewModel
        Log.d(TAG, "Calling uploadVideo in ViewModel...");
        videoViewModel.uploadVideo(selectedVideoUri, title, description);
    }

    // Observe LiveData từ ViewModel
    private void observeViewModel() {
        videoViewModel.isLoading.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                Log.d(TAG, "isLoading changed: " + isLoading);
                binding.progressBarUpload.setVisibility(isLoading ? View.VISIBLE : View.GONE); // Giả sử có ProgressBar progressBarUpload
                binding.buttonUpload.setEnabled(!isLoading && selectedVideoUri != null); // Chỉ bật khi không load và đã chọn video
                binding.buttonSelectVideo.setEnabled(!isLoading);
            }
        });

        videoViewModel.error.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String error) {
                if (error != null && !error.isEmpty()) {
                    Log.e(TAG, "Error observed: " + error);
                    Toast.makeText(getContext(), "Upload Error: " + error, Toast.LENGTH_LONG).show();
                }
            }
        });

        videoViewModel.uploadSuccessUrl.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String uploadedUrl) {
                if (uploadedUrl != null && !uploadedUrl.isEmpty()) {
                    Log.d(TAG, "Upload successful! URL: " + uploadedUrl);
                    Toast.makeText(getContext(), "Upload successful!", Toast.LENGTH_SHORT).show();
                    // Reset form sau khi upload thành công
                    resetUploadForm();
                    // Có thể chuyển sang FeedFragment hoặc màn hình khác
                    // ((MainActivity) requireActivity()).navigateToFeed(); // Ví dụ gọi hàm trong MainActivity
                }
            }
        });
        Log.d(TAG, "observeViewModel: Observers set.");
    }

    // Reset trạng thái form upload
    private void resetUploadForm() {
        selectedVideoUri = null;
        binding.imageViewVideoPreview.setImageResource(R.drawable.ic_placeholder); // Reset ảnh preview
        binding.editTextVideoTitle.setText("");
        binding.editTextVideoDescription.setText("");
        binding.buttonSelectVideo.setText("Select Video");
        binding.buttonUpload.setEnabled(false); // Vô hiệu hóa nút upload lại
        Log.d(TAG, "Upload form reset.");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Quan trọng: Giải phóng binding
        Log.d(TAG, "onDestroyView: Binding set to null.");
    }
}