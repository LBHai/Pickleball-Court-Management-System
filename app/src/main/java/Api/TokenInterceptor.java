package Api;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import Session.SessionManager;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;

import Activity.LoginActivity;

public class TokenInterceptor implements Interceptor {
    private final Context context;
    private final SessionManager sessionManager;

    public TokenInterceptor(Context context) {
        this.context = context.getApplicationContext();
        this.sessionManager = new SessionManager(context);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        String url = originalRequest.url().toString();

        // Bỏ qua xử lý token cho API đăng nhập (kiểm tra chính xác endpoint)
        if (url.endsWith("identity/auth/token")) {
            return chain.proceed(originalRequest); // Không thêm token, không xử lý 401/403
        }

        // Thêm token vào header nếu có
        String token = sessionManager.getToken();
        Request.Builder requestBuilder = originalRequest.newBuilder();
        if (token != null && !token.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        // Thực hiện request
        Response response = chain.proceed(requestBuilder.build());

        // Xử lý mã lỗi 401/403 (token hết hạn hoặc không hợp lệ)
        if (response.code() == 401 || response.code() == 403) {
            // Đọc toàn bộ body để kiểm tra nội dung lỗi
            try {
                ResponseBody responseBody = response.peekBody(Long.MAX_VALUE); // Đọc toàn bộ body
                String responseString = responseBody.string();

                // Nếu server thông báo token hết hạn
                if (responseString.contains("expired") || responseString.contains("hết hạn")) {
                    handleSessionExpired();

                    // Trả về response giả để tránh crash ứng dụng
                    return new Response.Builder()
                            .request(originalRequest)
                            .protocol(Protocol.HTTP_1_1)
                            .code(200)
                            .message("Session expired handled")
                            .body(ResponseBody.create("{}", MediaType.parse("application/json")))
                            .build();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Xử lý các trường hợp 401/403 khác (đăng xuất người dùng)
            handleSessionExpired();
            return new Response.Builder()
                    .request(originalRequest)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("Unauthorized handled")
                    .body(ResponseBody.create("{}", MediaType.parse("application/json")))
                    .build();
        }

        return response;
    }

    // Hàm xử lý hết hạn token: Xoá session, báo Toast, chuyển màn hình
    private void handleSessionExpired() {
        sessionManager.clearSession();

        // Phải post lên main thread để hiển thị Toast và startActivity
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(context, "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(context, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        });
    }
}
