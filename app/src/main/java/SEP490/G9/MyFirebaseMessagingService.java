package SEP490.G9;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import Api.ApiService;
import Api.RetrofitClient;
import Model.NotificationRequest;
import Session.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "my_channel_id";
    private static final String CHANNEL_NAME = "Thông báo chung";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d("FCM", "New token: " + token);

        // Lấy key từ Session (userId hoặc số điện thoại nếu Guest)
        SessionManager session = new SessionManager(getApplicationContext());
        String key = session.getUserId();

        if (key != null && !key.isEmpty()) {
            // Gửi token thực sự từ Firebase lên server cùng với key
            sendTokenToServer(key, token);
        } else {
            Log.e("FCM", "Không tìm thấy key để cập nhật token");
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d("FCM", "Message received: " + remoteMessage.toString());
        if (remoteMessage.getNotification() != null) {
            showNotification(
                    remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody()
            );
        } else {
            Log.d("FCM", "Chỉ nhận data payload: " + remoteMessage.getData().toString());
        }
    }

    private void sendTokenToServer(String key, String token) {
        ApiService api = RetrofitClient.getApiService(getApplicationContext());
        NotificationRequest request = new NotificationRequest(key, token);
        api.registerNotification(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("Notify", "Đăng ký FCM token thành công với key: " + key);
                } else {
                    Log.e("Notify", "Đăng ký token thất bại: " + response.code() + " - " +
                            (response.errorBody() != null ? response.errorBody().toString() : "No error body"));
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("FCM", "Lỗi kết nối khi cập nhật token", t);
            }
        });
    }

    private void showNotification(String title, String body) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Kênh thông báo của ứng dụng");
            channel.enableLights(true);
            channel.setLightColor(Color.GREEN);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title != null ? title : "Thông báo mới")
                .setContentText(body != null ? body : "Bạn có thông báo mới")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}