package com.khoalt0811.javavideoapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.khoalt0811.javavideoapp.R;
import com.khoalt0811.javavideoapp.viewmodels.AuthViewModel;

public class AuthActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonSignUp;
    private Button buttonSignIn;
    private Button buttonSwitchMode;
    private TextView textViewTitle;
    private ProgressBar progressBar;
    private AuthViewModel authViewModel;
    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // Khởi tạo ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Ánh xạ View
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSignIn = findViewById(R.id.buttonSignIn);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        buttonSwitchMode = findViewById(R.id.buttonSwitchMode);
        textViewTitle = findViewById(R.id.textViewTitle);
        progressBar = findViewById(R.id.progressBar);

        // Thiết lập giao diện ban đầu
        updateUI();

        // Xử lý sự kiện chuyển đổi mode
        buttonSwitchMode.setOnClickListener(v -> {
            isLoginMode = !isLoginMode;
            updateUI();
        });

        // Xử lý nút đăng nhập
        buttonSignIn.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (validateInput(email, password)) {
                progressBar.setVisibility(View.VISIBLE);
                authViewModel.signInUser(email, password); // Sửa từ loginUser thành signInUser
            }
        });

        // Xử lý nút đăng ký
        buttonSignUp.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (validateInput(email, password)) {
                progressBar.setVisibility(View.VISIBLE);
                authViewModel.signUpUser(email, password); // Sửa từ signupUser thành signUpUser
            }
        });

        // Quan sát kết quả đăng nhập
        authViewModel.getSignInSuccess().observe(this, isSuccess -> {
            progressBar.setVisibility(View.GONE);

            if (isSuccess) {
                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                navigateToMainActivity();
            }
        });

        // Quan sát kết quả đăng ký
        authViewModel.getSignUpSuccess().observe(this, isSuccess -> {
            progressBar.setVisibility(View.GONE);

            if (isSuccess) {
                Toast.makeText(this, "Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.", Toast.LENGTH_LONG).show();
                // Nếu muốn chuyển sang chế độ đăng nhập sau khi đăng ký
                isLoginMode = true;
                updateUI();
            }
        });

        // Quan sát thông báo lỗi
        authViewModel.getErrorMessage().observe(this, errorMsg -> {
            progressBar.setVisibility(View.GONE);

            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Kiểm tra người dùng đã đăng nhập khi mở ứng dụng
    @Override
    protected void onStart() {
        super.onStart();
        checkUserLoggedIn();
    }

    private void checkUserLoggedIn() {
        progressBar.setVisibility(View.VISIBLE);
        authViewModel.checkUserSession();
    }

    // Phương thức cập nhật giao diện dựa vào mode hiện tại
    private void updateUI() {
        if (isLoginMode) {
            // Chế độ đăng nhập
            textViewTitle.setText("Đăng nhập");
            buttonSignIn.setVisibility(View.VISIBLE);
            buttonSignUp.setVisibility(View.GONE);
            buttonSwitchMode.setText("Chưa có tài khoản? Đăng ký ngay");
        } else {
            // Chế độ đăng ký
            textViewTitle.setText("Đăng ký");
            buttonSignIn.setVisibility(View.GONE);
            buttonSignUp.setVisibility(View.VISIBLE);
            buttonSwitchMode.setText("Đã có tài khoản? Đăng nhập ngay");
        }
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Kiểm tra định dạng email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Kiểm tra độ dài mật khẩu (tối thiểu 6 ký tự)
        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Đóng AuthActivity
    }
}