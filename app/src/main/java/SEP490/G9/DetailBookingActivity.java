package SEP490.G9;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import Api.NetworkUtils;
import Api.RetrofitClient;
import Model.Orders;
import retrofit2.Call;

public class DetailBookingActivity extends AppCompatActivity {

    // Các view từ layout
    private ImageButton btnBack;
    private TextView tvTitleMain;
    private TextView tvTabBookingInfo, tvTabServiceDetail;
    private View lineBookingInfo, lineServiceDetail;
    private ScrollView layoutBookingInfo, layoutServiceDetail;

    private TextView tvStadiumName, tvAddress, tvBookingDate, tvTotalTime, tvTotalPrice, tvPaymentStatus, tvPhonenumber, tvName, tvNote;
    private Button btnCancel;

    // Dữ liệu từ Intent
    private String orderId, totalTime, selectedDate;
    private int totalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_booking);

        // Ánh xạ các view
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
        tvNote = findViewById(R.id.tvNote); // Ghi chú (Card 3)
        btnCancel = findViewById(R.id.btnCancel);

        // Xử lý nút Back
        btnBack.setOnClickListener(v -> finish());

        // Lấy dữ liệu từ Intent
        orderId = getIntent().getStringExtra("orderId");
        totalTime = getIntent().getStringExtra("totalTime");
        selectedDate = getIntent().getStringExtra("selectedDate");
        totalPrice = getIntent().getIntExtra("totalPrice", 0);

        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(this, "Không có orderId", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gọi API lấy các thông tin còn lại theo orderId
        fetchOrderDetails(orderId);

        // Hiển thị các giá trị từ Intent lên XML
        // (Sử dụng Intent cho ngày, tổng thời gian và tổng tiền)
        tvBookingDate.setText("Ngày: " + selectedDate);
        tvTotalTime.setText("Tổng thời gian: " + totalTime);
        tvTotalPrice.setText("Tổng tiền: " + totalPrice + " đ");

        // Mặc định hiển thị tab "Thông tin Đặt lịch"
        showBookingInfoTab();

        // Xử lý chuyển tab
        tvTabBookingInfo.setOnClickListener(v -> showBookingInfoTab());
        tvTabServiceDetail.setOnClickListener(v -> showServiceDetailTab());

        // Nút hủy đặt lịch
        btnCancel.setOnClickListener(v ->
                Toast.makeText(DetailBookingActivity.this, "Chức năng hủy đặt lịch chưa được xử lý", Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * Gọi API lấy thông tin đơn hàng theo orderId và cập nhật các TextView (không ghi đè lên dữ liệu từ Intent)
     */
    private void fetchOrderDetails(String orderId) {
        Call<Orders> call = RetrofitClient.getApiService(this).getOrderById(orderId);
        NetworkUtils.callApi(call, this, new NetworkUtils.ApiCallback<Orders>() {
            @Override
            public void onSuccess(Orders order) {
                // Các trường lấy từ API
                String courtName = order.getCourtName();
                String address = order.getAddress();
                String paymentStatus = order.getPaymentStatus();
                String customerName = order.getCustomerName();
                String phoneNumber = order.getPhoneNumber();
                String note = order.getNote();
                // Cập nhật UI (Intent đã cung cấp: selectedDate, totalTime, totalPrice không bị thay đổi)
                runOnUiThread(() -> {
                    tvStadiumName.setText(courtName);
                    tvAddress.setText(address);
                    tvPaymentStatus.setText("Trạng thái thanh toán: " + paymentStatus);
                    tvName.setText("Khách Hàng: " + customerName);
                    tvPhonenumber.setText("SDT: " + phoneNumber);
                    tvNote.setText("Khách hàng ghi chú: " + ((note == null || note.isEmpty()) ? "Không có" : note));
                });
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("DetailBookingActivity", "Lỗi khi lấy dữ liệu: " + errorMessage);
            }
        });
    }

    /**
     * Hiển thị tab "Thông tin Đặt lịch" (với layoutBookingInfo) và ẩn layout ServiceDetail
     */
    private void showBookingInfoTab() {
        tvTabBookingInfo.setTextColor(Color.WHITE);
        tvTabServiceDetail.setTextColor(Color.DKGRAY);
        lineBookingInfo.setVisibility(View.VISIBLE);
        lineServiceDetail.setVisibility(View.INVISIBLE);
        layoutBookingInfo.setVisibility(View.VISIBLE);
        layoutServiceDetail.setVisibility(View.GONE);
    }

    /**
     * Hiển thị tab "Chi tiết Dịch vụ" (với layoutServiceDetail) và ẩn layoutBookingInfo
     */
    private void showServiceDetailTab() {
        tvTabBookingInfo.setTextColor(Color.DKGRAY);
        tvTabServiceDetail.setTextColor(Color.WHITE);
        lineBookingInfo.setVisibility(View.INVISIBLE);
        lineServiceDetail.setVisibility(View.VISIBLE);
        layoutBookingInfo.setVisibility(View.GONE);
        layoutServiceDetail.setVisibility(View.VISIBLE);
    }
}
