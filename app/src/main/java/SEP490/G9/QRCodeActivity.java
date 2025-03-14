package SEP490.G9;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.google.zxing.BarcodeFormat;
import Socket.PaymentSocketListener;
import Socket.PaymentSocketListener.ExtendedPaymentStatusCallback;

public class QRCodeActivity extends AppCompatActivity {

    private ImageView ivQRCode;
    private ImageButton btnBack;
    private TextView tvCountdownTimer;
    private CountDownTimer countDownTimer;
    private long timeoutTimeMillis;
    private String orderId;

    private PaymentSocketListener socketListener;
    private Handler socketHandler = new Handler();
    private Runnable socketCheckRunnable = new Runnable() {
        @Override
        public void run() {
            if (socketListener != null && !socketListener.isConnected()) {
                Log.d("QRCodeActivity", "Socket disconnected, attempting to reconnect...");
                socketListener.connect();
            }
            socketHandler.postDelayed(this, 500);
        }
    };

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

        // Lấy dữ liệu từ Intent
        String qrCodeData = getIntent().getStringExtra("qrCodeData");
        timeoutTimeMillis = getIntent().getLongExtra("timeoutTimeMillis", 0);
        orderId = getIntent().getStringExtra("orderId");

        if (qrCodeData == null || qrCodeData.isEmpty()) {
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
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi tạo QR Code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        if (orderId != null && !orderId.isEmpty()) {
            ExtendedPaymentStatusCallback callback = new ExtendedPaymentStatusCallback() {
                @Override
                public void onPaymentSuccess(String orderId) {
                    runOnUiThread(() -> {
                        Log.d("QRCodeActivity", "onPaymentSuccess called with orderId: " + orderId);
                        // Ở đây chuyển sang PaymentSuccessActivity và truyền club_id
                        Intent intent = new Intent(QRCodeActivity.this, PaymentSuccessActivity.class);
                        intent.putExtra("resCode", 200);
                        intent.putExtra("orderId", orderId);
                        startActivity(intent);
                        finish();
                    });
                }

                @Override
                public void onPaymentFailure(String error) {
                    runOnUiThread(() -> {
                        Log.e("QRCodeActivity", "Payment failure: " + error);
                        Toast.makeText(QRCodeActivity.this, "Thanh toán thất bại: " + error, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(QRCodeActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    });
                }
            };
            socketListener = new PaymentSocketListener(orderId, callback);
            socketListener.connect();
            socketHandler.postDelayed(socketCheckRunnable, 500);
        }

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
        if (socketListener != null) {
            socketHandler.postDelayed(socketCheckRunnable, 500);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        socketHandler.removeCallbacks(socketCheckRunnable);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(QRCodeActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
