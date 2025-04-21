package UI.Activity;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import Data.Network.NetworkUtils;
import Data.Network.RetrofitClient;
import Data.Holder.DataHolder;
import Data.Model.Orders;
import SEP490.G9.R;
import Data.Socket.PaymentSocketListener;
import Data.Socket.PaymentSocketListener.ExtendedPaymentStatusCallback;
import retrofit2.Call;

public class QRCodeActivity extends AppCompatActivity {

    private ImageView ivQRCode;
    private ImageButton btnBack;
    private TextView tvCountdownTimer, tvWarning;
    private Handler handler = new Handler();
    private Runnable countdownRunnable;

    private long timeoutTimeMillis;
    private long createdTimeMillis;
    private static final long DEFAULT_TIMEOUT_DURATION;
    private String orderId;
    private boolean hasRedirected = false;
    private int overallTotalPrice, totalPrice, depositAmount, paymentAmount, totalPriceFixedOrder;
    private boolean isDeposit;
    private String orderStatus, totalTime, selectedDate, source, courtId, orderType;
    private ArrayList<Integer> slotPrices;
    private PaymentSocketListener socketListener;
    private Orders currentOrder;

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

    static {
        String orderTypeFromIntent = null;
        try {
            Intent intent = new Intent();
            orderTypeFromIntent = intent.getStringExtra("orderType");
        } catch (Exception e) {
            Log.e("QRCodeActivity", "Lỗi khi lấy orderType từ Intent: " + e.getMessage());
        }
        if ("Đơn cố định".equals(orderTypeFromIntent)) {
            DEFAULT_TIMEOUT_DURATION = 15 * 60 * 1000; // 15 phút
        } else {
            DEFAULT_TIMEOUT_DURATION = 5 * 60 * 1000; // 5 phút
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        ivQRCode = findViewById(R.id.ivQRCode);
        btnBack = findViewById(R.id.btnBack);
        tvCountdownTimer = findViewById(R.id.tvCountdownTimer);
        tvWarning = findViewById(R.id.tvWarning);

        String qrCodeData = getIntent().getStringExtra("qrCodeData");
        String paymentTimeoutStr = getIntent().getStringExtra("paymentTimeout");
        orderId = getIntent().getStringExtra("orderId");
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
        orderType = getIntent().getStringExtra("orderType");
        totalPriceFixedOrder = getIntent().getIntExtra("totalPriceFixedOrder", 0);

        if (totalTime == null) {
            Log.e("QRCodeActivity", "totalTime là null, kiểm tra lại ConfirmActivity");
            totalTime = "0h00";
        }

        DataHolder.getInstance().setSlotPrices(slotPrices);
        DataHolder.getInstance().setTotalTime(totalTime);

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
                    currentOrder = order;
                    String qrCodeData = order.getQrcode();
                    String paymentTimeoutStr = order.getPaymentTimeout();
                    String createdAt = order.getCreatedAt();
                    if ("Chưa thanh toán".equals(order.getPaymentStatus()) || "Chưa đặt cọc".equals(order.getPaymentStatus())) {
                        if (createdAt != null && !createdAt.isEmpty()) {
                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSS", Locale.getDefault());
                                sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
                                Date createdDate = sdf.parse(createdAt);
                                createdTimeMillis = createdDate.getTime();
                                timeoutTimeMillis = createdTimeMillis + DEFAULT_TIMEOUT_DURATION;
                            } catch (Exception e) {
                                Log.e("QRCodeActivity", "Lỗi parse createdAt: " + e.getMessage());
                                createdTimeMillis = System.currentTimeMillis();
                                timeoutTimeMillis = createdTimeMillis + DEFAULT_TIMEOUT_DURATION;
                            }
                        }
                    }
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
                Toast.makeText(QRCodeActivity.this, "Lỗi khi lấy dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processQRCodeData(String qrCodeData, String paymentTimeoutStr) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(qrCodeData, BarcodeFormat.QR_CODE, 320, 320);
            ivQRCode.setImageBitmap(bitmap);

            // Chỉ khởi động bộ đếm thời gian khi qrCodeData được xử lý thành công
            startCountdown();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi tạo QR Code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            // Không khởi động bộ đếm thời gian nếu có lỗi
            return;
        }

        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        if ("Đơn cố định".equals(orderType)) {
            if (isDeposit) {
                String formattedTotalPriceFixedOrder = decimalFormat.format(totalPriceFixedOrder);
                tvWarning.setText("Vui lòng chuyển khoản " + formattedTotalPriceFixedOrder + "₫ để hoàn tất đặt cọc!");
            } else {
                String formattedTotalPriceFixedOrder = decimalFormat.format(totalPriceFixedOrder);
                tvWarning.setText("Vui lòng chuyển khoản " + formattedTotalPriceFixedOrder + "₫ để hoàn tất thanh toán!");
            }
        } else {
            if (isDeposit) {
                String formattedDeposit = decimalFormat.format(depositAmount);
                tvWarning.setText("Vui lòng chuyển khoản " + formattedDeposit + "₫ để hoàn tất đặt cọc!");
            } else {
                String formattedPaymentAmount = decimalFormat.format(paymentAmount > 0 ? paymentAmount : totalPrice);
                tvWarning.setText("Vui lòng chuyển khoản " + formattedPaymentAmount + "₫ để hoàn tất thanh toán!");
            }
        }
    }

    private void startCountdown() {
        if (createdTimeMillis == 0) {
            createdTimeMillis = System.currentTimeMillis();
            timeoutTimeMillis = createdTimeMillis + DEFAULT_TIMEOUT_DURATION;
        }

        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                long currentTimeMillis = System.currentTimeMillis();
                long remainingTime = timeoutTimeMillis - currentTimeMillis;
                if (remainingTime <= 0) {
                    tvCountdownTimer.setText("Hết thời gian thanh toán");
                    handler.removeCallbacks(countdownRunnable);
                    navigateToDetailBooking();
                } else {
                    long minutes = remainingTime / 60000;
                    long seconds = (remainingTime % 60000) / 1000;
                    tvCountdownTimer.setText(String.format("%02d:%02d", minutes, seconds));
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(countdownRunnable);
    }

    private void updatePaymentTimeout() {
        Call<Orders> call = RetrofitClient.getApiService(this).getOrderById(orderId);
        NetworkUtils.callApi(call, this, new NetworkUtils.ApiCallback<Orders>() {
            @Override
            public void onSuccess(Orders order) {
                if (order != null) {
                    currentOrder = order;
                    String createdAt = order.getCreatedAt();
                    if (createdAt != null && !createdAt.isEmpty()) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSS", Locale.getDefault());
                            sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
                            Date createdDate = sdf.parse(createdAt);
                            long newCreatedTimeMillis = createdDate.getTime();
                            if (newCreatedTimeMillis != createdTimeMillis) {
                                createdTimeMillis = newCreatedTimeMillis;
                                timeoutTimeMillis = createdTimeMillis + DEFAULT_TIMEOUT_DURATION;
                                handler.removeCallbacks(countdownRunnable);
                                startCountdown();
                            }
                        } catch (Exception e) {
                            Log.e("QRCodeActivity", "Lỗi parse createdAt trong update: " + e.getMessage());
                        }
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
            Intent intent = new Intent(QRCodeActivity.this, PaymentSuccessActivity.class);
            intent.putExtra("resCode", 200);
            intent.putExtra("orderId", orderIdToUse);
            intent.putExtra("totalTime", totalTime);
            intent.putExtra("serviceDetailsJson", getIntent().getStringExtra("serviceDetailsJson"));
            intent.putExtra("selectedDate", selectedDate);
            intent.putExtra("totalPrice", totalPrice);
            intent.putExtra("courtId", courtId);
            intent.putExtra("orderType", orderType);
            intent.putExtra("customerName", getIntent().getStringExtra("customerName"));  // Truyền tiếp
            intent.putExtra("phoneNumber", getIntent().getStringExtra("phoneNumber"));    // Truyền tiếp
            intent.putExtra("note", getIntent().getStringExtra("note"));                  // Truyền tiếp
            startActivity(intent);
            finish();
        }
    }

    private void navigateToDetailBooking() {
        if (!hasRedirected && !isFinishing()) {
            hasRedirected = true;
            Intent intent = new Intent(QRCodeActivity.this, PaymentFailedActivity.class);
            intent.putExtra("orderId", orderId);
            intent.putExtra("totalTime", totalTime);
            intent.putExtra("selectedDate", selectedDate);
            intent.putExtra("totalPrice", totalPrice);
            intent.putExtra("orderStatus", orderStatus);
            intent.putExtra("courtId", courtId);
            intent.putExtra("orderType", orderType);
            intent.putExtra("serviceDetailsJson", getIntent().getStringExtra("serviceDetailsJson"));
            intent.putExtra("customerName", getIntent().getStringExtra("customerName"));  // Truyền tiếp
            intent.putExtra("phoneNumber", getIntent().getStringExtra("phoneNumber"));    // Truyền tiếp
            intent.putExtra("note", getIntent().getStringExtra("note"));                  // Truyền tiếp
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
        if (countdownRunnable != null) {
            handler.post(countdownRunnable);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(countdownRunnable);
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