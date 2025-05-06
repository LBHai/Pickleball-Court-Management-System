package UI.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import Data.Holder.OrderServiceHolder;
import Data.Model.Orders;
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

public class NotificationActivity extends AppCompatActivity
        implements NotificationAdapter.OnNotificationItemClickListener {

    private RecyclerView rvNotifications;
    private List<NotificationItem> notificationList = new ArrayList<>();
    private NotificationAdapter notificationAdapter;
    private SessionManager sessionManager;
    private SharedPreferences sharedPreferences;
    private Set<String> deletedNotificationIds = new HashSet<>();
    private static final String TAG = "NotificationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // 1) Setup RecyclerView
        rvNotifications = findViewById(R.id.rvNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        notificationAdapter = new NotificationAdapter(this, notificationList, this);
        rvNotifications.setAdapter(notificationAdapter);

        // 2) Load deleted IDs from prefs
        sharedPreferences = getSharedPreferences("DeletedNotifications", MODE_PRIVATE);
        deletedNotificationIds.addAll(
                sharedPreferences.getStringSet("deletedIds", Collections.emptySet())
        );

        sessionManager = new SessionManager(this);
        String userKey = sessionManager.getUserId();
        if (userKey == null || userKey.isEmpty()) {
            userKey = sessionManager.getGuestPhone();
        }

        if (userKey != null && !userKey.isEmpty()) {
            getNotifications(userKey);
        } else {
            Toast.makeText(this, getString(R.string.no_user_info), Toast.LENGTH_SHORT).show();
        }

        // 3) Swipe to delete
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT
        ) {
            @Override public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder vh,
                                            RecyclerView.ViewHolder target) { return false; }

            @Override public void onSwiped(RecyclerView.ViewHolder vh, int dir) {
                NotificationItem n = notificationList.get(vh.getAdapterPosition());
                onCloseClick(n);
            }
        }).attachToRecyclerView(rvNotifications);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void getNotifications(String userKey) {
        RetrofitClient.getApiService(this)
                .getNotifications(userKey)
                .enqueue(new Callback<NotificationResponse>() {
                    @Override
                    public void onResponse(Call<NotificationResponse> c,
                                           Response<NotificationResponse> r) {
                        if (!r.isSuccessful() || r.body()==null) {
                            Toast.makeText(NotificationActivity.this,
                                    getString(R.string.cannot_get_notifications),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        List<NotificationItem> all = r.body().getNotifications();
                        notificationList.clear();
                        for (NotificationItem n : all) {
                            if (!deletedNotificationIds.contains(n.getId())) {
                                notificationList.add(n);
                            }
                        }
                        sortByDateDesc(notificationList);
                        notificationAdapter.notifyDataSetChanged();
                    }
                    @Override public void onFailure(Call<NotificationResponse> c, Throwable t) {
                        Log.e(TAG, "Failed to get notifications: " + t.getMessage());
                        Toast.makeText(NotificationActivity.this,
                                getString(R.string.cannot_get_notifications),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sortByDateDesc(List<NotificationItem> list) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
        Collections.sort(list, (a,b) -> {
            try {
                Date da = fmt.parse(a.getCreateAt());
                Date db = fmt.parse(b.getCreateAt());
                return db.compareTo(da);
            } catch (ParseException e) {
                return b.getCreateAt().compareTo(a.getCreateAt());
            }
        });
    }

    @Override
    public void onCloseClick(NotificationItem n) {
        RetrofitClient.getApiService(this)
                .markAsRead(n.getId())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> c, Response<Void> r) {
                        // always remove locally, even on failure
                        deletedNotificationIds.add(n.getId());
                        sharedPreferences.edit()
                                .putStringSet("deletedIds", deletedNotificationIds)
                                .apply();
                        notificationList.remove(n);
                        notificationAdapter.notifyDataSetChanged();
                        Toast.makeText(NotificationActivity.this,
                                getString(R.string.deleted_and_marked_read),
                                Toast.LENGTH_SHORT).show();
                    }
                    @Override public void onFailure(Call<Void> c, Throwable t) {
                        deletedNotificationIds.add(n.getId());
                        sharedPreferences.edit()
                                .putStringSet("deletedIds", deletedNotificationIds)
                                .apply();
                        notificationList.remove(n);
                        notificationAdapter.notifyDataSetChanged();
                        Toast.makeText(NotificationActivity.this,
                                getString(R.string.delete_success),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onItemClick(NotificationItem n) {
        String clickedOrderId = n.getNotificationData().getOrderId();
        if (clickedOrderId == null || clickedOrderId.isEmpty()) {
            Toast.makeText(this,
                    "Không tìm thấy thông tin đơn hàng",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Notification clicked with orderId: " + clickedOrderId);
        // 1) Đánh dấu đã đọc, 2) rồi fetch chi tiết đúng orderId
        RetrofitClient.getApiService(this)
                .markAsRead(n.getId())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> c, Response<Void> r) {
                        fetchOrderDetailsForNotification(clickedOrderId);
                    }
                    @Override public void onFailure(Call<Void> c, Throwable t) {
                        Log.e(TAG, "Failed to mark notification as read: " + t.getMessage());
                        // Still try to fetch order details even if marking as read fails
                        fetchOrderDetailsForNotification(clickedOrderId);
                    }
                });
    }

    private void fetchOrderDetailsForNotification(String orderId) {
        Log.d(TAG, "Fetching order details for orderId: " + orderId);
        ApiService apiService = RetrofitClient.getApiService(this);

        apiService.getOrderById(orderId)
                .enqueue(new Callback<Orders>() {
                    @Override
                    public void onResponse(Call<Orders> c, Response<Orders> r) {
                        if (r.isSuccessful() && r.body() != null) {
                            Orders order = r.body();
                            Log.d(TAG, "Order details fetched successfully for orderId: " + orderId);
                            openDetailBookingActivity(order);
                        } else {
                            Log.e(TAG, "Error response from getOrderById: " +
                                    (r.errorBody() != null ? r.errorBody().toString() : "No error body"));
                            Toast.makeText(NotificationActivity.this,
                                    "Không thể lấy thông tin đơn hàng (Error code: " + r.code() + ")",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onFailure(Call<Orders> c, Throwable t) {
                        Log.e(TAG, "Network failure when fetching order details: " + t.getMessage(), t);
                        Toast.makeText(NotificationActivity.this,
                                "Lỗi kết nối: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openDetailBookingActivity(Orders order) {
        if (order == null) {
            Log.e(TAG, "Cannot open DetailBookingActivity: order is null");
            Toast.makeText(this, "Lỗi: Không có thông tin đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Opening DetailBookingActivity for order: " + order.getId());

        // Khởi tạo Intent
        Intent intent = new Intent(this, DetailBookingActivity.class);

        // Truyền các thông tin đơn hàng
        intent.putExtra("orderId", order.getId());
        intent.putExtra("totalTime", order.getTotalTime() != null ? order.getTotalTime() : "");
        intent.putExtra("selectedDate", order.getCreatedAt() != null
                ? order.getCreatedAt().substring(0, Math.min(10, order.getCreatedAt().length()))
                : "");
        intent.putExtra("totalPrice", order.getTotalAmount());
        intent.putExtra("courtId", order.getCourtId() != null ? order.getCourtId() : "");
        intent.putExtra("orderType", order.getOrderType() != null ? order.getOrderType() : "");
        intent.putExtra("customerName", order.getCustomerName() != null ? order.getCustomerName() : "");
        intent.putExtra("phoneNumber", order.getPhoneNumber() != null ? order.getPhoneNumber() : "");
        intent.putExtra("note", order.getNote() != null ? order.getNote() : "");
        intent.putExtra("orderStatus", order.getOrderStatus() != null ? order.getOrderStatus() : "");
        intent.putExtra("paymentStatus", order.getPaymentStatus() != null ? order.getPaymentStatus() : "");

        // Lấy service details nếu có
        OrderServiceHolder serviceHolder = OrderServiceHolder.getInstance();
        String svcDetails = serviceHolder.getServiceDetailsJson(order.getId());
        String svcList    = serviceHolder.getServiceListJson(order.getId());
        if (svcDetails != null) {
            intent.putExtra("serviceDetailsJson", svcDetails);
            Log.d(TAG, "Adding serviceDetailsJson to intent");
        }
        if (svcList != null) {
            intent.putExtra("serviceListJson", svcList);
            Log.d(TAG, "Adding serviceListJson to intent");
        }

        // Truyền avatarUrl và isStudent từ SessionManager
        String avatarUrl = sessionManager.getAvatarUrl();
        boolean isStudent = sessionManager.getStudentStatus();
        intent.putExtra("avatarUrl", avatarUrl);
        intent.putExtra("isStudent", isStudent);
        Log.d(TAG, "Adding avatarUrl and isStudent to intent: avatarUrl="
                + avatarUrl + ", isStudent=" + isStudent);

        // Khởi chạy Activity
        startActivity(intent);
    }

}