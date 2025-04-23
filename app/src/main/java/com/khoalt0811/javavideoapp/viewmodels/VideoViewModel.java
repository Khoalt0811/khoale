package com.khoalt0811.javavideoapp.viewmodels; // Thay package name nếu khác

import android.app.Application;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.khoalt0811.javavideoapp.data.models.Profile;
import com.khoalt0811.javavideoapp.data.models.VideoMetadata;
import com.khoalt0811.javavideoapp.networking.RetrofitClient;
import com.khoalt0811.javavideoapp.networking.SupabaseApiService;
import com.khoalt0811.javavideoapp.utils.FileUtils;
import com.khoalt0811.javavideoapp.utils.TokenManager;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoViewModel extends AndroidViewModel {
    private static final String TAG = "VideoViewModel";
    private final SupabaseApiService apiService;
    private final ExecutorService executorService;
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private final TokenManager tokenManager;

    // LiveData for loading state and errors (shared)
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;
    private final MutableLiveData<String> _error = new MutableLiveData<>(null); // Initialize with null
    public LiveData<String> error = _error;

    // LiveData specific to video feed
    private final MutableLiveData<List<VideoMetadata>> _videos = new MutableLiveData<>();
    public LiveData<List<VideoMetadata>> videos = _videos;

    // LiveData specific to video upload
    private final MutableLiveData<String> _uploadSuccessUrl = new MutableLiveData<>(null); // Initialize with null
    public LiveData<String> uploadSuccessUrl = _uploadSuccessUrl;

    // LiveData specific to profile details
    private final MutableLiveData<Profile> _profileDetails = new MutableLiveData<>(null); // Initialize with null
    public LiveData<Profile> profileDetails = _profileDetails;


    public VideoViewModel(@NonNull Application application) {
        super(application);
        apiService = RetrofitClient.getApiService();
        // Use a thread pool suitable for multiple potential background tasks
        executorService = Executors.newFixedThreadPool(3);
        tokenManager = TokenManager.getInstance(application);
    }

    // --- Video Feed Methods ---

    public void fetchVideos() {
        Log.d(TAG, "fetchVideos called");
        _isLoading.postValue(true);
        clearError(); // Clear previous error before fetching
        executorService.execute(() -> {
            String accessToken = tokenManager.getAccessToken();
            if (accessToken == null) {
                postError("User not authenticated");
                _isLoading.postValue(false);
                return;
            }
            String authHeader = "Bearer " + accessToken;
            // Include profile data with videos
            String selectQuery = "*,profiles(id,email,avatar_url,full_name)";
            Map<String, String> options = new HashMap<>();
            options.put("order", "created_at.desc"); // Order by newest first

            Call<List<VideoMetadata>> call = apiService.getVideos(authHeader, selectQuery, options);
            call.enqueue(new Callback<List<VideoMetadata>>() {
                @Override
                public void onResponse(@NonNull Call<List<VideoMetadata>> call, @NonNull Response<List<VideoMetadata>> response) {
                    mainThreadHandler.post(() -> {
                        _isLoading.setValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            _videos.setValue(response.body());
                            Log.d(TAG, "Fetched " + response.body().size() + " videos successfully.");
                        } else {
                            handleApiError("Failed to fetch videos", response);
                        }
                    });
                }

                @Override
                public void onFailure(@NonNull Call<List<VideoMetadata>> call, @NonNull Throwable t) {
                    handleNetworkError("Network error fetching videos", t);
                }
            });
        });
    }

    public void likeVideo(long videoId) {
        Log.d(TAG, "likeVideo called for ID: " + videoId);
        executorService.execute(() -> {
            String accessToken = tokenManager.getAccessToken();
            if (accessToken == null) {
                Log.w(TAG, "Cannot like video, user not authenticated.");
                // Optionally post an error if feedback is needed
                // postError("Authentication required to like video");
                return;
            }
            String authHeader = "Bearer " + accessToken;
            Map<String, Long> params = Collections.singletonMap("video_id_input", videoId);
            Call<Void> call = apiService.incrementLikes(authHeader, params);
            call.enqueue(new Callback<Void>() {
                @Override public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Like successful for video ID: " + videoId);
                        // Optionally trigger a refetch or update UI more granularly
                        // fetchVideos(); // Simple refetch
                    } else {
                        Log.e(TAG, "Like failed for video ID: " + videoId + ": " + response.code());
                        // Optionally post error message
                    }
                }
                @Override public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    Log.e(TAG, "Like network error for video ID: " + videoId, t);
                    // Optionally post error message
                }
            });
        });
    }

    public void dislikeVideo(long videoId) {
        Log.d(TAG, "dislikeVideo called for ID: " + videoId);
        executorService.execute(() -> {
            String accessToken = tokenManager.getAccessToken();
            if (accessToken == null) {
                Log.w(TAG, "Cannot dislike video, user not authenticated.");
                // postError("Authentication required to dislike video");
                return;
            }
            String authHeader = "Bearer " + accessToken;
            Map<String, Long> params = Collections.singletonMap("video_id_input", videoId);
            Call<Void> call = apiService.incrementDislikes(authHeader, params);
            call.enqueue(new Callback<Void>() {
                @Override public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Dislike successful for video ID: " + videoId);
                        // Optionally trigger a refetch or update UI more granularly
                        // fetchVideos();
                    } else {
                        Log.e(TAG, "Dislike failed for video ID: " + videoId + ": " + response.code());
                    }
                }
                @Override public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    Log.e(TAG, "Dislike network error for video ID: " + videoId, t);
                }
            });
        });
    }


    // --- Video Upload Methods ---

    public void uploadVideo(Uri videoUri, String title, String description) {
        Log.d(TAG, "uploadVideo called for URI: " + videoUri);
        _isLoading.postValue(true);
        clearError();
        _uploadSuccessUrl.postValue(null); // Reset success URL
        executorService.execute(() -> {
            String accessToken = tokenManager.getAccessToken();
            String userId = tokenManager.getUserId();

            if (accessToken == null || userId == null) {
                postError("User not authenticated or User ID not found");
                _isLoading.postValue(false);
                return;
            }
            String authHeader = "Bearer " + accessToken;

            File videoFile = FileUtils.getFileFromUri(getApplication(), videoUri);
            if (videoFile == null) {
                postError("Could not get file from Uri");
                _isLoading.postValue(false);
                return;
            }

            String mimeType = FileUtils.getMimeType(getApplication(), videoUri);
            if (mimeType == null) mimeType = "video/*"; // Fallback MIME type
            RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), videoFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", videoFile.getName(), requestFile);

            String fileExt = FileUtils.getFileExtension(getApplication(), videoUri);
            if (fileExt == null) fileExt = "mp4"; // Fallback extension
            String fileName = System.currentTimeMillis() + "_" + userId + "." + fileExt;
            String supabasePath = userId + "/" + fileName; // Path in Supabase Storage: userId/timestamp_userId.ext

            Log.d(TAG, "Attempting to upload file to path: " + supabasePath);

            Call<Void> uploadCall = apiService.uploadVideo(authHeader, "videos", supabasePath, body);
            try {
                // Execute synchronously within the background thread
                Response<Void> uploadResponse = uploadCall.execute();
                if (!uploadResponse.isSuccessful()) {
                    String errorDetail = getErrorBodyString(uploadResponse);
                    throw new IOException("Upload to Storage failed: " + uploadResponse.code() + " - " + errorDetail);
                }
                Log.d(TAG, "File uploaded successfully to Storage.");

                // Construct the public URL (Replace YOUR_PROJECT_ID with your actual Supabase Project ID)
                String publicUrl = "https://mvmwjkbuomrmksqavtup" + ".supabase.co/storage/v1/object/public/videos/" + supabasePath;
                Log.d(TAG, "Generated public URL: " + publicUrl);

                // Insert metadata into the 'videos' table
                VideoMetadata metadata = new VideoMetadata(userId, publicUrl, title, description);
                Call<Void> insertCall = apiService.insertVideo(authHeader, "return=minimal", metadata);
                Response<Void> insertResponse = insertCall.execute();

                if (!insertResponse.isSuccessful()) {
                    String errorDetail = getErrorBodyString(insertResponse);
                    throw new IOException("Insert metadata failed: " + insertResponse.code() + " - " + errorDetail);
                }
                Log.d(TAG, "Video metadata inserted successfully.");

                // Post success URL to LiveData on the main thread
                mainThreadHandler.post(() -> {
                    _isLoading.setValue(false);
                    _uploadSuccessUrl.setValue(publicUrl);
                    Log.d(TAG, "Upload process completed successfully.");
                });

            } catch (Exception e) {
                Log.e(TAG, "Upload process failed", e);
                handleNetworkError("Upload failed", e); // Use helper for error posting
            } finally {
                // Clean up the temporary file from cache
                if (videoFile != null && videoFile.exists() && videoFile.getPath().contains(getApplication().getCacheDir().getPath())) {
                    if(videoFile.delete()) {
                        Log.d(TAG,"Temporary video file deleted: " + videoFile.getAbsolutePath());
                    } else {
                        Log.w(TAG,"Failed to delete temporary video file: " + videoFile.getAbsolutePath());
                    }
                }
            }
        });
    }

    // --- Profile Methods ---

    public void fetchProfileDetails(String userId) {
        Log.d(TAG, "fetchProfileDetails called for user ID: " + userId);
        if (userId == null || userId.isEmpty()) {
            postError("User ID is required to fetch profile details");
            return;
        }
        _isLoading.postValue(true);
        clearError();
        _profileDetails.postValue(null); // Clear previous profile before fetching
        executorService.execute(() -> {
            String accessToken = tokenManager.getAccessToken();
            if (accessToken == null) {
                postError("User not authenticated");
                _isLoading.postValue(false);
                return;
            }
            String authHeader = "Bearer " + accessToken;
            String selectQuery = "*"; // Select all columns for the profile
            String filter = "id=eq." + userId; // Filter by user ID

            Call<List<Profile>> call = apiService.getProfile(authHeader, selectQuery, filter, "limit=1"); // Limit to 1 result
            call.enqueue(new Callback<List<Profile>>() {
                @Override
                public void onResponse(@NonNull Call<List<Profile>> call, @NonNull Response<List<Profile>> response) {
                    mainThreadHandler.post(() -> {
                        _isLoading.setValue(false);
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            _profileDetails.setValue(response.body().get(0)); // Update profile LiveData
                            Log.d(TAG, "Fetched profile details successfully for user: " + userId);
                        } else if (response.isSuccessful() && (response.body() == null || response.body().isEmpty())) {
                            Log.w(TAG, "Profile not found for user: " + userId);
                            postError("Profile not found"); // Specific error for not found
                            _profileDetails.setValue(null); // Ensure LiveData is null
                        } else {
                            handleApiError("Failed to fetch profile", response);
                            _profileDetails.setValue(null); // Ensure LiveData is null on error
                        }
                    });
                }
                @Override
                public void onFailure(@NonNull Call<List<Profile>> call, @NonNull Throwable t) {
                    handleNetworkError("Network error fetching profile", t);
                    _profileDetails.postValue(null); // Ensure LiveData is null on network error
                }
            });
        });
    }

    // --- Helper Methods ---

    private void handleApiError(String messagePrefix, Response<?> response) {
        String errorMsg = messagePrefix + ": " + response.code();
        String errorBody = getErrorBodyString(response);
        if (!errorBody.isEmpty()) {
            errorMsg += " - " + errorBody;
        }
        postError(errorMsg);
        Log.e(TAG, errorMsg);
    }

    private void handleNetworkError(String messagePrefix, Throwable t) {
        String errorMsg = messagePrefix + ": " + t.getMessage();
        postError(errorMsg);
        Log.e(TAG, messagePrefix, t);
        mainThreadHandler.post(() -> _isLoading.setValue(false)); // Ensure loading stops on network error
    }

    private String getErrorBodyString(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                return response.errorBody().string();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing error body", e);
        }
        return "";
    }

    private void postError(String errorMessage) {
        mainThreadHandler.post(() -> _error.setValue(errorMessage));
    }

    // Call this before starting a new network request
    public void clearError() {
        if (_error.getValue() != null) {
            mainThreadHandler.post(() -> _error.setValue(null));
        }
    }

    // Call this to clear upload success state after handling it
    public void clearUploadSuccess() {
        if (_uploadSuccessUrl.getValue() != null) {
            mainThreadHandler.post(() -> _uploadSuccessUrl.setValue(null));
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
        Log.d(TAG, "VideoViewModel cleared and executor shut down.");
    }

    // --- ViewModel Factory ---
    public static class VideoViewModelFactory implements ViewModelProvider.Factory {
        private final Application application;

        public VideoViewModelFactory(Application application) {
            this.application = application;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(VideoViewModel.class)) {
                //noinspection unchecked
                return (T) new VideoViewModel(application);
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}