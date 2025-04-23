package com.khoalt0811.javavideoapp.activities; // Thay package name nếu khác

import android.os.Bundle;
import android.view.View; // Import View
import android.webkit.WebChromeClient; // Import WebChromeClient để xử lý progress
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import com.khoalt0811.javavideoapp.databinding.ActivityWebviewBinding; // Đảm bảo tên layout là activity_webview.xml

public class WebViewActivity extends AppCompatActivity {

    private ActivityWebviewBinding binding; // ViewBinding

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate layout
        binding = ActivityWebviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Lấy URL từ Intent (nếu có)
        String url = getIntent().getStringExtra("URL_TO_LOAD");
        if (url == null || url.isEmpty()) {
            url = "https://google.com"; // URL mặc định nếu không có Intent extra
        }

        // --- Cấu hình WebView ---
        // Bật JavaScript
        binding.webView.getSettings().setJavaScriptEnabled(true);

        // Mở link trong app thay vì trình duyệt ngoài
        binding.webView.setWebViewClient(new WebViewClient());

        // Hiển thị thanh progress khi load trang (tùy chọn)
        binding.webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100 && binding.progressBarWebview.getVisibility() == View.GONE) { // Giả sử có ProgressBar tên progressBarWebview
                    binding.progressBarWebview.setVisibility(View.VISIBLE);
                }
                binding.progressBarWebview.setProgress(newProgress);
                if (newProgress == 100) {
                    binding.progressBarWebview.setVisibility(View.GONE);
                }
                super.onProgressChanged(view, newProgress);
            }
        });

        // Cài đặt cho Responsive (thường hữu ích)
        binding.webView.getSettings().setUseWideViewPort(true);
        binding.webView.getSettings().setLoadWithOverviewMode(true);
        // binding.webView.getSettings().setSupportZoom(true); // Bật nếu muốn zoom
        // binding.webView.getSettings().setBuiltInZoomControls(true); // Bật nếu muốn zoom
        // binding.webView.getSettings().setDisplayZoomControls(false); // Ẩn nút zoom +/-

        // --- Load URL ---
        binding.webView.loadUrl(url);
    }

    // Xử lý nút back để quay lại trang trước trong WebView
    @Override
    public void onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack(); // Quay lại trang trước trong WebView
        } else {
            super.onBackPressed(); // Thoát Activity nếu không còn trang nào để quay lại
        }
    }
}