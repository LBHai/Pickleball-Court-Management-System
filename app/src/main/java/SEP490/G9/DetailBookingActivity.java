package SEP490.G9;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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

    private TextView tvStadiumName, tvAddress, tvBookingDate, tvTotalTime, tvTotalPrice, tvPaymentStatus, tvPhonenumber, tvName;
    private Button btnCancel;
    private String orderId, courtName, address, totalTime, note, selectedDate, phone, name;
    private int totalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_booking);

        // Ánh xạ các view từ layout
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
        btnCancel = findViewById(R.id.btnCancel);

        // Xử lý sự kiện nút back
        btnBack.setOnClickListener(v -> finish());

        // Nhận dữ liệu từ Intent
        orderId = getIntent().getStringExtra("orderId");
        courtName = getIntent().getStringExtra("courtName");
        address = getIntent().getStringExtra("address");
        totalTime = getIntent().getStringExtra("totalTime");
        note = getIntent().getStringExtra("note");
        selectedDate = getIntent().getStringExtra("selectedDate");
        totalPrice = getIntent().getIntExtra("totalPrice", 0);
        name = getIntent().getStringExtra("name");
        phone = getIntent().getStringExtra("phone");
        Log.d("DetailBookingActivity", "Name: " + name + ", Phone: " + phone);

        // Gán dữ liệu cho các TextView nếu có
        if (courtName != null) {
            tvStadiumName.setText("Tên sân: " + courtName);
        }
        if (address != null) {
            tvAddress.setText("Địa chỉ: " + address);
        }
        if (totalTime != null) {
            tvTotalTime.setText("Tổng thời gian: " + totalTime);
        }
        if (selectedDate != null) {
            tvBookingDate.setText("Ngày: " + selectedDate);
        }
        if (totalPrice != 0) {
            tvTotalPrice.setText("Tổng tiền: " + totalPrice + " đ");
        }
        String paymentStatusExtra = getIntent().getStringExtra("paymentStatus");
        if (paymentStatusExtra != null) {
            tvPaymentStatus.setText("Trạng thái thanh toán: Đã thanh toán");
        }

        // Hiển thị tên và số điện thoại
        if (name != null && !name.isEmpty()) {
            tvName.setText("Khách Hàng: " + name);
        } else {
            tvName.setText("Khách Hàng: N/A");
        }
        if (phone != null && !phone.isEmpty()) {
            tvPhonenumber.setText("SDT: " + phone);
        } else {
            tvPhonenumber.setText("SDT: N/A");
        }

        // Xử lý sự kiện nút hủy đặt lịch
        btnCancel.setOnClickListener(v -> {
            // TODO: Thêm logic hủy đặt lịch (hiện dialog xác nhận, gọi API, v.v.)
        });

        // Mặc định hiển thị tab "Thông tin Đặt lịch"
        showBookingInfoTab();

        // Xử lý chuyển tab khi ấn vào "Thông tin Đặt lịch"
        tvTabBookingInfo.setOnClickListener(v -> showBookingInfoTab());

        // Xử lý chuyển tab khi ấn vào "Chi tiết Dịch vụ"
        tvTabServiceDetail.setOnClickListener(v -> showServiceDetailTab());
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
