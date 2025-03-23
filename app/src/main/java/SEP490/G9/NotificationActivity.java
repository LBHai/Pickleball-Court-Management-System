package SEP490.G9;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import Adapter.NotificationAdapter;
import Api.ApiService;
import Api.RetrofitClient;
import Model.Notification;
import Model.NotificationResponse;
import Session.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActivity extends AppCompatActivity implements NotificationAdapter.OnNotificationItemClickListener {

    private RecyclerView rvNotifications;
    private ArrayList<Notification> notificationList;
    private NotificationAdapter notificationAdapter;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        rvNotifications = findViewById(R.id.rvNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));

        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(this, notificationList, this);
        rvNotifications.setAdapter(notificationAdapter);

        sessionManager = new SessionManager(this);
        String key = sessionManager.getUserId();
        if (key == null || key.isEmpty()) {
            Toast.makeText(this, "Chưa có thông tin để lấy thông báo", Toast.LENGTH_SHORT).show();
            return;
        }

        getNotifications(key);

        // Nút back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        // Nút clear all
        findViewById(R.id.btnClearAll).setOnClickListener(v -> {
            notificationList.clear();
            notificationAdapter.notifyDataSetChanged();
        });
    }

    private void getNotifications(String key) {
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getNotifications(key).enqueue(new Callback<NotificationResponse>() {
            @Override
            public void onResponse(Call<NotificationResponse> call, Response<NotificationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    NotificationResponse notificationResponse = response.body();
                    notificationList.clear();
                    if (notificationResponse.getNotifications() != null) {
                        notificationList.addAll(notificationResponse.getNotifications());
                    }
                    notificationAdapter.notifyDataSetChanged();
                    Log.d("Notify", "Total notifications: " + notificationResponse.getTotalCount());
                } else {
                    Toast.makeText(NotificationActivity.this, "Lấy danh sách thông báo thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<NotificationResponse> call, Throwable t) {
                Toast.makeText(NotificationActivity.this, "Lỗi API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Notify", "API failure", t);
            }
        });
    }

    @Override
    public void onCloseClick(int position) {
        notificationList.remove(position);
        notificationAdapter.notifyItemRemoved(position);
    }

    @Override
    public void onItemClick(int position) {
        Toast.makeText(this, "Bạn đã bấm vào thông báo: " + notificationList.get(position).getTitle(), Toast.LENGTH_SHORT).show();
    }
}
