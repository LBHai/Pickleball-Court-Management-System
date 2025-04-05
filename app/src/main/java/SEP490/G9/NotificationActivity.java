package SEP490.G9;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private SharedPreferences sharedPreferences;
    private Set<String> deletedNotificationIds;
    private ImageButton btnClearAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // Ánh xạ giao diện
        rvNotifications = findViewById(R.id.rvNotifications);
        btnClearAll = findViewById(R.id.btnClearAll);

        // Cấu hình RecyclerView
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo danh sách thông báo
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(this, notificationList, this);
        rvNotifications.setAdapter(notificationAdapter);

        // Lấy danh sách thông báo đã xóa từ SharedPreferences
        sharedPreferences = getSharedPreferences("DeletedNotifications", MODE_PRIVATE);
        deletedNotificationIds = new HashSet<>(sharedPreferences.getStringSet("deletedIds", new HashSet<>()));

        // Kiểm tra thông tin người dùng
        sessionManager = new SessionManager(this);
        String userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Không có thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy danh sách thông báo từ API
        getNotifications(userId);

        // Xử lý vuốt để xóa một thông báo
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Notification notification = notificationList.get(position);
                onCloseClick(notification);
            }
        };

        new ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(rvNotifications);

        // Nút quay lại
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Xử lý sự kiện khi bấm "Xóa tất cả"
        btnClearAll.setOnClickListener(v -> clearAllNotifications());
    }

    /**
     * Gọi API để lấy danh sách thông báo
     */
    private void getNotifications(String userId) {
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getNotifications(userId).enqueue(new Callback<NotificationResponse>() {
            @Override
            public void onResponse(Call<NotificationResponse> call, Response<NotificationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Notification> allNotifications = response.body().getNotifications();

                    // Lấy danh sách thông báo đã xóa
                    //deletedNotificationIds = sharedPreferences.getStringSet("deletedIds", new HashSet<>());
                    // Đọc lại danh sách đã xóa từ SharedPreferences
                    deletedNotificationIds = new HashSet<>(sharedPreferences.getStringSet("deletedIds", new HashSet<>()));
                    // Lọc danh sách, loại bỏ các thông báo đã bị xóa
                    notificationList.clear();
                    for (Notification notification : allNotifications) {
                        if (!deletedNotificationIds.contains(notification.getId())) {
                            notificationList.add(notification);
                        }
                    }

                    // Cập nhật giao diện
                    notificationAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(NotificationActivity.this, "Không thể lấy danh sách thông báo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<NotificationResponse> call, Throwable t) {
                Toast.makeText(NotificationActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Xóa một thông báo
     */
    @Override
    public void onCloseClick(Notification notification) {
        // Thêm ID vào danh sách xóa
        deletedNotificationIds.add(notification.getId());
        sharedPreferences.edit().putStringSet("deletedIds", deletedNotificationIds).commit();

        // Xóa thông báo khỏi danh sách hiển thị
        notificationList.remove(notification);
        notificationAdapter.notifyDataSetChanged();

        Toast.makeText(this, "Đã xóa thông báo", Toast.LENGTH_SHORT).show();
    }

    /**
     * Xóa tất cả thông báo
     */
    private void clearAllNotifications() {
        // Lấy tất cả ID hiện tại
        Set<String> idsToDelete = new HashSet<>();
        for (Notification notification : notificationList) {
            idsToDelete.add(notification.getId());
        }

        // Đọc lại danh sách đã xóa và thêm mới
        deletedNotificationIds = new HashSet<>(sharedPreferences.getStringSet("deletedIds", new HashSet<>()));
        deletedNotificationIds.addAll(idsToDelete);

        // Lưu và xóa
        sharedPreferences.edit().putStringSet("deletedIds", deletedNotificationIds).commit();
        notificationList.clear();
        notificationAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(Notification notification) {
        Toast.makeText(this, "Bạn đã chọn thông báo: " + notification.getTitle(), Toast.LENGTH_SHORT).show();
    }
}
