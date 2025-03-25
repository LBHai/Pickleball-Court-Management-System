package SEP490.G9;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class PaymentSuccessActivity extends AppCompatActivity {

    private TextView tvTitle, tvSubTitle;
    private Button btnXemLichDatChiTiet, btnQuayVe;
    private ImageButton btnBack;
    private String orderId, totalTime, selectedDate,courtId;
    private int totalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_success);

        // Ánh xạ view
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        tvSubTitle = findViewById(R.id.tvSubTitle);
        btnXemLichDatChiTiet = findViewById(R.id.btnXemLichDatChiTiet);
        btnQuayVe = findViewById(R.id.btnQuayVe);

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        if(intent != null){
            orderId = intent.getStringExtra("orderId");
            totalTime = intent.getStringExtra("totalTime");
            selectedDate = intent.getStringExtra("selectedDate");
            totalPrice = intent.getIntExtra("totalPrice", 0);
            courtId = intent.getStringExtra("courtId");

        }

        // Log giá trị orderId nhận được
        if (orderId != null && !orderId.isEmpty()) {
            Log.d("Paymentsuscess", "orderId nhan dc: " + orderId);
        } else {
            Log.e("Paymentsuscess", "orderId is null or empty!");
        }

        // Xử lý nút Back: quay về MainActivity
        btnBack.setOnClickListener(v -> {
            Intent mainIntent = new Intent(PaymentSuccessActivity.this, MainActivity.class);
            startActivity(mainIntent);
            finish();
        });

        // Cập nhật thông tin thông báo cho người dùng
        tvTitle.setText("Đặt lịch thành công");
        tvSubTitle.setText("Lịch đặt của bạn đã được gửi tới chủ sân.\nVui lòng kiểm tra trạng thái tại mục \"Tài khoản\" để kiểm soát và xác nhận lịch đặt.");

        // Xử lý nút Xem lịch đặt chi tiết
        btnXemLichDatChiTiet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent detailIntent = new Intent(PaymentSuccessActivity.this, DetailBookingActivity.class);
                detailIntent.putExtra("orderId", orderId);
                detailIntent.putExtra("totalTime", totalTime);
                detailIntent.putExtra("selectedDate", selectedDate);
                detailIntent.putExtra("totalPrice", totalPrice);
                detailIntent.putExtra("courtId", courtId);
                Log.d("DetailBookingActivity", "courtId truyen di: " + courtId);
                startActivity(detailIntent);
                finish();
            }
        });

        // Xử lý nút Quay về: quay trở lại MainActivity
        btnQuayVe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(PaymentSuccessActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
            }
        });
    }
}
