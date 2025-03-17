package SEP490.G9;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import Adapter.NotificationAdapter;
import Model.Notification;
import Model.NotificationResponse;

public class NotificationActivity extends AppCompatActivity implements NotificationAdapter.OnNotificationItemClickListener {

    private RecyclerView rvNotifications;
    private NotificationAdapter notificationAdapter;
    private ArrayList<Notification> notificationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        rvNotifications = findViewById(R.id.rvNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));

        // Giả sử bạn đã gọi API lấy NotificationResponse
        // notificationList = new ArrayList<>(notificationResponse.getNotifications());
        notificationList = new ArrayList<>(); // tạm thời rỗng

        // Tạo adapter
        notificationAdapter = new NotificationAdapter(this, notificationList, this);
        rvNotifications.setAdapter(notificationAdapter);

        // Xử lý toolbar
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnClearAll).setOnClickListener(v -> {
            // Ví dụ: clear hết thông báo
            notificationList.clear();
            notificationAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onCloseClick(int position) {
        // Xử lý khi bấm nút X của 1 item
        notificationList.remove(position);
        notificationAdapter.notifyItemRemoved(position);
    }

    @Override
    public void onItemClick(int position) {
        // Xử lý khi click vào cả item
        Toast.makeText(this, "Bạn đã bấm vào thông báo: " + notificationList.get(position).getTitle(), Toast.LENGTH_SHORT).show();
    }
}
