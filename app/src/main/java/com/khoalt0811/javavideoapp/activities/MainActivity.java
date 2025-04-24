package com.khoalt0811.javavideoapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.khoalt0811.javavideoapp.R;
import com.khoalt0811.javavideoapp.viewmodels.AuthViewModel;

public class MainActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Kiểm tra người dùng đã đăng nhập chưa
        checkUserLoggedIn();

        // Thiết lập observers
        setupObservers();
    }

    private void checkUserLoggedIn() {
        // Gọi phương thức checkUserSession thay vì isLoggedIn
        authViewModel.checkUserSession();
    }

    private void setupObservers() {
        // Quan sát trạng thái đăng nhập
        authViewModel.getSignInSuccess().observe(this, isLoggedIn -> {
            if (!isLoggedIn) {
                // Nếu chưa đăng nhập, chuyển đến trang đăng nhập
                navigateToAuthActivity();
            }
        });

        // Quan sát thông báo lỗi nếu có
        authViewModel.getErrorMessage().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Tạo menu Options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    // Xử lý sự kiện menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            signOut();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        authViewModel.signOut();
        // Kết quả đăng xuất sẽ được xử lý trong observer getSignInSuccess
    }

    private void navigateToAuthActivity() {
        Intent intent = new Intent(MainActivity.this, AuthActivity.class);
        startActivity(intent);
        finish(); // Đóng MainActivity
    }
}