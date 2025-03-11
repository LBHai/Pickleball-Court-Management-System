package SEP490.G9;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.google.zxing.BarcodeFormat;

import Socket.PaymentSocketListener;

public class QRCodeActivity extends AppCompatActivity {

    private ImageView ivQRCode;
    private ImageButton btnBack;
    private TextView tvCountdownTimer;
    private CountDownTimer countDownTimer;
    private long timeoutTimeMillis; // thời gian thanh toán hết (epoch millis)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        ivQRCode = findViewById(R.id.ivQRCode);
        btnBack = findViewById(R.id.btnBack);
        tvCountdownTimer = findViewById(R.id.tvCountdownTimer);

        // Nút Back chuyển về MainActivity
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(QRCodeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Lấy dữ liệu truyền qua Intent
        String qrCodeData = getIntent().getStringExtra("qrCodeData");
        timeoutTimeMillis = getIntent().getLongExtra("timeoutTimeMillis", 0);
        String orderId = getIntent().getStringExtra("orderId");

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

        // Khởi tạo socket sử dụng orderId làm key
        if (orderId != null && !orderId.isEmpty()) {
            PaymentSocketListener.PaymentStatusCallback callback = new PaymentSocketListener.PaymentStatusCallback() {
                @Override
                public void onPaymentSuccess(String orderId) {
                    // Khi thanh toán thành công, chuyển sang màn hình cảm ơn
                    Intent intent = new Intent(QRCodeActivity.this, PaymentSuccessActivity.class);
                    intent.putExtra("resCode", 200);
                    intent.putExtra("orderId", orderId);
                    startActivity(intent);
                    finish();
                }
            };
            PaymentSocketListener socketListener = new PaymentSocketListener(orderId, callback);
            socketListener.connect();
        }

        // Bắt đầu đếm ngược dựa trên thời gian thanh toán hết
        startCountdown();
    }

    private void startCountdown() {
        long remainingTimeMillis = timeoutTimeMillis - System.currentTimeMillis();
        if (remainingTimeMillis <= 0) {
            tvCountdownTimer.setText("Hết thời gian thanh toán");
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(QRCodeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }, 1500);
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
                new Handler().postDelayed(() -> {
                    Intent intent = new Intent(QRCodeActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }, 1500);
            }
        }.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCountdown();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(QRCodeActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
