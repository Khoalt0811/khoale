package com.khoalt0811.javavideoapp.activities; // Thay package name nếu khác

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils; // Import TextUtils
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer; // Import Observer
import androidx.lifecycle.ViewModelProvider;

// Import lớp Binding được tạo tự động
import com.khoalt0811.javavideoapp.databinding.ActivityAuthBinding;
import com.khoalt0811.javavideoapp.data.models.AuthResponse; // Import model
import com.khoalt0811.javavideoapp.viewmodels.AuthViewModel;

public class AuthActivity extends AppCompatActivity {

    private ActivityAuthBinding binding; // Sử dụng ViewBinding
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate layout bằng ViewBinding
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Lấy ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // --- Observe LiveData từ ViewModel ---
        authViewModel.isLoading.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                // Hiển thị/ẩn ProgressBar và bật/tắt nút
                binding.progressBarAuth.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                binding.buttonLogin.setEnabled(!isLoading);
                binding.buttonSignup.setEnabled(!isLoading);
            }
        });

        authViewModel.error.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String error) {
                // Hiển thị lỗi bằng Toast
                if (error != null && !error.isEmpty()) {
                    Toast.makeText(AuthActivity.this, error, Toast.LENGTH_LONG).show();
                    // Có thể reset error trong ViewModel sau khi hiển thị
                    // authViewModel.clearError(); // Cần thêm hàm này trong ViewModel
                }
            }
        });

        authViewModel.authResult.observe(this, new Observer<AuthResponse>() {
            @Override
            public void onChanged(AuthResponse authResponse) {
                // Xử lý khi đăng nhập/đăng ký thành công (có access token)
                if (authResponse != null && authResponse.getAccessToken() != null) {
                    Toast.makeText(AuthActivity.this, "Authentication Successful!", Toast.LENGTH_SHORT).show();
                    // Chuyển sang MainActivity
                    goToMainActivity();
                }
                // Không cần xử lý gì nếu authResponse là null (ví dụ khi logout)
            }
        });

        // --- Set OnClickListener cho các button ---
        binding.buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

        binding.buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSignup();
            }
        });
    }

    // Hàm xử lý logic khi nhấn nút Login
    private void handleLogin() {
        String email = binding.editTextEmail.getText().toString().trim();
        String password = binding.editTextPassword.getText().toString().trim();

        // Kiểm tra đầu vào cơ bản
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextEmail.setError("Enter a valid email");
            binding.editTextEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) { // Supabase yêu cầu ít nhất 6 ký tự
            binding.editTextPassword.setError("Password must be at least 6 characters");
            binding.editTextPassword.requestFocus();
            return;
        }

        // Gọi hàm login trong ViewModel
        authViewModel.loginUser(email, password);
    }

    // Hàm xử lý logic khi nhấn nút Signup
    private void handleSignup() {
        String email = binding.editTextEmail.getText().toString().trim();
        String password = binding.editTextPassword.getText().toString().trim();

        // Kiểm tra đầu vào cơ bản
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextEmail.setError("Enter a valid email");
            binding.editTextEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            binding.editTextPassword.setError("Password must be at least 6 characters");
            binding.editTextPassword.requestFocus();
            return;
        }

        // Gọi hàm signup trong ViewModel
        authViewModel.signupUser(email, password);
    }

    // Hàm chuyển sang MainActivity
    private void goToMainActivity() {
        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
        // Xóa tất cả activity trước đó khỏi stack để người dùng không quay lại màn hình login/signup
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Đóng AuthActivity
    }
}