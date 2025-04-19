package SEP490.G9;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import Model.NotificationData;
import Model.NotificationItem;
import Model.NotificationResponse;
import Session.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActivity extends AppCompatActivity implements NotificationAdapter.OnNotificationItemClickListener {

    private RecyclerView rvNotifications;
    private ArrayList<NotificationItem> notificationList;
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
        String phoneNumber = sessionManager.getPhoneNumber();
        Log.d("NotificationActivity", "userId = " + userId + ", phoneNumber = " + phoneNumber);
        if (userId != null && !userId.isEmpty()) {
            // Nếu có userId, lấy thông báo theo userId
            getNotificationsByUserId(userId);
        } else if (phoneNumber != null && !phoneNumber.isEmpty()) {
            // Nếu không có userId nhưng có phone, lấy thông báo theo phone
            getNotificationsByPhone(phoneNumber);
        } else {
            Toast.makeText(this, "Không có thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }



        // Xử lý vuốt để xóa thông báo
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                NotificationItem notification = notificationList.get(position);
                onCloseClick(notification);
            }
        };
        new ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(rvNotifications);

        // Nút quay lại
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Xử lý nút "Xóa tất cả"
        btnClearAll.setOnClickListener(v -> clearAllNotifications());
        Intent intent = getIntent();
        String orderId = intent.getStringExtra("orderId");
        String totalTime = intent.getStringExtra("totalTime");
        int totalPrice = intent.getIntExtra("totalPrice", 0);
        String orderStatus = intent.getStringExtra("orderStatus");
        String courtId = intent.getStringExtra("courtId");
        String orderType = intent.getStringExtra("orderType");
    }

    private void getNotificationsByUserId(String userId) {
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getNotifications(userId).enqueue(new Callback<NotificationResponse>() {
            @Override
            public void onResponse(Call<NotificationResponse> call, Response<NotificationResponse> response) {
                handleNotificationResponse(response);
            }
            @Override
            public void onFailure(Call<NotificationResponse> call, Throwable t) {
                Toast.makeText(NotificationActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getNotificationsByPhone(String phoneNumber) {
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getNotificationsByPhone(phoneNumber).enqueue(new Callback<NotificationResponse>() {
            @Override
            public void onResponse(Call<NotificationResponse> call, Response<NotificationResponse> response) {
                handleNotificationResponse(response);
            }
            @Override
            public void onFailure(Call<NotificationResponse> call, Throwable t) {
                Toast.makeText(NotificationActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleNotificationResponse(Response<NotificationResponse> response) {
        if (response.isSuccessful() && response.body() != null) {
            List<NotificationItem> allNotifications = response.body().getNotifications();
            // Cập nhật danh sách đã xóa từ SharedPreferences
            deletedNotificationIds = new HashSet<>(sharedPreferences.getStringSet("deletedIds", new HashSet<>()));
            notificationList.clear();
            for (NotificationItem notification : allNotifications) {
                if (!deletedNotificationIds.contains(notification.getId())) {
                    notificationList.add(notification);
                }
            }
            notificationAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(NotificationActivity.this, "Không thể lấy danh sách thông báo", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCloseClick(NotificationItem notification) {
        deletedNotificationIds.add(notification.getId());
        sharedPreferences.edit().putStringSet("deletedIds", deletedNotificationIds).commit();
        notificationList.remove(notification);
        notificationAdapter.notifyDataSetChanged();
        Toast.makeText(this, "Đã xóa thông báo", Toast.LENGTH_SHORT).show();
    }

    private void clearAllNotifications() {
        Set<String> idsToDelete = new HashSet<>();
        for (NotificationItem notification : notificationList) {
            idsToDelete.add(notification.getId());
        }
        deletedNotificationIds = new HashSet<>(sharedPreferences.getStringSet("deletedIds", new HashSet<>()));
        deletedNotificationIds.addAll(idsToDelete);
        sharedPreferences.edit().putStringSet("deletedIds", deletedNotificationIds).commit();
        notificationList.clear();
        notificationAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(NotificationItem notification) {
        // Nếu thông báo chưa "read", gọi API đánh dấu đã đọc
        if (!"read".equalsIgnoreCase(notification.getStatus())) {
            markNotificationAsRead(notification);
        } else {
            openNotificationDetail(notification);
        }
    }

    /**
     * Gọi API để đánh dấu thông báo với ID tương ứng là "read".
     */
    private void markNotificationAsRead(NotificationItem notification) {
        ApiService apiService = RetrofitClient.getApiService(NotificationActivity.this);
        apiService.markAsRead(notification.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Cập nhật trạng thái thông báo cục bộ
                    notification.setStatus("read");

                    // Cập nhật lại giao diện cho item này. Nếu biết vị trí của item, bạn có thể dùng:
                    int index = notificationList.indexOf(notification);
                    if (index != -1) {
                        notificationAdapter.notifyItemChanged(index);
                    } else {
                        // Nếu không biết vị trí, có thể cập nhật toàn bộ danh sách
                        notificationAdapter.notifyDataSetChanged();
                    }

                    // (Tùy chọn) Cập nhật số thông báo chưa đọc, ví dụ: giảm số hiển thị trên badge

                    // Sau đó mở màn hình chi tiết
                    openNotificationDetail(notification);
                } else {
                    Toast.makeText(NotificationActivity.this, "Không thể đánh dấu đã đọc", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(NotificationActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * Mở màn hình DetailBookingActivity với thông tin của thông báo được chọn.
     */
    private void openNotificationDetail(NotificationItem notification) {
        Intent intent = new Intent(NotificationActivity.this, DetailBookingActivity.class);
        String orderId = notification.getId(); // Hoặc notification.getNotificationData().getOrderId();
        intent.putExtra("orderId", orderId);
        intent.putExtra("notificationTitle", notification.getTitle());
        intent.putExtra("notificationDescription", notification.getDescription());
        intent.putExtra("notificationTime", notification.getCreateAt());
        intent.putExtra("notificationStatus", notification.getStatus());

        NotificationData data = notification.getNotificationData();
        if (data != null) {
            intent.putExtra("orderId", data.getOrderId());
            intent.putExtra("totalTime", data.getTotalTime());
            intent.putExtra("totalPrice", data.getTotalPrice());
            intent.putExtra("orderStatus", data.getOrderStatus());
            intent.putExtra("courtId", data.getCourtId());
            intent.putExtra("dateBooking", data.getDateBooking());
            intent.putExtra("slotPrices", data.getSlotPrices());
        }
        startActivity(intent);
    }

}
