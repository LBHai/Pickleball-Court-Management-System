package UI.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
    private boolean isRemainingPayment;
    private long timeoutTimeMillis;
    private String orderId, setpaymentimeoutStr;
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
        isRemainingPayment = getIntent().getBooleanExtra("isRemainingPayment", false);
        setpaymentimeoutStr = getIntent().getStringExtra("setPaymentTimeout");


        Log.d("QRCodeActivity", "Nhận từ ConfirmActivity:");
        Log.d("QRCodeActivity", "qrCodeData: " + qrCodeData);
        Log.d("QRCodeActivity", "paymentTimeoutStr: " + paymentTimeoutStr);
        Log.d("QRCodeActivity", "orderId: " + orderId);
        Log.d("QRCodeActivity", "totalTime: " + totalTime);
        Log.d("QRCodeActivity", "selectedDate: " + selectedDate);
        Log.d("QRCodeActivity", "totalPrice: " + totalPrice);
        Log.d("QRCodeActivity", "source: " + source);
        Log.d("QRCodeActivity", "depositAmount: " + depositAmount);
        Log.d("QRCodeActivity", "overallTotalPrice: " + overallTotalPrice);
        Log.d("QRCodeActivity", "isDeposit: " + isDeposit);
        Log.d("QRCodeActivity", "orderStatus: " + orderStatus);
        Log.d("QRCodeActivity", "courtId: " + courtId);
        Log.d("QRCodeActivity", "paymentAmount: " + paymentAmount);
        Log.d("QRCodeActivity", "slotPrices: " + (slotPrices != null ? slotPrices.toString() : "null"));
        Log.d("QRCodeActivity", "orderType: " + orderType);
        if (orderType == null) {
            orderType = "Unknown";
            Log.w("QRCodeActivity", "orderType là null, đặt thành 'Unknown'");
        }
        if (totalTime == null) {
            totalTime = "0h00";
            Log.w("QRCodeActivity", "totalTime là null, đặt thành '0h00'");
        }
        if (selectedDate == null) {
            selectedDate = "N/A";
            Log.w("QRCodeActivity", "selectedDate là null, đặt thành 'N/A'");
        }
        if (orderId == null) {
            orderId = "";
            Log.w("QRCodeActivity", "orderId là null, đặt thành rỗng");
        }
        if (setpaymentimeoutStr != null && !setpaymentimeoutStr.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSS", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
                Date timeoutDate = sdf.parse(setpaymentimeoutStr);
                timeoutTimeMillis = timeoutDate.getTime();
            } catch (Exception e) {
                Log.e("QRCodeActivity", "Lỗi parse setpaymentimeoutStr: " + e.getMessage());
                timeoutTimeMillis = System.currentTimeMillis() + 5 * 60 * 1000; // Fallback
            }
        } else if (paymentTimeoutStr != null && !paymentTimeoutStr.isEmpty()) {
            // Sử dụng paymentTimeoutStr nếu setpaymentimeoutStr không có
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSS", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
                Date timeoutDate = sdf.parse(paymentTimeoutStr);
                timeoutTimeMillis = timeoutDate.getTime();
            } catch (Exception e) {
                Log.e("QRCodeActivity", "Lỗi parse paymentTimeoutStr: " + e.getMessage());
                timeoutTimeMillis = System.currentTimeMillis() + 5 * 60 * 1000; // Fallback
            }
        } else {
            timeoutTimeMillis = System.currentTimeMillis() + 5 * 60 * 1000; // Fallback mặc định
        }

        // Tiếp tục xử lý QR code và countdown như trong mã gốc
        processQRCodeData(qrCodeData, setpaymentimeoutStr != null ? setpaymentimeoutStr : paymentTimeoutStr);

        if (totalTime == null) {
            Log.e("QRCodeActivity", "totalTime là null, kiểm tra lại ConfirmActivity");
            totalTime = "0h00";
        }
        Log.d("QRCodeActivity", "qrCodeData từ Intent: " + qrCodeData);
        Log.d("QRCodeActivity", "orderId: " + orderId);
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
                    Log.d("QRCodeActivity", "Payment failure received: " + error);
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
                    Log.d("QRCodeActivity", "Order status: " + order.getOrderStatus() + ", Payment status: " + order.getPaymentStatus());
                    Log.d("QRCodeActivity", "qrCodeData từ API: " + qrCodeData);

                    if (qrCodeData != null && !qrCodeData.isEmpty() && paymentTimeoutStr != null && !paymentTimeoutStr.isEmpty()) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSS", Locale.getDefault());
                            sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
                            Date timeoutDate = sdf.parse(paymentTimeoutStr);
                            timeoutTimeMillis = timeoutDate.getTime();
                            processQRCodeData(qrCodeData, paymentTimeoutStr);
                        } catch (Exception e) {
                            Log.e("QRCodeActivity", "Lỗi parse paymentTimeoutStr từ API: " + e.getMessage());
                            Toast.makeText(QRCodeActivity.this, "Lỗi dữ liệu thời gian thanh toán từ server", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(QRCodeActivity.this, "Không có dữ liệu QR Code hoặc thời gian thanh toán từ API", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(QRCodeActivity.this, "Không thể lấy thông tin đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(QRCodeActivity.this, "Lỗi khi lấy dữ liệu: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void processQRCodeData(String qrCodeData, String paymentTimeoutStr) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(qrCodeData, BarcodeFormat.QR_CODE, 320, 320);
            ivQRCode.setImageBitmap(bitmap);
            if (paymentTimeoutStr != null && !paymentTimeoutStr.isEmpty()) {
                // Khi không phải thanh toán phần còn lại, sử dụng paymentTimeoutStr nếu có
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSS", Locale.getDefault());
                    sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
                    Date timeoutDate = sdf.parse(paymentTimeoutStr);
                    timeoutTimeMillis = timeoutDate.getTime();

                    // Kiểm tra nếu timeout đã hết hạn
                    if (timeoutTimeMillis <= System.currentTimeMillis()) {
                        Log.e("QRCodeActivity", "paymentTimeout đã hết hạn trước khi hiển thị");
                        timeoutTimeMillis = System.currentTimeMillis() + 5 * 60 * 1000; // Fallback 5 phút
                    }
                } catch (Exception e) {
                    Log.e("QRCodeActivity", "Lỗi parse paymentTimeout: " + e.getMessage());
                    timeoutTimeMillis = System.currentTimeMillis() + 5 * 60 * 1000; // Fallback 5 phút
                }
            } else {
                // Trường hợp không có paymentTimeoutStr
                Log.e("QRCodeActivity", "paymentTimeout từ Intent là null hoặc rỗng");
                timeoutTimeMillis = System.currentTimeMillis() + 5 * 60 * 1000; // Fallback 5 phút
            }

            // Luôn chạy đếm ngược
            startCountdown();

        } catch (Exception e) {
            //Toast.makeText(this, "Lỗi tạo QR Code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        // Phần còn lại của hàm giữ nguyên (hiển thị thông báo thanh toán)
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        if ("Đơn cố định".equals(orderType)) {
            if (isDeposit) {
                String formattedTotalPriceFixedOrder = decimalFormat.format(totalPriceFixedOrder);
                tvWarning.setText("Please transfer " + formattedTotalPriceFixedOrder + "₫ to complete the deposit!");
            } else {
                String formattedTotalPriceFixedOrder = decimalFormat.format(totalPriceFixedOrder);
                tvWarning.setText("Please transfer " + formattedTotalPriceFixedOrder + "₫ complete the payment!");
            }
        } else {
            if (isRemainingPayment) {
                int remainingAmount = totalPrice - depositAmount;
                String formattedRemainingAmount = decimalFormat.format(remainingAmount);
                tvWarning.setText("Please transfer " + formattedRemainingAmount + "₫ to complete the remaining payment!");
            } else if (isDeposit) {
                String formattedDeposit = decimalFormat.format(depositAmount);
                tvWarning.setText("Please transfer " + formattedDeposit + "₫ to complete the deposit!");
            } else {
                String formattedPaymentAmount = decimalFormat.format(paymentAmount > 0 ? paymentAmount : totalPrice);
                tvWarning.setText("Please transfer " + formattedPaymentAmount + "₫ complete the payment!");
            }
        }

    }

    private void startCountdown() {
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
                    String paymentTimeoutStr = order.getPaymentTimeout();
                    if (paymentTimeoutStr != null && !paymentTimeoutStr.isEmpty()) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSS", Locale.getDefault());
                            sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
                            Date timeoutDate = sdf.parse(paymentTimeoutStr);
                            long newTimeoutTimeMillis = timeoutDate.getTime();
                            if (newTimeoutTimeMillis != timeoutTimeMillis) {
                                timeoutTimeMillis = newTimeoutTimeMillis;
                                handler.removeCallbacks(countdownRunnable);
                                startCountdown();
                            }
                        } catch (Exception e) {
                            Log.e("QRCodeActivity", "Lỗi parse paymentTimeout trong update: " + e.getMessage());
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
            intent.putExtra("customerName", getIntent().getStringExtra("customerName"));
            intent.putExtra("phoneNumber", getIntent().getStringExtra("phoneNumber"));
            intent.putExtra("note", getIntent().getStringExtra("note"));


            Log.d("QRCodeActivity", "Truyền sang PaymentFailedActivity:");
            Log.d("QRCodeActivity", "orderId: " + orderId);
            Log.d("QRCodeActivity", "totalTime: " + totalTime);
            Log.d("QRCodeActivity", "selectedDate: " + selectedDate);
            Log.d("QRCodeActivity", "totalPrice: " + totalPrice);
            Log.d("QRCodeActivity", "orderStatus: " + orderStatus);
            Log.d("QRCodeActivity", "courtId: " + courtId);
            Log.d("QRCodeActivity", "orderType: " + orderType);
            Log.d("QRCodeActivity", "serviceDetailsJson: " + getIntent().getStringExtra("serviceDetailsJson"));
            Log.d("QRCodeActivity", "customerName: " + getIntent().getStringExtra("customerName"));
            Log.d("QRCodeActivity", "phoneNumber: " + getIntent().getStringExtra("phoneNumber"));
            Log.d("QRCodeActivity", "note: " + getIntent().getStringExtra("note"));
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
            intent.putExtra("customerName", getIntent().getStringExtra("customerName"));
            intent.putExtra("phoneNumber", getIntent().getStringExtra("phoneNumber"));
            intent.putExtra("note", getIntent().getStringExtra("note"));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);


            Log.d("QRCodeActivity", "Truyền sang PaymentFailedActivity:");
            Log.d("QRCodeActivity", "orderId: " + orderId);
            Log.d("QRCodeActivity", "totalTime: " + totalTime);
            Log.d("QRCodeActivity", "selectedDate: " + selectedDate);
            Log.d("QRCodeActivity", "totalPrice: " + totalPrice);
            Log.d("QRCodeActivity", "orderStatus: " + orderStatus);
            Log.d("QRCodeActivity", "courtId: " + courtId);
            Log.d("QRCodeActivity", "orderType: " + orderType);
            Log.d("QRCodeActivity", "serviceDetailsJson: " + getIntent().getStringExtra("serviceDetailsJson"));
            Log.d("QRCodeActivity", "customerName: " + getIntent().getStringExtra("customerName"));
            Log.d("QRCodeActivity", "phoneNumber: " + getIntent().getStringExtra("phoneNumber"));
            Log.d("QRCodeActivity", "note: " + getIntent().getStringExtra("note"));
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        socketHandler.post(socketCheckRunnable);
        timeoutHandler.post(updateTimeoutRunnable);
        if (countdownRunnable != null && !isRemainingPayment) {
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