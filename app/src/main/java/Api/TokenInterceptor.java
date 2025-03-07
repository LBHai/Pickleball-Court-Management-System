package Api;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import Session.SessionManager;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

import SEP490.G9.LoginActivity;

public class TokenInterceptor implements Interceptor {
    private final Context context;
    private final SessionManager sessionManager;

    public TokenInterceptor(Context context) {
        // Sử dụng Application Context để tránh leak bộ nhớ
        this.context = context.getApplicationContext();
        this.sessionManager = new SessionManager(context);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        // Lấy request gốc
        Request originalRequest = chain.request();

        // Lấy token từ session
        String token = sessionManager.getToken();
        Request request;
        if (token != null && !token.isEmpty()) {
            // Thêm header Authorization vào request
            request = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
        } else {
            request = originalRequest;
        }

        // Thực thi request và lấy response
        Response response = chain.proceed(request);

        // Nếu response trả về 401 hoặc 403 => token hết hạn hoặc tài khoản đăng nhập ở nơi khác
        if (response.code() == 401 || response.code() == 403) {
            handleSessionExpired();
        }

        return response;
    }

    /**
     * Xử lý khi token hết hạn hoặc bị đăng nhập ở thiết bị khác:
     * - Xóa session
     * - Thông báo cho người dùng
     * - Chuyển về LoginActivity
     */
    private void handleSessionExpired() {
        // Xóa token trong session
        sessionManager.clearSession();

        // Chạy trên luồng chính (UI thread)
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(context,
                    "Phiên đăng nhập đã hết hạn hoặc tài khoản đăng nhập ở nơi khác. Vui lòng đăng nhập lại!",
                    Toast.LENGTH_LONG).show();

            // Chuyển về LoginActivity
            Intent intent = new Intent(context, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        });
    }
}
