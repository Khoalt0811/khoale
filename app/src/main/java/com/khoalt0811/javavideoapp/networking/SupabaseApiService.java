package com.khoalt0811.javavideoapp.networking; // Thay package name nếu khác

// Import các model bạn sẽ tạo trong package data.models
import com.khoalt0811.javavideoapp.data.models.AuthRequest;
import com.khoalt0811.javavideoapp.data.models.AuthResponse;
import com.khoalt0811.javavideoapp.data.models.Profile;
import com.khoalt0811.javavideoapp.data.models.SignInRequest;
import com.khoalt0811.javavideoapp.data.models.SignUpRequest;
import com.khoalt0811.javavideoapp.data.models.VideoMetadata;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface SupabaseApiService {
    // Đăng ký user mới
    @POST("auth/v1/signup")
    Call<AuthResponse> signUp(@Body SignUpRequest request);

    // Đăng nhập
    @POST("auth/v1/token?grant_type=password")
    Call<AuthResponse> signIn(@Body SignInRequest request);

    // Lấy thông tin user hiện tại
    @GET("auth/v1/user")
    Call<AuthResponse> getUser(@Header("Authorization") String authHeader);

    // Đăng xuất
    @POST("auth/v1/logout")
    Call<Void> signOut(@Header("Authorization") String authHeader);

    // --- AUTH ---
    // Endpoint đăng nhập bằng email/password
    // Kiểm tra tài liệu Supabase cho URL và cấu trúc response mới nhất
    @Headers({"Accept: application/json"})
    @POST("/auth/v1/token?grant_type=password")
    Call<AuthResponse> login(@Body AuthRequest authRequest);

    // Endpoint đăng ký
    // Kiểm tra tài liệu Supabase cho URL và cấu trúc response mới nhất
    @Headers({"Accept: application/json"})
    @POST("/auth/v1/signup")
    Call<AuthResponse> signup(@Body AuthRequest authRequest); // Response có thể khác AuthResponse khi login

    // --- DATABASE (PostgREST) ---
    // Lấy danh sách video (tên bảng 'videos')
    @GET("/rest/v1/videos")
    Call<List<VideoMetadata>> getVideos(
            @Header("Authorization") String authToken, // "Bearer ACCESS_TOKEN"
            @Query("select") String select, // Ví dụ: "*" hoặc "id,title,profiles(*)"
            @QueryMap Map<String, String> options // Cho limit, order, filter... Ví dụ: Map.of("order", "created_at.desc")
    );

    // Lấy thông tin profile (tên bảng 'profiles')
    @GET("/rest/v1/profiles")
    Call<List<Profile>> getProfile(
            @Header("Authorization") String authToken, // "Bearer ACCESS_TOKEN"
            @Query("select") String select, // Ví dụ: "*"
            @Query("id") String userIdFilter, // Ví dụ: "eq." + userId (equals user ID)
            String s);

    // Thêm video metadata vào bảng 'videos'
    @POST("/rest/v1/videos")
    Call<Void> insertVideo( // Hoặc Call<List<VideoMetadata>> nếu dùng Prefer header
                            @Header("Authorization") String authToken, // "Bearer ACCESS_TOKEN"
                            @Header("Prefer") String prefer, // Ví dụ: "return=representation" hoặc "return=minimal"
                            @Body VideoMetadata videoData // Dữ liệu video cần insert
    );

    // Gọi RPC function để tăng like
    @POST("/rest/v1/rpc/increment_likes") // Tên function RPC bạn tạo
    Call<Void> incrementLikes(
            @Header("Authorization") String authToken, // "Bearer ACCESS_TOKEN"
            @Body Map<String, Long> params // Ví dụ: Map.of("video_id_input", videoId)
    );

    // Gọi RPC function để tăng dislike
    @POST("/rest/v1/rpc/increment_dislikes") // Tên function RPC bạn tạo
    Call<Void> incrementDislikes(
            @Header("Authorization") String authToken, // "Bearer ACCESS_TOKEN"
            @Body Map<String, Long> params // Ví dụ: Map.of("video_id_input", videoId)
    );


    // --- STORAGE ---
    // Upload file video
    @Multipart // Đánh dấu đây là request multipart
    @POST("/storage/v1/object/{bucketName}/{filePath}") // Endpoint upload storage
    Call<Void> uploadVideo( // Kiểm tra response thực tế từ Supabase Storage API
                            @Header("Authorization") String authToken, // "Bearer ACCESS_TOKEN"
                            @Path("bucketName") String bucketName, // Tên bucket, ví dụ: "videos"
                            @Path(value = "filePath", encoded = true) String filePath, // Đường dẫn file trên storage, ví dụ: "userId/filename.mp4"
                            @Part MultipartBody.Part file // Dữ liệu file dưới dạng MultipartBody.Part
    );

    // Lưu ý: Lấy Public URL của file trên Storage thường không cần gọi API nếu bucket là public.
    // URL có dạng: BASE_URL + "/storage/v1/object/public/" + bucketName + "/" + filePath;
}