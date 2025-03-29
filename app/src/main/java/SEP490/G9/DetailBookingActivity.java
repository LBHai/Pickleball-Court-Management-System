package SEP490.G9;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import Api.NetworkUtils;
import Api.RetrofitClient;
import Model.Orders;
import retrofit2.Call;

public class DetailBookingActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvTitleMain;
    private TextView tvTabBookingInfo, tvTabServiceDetail;
    private View lineBookingInfo, lineServiceDetail;
    private ScrollView layoutBookingInfo, layoutServiceDetail;
    private TextView tvStadiumName, tvAddress, tvBookingDate, tvTotalTime, tvTotalPrice, tvPaymentStatus, tvPhonenumber, tvName, tvNote;
    private Button btnCancelBooking, btnChangeBooking;

    private String orderId, totalTime, selectedDate, orderStatus, courtId;
    private int totalPrice;

    private Handler handler = new Handler();
    private Runnable checkOrderStatusRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_booking);

        btnBack = findViewById(R.id.btnBack);
        tvTitleMain = findViewById(R.id.tvTitleMain);
        tvTabBookingInfo = findViewById(R.id.tvTabBookingInfo);
        tvTabServiceDetail = findViewById(R.id.tvTabServiceDetail);
        lineBookingInfo = findViewById(R.id.lineBookingInfo);
        lineServiceDetail = findViewById(R.id.lineServiceDetail);
        layoutBookingInfo = findViewById(R.id.layoutBookingInfo);
        layoutServiceDetail = findViewById(R.id.layoutServiceDetail);
        tvStadiumName = findViewById(R.id.tvStadiumName);
        tvAddress = findViewById(R.id.tvAddress);
        tvBookingDate = findViewById(R.id.tvBookingDate);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvPaymentStatus = findViewById(R.id.tvPaymentStatus);
        tvName = findViewById(R.id.tvName);
        tvPhonenumber = findViewById(R.id.tvPhonenumber);
        tvNote = findViewById(R.id.tvNote);
        btnCancelBooking = findViewById(R.id.btnCancelBooking);
        btnChangeBooking = findViewById(R.id.btnChangeBooking);

        btnCancelBooking.setOnClickListener(v -> {
            new AlertDialog.Builder(DetailBookingActivity.this)
                    .setTitle("Xác nhận")
                    .setMessage("Bạn có chắc chắn là muốn hủy đặt lịch không?")
                    .setPositiveButton("Có", (dialog, which) -> cancelOrder(orderId))
                    .setNegativeButton("Không", null)
                    .show();
        });

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(DetailBookingActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        orderId = getIntent().getStringExtra("orderId");
        totalTime = getIntent().getStringExtra("totalTime");
        selectedDate = getIntent().getStringExtra("selectedDate");
        totalPrice = getIntent().getIntExtra("totalPrice", 0);
        orderStatus = getIntent().getStringExtra("orderStatus");
        Log.d("DetailBookingActivity", "orderStatus nhận được: " + orderStatus);
        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(this, "Không có orderId", Toast.LENGTH_SHORT).show();
            return;
        }
        courtId = getIntent().getStringExtra("courtId");
        Log.d("DetailBookingActivity", "courtId nhận được: " + courtId);

        tvBookingDate.setText("Ngày: " + selectedDate);
        tvTotalTime.setText("Tổng thời gian: " + totalTime);
        tvTotalPrice.setText("Tổng tiền: " + totalPrice + " đ");

        updateButtonsBasedOnStatus(orderStatus);
        fetchOrderDetails(orderId);

        checkOrderStatusRunnable = new Runnable() {
            @Override
            public void run() {
                fetchOrderDetails(orderId);
                handler.postDelayed(this, 500);
            }
        };
        handler.postDelayed(checkOrderStatusRunnable, 500);

        tvTabBookingInfo.setOnClickListener(v -> showBookingInfoTab());
        tvTabServiceDetail.setOnClickListener(v -> showServiceDetailTab());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(checkOrderStatusRunnable);
        }
    }

    private void cancelOrder(String orderId) {
        Call<Orders> call = RetrofitClient.getApiService(this).cancelOrder(orderId);
        NetworkUtils.callApi(call, this, new NetworkUtils.ApiCallback<Orders>() {
            @Override
            public void onSuccess(Orders order) {
                if (order != null && "Hủy đặt lịch".equals(order.getOrderStatus())) {
                    runOnUiThread(() -> {
                        btnCancelBooking.setVisibility(View.GONE);
                        btnChangeBooking.setVisibility(View.GONE);
                        tvPaymentStatus.setText("Trạng thái thanh toán: " + order.getOrderStatus());
                        Toast.makeText(DetailBookingActivity.this, "Đã hủy đặt lịch", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("DetailBookingActivity", "Lỗi khi hủy đặt lịch: " + errorMessage);
                runOnUiThread(() -> Toast.makeText(DetailBookingActivity.this, "Lỗi khi hủy đặt lịch", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void fetchOrderDetails(String orderId) {
        Call<Orders> call = RetrofitClient.getApiService(this).getOrderById(orderId);
        NetworkUtils.callApi(call, this, new NetworkUtils.ApiCallback<Orders>() {
            @Override
            public void onSuccess(Orders order) {
                String courtName = order.getCourtName();
                String address = order.getAddress();
                String paymentStatus = order.getPaymentStatus();
                String customerName = order.getCustomerName();
                String phoneNumber = order.getPhoneNumber();
                String note = order.getNote();
                String updatedOrderStatus = order.getOrderStatus();

                runOnUiThread(() -> {
                    tvStadiumName.setText("Tên sân: " + courtName);
                    tvAddress.setText("Địa chỉ: "+ address);
                    tvPaymentStatus.setText("Trạng thái thanh toán: " + paymentStatus);
                    tvName.setText("Khách Hàng: " + customerName);
                    tvPhonenumber.setText("SDT: " + phoneNumber);
                    tvNote.setText("Khách hàng ghi chú: " + ((note == null || note.isEmpty()) ? "Không có" : note));

                    if (!updatedOrderStatus.equals(orderStatus)) {
                        orderStatus = updatedOrderStatus;
                        Log.d("DetailBookingActivity", "orderStatus cập nhật: " + orderStatus);
                        updateButtonsBasedOnStatus(orderStatus);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("DetailBookingActivity", "Lỗi khi lấy dữ liệu: " + errorMessage);
            }
        });
    }

    private void updateButtonsBasedOnStatus(String status) {
        // Nếu trạng thái là hủy lịch hoặc thay đổi lịch thì ẩn 2 nút
        if ("Hủy đặt lịch".equals(status) ||
                "Hủy đặt lịch do quá giờ thanh toán".equals(status) ||
                "Thay đổi lịch đặt".equals(status) ||"Thay đổi lịch đặt thành công".equals(status)) {
            btnCancelBooking.setVisibility(View.GONE);
            btnChangeBooking.setVisibility(View.GONE);
        }
        // Nếu trạng thái đang xử lý thì hiển thị nút Thanh toán và nút hủy
        else if ("Đang xử lý".equals(status)) {
            btnChangeBooking.setText("Thanh toán");
            btnChangeBooking.setVisibility(View.VISIBLE);
            btnCancelBooking.setVisibility(View.VISIBLE);
            btnChangeBooking.setOnClickListener(v -> {
                Intent intent = new Intent(DetailBookingActivity.this, QRCodeActivity.class);
                intent.putExtra("orderId", orderId);
                intent.putExtra("totalTime", totalTime);
                intent.putExtra("selectedDate", selectedDate);
                intent.putExtra("totalPrice", totalPrice);
                intent.putExtra("courtId", courtId);
                startActivity(intent);
            });
        }
        // THÊM: Nếu trạng thái là "Đặt lịch thành công" thì hiển thị cả 2 nút với hành động thay đổi lịch đặt
        else if ("Đặt lịch thành công".equals(status)) {
            btnChangeBooking.setText("Thay đổi lịch đặt");
            btnChangeBooking.setVisibility(View.VISIBLE);
            btnCancelBooking.setVisibility(View.VISIBLE);
            btnChangeBooking.setOnClickListener(v -> {
                new AlertDialog.Builder(DetailBookingActivity.this)
                        .setTitle("Xác nhận")
                        .setMessage("Bạn có chắc chắn là muốn thay đổi lịch đặt không?")
                        .setPositiveButton("Có", (dialog, which) -> {
                            Intent intent = new Intent(DetailBookingActivity.this, BookingTableActivity.class);
                            intent.putExtra("orderId", orderId);
                            intent.putExtra("club_id", courtId); // Truyền courtId như club_id
                            startActivity(intent);
                        })
                        .setNegativeButton("Không", null)
                        .show();
            });
        }
        // Trường hợp mặc định (các trạng thái khác)
        else {
            btnChangeBooking.setText("Thay đổi lịch đặt");
            btnChangeBooking.setVisibility(View.VISIBLE);
            btnCancelBooking.setVisibility(View.VISIBLE);
            btnChangeBooking.setOnClickListener(v -> {
                new AlertDialog.Builder(DetailBookingActivity.this)
                        .setTitle("Xác nhận")
                        .setMessage("Bạn có chắc chắn là muốn thay đổi lịch đặt không?")
                        .setPositiveButton("Có", (dialog, which) -> {
                            Intent intent = new Intent(DetailBookingActivity.this, BookingTableActivity.class);
                            intent.putExtra("orderId", orderId);
                            intent.putExtra("club_id", courtId); // Truyền courtId như club_id
                            startActivity(intent);
                        })
                        .setNegativeButton("Không", null)
                        .show();
            });
        }
    }

    private void showBookingInfoTab() {
        tvTabBookingInfo.setTextColor(Color.WHITE);
        tvTabServiceDetail.setTextColor(Color.DKGRAY);
        lineBookingInfo.setVisibility(View.VISIBLE);
        lineServiceDetail.setVisibility(View.INVISIBLE);
        layoutBookingInfo.setVisibility(View.VISIBLE);
        layoutServiceDetail.setVisibility(View.GONE);
    }

    private void showServiceDetailTab() {
        tvTabBookingInfo.setTextColor(Color.DKGRAY);
        tvTabServiceDetail.setTextColor(Color.WHITE);
        lineBookingInfo.setVisibility(View.INVISIBLE);
        lineServiceDetail.setVisibility(View.VISIBLE);
        layoutBookingInfo.setVisibility(View.GONE);
        layoutServiceDetail.setVisibility(View.VISIBLE);
    }
}