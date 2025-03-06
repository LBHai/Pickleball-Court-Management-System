package SEP490.G9;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.google.zxing.BarcodeFormat;

public class QRCodeActivity extends AppCompatActivity {

    private ImageView ivQRCode;
    private ImageButton btnBack;
    private TextView tvCountdownTimer; // Sử dụng biến tvCountdownTimer
    private CountDownTimer countDownTimer;
    private long timeoutTimeMillis; // Thời gian kết thúc thanh toán (epoch millis)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        ivQRCode = findViewById(R.id.ivQRCode);
        btnBack = findViewById(R.id.btnBack);
        tvCountdownTimer = findViewById(R.id.tvCountdownTimer);

        // Xử lý nút Back trên layout: chuyển về MainActivity
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(QRCodeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Lấy dữ liệu được truyền qua Intent
        String qrCodeData = getIntent().getStringExtra("qrCodeData");
        timeoutTimeMillis = getIntent().getLongExtra("timeoutTimeMillis", 0);

        if (qrCodeData == null || qrCodeData.isEmpty()){
            Toast.makeText(this, "Không có dữ liệu QR Code", Toast.LENGTH_SHORT).show();
            return;
        }
        if (timeoutTimeMillis == 0) {
            Toast.makeText(this, "Không có thông tin thời gian thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(qrCodeData, BarcodeFormat.QR_CODE, 320, 320);
            ivQRCode.setImageBitmap(bitmap);
        } catch(Exception e) {
            Toast.makeText(this, "Lỗi tạo QR Code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        // Bắt đầu đếm ngược dựa trên thời gian kết thúc đã lưu
        startCountdown();
    }

    private void startCountdown() {
        // Tính toán thời gian còn lại dựa trên thời gian hiện tại
        long remainingTimeMillis = timeoutTimeMillis - System.currentTimeMillis();
        if (remainingTimeMillis <= 0) {
            tvCountdownTimer.setText("Hết thời gian thanh toán");
            return;
        }
        countDownTimer = new CountDownTimer(remainingTimeMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                tvCountdownTimer.setText(String.format("%02d:%02d", minutes, seconds));
            }
            @Override
            public void onFinish() {
                tvCountdownTimer.setText("Hết thời gian thanh toán");
            }
        }.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Mỗi khi activity được hiển thị lại, khởi tạo lại CountDownTimer dựa trên thời gian kết thúc đã lưu
        startCountdown();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Hủy CountDownTimer khi activity tạm dừng để tránh rò rỉ bộ nhớ
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    // Override nút back của hệ thống để chuyển về MainActivity
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(QRCodeActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
