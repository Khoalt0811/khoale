package com.khoalt0811.javavideoapp.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.khoalt0811.javavideoapp.R;
import com.khoalt0811.javavideoapp.viewmodels.VideoViewModel;

public class UploadFragment extends Fragment {
    private static final String TAG = "UploadFragment";
    private static final int REQUEST_VIDEO_PICK = 1001;
    private static final int PERMISSION_REQUEST_CODE = 1002;

    private VideoViewModel videoViewModel;
    private VideoView videoPreview;
    private EditText editTextTitle;
    private EditText editTextDescription;
    private Button buttonPickVideo;
    private Button buttonUpload;
    private ProgressBar progressBar;

    private Uri selectedVideoUri = null;

    public static UploadFragment newInstance() {
        return new UploadFragment();
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
        View view = inflater.inflate(R.layout.fragment_upload, container, false);

        // Khởi tạo views
        videoPreview = view.findViewById(R.id.videoPreview);
        editTextTitle = view.findViewById(R.id.editTextVideoTitle);
        editTextDescription = view.findViewById(R.id.editTextVideoDescription);
        buttonPickVideo = view.findViewById(R.id.buttonPickVideo);
        buttonUpload = view.findViewById(R.id.buttonUpload);
        progressBar = view.findViewById(R.id.progressBarUpload);

        // Thiết lập sự kiện click
        buttonPickVideo.setOnClickListener(v -> checkPermissionAndPickVideo());
        buttonUpload.setOnClickListener(v -> uploadVideo());

        // Ban đầu nút Upload bị vô hiệu hóa cho đến khi chọn video
        buttonUpload.setEnabled(false);

        // Thiết lập observers
        setupObservers();

        return view;
    }

    private void setupObservers() {
        // Quan sát trạng thái loading
        videoViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            buttonUpload.setEnabled(!isLoading && selectedVideoUri != null);
            buttonPickVideo.setEnabled(!isLoading);
        });

        // Quan sát lỗi
        videoViewModel.error.observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });

        // Quan sát kết quả upload
        videoViewModel.uploadSuccessUrl.observe(getViewLifecycleOwner(), url -> {
            if (url != null) {
                Toast.makeText(requireContext(), "Video đã được tải lên thành công!", Toast.LENGTH_SHORT).show();
                // Reset form sau khi upload thành công
                resetForm();
                // Clear success state
                videoViewModel.clearUploadSuccess();
            }
        });
    }

    private void checkPermissionAndPickVideo() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        } else {
            openVideoGallery();
        }
    }

    private void openVideoGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_VIDEO_PICK);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openVideoGallery();
            } else {
                Toast.makeText(requireContext(), "Cần quyền truy cập bộ nhớ để chọn video", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VIDEO_PICK && resultCode == Activity.RESULT_OK && data != null) {
            selectedVideoUri = data.getData();
            if (selectedVideoUri != null) {
                // Hiển thị video preview
                videoPreview.setVideoURI(selectedVideoUri);
                videoPreview.requestFocus();
                videoPreview.start();
                // Bật nút Upload
                buttonUpload.setEnabled(true);
            }
        }
    }

    private void uploadVideo() {
        if (selectedVideoUri == null) {
            Toast.makeText(requireContext(), "Vui lòng chọn video trước", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        if (title.isEmpty()) {
            editTextTitle.setError("Tiêu đề không được để trống");
            return;
        }

        // Upload video
        videoViewModel.uploadVideo(selectedVideoUri, title, description);
    }

    private void resetForm() {
        selectedVideoUri = null;
        editTextTitle.setText("");
        editTextDescription.setText("");
        videoPreview.setVideoURI(null);
        videoPreview.setVisibility(View.GONE);
        buttonUpload.setEnabled(false);
    }
}