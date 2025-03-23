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
import java.text.DecimalFormat;
import Socket.PaymentSocketListener;
import Socket.PaymentSocketListener.ExtendedPaymentStatusCallback;

public class QRCodeActivity extends AppCompatActivity {

    private ImageView ivQRCode;
    private ImageButton btnBack;
    private TextView tvCountdownTimer, tvWarning;
    private CountDownTimer countDownTimer;
    private Handler handler = new Handler();

    private long timeoutTimeMillis;
    private String orderId;
    private boolean hasRedirected = false;
    private int overallTotalPrice, totalPrice, depositAmount;
    private boolean isDeposit;
    private String courtName, note, address, totalTime, selectedDate, source, name, phone;

    private PaymentSocketListener socketListener;
    // Handler và Runnable để kiểm tra kết nối socket mỗi 0.5 giây
    private Handler socketHandler = new Handler();
    private Runnable socketCheckRunnable = new Runnable() {
        @Override
        public void run() {
            if (socketListener != null && !socketListener.isConnected()) {
                Log.d("QRCodeActivity", "Socket disconnected, attempting to reconnect...");
                socketListener.connect();
            }
            socketHandler.postDelayed(this, 500);  // Kiểm tra lại sau 0.5 giây
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        // Ánh xạ các view
        ivQRCode = findViewById(R.id.ivQRCode);
        btnBack = findViewById(R.id.btnBack);
        tvCountdownTimer = findViewById(R.id.tvCountdownTimer);
        tvWarning = findViewById(R.id.tvWarning);

        // Lấy dữ liệu từ Intent
        String qrCodeData = getIntent().getStringExtra("qrCodeData");
        timeoutTimeMillis = getIntent().getLongExtra("timeoutTimeMillis", 0);
        orderId = getIntent().getStringExtra("orderId");
        totalTime = getIntent().getStringExtra("totalTime");
        selectedDate = getIntent().getStringExtra("selectedDate");
        totalPrice = getIntent().getIntExtra("totalPrice", 0);
        source = getIntent().getStringExtra("source");
        depositAmount = getIntent().getIntExtra("depositAmount", 0);
        overallTotalPrice = getIntent().getIntExtra("overallTotalPrice", 0);
        isDeposit = getIntent().getBooleanExtra("isDeposit", false);

        // Kiểm tra dữ liệu cần thiết
        if (qrCodeData == null || qrCodeData.isEmpty()) {
            Toast.makeText(this, "Không có dữ liệu QR Code", Toast.LENGTH_SHORT).show();
            return;
        }
        if (timeoutTimeMillis == 0) {
            Toast.makeText(this, "Không có thông tin thời gian thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo QR Code và hiển thị
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(qrCodeData, BarcodeFormat.QR_CODE, 320, 320);
            ivQRCode.setImageBitmap(bitmap);
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi tạo QR Code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        if (isDeposit) {
            String formattedDeposit = decimalFormat.format(depositAmount);
            tvWarning.setText("Vui lòng chuyển khoản " + formattedDeposit + "₫ để hoàn tất đặt cọc!");
        } else {
            String formattedPrice = decimalFormat.format(overallTotalPrice);
            tvWarning.setText("Vui lòng chuyển khoản " + formattedPrice + "₫ để hoàn tất đặt lịch!");
        }

        // Xử lý nút Back
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(QRCodeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });


        // Bắt đầu đếm ngược thời gian thanh toán
        startCountdown();

        // Thiết lập PaymentSocketListener để lắng nghe sự kiện thanh toán
        if (orderId != null && !orderId.isEmpty()) {
            socketListener = new PaymentSocketListener(orderId, new ExtendedPaymentStatusCallback() {
                @Override
                public void onPaymentSuccess(String orderId) {
                    // Khi thanh toán thành công, chuyển sang PaymentSuccessActivity
                    runOnUiThread(() -> handlePaymentSuccess(orderId));
                }

                @Override
                public void onPaymentFailure(String error) {
                    // Nếu thanh toán thất bại, chuyển sang PaymentFailedActivity
                    runOnUiThread(() -> {
                        Toast.makeText(QRCodeActivity.this, "Thanh toán thất bại: " + error, Toast.LENGTH_SHORT).show();
                        navigateToPaymentFailed();
                    });
                }
            });
            socketListener.connect();
        }
    }

    /**
     * Khởi tạo CountDownTimer dựa trên timeoutTimeMillis nhận từ API.
     */
    private void startCountdown() {
        long remainingTime = timeoutTimeMillis - System.currentTimeMillis();

        if (remainingTime <= 0) {
            tvCountdownTimer.setText("Hết thời gian thanh toán");
            navigateToPaymentFailed();
            return;
        }

        countDownTimer = new CountDownTimer(remainingTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                tvCountdownTimer.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                tvCountdownTimer.setText("Hết thời gian thanh toán");
                navigateToPaymentFailed();
            }
        }.start();
    }

    /**
     * Xử lý sự kiện thanh toán thành công.
     */
    private void handlePaymentSuccess(String orderId) {
        if (!hasRedirected && !isFinishing()) {
            hasRedirected = true;
            Log.d("QRCodeActivity", "Payment success: " + orderId);
            Intent intent = new Intent(QRCodeActivity.this, PaymentSuccessActivity.class);
            intent.putExtra("resCode", 200);
            intent.putExtra("orderId", orderId);
            intent.putExtra("totalTime", totalTime);
            intent.putExtra("selectedDate", selectedDate);
            intent.putExtra("totalPrice", totalPrice);
            startActivity(intent);
            finish();
        }
    }

    /**
     * Chuyển hướng sang PaymentFailedActivity khi thanh toán thất bại hoặc hết thời gian.
     */
    private void navigateToPaymentFailed() {
        if (!hasRedirected && !isFinishing()) {
            hasRedirected = true;
            Log.d("QRCodeActivity", "Navigating to PaymentFailedActivity");
            Intent intent = new Intent(QRCodeActivity.this, PaymentFailedActivity.class);
            intent.putExtra("orderId", orderId);
            intent.putExtra("totalTime", totalTime);
            intent.putExtra("selectedDate", selectedDate);
            intent.putExtra("totalPrice", totalPrice);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    // Khởi động socket check khi Activity hiển thị
    @Override
    protected void onResume() {
        super.onResume();
        socketHandler.post(socketCheckRunnable);
    }

    // Dừng socket check khi Activity bị ẩn
    @Override
    protected void onPause() {
        super.onPause();
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        socketHandler.removeCallbacks(socketCheckRunnable);
        if (socketListener != null) {
            socketListener.disconnect();
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
