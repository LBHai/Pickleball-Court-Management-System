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

        // Ánh xạ các view
        ivQRCode = findViewById(R.id.ivQRCode);
        btnBack = findViewById(R.id.btnBack);
        tvCountdownTimer = findViewById(R.id.tvCountdownTimer);
        tvWarning = findViewById(R.id.tvWarning);

        // Lấy dữ liệu từ Intent
        String qrCodeData = getIntent().getStringExtra("qrCodeData");
        timeoutTimeMillis = getIntent().getLongExtra("timeoutTimeMillis", 0);
        orderId = getIntent().getStringExtra("orderId");
        courtName = getIntent().getStringExtra("courtName");
        address = getIntent().getStringExtra("address");
        totalTime = getIntent().getStringExtra("totalTime");
        note = getIntent().getStringExtra("note");
        selectedDate = getIntent().getStringExtra("selectedDate");
        totalPrice = getIntent().getIntExtra("totalPrice", 0);
        source = getIntent().getStringExtra("source");
        depositAmount = getIntent().getIntExtra("depositAmount", 0);
        overallTotalPrice = getIntent().getIntExtra("overallTotalPrice", 0);
        name = getIntent().getStringExtra("name");
        phone = getIntent().getStringExtra("phone");
        isDeposit = getIntent().getBooleanExtra("isDeposit", false);

        // Hiển thị cảnh báo chuyển khoản dựa trên hình thức thanh toán
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
            if ("order".equals(source)) {
                finish();
            } else {
                redirectToMain();
            }
        });

        // Kiểm tra dữ liệu QR Code và timeout
        if (qrCodeData == null || qrCodeData.isEmpty()) {
            Toast.makeText(this, "Không có dữ liệu QR Code", Toast.LENGTH_SHORT).show();
            return;
        }
        if (timeoutTimeMillis == 0) {
            Toast.makeText(this, "Không có thông tin thời gian thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo bitmap QR Code
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(qrCodeData, BarcodeFormat.QR_CODE, 320, 320);
            ivQRCode.setImageBitmap(bitmap);
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi tạo QR Code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        // Thiết lập PaymentSocketListener nếu có orderId
        if (orderId != null && !orderId.isEmpty()) {
            ExtendedPaymentStatusCallback callback = new ExtendedPaymentStatusCallback() {
                @Override
                public void onPaymentSuccess(String orderId) {
                    runOnUiThread(() -> {
                        if (!hasRedirected) {
                            hasRedirected = true;
                            Log.d("QRCodeActivity", "onPaymentSuccess: " + orderId);
                            Intent intent = new Intent(QRCodeActivity.this, PaymentSuccessActivity.class);
                            intent.putExtra("resCode", 200);
                            intent.putExtra("orderId", orderId);
                            intent.putExtra("courtName", courtName);
                            intent.putExtra("address", address);
                            intent.putExtra("totalTime", totalTime);
                            intent.putExtra("note", note);
                            intent.putExtra("selectedDate", selectedDate);
                            intent.putExtra("totalPrice", totalPrice);
                            intent.putExtra("phone", phone);
                            intent.putExtra("name", name);
                            startActivity(intent);
                            finish();
                        }
                    });
                }

                @Override
                public void onPaymentFailure(String error) {
                    runOnUiThread(() -> {
                        if (!hasRedirected) {
                            hasRedirected = true;
                            Log.e("QRCodeActivity", "Payment failure: " + error);
                            handleTimeout();
                        }
                    });
                }
            };
            socketListener = new PaymentSocketListener(orderId, callback);
            socketListener.connect();
            socketHandler.postDelayed(socketCheckRunnable, 500);
        }

        // Bắt đầu đếm ngược thời gian thanh toán
        startCountdown();
    }

    // Hàm đếm ngược sử dụng timeoutTimeMillis nhận được từ API
    private void startCountdown() {
        long remainingTimeMillis = timeoutTimeMillis - System.currentTimeMillis();
        Log.d("QRCodeActivity", "Thời gian còn lại (ms): " + remainingTimeMillis);
        if (remainingTimeMillis <= 0) {
            tvCountdownTimer.setText("Hết thời gian thanh toán");
            // Gọi chuyển màn hình ngay lập tức nếu thời gian đã hết
            handler.post(() -> handleTimeout());
            return;
        }

        if (countDownTimer != null) {
            countDownTimer.cancel();
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
                // Đảm bảo chuyển hướng khi đếm ngược kết thúc
                handleTimeout();
            }
        }.start();
    }

    // Hàm chuyển hướng sang PaymentFailedActivity khi hết thời gian
    private void handleTimeout() {
        if (!hasRedirected) {
            hasRedirected = true;
            Log.d("QRCodeActivity", "Hết thời gian thanh toán. Chuyển hướng sang PaymentFailedActivity.");
            Intent intent = new Intent(QRCodeActivity.this, PaymentFailedActivity.class);
            intent.putExtra("orderId", orderId);
            startActivity(intent);
            finish();
        }
    }

    // Hàm chuyển về MainActivity nếu cần
    private void redirectToMain() {
        handler.removeCallbacksAndMessages(null);
        socketHandler.removeCallbacks(socketCheckRunnable);
        Intent intent = new Intent(QRCodeActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Nếu Activity chưa bị chuyển hướng, khởi chạy lại đếm ngược
        if (!hasRedirected) {
            startCountdown();
        }
        if (socketListener != null) {
            socketHandler.postDelayed(socketCheckRunnable, 500);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        socketHandler.removeCallbacks(socketCheckRunnable);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if ("order".equals(source)) {
            finish();
        } else {
            redirectToMain();
        }
    }
}
