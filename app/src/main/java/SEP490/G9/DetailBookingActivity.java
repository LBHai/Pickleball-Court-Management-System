package SEP490.G9;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DetailBookingActivity extends AppCompatActivity {

    // Các view trên layout
    private ImageButton btnBack;
    private TextView tvTitleMain;
    private TextView tvTabBookingInfo, tvTabServiceDetail;
    private View lineBookingInfo, lineServiceDetail;
    private ScrollView layoutBookingInfo, layoutServiceDetail;

    private TextView tvStadiumName, tvAddress, tvBookingDate, tvTotalTime, tvTotalPrice, tvPaymentStatus;
    private Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_booking);

        // Ánh xạ các view từ layout
        btnBack = findViewById(R.id.btnBack);
        tvTitleMain = findViewById(R.id.tvTitleMain);

        // Tab
        tvTabBookingInfo = findViewById(R.id.tvTabBookingInfo);
        tvTabServiceDetail = findViewById(R.id.tvTabServiceDetail);
        lineBookingInfo = findViewById(R.id.lineBookingInfo);
        lineServiceDetail = findViewById(R.id.lineServiceDetail);
        layoutBookingInfo = findViewById(R.id.layoutBookingInfo);
        layoutServiceDetail = findViewById(R.id.layoutServiceDetail);

        // Thông tin đặt lịch
        tvStadiumName = findViewById(R.id.tvStadiumName);
        tvAddress = findViewById(R.id.tvAddress);
        tvBookingDate = findViewById(R.id.tvBookingDate);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvPaymentStatus = findViewById(R.id.tvPaymentStatus);
        btnCancel = findViewById(R.id.btnCancel);

        // Xử lý sự kiện nút back
        btnBack.setOnClickListener(v -> finish());

        // Nhận dữ liệu từ Intent (nếu có)
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String orderId = extras.getString("orderId");
            String selectedDate = extras.getString("selectedDate");
            String totalPriceExtra = extras.getString("totalPrice");
            String courtName = extras.getString("courtName");
            String orderAddress = extras.getString("orderAddress");
            String paymentStatusExtra = extras.getString("paymentStatus");
            String totalTimeExtra = extras.getString("totalTime");

            // Cập nhật thông tin vào giao diện
            if (courtName != null) {
                tvStadiumName.setText("Tên sân: " + courtName);
            }
            if (orderAddress != null) {
                tvAddress.setText("Địa chỉ: " + orderAddress);
            }
            if (selectedDate != null) {
                tvBookingDate.setText("Ngày: " + selectedDate);
            }
            if (totalTimeExtra != null) {
                tvTotalTime.setText("Tổng thời gian: " + totalTimeExtra);
            }
            if (totalPriceExtra != null) {
                tvTotalPrice.setText("Tổng tiền: " + totalPriceExtra);
            }
            if (paymentStatusExtra != null) {
                tvPaymentStatus.setText("Trạng thái thanh toán: " + paymentStatusExtra);
            }
        }

        // Xử lý sự kiện nút hủy đặt lịch
        btnCancel.setOnClickListener(v -> {
            // TODO: Thêm logic hủy đặt lịch (hiện dialog xác nhận, gọi API, v.v.)
        });

        // Mặc định hiển thị tab "Thông tin Đặt lịch"
        showBookingInfoTab();

        // Xử lý chuyển tab khi ấn vào "Thông tin Đặt lịch"
        tvTabBookingInfo.setOnClickListener(v -> {
            showBookingInfoTab();
        });

        // Xử lý chuyển tab khi ấn vào "Chi tiết Dịch vụ"
        tvTabServiceDetail.setOnClickListener(v -> {
            showServiceDetailTab();
        });
    }

    /**
     * Hiển thị tab "Thông tin Đặt lịch", ẩn tab "Chi tiết Dịch vụ"
     */
    private void showBookingInfoTab() {
        // Đổi màu chữ tab
        tvTabBookingInfo.setTextColor(Color.WHITE);
        tvTabServiceDetail.setTextColor(Color.DKGRAY);

        // Đổi trạng thái line gạch chân
        lineBookingInfo.setVisibility(View.VISIBLE);
        lineServiceDetail.setVisibility(View.INVISIBLE);

        // Hiển thị layout BookingInfo, ẩn layout ServiceDetail
        layoutBookingInfo.setVisibility(View.VISIBLE);
        layoutServiceDetail.setVisibility(View.GONE);
    }

    /**
     * Hiển thị tab "Chi tiết Dịch vụ", ẩn tab "Thông tin Đặt lịch"
     */
    private void showServiceDetailTab() {
        // Đổi màu chữ tab
        tvTabBookingInfo.setTextColor(Color.DKGRAY);
        tvTabServiceDetail.setTextColor(Color.WHITE);

        // Đổi trạng thái line gạch chân
        lineBookingInfo.setVisibility(View.INVISIBLE);
        lineServiceDetail.setVisibility(View.VISIBLE);

        // Hiển thị layout ServiceDetail, ẩn layout BookingInfo
        layoutBookingInfo.setVisibility(View.GONE);
        layoutServiceDetail.setVisibility(View.VISIBLE);
    }
}
