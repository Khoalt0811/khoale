package com.khoalt0811.javavideoapp.viewmodels; // Thay package name nếu khác

import android.app.Application; // Cần Application context để truy cập SharedPreferences
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel; // Dùng AndroidViewModel nếu cần context
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

// Import các lớp cần thiết
import com.khoalt0811.javavideoapp.data.models.AuthRequest;
import com.khoalt0811.javavideoapp.data.models.AuthResponse;
import com.khoalt0811.javavideoapp.networking.RetrofitClient;
import com.khoalt0811.javavideoapp.networking.SupabaseApiService;
import com.khoalt0811.javavideoapp.utils.TokenManager; // Import lớp quản lý token của bạn

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthViewModel extends AndroidViewModel {

    private static final String TAG = "AuthViewModel";

    private final SupabaseApiService apiService;
    private final ExecutorService executorService; // Để chạy network trên background thread
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper()); // Để post kết quả về main thread
    private final TokenManager tokenManager; // Để lưu/lấy token

    // LiveData cho trạng thái loading
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    // LiveData cho kết quả xác thực thành công
    private final MutableLiveData<AuthResponse> _authResult = new MutableLiveData<>();
    public LiveData<AuthResponse> authResult = _authResult;

    // LiveData cho thông báo lỗi
    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;


    public AuthViewModel(@NonNull Application application) {
        super(application);
        apiService = RetrofitClient.getApiService();
        executorService = Executors.newSingleThreadExecutor(); // Một luồng cho các tác vụ auth
        tokenManager = TokenManager.getInstance(application); // Lấy instance TokenManager
    }

    // Hàm xử lý đăng nhập
    public void loginUser(String email, String password) {
        _isLoading.postValue(true); // Bắt đầu loading (an toàn từ mọi luồng)
        _error.postValue(null);    // Xóa lỗi cũ
        executorService.execute(() -> { // Chạy trên background thread
            AuthRequest request = new AuthRequest(email, password);
            Call<AuthResponse> call = apiService.login(request);

            // Dùng enqueue của Retrofit để xử lý callback bất đồng bộ
            call.enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                    mainThreadHandler.post(() -> { // Đảm bảo cập nhật LiveData trên Main thread
                        _isLoading.setValue(false); // Kết thúc loading (phải trên Main thread)
                        if (response.isSuccessful() && response.body() != null) {
                            Log.d(TAG, "Login successful");
                            AuthResponse authData = response.body();
                            _authResult.setValue(authData); // Thông báo thành công
                            // Lưu token và user ID sau khi đăng nhập thành công
                            if (authData.getAccessToken() != null && authData.getRefreshToken() != null && authData.getUser() != null) {
                                tokenManager.saveTokens(authData.getAccessToken(), authData.getRefreshToken());
                                tokenManager.saveUserId(authData.getUser().getId()); // Lưu User ID
                            }
                        } else {
                            String errorMsg = "Login failed: " + response.code();
                            try {
                                if (response.errorBody() != null) {
                                    errorMsg += " - " + response.errorBody().string();
                                }
                            } catch (Exception e) { Log.e(TAG, "Error parsing error body", e); }
                            Log.e(TAG, errorMsg);
                            _error.setValue(errorMsg); // Thông báo lỗi
                        }
                    });
                }

                @Override
                public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                    mainThreadHandler.post(() -> {
                        _isLoading.setValue(false); // Kết thúc loading
                        Log.e(TAG, "Login network error: " + t.getMessage(), t);
                        _error.setValue("Network error: " + t.getMessage()); // Thông báo lỗi mạng
                    });
                }
            });
        });
    }

    // Hàm xử lý đăng ký
    public void signupUser(String email, String password) {
        _isLoading.postValue(true);
        _error.postValue(null);
        executorService.execute(() -> {
            AuthRequest request = new AuthRequest(email, password);
            Call<AuthResponse> call = apiService.signup(request); // Gọi endpoint signup

            call.enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                    mainThreadHandler.post(() -> {
                        _isLoading.setValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            Log.d(TAG, "Signup successful (check response body details)");
                            AuthResponse signupData = response.body();

                            // Kiểm tra xem có cần xác thực email không
                            if (signupData.getUser() != null && signupData.getAccessToken() == null) {
                                // Signup thành công nhưng cần confirm email
                                _error.setValue("Signup successful! Please check your email to confirm.");
                                // Không lưu token, không set _authResult
                            } else if (signupData.getAccessToken() != null) {
                                // Signup thành công và trả về token luôn
                                _authResult.setValue(signupData);
                                if (signupData.getRefreshToken() != null && signupData.getUser() != null) {
                                    tokenManager.saveTokens(signupData.getAccessToken(), signupData.getRefreshToken());
                                    tokenManager.saveUserId(signupData.getUser().getId()); // Lưu User ID
                                }
                            } else {
                                // Trường hợp không mong muốn
                                _error.setValue("Signup response is unclear.");
                            }

                        } else {
                            String errorMsg = "Signup failed: " + response.code();
                            try {
                                if (response.errorBody() != null) {
                                    errorMsg += " - " + response.errorBody().string();
                                }
                            } catch (Exception e) { Log.e(TAG, "Error parsing error body", e); }
                            Log.e(TAG, errorMsg);
                            _error.setValue(errorMsg);
                        }
                    });
                }

                @Override
                public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                    mainThreadHandler.post(() -> {
                        _isLoading.setValue(false);
                        Log.e(TAG, "Signup network error: " + t.getMessage(), t);
                        _error.setValue("Network error: " + t.getMessage());
                    });
                }
            });
        });
    }

    // Hàm đăng xuất (xóa token đã lưu)
    public void logoutUser() {
        tokenManager.clearTokens();
        // Có thể gọi API /auth/v1/logout của Supabase nếu cần thiết (yêu cầu access token)
        Log.d(TAG, "User tokens cleared (logged out locally).");
        // Thông báo cho UI biết đã đăng xuất (ví dụ: set _authResult về null hoặc dùng LiveData riêng)
        _authResult.postValue(null); // Đặt kết quả về null để MainActivity có thể kiểm tra
    }

    // Kiểm tra xem người dùng đã đăng nhập chưa (dựa vào token)
    public boolean isLoggedIn() {
        return tokenManager.getAccessToken() != null;
    }

    // Lấy User ID đã lưu
    public String getCurrentUserId() {
        return tokenManager.getUserId(); // Giả sử TokenManager có hàm này
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown(); // Dọn dẹp ExecutorService khi ViewModel bị hủy
    }
}