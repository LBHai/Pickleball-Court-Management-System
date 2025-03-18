package SEP490.G9;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private TextView tvInstruction, tvCountdownTimer, tvWarning;
    private LinearLayout layoutWarning;
    private CountDownTimer countDownTimer;
    private Handler handler = new Handler();

    private long timeoutTimeMillis;
    private String orderId;
    private boolean hasRedirected = false;
    private String source; // "order" nếu gọi từ Order (AccountFragment), null hoặc khác nếu từ checkout

    // Biến toàn cục để tránh lỗi "needs to be final or effectively final"
    private int overallTotalPrice;
    private int depositAmount;
    private boolean isDeposit;
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

        // Ánh xạ các View
        ivQRCode = findViewById(R.id.ivQRCode);
        btnBack = findViewById(R.id.btnBack);
        tvInstruction = findViewById(R.id.tvInstruction);
        tvCountdownTimer = findViewById(R.id.tvCountdownTimer);
        tvWarning = findViewById(R.id.tvWarning);
        layoutWarning = findViewById(R.id.layoutWarning);

        // Lấy dữ liệu từ Intent
        String qrCodeData = getIntent().getStringExtra("qrCodeData");
        timeoutTimeMillis = getIntent().getLongExtra("timeoutTimeMillis", 0);
        orderId = getIntent().getStringExtra("orderId");
        source = getIntent().getStringExtra("source"); // "order" nếu gọi từ AccountFragment
        depositAmount = getIntent().getIntExtra("depositAmount", 0);
        isDeposit = getIntent().getBooleanExtra("isDeposit", false);
        // Lấy tổng tiền từ Intent
        overallTotalPrice = getIntent().getIntExtra("overallTotalPrice", 0);

        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        if (isDeposit) {
            String formattedDeposit = decimalFormat.format(depositAmount);
            String warningText = "Vui lòng chuyển khoản " + formattedDeposit + "₫ để hoàn tất đặt cọc!";
            tvWarning.setText(warningText);
        } else {
            String formattedPrice = decimalFormat.format(overallTotalPrice);
            String warningText = "Vui lòng chuyển khoản " + formattedPrice + "₫ để hoàn tất đặt lịch!";
            tvWarning.setText(warningText);
        }

        // Nút Back
        btnBack.setOnClickListener(v -> {
            if ("order".equals(source)) {
                finish();
            } else {
                redirectToMain();
            }
        });

        // Kiểm tra dữ liệu QR code
        if (qrCodeData == null || qrCodeData.isEmpty()) {
            Toast.makeText(this, "Không có dữ liệu QR Code", Toast.LENGTH_SHORT).show();
            return;
        }
        if (timeoutTimeMillis == 0) {
            Toast.makeText(this, "Không có thông tin thời gian thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo bitmap QR code
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(qrCodeData, BarcodeFormat.QR_CODE, 320, 320);
            ivQRCode.setImageBitmap(bitmap);
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi tạo QR Code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        // Nếu có orderId, thiết lập PaymentSocketListener để lắng nghe trạng thái thanh toán
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
                            // Thực hiện logic khi thanh toán thất bại
                            handleTimeout();
                        }
                    });
                }
            };
            socketListener = new PaymentSocketListener(orderId, callback);
            socketListener.connect();
            socketHandler.postDelayed(socketCheckRunnable, 500);
        }

        // Bắt đầu đếm ngược
        startCountdown();
    }

    private void startCountdown() {
        long remainingTimeMillis = timeoutTimeMillis - System.currentTimeMillis();
        if (remainingTimeMillis <= 0) {
            tvCountdownTimer.setText("Hết thời gian thanh toán");
            handler.postDelayed(this::handleTimeout, 1500);
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
                handler.postDelayed(QRCodeActivity.this::handleTimeout, 1500);
            }
        }.start();
    }

    private void handleTimeout() {
        if (!hasRedirected) {
            hasRedirected = true;
            if ("order".equals(source)) {
                // Gọi API cập nhật trạng thái đơn nếu cần
                // ...
                finish();
            } else {
                redirectToMain();
            }
        }
    }

    private void redirectToMain() {
        handler.removeCallbacksAndMessages(null);
        socketHandler.removeCallbacks(socketCheckRunnable);
        Intent intent = new Intent(QRCodeActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        // Nếu gọi từ Order thì chỉ finish, không về Main
        super.onBackPressed();
        if ("order".equals(source)) {
            finish();
        } else {
            redirectToMain();
        }
    }
}
