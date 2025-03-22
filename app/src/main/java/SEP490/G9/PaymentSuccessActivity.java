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
    private String orderId, courtName, address, totalTime, note, selectedDate, phone, name;
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
        courtName = getIntent().getStringExtra("courtName");
        address = getIntent().getStringExtra("address");
        totalTime = getIntent().getStringExtra("totalTime");
        note = getIntent().getStringExtra("note");
        selectedDate = getIntent().getStringExtra("selectedDate");
        totalPrice = getIntent().getIntExtra("totalPrice", 0);
        name = getIntent().getStringExtra("name");
        phone = getIntent().getStringExtra("phone");
        Log.d("QRCodeActivity", "Name: " + name + ", Phone: " + phone);
        // Log kiểm tra
        Log.d("PaymentSuccessActivity", "orderId nhận được: " + orderId);
        Log.d("PaymentSuccessActivity", "courtName nhận được: " + courtName);
        Log.d("PaymentSuccessActivity", "address nhận được: " + address);
        Log.d("PaymentSuccessActivity", "totalTime nhận được: " + totalTime);
        Log.d("PaymentSuccessActivity", "note nhận được: " + note);
        Log.d("QRCodeActivity", "Selected Date: " + selectedDate);
        Log.d("QRCodeActivity", "Total Price: " + totalPrice);
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
                intent.putExtra("courtName", courtName);
                intent.putExtra("address", address);
                intent.putExtra("totalTime", totalTime);
                intent.putExtra("note", note);
                intent.putExtra("selectedDate", selectedDate);
                intent.putExtra("totalPrice", totalPrice);
                intent.putExtra("phone", phone);
                intent.putExtra("name", name);
                Log.d("PaymentSuccessActivity", "orderId truyền đi: " + orderId);
                Log.d("PaymentSuccessActivity", "courtName truyền đi: " + courtName);
                Log.d("PaymentSuccessActivity", "address truyền đi: " + address);
                Log.d("PaymentSuccessActivity", "totalTime truyền đi: " + totalTime);
                Log.d("PaymentSuccessActivity", "note truyền đi: " + note);
                Log.d("PaymentSuccessActivity", "totalPrice truyền đi: " + totalPrice);
                Log.d("PaymentSuccessActivity", "selectedDate truyền đi: " + selectedDate);
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
