package SEP490.G9;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import SEP490.G9.R;

public class DetailBookingActivity extends AppCompatActivity {

    // Các view trong layout activity_detail_booking.xml
    private ImageButton btnBack;
    private TextView tvTitleMain;
    private TextView tvStadiumName, tvAddress, tvBookingDate, tvTotalTime, tvTotalPrice, tvPaymentStatus;
    private Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_booking); // Tên file layout của bạn

        // Ánh xạ các view từ layout
        btnBack = findViewById(R.id.btnBack);
        tvTitleMain = findViewById(R.id.tvTitleMain);
        tvStadiumName = findViewById(R.id.tvStadiumName);
        tvAddress = findViewById(R.id.tvAddress);
        tvBookingDate = findViewById(R.id.tvBookingDate);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvPaymentStatus = findViewById(R.id.tvPaymentStatus);
        btnCancel = findViewById(R.id.btnCancel);

        // Xử lý sự kiện nút back
        btnBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish(); // Đóng Activity hiện tại
            }
        });

        // Lấy dữ liệu được truyền qua Intent từ OrderAdapter
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String orderId = extras.getString("orderId");
            String selectedDate = extras.getString("selectedDate");
            String totalPriceExtra = extras.getString("totalPrice");
            String courtName = extras.getString("courtName");
            String orderAddress = extras.getString("orderAddress");
            String paymentStatusExtra = extras.getString("paymentStatus");
            String totalTimeExtra = extras.getString("totalTime");

            // Cập nhật thông tin vào Card "Thông tin đặt lịch"
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

            // Nếu cần, bạn có thể sử dụng orderId để gọi API lấy thêm chi tiết đặt lịch
        }

        // Xử lý sự kiện nút hủy đặt lịch (btnCancel)
        btnCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO: Thêm xử lý hủy đặt lịch, ví dụ: hiện dialog xác nhận hủy.
            }
        });
    }
}
