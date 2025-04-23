package com.khoalt0811.javavideoapp.networking; // Thay package name nếu khác

// Import TokenManager nếu bạn đã tạo
// import com.khoalt0811.javavideoapp.utils.TokenManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // !!! THAY BẰNG URL PROJECT SUPABASE CỦA BẠN !!!
    private static final String PROJECT_ID = "mvmwjkbomrmksqavtup";

    private static final String BASE_URL = "https://" + PROJECT_ID + ".supabase.co/";    // !!! ------------------------------------- !!!

    // !!! THAY BẰNG ANON KEY SUPABASE CỦA BẠN !!!
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im12bXdqa2J1b21ybWtzcWF2dHVwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDQ4Njg1OTcsImV4cCI6MjA2MDQ0NDU5N30.3PtRxUP-ZBVj8yyN-ch6-rEpAuWO3ueUTm6U-4izgSg";
    // !!! ------------------------------------- !!!


    private static Retrofit retrofit = null;
    private static SupabaseApiService apiService = null;

    private static Retrofit getClient() {
        if (retrofit == null) {
            // Interceptor để thêm header mặc định (apikey)
            Interceptor headerInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("apikey", SUPABASE_ANON_KEY); // Luôn gửi anon key

                    // Lưu ý: Việc thêm Authorization header ở đây không lý tưởng nếu không có Context
                    // để lấy token. Nên thêm vào từng request cần thiết trong ApiService hoặc dùng Authenticator.
                    // String accessToken = TokenManager.getInstance(null).getAccessToken(); // Cần Context
                    // if (accessToken != null) {
                    //     requestBuilder.header("Authorization", "Bearer " + accessToken);
                    // }

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            };

            // Logging Interceptor (để xem request/response trong Logcat)
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            // Đặt Level.BODY chỉ khi debug, có thể ảnh hưởng performance
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(headerInterceptor)
                    .addInterceptor(logging) // Thêm logging
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client) // Sử dụng OkHttpClient đã cấu hình
                    .addConverterFactory(GsonConverterFactory.create()) // Dùng Gson để parse JSON
                    .build();
        }
        return retrofit;
    }

    // Phương thức để lấy instance của ApiService
    public static SupabaseApiService getApiService() {
        if (apiService == null) {
            apiService = getClient().create(SupabaseApiService.class);
        }
        return apiService;
    }
}