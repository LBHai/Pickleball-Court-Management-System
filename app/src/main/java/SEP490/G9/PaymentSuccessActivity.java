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
    private String orderId, totalTime, selectedDate;
    private int totalPrice;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_success);

        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        tvSubTitle = findViewById(R.id.tvSubTitle);
        btnXemLichDatChiTiet = findViewById(R.id.btnXemLichDatChiTiet);
        btnQuayVe = findViewById(R.id.btnQuayVe);

        // Nhận dữ liệu từ Intent
        orderId = getIntent().getStringExtra("orderId");
        totalTime = getIntent().getStringExtra("totalTime");
        selectedDate = getIntent().getStringExtra("selectedDate");
        totalPrice = getIntent().getIntExtra("totalPrice", 0);

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(PaymentSuccessActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        tvTitle.setText("Đặt lịch thành công");
        tvSubTitle.setText("Lịch đặt của bạn đã được gửi tới chủ sân.\nVui lòng kiểm tra trạng thái tại mục \"Tài khoản\" để kiểm soát và xác nhận lịch đặt.");

        btnXemLichDatChiTiet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PaymentSuccessActivity.this, DetailBookingActivity.class);
                // Truyền các trường bổ sung
                intent.putExtra("orderId", orderId);
                intent.putExtra("totalTime", totalTime);
                intent.putExtra("selectedDate", selectedDate);
                intent.putExtra("totalPrice", totalPrice);

                startActivity(intent);
                finish();
            }
        });

        btnQuayVe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PaymentSuccessActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
