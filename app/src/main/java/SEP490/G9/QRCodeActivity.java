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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import Api.NetworkUtils;
import Api.RetrofitClient;
import Model.Orders;
import retrofit2.Call;
import Socket.PaymentSocketListener;
import Socket.PaymentSocketListener.ExtendedPaymentStatusCallback;
import Holder.DataHolder;

public class QRCodeActivity extends AppCompatActivity {

    private ImageView ivQRCode;
    private ImageButton btnBack;
    private TextView tvCountdownTimer, tvWarning;
    private CountDownTimer countDownTimer;
    private Handler handler = new Handler();

    private long timeoutTimeMillis;
    private String orderId;
    private boolean hasRedirected = false;
    private int overallTotalPrice, totalPrice, depositAmount, paymentAmount;
    private boolean isDeposit;
    private String orderStatus, totalTime, selectedDate, source, courtId;
    private ArrayList<Integer> slotPrices;
    private PaymentSocketListener socketListener;
    private Handler socketHandler = new Handler();
    private Runnable socketCheckRunnable = new Runnable() {
        @Override
        public void run() {
            if (socketListener != null && !socketListener.isConnected()) {
                Log.d("QRCodeActivity", "Socket disconnected, attempting to reconnect...");
                socketListener.connect();
            }
            socketHandler.postDelayed(this, 1000);
        }
    };

    private Handler timeoutHandler = new Handler();
    private Runnable updateTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            updatePaymentTimeout();
            timeoutHandler.postDelayed(this, 30000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        ivQRCode = findViewById(R.id.ivQRCode);
        btnBack = findViewById(R.id.btnBack);
        tvCountdownTimer = findViewById(R.id.tvCountdownTimer);
        tvWarning = findViewById(R.id.tvWarning);

        // Nhận dữ liệu truyền từ Intent
        String qrCodeData = getIntent().getStringExtra("qrCodeData");
        String paymentTimeoutStr = getIntent().getStringExtra("paymentTimeout");
        orderId = getIntent().getStringExtra("orderId");
        Log.d("QRCode", "orderId: " + orderId);
        totalTime = getIntent().getStringExtra("totalTime");
        selectedDate = getIntent().getStringExtra("selectedDate");
        totalPrice = getIntent().getIntExtra("totalPrice", 0);
        source = getIntent().getStringExtra("source");
        depositAmount = getIntent().getIntExtra("depositAmount", 0);
        overallTotalPrice = getIntent().getIntExtra("overallTotalPrice", 0);
        isDeposit = getIntent().getBooleanExtra("isDeposit", false);
        orderStatus = getIntent().getStringExtra("orderStatus");
        courtId = getIntent().getStringExtra("courtId");
        paymentAmount = getIntent().getIntExtra("paymentAmount", 0);
        slotPrices = getIntent().getIntegerArrayListExtra("slotPrices");

        // Log danh sách slotPrices
        if (slotPrices != null) {
            for (Integer price : slotPrices) {
                Log.d("QRCodeActivity", "Slot price: " + price);
            }
        } else {
            Log.d("QRCodeActivity", "Không có dữ liệu slotPrices được truyền qua Intent");
        }

        // Lưu slotPrices vào DataHolder
        DataHolder.getInstance().setSlotPrices(slotPrices);

        // Nếu không có qrCodeData, gọi API để lấy
        if (qrCodeData == null || qrCodeData.isEmpty()) {
            if (orderId != null && !orderId.isEmpty()) {
                fetchOrderDetails(orderId);
            } else {
                Toast.makeText(this, "Không có orderId để lấy dữ liệu QR Code", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            processQRCodeData(qrCodeData, paymentTimeoutStr);
        }

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(QRCodeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        timeoutHandler.post(updateTimeoutRunnable);

        if (orderId != null && !orderId.isEmpty()) {
            socketListener = new PaymentSocketListener(orderId, new ExtendedPaymentStatusCallback() {
                @Override
                public void onPaymentSuccess(String orderIdFromSocket) {
                    runOnUiThread(() -> handlePaymentSuccess(orderIdFromSocket));
                }
                @Override
                public void onPaymentFailure(String error) {
                    runOnUiThread(() -> navigateToDetailBooking());
                }
            });
            socketListener.connect();
        }
    }

    private void fetchOrderDetails(String orderId) {
        Call<Orders> call = RetrofitClient.getApiService(this).getOrderById(orderId);
        NetworkUtils.callApi(call, this, new NetworkUtils.ApiCallback<Orders>() {
            @Override
            public void onSuccess(Orders order) {
                if (order != null) {
                    String qrCodeData = order.getQrcode();
                    String paymentTimeoutStr = order.getPaymentTimeout();
                    if (qrCodeData != null && !qrCodeData.isEmpty()) {
                        processQRCodeData(qrCodeData, paymentTimeoutStr);
                    } else {
                        Toast.makeText(QRCodeActivity.this, "Không có dữ liệu QR Code từ API", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(QRCodeActivity.this, "Không thể lấy thông tin đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("QRCodeActivity", "Lỗi khi lấy dữ liệu: " + errorMessage);
                Toast.makeText(QRCodeActivity.this, "Lỗi khi lấy dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processQRCodeData(String qrCodeData, String paymentTimeoutStr) {
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
            String formattedPaymentAmount = decimalFormat.format(paymentAmount > 0 ? paymentAmount : totalPrice);
            tvWarning.setText("Vui lòng chuyển khoản " + formattedPaymentAmount + "₫ để hoàn tất thanh toán!");
        }

        if (paymentTimeoutStr != null && !paymentTimeoutStr.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSS", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
                Date timeoutDate = sdf.parse(paymentTimeoutStr);
                timeoutTimeMillis = timeoutDate.getTime();
            } catch (Exception e) {
                Log.e("QRCodeActivity", "Lỗi parse paymentTimeout: " + e.getMessage());
                timeoutTimeMillis = System.currentTimeMillis() + 15 * 60 * 1000;
            }
        } else {
            timeoutTimeMillis = System.currentTimeMillis() + 15 * 60 * 1000;
        }

        startCountdown();
    }

    private void startCountdown() {
        long remainingTime = timeoutTimeMillis - System.currentTimeMillis();
        if (remainingTime <= 0) {
            tvCountdownTimer.setText("Hết thời gian thanh toán");
            navigateToDetailBooking();
            return;
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
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
                navigateToDetailBooking();
            }
        }.start();
    }

    private void updatePaymentTimeout() {
        Call<Orders> call = RetrofitClient.getApiService(this).getOrderById(orderId);
        NetworkUtils.callApi(call, this, new NetworkUtils.ApiCallback<Orders>() {
            @Override
            public void onSuccess(Orders order) {
                String paymentTimeoutStr = order.getPaymentTimeout();
                if (paymentTimeoutStr != null && !paymentTimeoutStr.isEmpty()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSS", Locale.getDefault());
                    try {
                        Date timeoutDate = sdf.parse(paymentTimeoutStr);
                        if (timeoutDate != null) {
                            long newTimeoutMillis = timeoutDate.getTime();
                            if (newTimeoutMillis > System.currentTimeMillis()) {
                                timeoutTimeMillis = newTimeoutMillis;
                                runOnUiThread(() -> startCountdown());
                            }
                        }
                    } catch (Exception e) {
                        Log.e("QRCodeActivity", "Lỗi parse paymentTimeout: " + e.getMessage());
                    }
                }
            }
            @Override
            public void onError(String errorMessage) {
                Log.e("QRCodeActivity", "Lỗi cập nhật thời gian thanh toán: " + errorMessage);
            }
        });
    }

    private void handlePaymentSuccess(String orderIdFromSocket) {
        String orderIdToUse = (orderIdFromSocket != null && !orderIdFromSocket.isEmpty()) ? orderIdFromSocket : this.orderId;
        if (orderIdToUse == null || orderIdToUse.isEmpty()) {
            Log.e("QRCodeActivity", "Không có orderId hợp lệ để xử lý!");
            return;
        }
        if (!hasRedirected && !isFinishing()) {
            hasRedirected = true;
            Log.d("QRCodeActivity", "Payment success với orderId: " + orderIdToUse);
            Intent intent = new Intent(QRCodeActivity.this, PaymentSuccessActivity.class);
            intent.putExtra("resCode", 200);
            intent.putExtra("orderId", orderIdToUse);
            intent.putExtra("totalTime", totalTime);
            intent.putExtra("selectedDate", selectedDate);
            intent.putExtra("totalPrice", totalPrice);
            intent.putExtra("courtId", courtId);
            // Không cần truyền slotPrices qua Intent vì đã lưu vào DataHolder
            Log.d("QRCodeActivity", "slotPrices đã được lưu vào DataHolder");
            startActivity(intent);
            finish();
        }
    }

    private void navigateToDetailBooking() {
        if (!hasRedirected && !isFinishing()) {
            hasRedirected = true;
            Log.d("QRCodeActivity", "Navigating to DetailBookingActivity");
            Intent intent = new Intent(QRCodeActivity.this, PaymentFailedActivity.class);
            intent.putExtra("orderId", orderId);
            intent.putExtra("totalTime", totalTime);
            intent.putExtra("selectedDate", selectedDate);
            intent.putExtra("totalPrice", totalPrice);
            intent.putExtra("orderStatus", orderStatus);
            intent.putExtra("courtId", courtId);
            // Không cần truyền slotPrices qua Intent vì đã lưu vào DataHolder
            Log.d("QRCodeActivity", "slotPrices đã được lưu vào DataHolder");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        socketHandler.post(socketCheckRunnable);
        timeoutHandler.post(updateTimeoutRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        socketHandler.removeCallbacks(socketCheckRunnable);
        timeoutHandler.removeCallbacks(updateTimeoutRunnable);
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
