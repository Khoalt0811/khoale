package com.khoalt0811.javavideoapp.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.khoalt0811.javavideoapp.data.models.AuthResponse;
import com.khoalt0811.javavideoapp.data.models.SignInRequest;
import com.khoalt0811.javavideoapp.data.models.SignUpRequest;
import com.khoalt0811.javavideoapp.networking.RetrofitClient;
import com.khoalt0811.javavideoapp.networking.SupabaseApiService;
import com.khoalt0811.javavideoapp.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthViewModel extends AndroidViewModel {
    private static final String TAG = "AuthViewModel";

    private final SupabaseApiService apiService;
    private final TokenManager tokenManager;

    // LiveData
    private final MutableLiveData<Boolean> _signUpSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _signInSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();

    public LiveData<Boolean> getSignUpSuccess() { return _signUpSuccess; }
    public LiveData<Boolean> getSignInSuccess() { return _signInSuccess; }
    public LiveData<String> getErrorMessage() { return _errorMessage; }

    public AuthViewModel(@NonNull Application application) {
        super(application);
        apiService = RetrofitClient.getApiService();
        tokenManager = TokenManager.getInstance(application);
    }

    public void signUpUser(String email, String password) {
        SignUpRequest request = new SignUpRequest(email, password);

        apiService.signUp(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();

                    // Lưu token nếu có
                    if (authResponse.getAccessToken() != null) {
                        tokenManager.saveToken(authResponse.getAccessToken());
                    }
                    if (authResponse.getRefreshToken() != null) {
                        tokenManager.saveRefreshToken(authResponse.getRefreshToken());
                    }

                    _signUpSuccess.setValue(true);
                    _errorMessage.setValue(null);

                    Log.d(TAG, "Đăng ký thành công!");
                } else {
                    // Xử lý lỗi response
                    String errorMsg = "Đăng ký thất bại: ";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += response.errorBody().string();
                        } else {
                            errorMsg += "Lỗi không xác định";
                        }
                    } catch (Exception e) {
                        errorMsg += "Lỗi không xác định";
                    }

                    _signUpSuccess.setValue(false);
                    _errorMessage.setValue(errorMsg);

                    Log.e(TAG, errorMsg);
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                String errorMsg = "Lỗi kết nối: " + t.getMessage();
                _signUpSuccess.setValue(false);
                _errorMessage.setValue(errorMsg);

                Log.e(TAG, "Đăng ký thất bại", t);
            }
        });
    }

    public void signInUser(String email, String password) {
        SignInRequest request = new SignInRequest(email, password);

        apiService.signIn(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();

                    // Lưu token
                    if (authResponse.getAccessToken() != null) {
                        tokenManager.saveToken(authResponse.getAccessToken());
                    }
                    if (authResponse.getRefreshToken() != null) {
                        tokenManager.saveRefreshToken(authResponse.getRefreshToken());
                    }

                    _signInSuccess.setValue(true);
                    _errorMessage.setValue(null);

                    Log.d(TAG, "Đăng nhập thành công!");
                } else {
                    // Xử lý lỗi
                    String errorMsg = "Đăng nhập thất bại: ";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += response.errorBody().string();
                        } else {
                            errorMsg += "Thông tin đăng nhập không đúng";
                        }
                    } catch (Exception e) {
                        errorMsg += "Thông tin đăng nhập không đúng";
                    }

                    _signInSuccess.setValue(false);
                    _errorMessage.setValue(errorMsg);

                    Log.e(TAG, errorMsg);
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                String errorMsg = "Lỗi kết nối: " + t.getMessage();
                _signInSuccess.setValue(false);
                _errorMessage.setValue(errorMsg);

                Log.e(TAG, "Đăng nhập thất bại", t);
            }
        });
    }

    public void checkUserSession() {
        if (!tokenManager.hasAccessToken()) {
            _signInSuccess.setValue(false);
            return;
        }

        String authHeader = tokenManager.getAuthorizationHeader();

        apiService.getUser(authHeader).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                boolean isLoggedIn = response.isSuccessful() && response.body() != null && response.body().getUser() != null;
                _signInSuccess.setValue(isLoggedIn);

                if (!isLoggedIn && response.code() == 401) {
                    // Token hết hạn hoặc không hợp lệ
                    tokenManager.clearTokens();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                _signInSuccess.setValue(false);
                Log.e(TAG, "Kiểm tra session thất bại", t);
            }
        });
    }

    public void signOut() {
        if (!tokenManager.hasAccessToken()) {
            _signInSuccess.setValue(false);
            return;
        }

        String authHeader = tokenManager.getAuthorizationHeader();

        apiService.signOut(authHeader).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // Xóa token dù API thành công hay thất bại
                tokenManager.clearTokens();
                _signInSuccess.setValue(false);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Vẫn xóa token
                tokenManager.clearTokens();
                _signInSuccess.setValue(false);

                _errorMessage.setValue("Có lỗi khi đăng xuất: " + t.getMessage());
                Log.e(TAG, "Đăng xuất thất bại", t);
            }
        });
    }
}