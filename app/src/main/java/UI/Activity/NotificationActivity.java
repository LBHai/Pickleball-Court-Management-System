package UI.Activity;

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
import UI.Adapter.NotificationAdapter;
import Data.Network.ApiService;
import Data.Network.RetrofitClient;
import Data.Model.NotificationData;
import Data.Model.NotificationItem;
import Data.Model.NotificationResponse;
import SEP490.G9.R;
import Data.Session.SessionManager;
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

        sessionManager = new SessionManager(this);
        String userId = sessionManager.getUserId();

        if (userId != null && !userId.isEmpty()) {
            // Nếu có userId, lấy thông báo theo userId
            getNotifications(userId);
            Log.d("Notification", "Đang lấy thông báo cho user ID: " + userId);
        } else {
            // Nếu không có userId, lấy thông báo theo số điện thoại guest
            String guestPhone = sessionManager.getGuestPhone();
            if (guestPhone != null && !guestPhone.isEmpty()) {
                getNotifications(guestPhone);
                Log.d("Notification", "Đang lấy thông báo cho guest phone: " + guestPhone);
            } else {
                Toast.makeText(this, "Không có thông tin người dùng", Toast.LENGTH_SHORT).show();
                Log.e("Notification", "Không tìm thấy user ID hoặc guest phone");
            }
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

        // Nhận extra data từ intent (nếu có)
        Intent intent = getIntent();
        String orderId = intent.getStringExtra("orderId");
        // Các extra khác...
    }

    /**
     * Phương thức thống nhất để lấy thông báo cho cả user và guest
     * @param value Có thể là userId hoặc số điện thoại
     */
    private void getNotifications(String value) {
        Log.d("Notification", "Đang lấy thông báo với value: " + value);
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getNotifications(value).enqueue(new Callback<NotificationResponse>() {
            @Override
            public void onResponse(Call<NotificationResponse> call, Response<NotificationResponse> response) {
                // Thêm log để kiểm tra response
                Log.d("Notification", "Response code: " + response.code());

                if (!response.isSuccessful()) {
                    try {
                        Log.e("Notification", "Error body: " + response.errorBody().string());
                    } catch (Exception e) {
                        Log.e("Notification", "Cannot read error body");
                    }
                }

                handleNotificationResponse(response);
            }

            @Override
            public void onFailure(Call<NotificationResponse> call, Throwable t) {
                Log.e("Notification", "Lỗi API: " + t.getMessage(), t);
                Toast.makeText(NotificationActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleNotificationResponse(Response<NotificationResponse> response) {
        if (response.isSuccessful() && response.body() != null) {
            List<NotificationItem> allNotifications = response.body().getNotifications();
            Log.d("Notification", "Đã nhận được " +
                    (allNotifications != null ? allNotifications.size() : 0) + " thông báo");

            // Cập nhật danh sách đã xóa từ SharedPreferences
            deletedNotificationIds = new HashSet<>(sharedPreferences.getStringSet("deletedIds", new HashSet<>()));
            notificationList.clear();

            if (allNotifications != null) {
                for (NotificationItem notification : allNotifications) {
                    if (!deletedNotificationIds.contains(notification.getId())) {
                        notificationList.add(notification);
                    }
                }
            }

            Log.d("Notification", "Sau khi lọc, hiển thị " + notificationList.size() + " thông báo");
            notificationAdapter.notifyDataSetChanged();
        } else {
            Log.e("Notification", "Response không thành công hoặc body null");
            Toast.makeText(NotificationActivity.this, "Không thể lấy danh sách thông báo", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCloseClick(NotificationItem notification) {
        // Gọi API mark-as-read
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.markAsRead(notification.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("Notification", "markAsRead SUCCESS for id=" + notification.getId());
                } else {
                    Log.e("Notification", "markAsRead FAILED for id=" + notification.getId()
                            + "  code=" + response.code());
                }
                // Dù thành công hay không, ta cũng tiếp tục xóa local
                deletedNotificationIds.add(notification.getId());
                sharedPreferences.edit()
                        .putStringSet("deletedIds", deletedNotificationIds)
                        .apply();
                notificationList.remove(notification);
                notificationAdapter.notifyDataSetChanged();
                Toast.makeText(NotificationActivity.this, "Đã xóa và đánh dấu đã đọc", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Nếu lỗi kết nối, bạn vẫn có thể xóa local và báo lỗi
                deletedNotificationIds.add(notification.getId());
                sharedPreferences.edit()
                        .putStringSet("deletedIds", deletedNotificationIds)
                        .apply();
                notificationList.remove(notification);
                notificationAdapter.notifyDataSetChanged();
                Toast.makeText(NotificationActivity.this, "Xóa thành công (server offline)", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void clearAllNotifications() {
        Set<String> idsToDelete = new HashSet<>();
        for (NotificationItem notification : notificationList) {
            idsToDelete.add(notification.getId());
        }
        deletedNotificationIds = new HashSet<>(sharedPreferences.getStringSet("deletedIds", new HashSet<>()));
        deletedNotificationIds.addAll(idsToDelete);
        sharedPreferences.edit().putStringSet("deletedIds", deletedNotificationIds).apply();
        notificationList.clear();
        notificationAdapter.notifyDataSetChanged();
        Toast.makeText(this, "Đã xóa tất cả thông báo", Toast.LENGTH_SHORT).show();
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
