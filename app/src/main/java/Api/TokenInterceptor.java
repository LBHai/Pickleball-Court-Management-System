package Api;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import Session.SessionManager;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.Protocol;

import java.io.IOException;

import SEP490.G9.LoginActivity;

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
        String token = sessionManager.getToken();
        Request request;

        // Thêm token vào header nếu tồn tại
        if (token != null && !token.isEmpty()) {
            request = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
        } else {
            request = originalRequest;
        }

        Response response = chain.proceed(request);

        // Xử lý mọi trường hợp 401/403
        if (response.code() == 401 || response.code() == 403) {
            handleSessionExpired();

            // Trả về response mới để ngăn lỗi hiển thị
            return new Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("Session expired handled")
                    .body(ResponseBody.create("", MediaType.parse("application/json")))
                    .build();
        }

        return response;
    }

    private void handleSessionExpired() {
        sessionManager.clearSession();
        new Handler(Looper.getMainLooper()).post(() -> {
            // Hiển thị thông báo và chuyển hướng
            Toast.makeText(
                    context,
                    "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!",
                    Toast.LENGTH_LONG
            ).show();

            Intent intent = new Intent(context, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        });
    }
}