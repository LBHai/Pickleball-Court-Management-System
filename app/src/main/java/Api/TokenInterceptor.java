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
import okhttp3.ResponseBody;

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
        if (token != null && !token.isEmpty()) {
            request = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
        } else {
            request = originalRequest;
        }
        Response response = chain.proceed(request);
        if (response.code() == 401 || response.code() == 403) {
            ResponseBody peekedBody = response.peekBody(1024);
            String responseString = peekedBody.string();

            if (responseString.contains("hết hạn") || responseString.contains("expired")) {
                handleSessionExpired();
            }
        }
        return response;
    }

    private void handleSessionExpired() {
        sessionManager.clearSession();
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(context,
                    "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!",
                    Toast.LENGTH_LONG).show();
            Intent intent = new Intent(context, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        });
    }
}
