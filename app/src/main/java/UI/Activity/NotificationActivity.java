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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import UI.Adapter.NotificationAdapter;
import Data.Network.ApiService;
import Data.Network.RetrofitClient;
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
//    private ImageButton btnClearAll;
    private String orderId;
    private String totalTime;
    private String selectedDate;
    private double totalPrice;
    private String courtId;
    private String orderType;
    private String customerName;
    private String phoneNumber;
    private String note;
    private String serviceDetailsJson;
    private String serviceListJson;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // Ánh xạ giao diện
        rvNotifications = findViewById(R.id.rvNotifications);
//        btnClearAll = findViewById(R.id.btnClearAll);

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
        Intent intent = getIntent();
        orderId = intent.getStringExtra("orderId");
        totalTime = intent.getStringExtra("totalTime");
        selectedDate = intent.getStringExtra("selectedDate");
        totalPrice = intent.getDoubleExtra("totalPrice", 0.0);
        courtId = intent.getStringExtra("courtId");
        orderType = intent.getStringExtra("orderType");
        customerName = intent.getStringExtra("customerName");
        phoneNumber = intent.getStringExtra("phoneNumber");
        note = intent.getStringExtra("note");
        serviceDetailsJson = intent.getStringExtra("serviceDetailsJson");
        serviceListJson = intent.getStringExtra("serviceListJson");

//        Log.d("NotificationActivity", "Received from AccountFragment: " +
//                "orderId=" + orderId +
//                ", totalTime=" + totalTime +
//                ", selectedDate=" + selectedDate +
//                ", totalPrice=" + totalPrice +
//                ", courtId=" + courtId +
//                ", orderType=" + orderType +
//                ", customerName=" + customerName +
//                ", phoneNumber=" + phoneNumber +
//                ", note=" + note
//        );
        if (userId != null && !userId.isEmpty()) {
            // Nếu có userId, lấy thông báo theo userId
            getNotifications(userId);
            Log.d("Notification", "Đang lấy thông báo cho user ID: " + userId);
        } else {
            // Nếu không có userId, lấy thông báo theo số điện thoại guest
            String guestPhone = sessionManager.getGuestPhone();
            if (guestPhone != null && !guestPhone.isEmpty()) {
                getNotifications(guestPhone);
//                Log.d("Notification", "Đang lấy thông báo cho guest phone: " + guestPhone);
            } else {
                Toast.makeText(this, "Không có thông tin người dùng", Toast.LENGTH_SHORT).show();
//                Log.e("Notification", "Không tìm thấy user ID hoặc guest phone");
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
//        btnClearAll.setOnClickListener(v -> clearAllNotifications());
    }

    private void getNotifications(String value) {
        Log.d("Notification", "Đang lấy thông báo với value: " + value);
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getNotifications(value).enqueue(new Callback<NotificationResponse>() {
            @Override
            public void onResponse(Call<NotificationResponse> call, Response<NotificationResponse> response) {
//                Log.d("Notification", "Response code: " + response.code());

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
//                Log.e("Notification", "Lỗi API: " + t.getMessage(), t);
//                Toast.makeText(NotificationActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                // Lọc các thông báo chưa xóa
                for (NotificationItem notification : allNotifications) {
                    if (!deletedNotificationIds.contains(notification.getId())) {
                        notificationList.add(notification);
                    }
                }

                // Sắp xếp thông báo từ mới nhất đến cũ nhất
                sortNotificationsByDate(notificationList);
            }

            Log.d("Notification", "Sau khi lọc và sắp xếp, hiển thị " + notificationList.size() + " thông báo");
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

    private void markNotificationAsRead(NotificationItem notification) {
        ApiService apiService = RetrofitClient.getApiService(NotificationActivity.this);
        apiService.markAsRead(notification.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    notification.setStatus("read");
                    int index = notificationList.indexOf(notification);
                    if (index != -1) {
                        notificationAdapter.notifyItemChanged(index);
                    } else {
                        notificationAdapter.notifyDataSetChanged();
                    }
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
    private void openNotificationDetail(NotificationItem notification) {
        Intent intent = new Intent(NotificationActivity.this, DetailBookingActivity.class);
        intent.putExtra("orderId", orderId);
        intent.putExtra("totalTime", totalTime);
        intent.putExtra("selectedDate", selectedDate);
        intent.putExtra("totalPrice", totalPrice);
        intent.putExtra("courtId", courtId);
        intent.putExtra("orderType", orderType);
        intent.putExtra("customerName", customerName);
        intent.putExtra("phoneNumber", phoneNumber);
        intent.putExtra("note", note);

        if (serviceDetailsJson != null) {
            intent.putExtra("serviceDetailsJson", serviceDetailsJson);
        }
        if (serviceListJson != null) {
            intent.putExtra("serviceListJson", serviceListJson);
        }


        startActivity(intent);
    }
    /**
     * Sắp xếp danh sách thông báo từ mới nhất đến cũ nhất dựa trên thời gian tạo (createAt)
     * @param notifications Danh sách thông báo cần sắp xếp
     */
    private void sortNotificationsByDate(List<NotificationItem> notifications) {
        if (notifications != null && !notifications.isEmpty()) {
            Collections.sort(notifications, (notification1, notification2) -> {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
                    Date date1 = dateFormat.parse(notification1.getCreateAt());
                    Date date2 = dateFormat.parse(notification2.getCreateAt());

                    // Sắp xếp giảm dần (mới nhất trước)
                    if (date1 != null && date2 != null) {
                        return date2.compareTo(date1);
                    }
                } catch (ParseException e) {
                    Log.e("NotificationActivity", "Lỗi khi phân tích ngày: " + e.getMessage());
                }

                // Nếu có lỗi khi phân tích ngày, so sánh chuỗi trực tiếp
                return notification2.getCreateAt().compareTo(notification1.getCreateAt());
            });
        }
    }

}
