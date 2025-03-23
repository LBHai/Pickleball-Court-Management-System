package SEP490.G9;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import Api.ApiService;
import Api.NetworkUtils;
import Api.RetrofitClient;
import Model.ConfirmOrder;
import Model.Courts;
import Model.CreateOrderRequest;
import Model.CreateOrderResponse;
import Model.MyInfoResponse;
import Model.NotificationRequest;
import Model.OrderDetail;
import Model.Orders;
import Session.SessionManager;
import retrofit2.Call;

public class ConfirmActivity extends AppCompatActivity {

    private TextView tvHeader, tvStadiumName, tvAddress, tvDate, tvTotalPriceLine, tvTotalTimeLine;
    private LinearLayout layoutConfirmOrders;
    private EditText etName, etPhone, etNote;
    private Button btnPayment, btnDeposit;
    private ImageButton btnBack;
    private String clubId, selectedDate, confirmOrdersJson, userId = null;
    private List<ConfirmOrder> confirmOrders;
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String SALT = "dHRzX2phdmFfMDFAaHlwZXJsb2d5LmNvbTpIeXBlckAxMjN0dHNfamF2YV8wMUBoeXBlcmxvZ3kuY29t";

    // Các biến mới để truyền sang QRCodeActivity
    private String courtName;     // Tên sân (không kèm tiền tố)
    private String courtAddress;  // Địa chỉ sân
    private String totalTime;     // Tổng thời gian chơi (định dạng xxhxx)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
        tvHeader = findViewById(R.id.tvHeader);
        tvStadiumName = findViewById(R.id.tvStadiumName);
        tvAddress = findViewById(R.id.tvAddress);
        tvDate = findViewById(R.id.tvDate);
        tvTotalPriceLine = findViewById(R.id.tvTotalPriceLine);
        tvTotalTimeLine = findViewById(R.id.tvTotalTimeLine);
        layoutConfirmOrders = findViewById(R.id.layoutConfirmOrders);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etNote = findViewById(R.id.etNote);
        btnPayment = findViewById(R.id.btnPayment);
        btnDeposit = findViewById(R.id.btnDeposit);
        btnBack = findViewById(R.id.btnBack);
        clubId = getIntent().getStringExtra("club_id");
        selectedDate = getIntent().getStringExtra("selectedDate");
        confirmOrdersJson = getIntent().getStringExtra("confirmOrdersJson");

        if (selectedDate == null || selectedDate.trim().isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            selectedDate = sdf.format(new Date());
        }
        tvDate.setText("Thông tin chi tiết:");
        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getToken();
        ApiService apiService = RetrofitClient.getApiService(ConfirmActivity.this);
        if (token != null && !token.isEmpty()) {
            String authHeader = "Bearer " + token;
            NetworkUtils.callApi(apiService.getMyInfo(authHeader), ConfirmActivity.this, new NetworkUtils.ApiCallback<MyInfoResponse>() {
                @Override
                public void onSuccess(MyInfoResponse r) {
                    if (r != null && r.getResult() != null) {
                        String fullName = r.getResult().getFirstName() + " " + r.getResult().getLastName();
                        etName.setText(fullName);
                        etPhone.setText(r.getResult().getPhoneNumber());
                        userId = r.getResult().getId();
                        sessionManager.saveUserId(userId);
                    } else {
                        Toast.makeText(ConfirmActivity.this, "Lấy thông tin thất bại, vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onError(String e) {
                    Toast.makeText(ConfirmActivity.this, "Lấy thông tin thất bại, vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(ConfirmActivity.this, "Bạn đang đặt sân với vai trò là Guest!", Toast.LENGTH_SHORT).show();
        }
        confirmOrders = new Gson().fromJson(confirmOrdersJson, new TypeToken<List<ConfirmOrder>>(){}.getType());
        if (clubId != null && !clubId.isEmpty()) {
            fetchCourtDetails(clubId);
        }
        buildConfirmOrdersUI();
        btnPayment.setOnClickListener(v -> processOrder(false));
        btnDeposit.setOnClickListener(v -> processOrder(true));
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ConfirmActivity.this, BookingTableActivity.class);
            intent.putExtra("club_id", clubId);
            startActivity(intent);
            finish();
        });
    }

    private void processOrder(boolean isDeposit) {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String note = etNote.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ Tên và Số điện thoại", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!name.matches("^[\\p{L}\\s]+$") || name.length() < 2) {
            Toast.makeText(this, "Tên chỉ chứa chữ cái và khoảng trắng, ít nhất 2 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] bannedWords = {};
        for (String b : bannedWords) {
            if (name.toLowerCase().contains(b.toLowerCase())) {
                Toast.makeText(this, "Tên chứa từ nhạy cảm, vui lòng nhập lại", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (!phone.matches("0\\d{9}")) {
            Toast.makeText(this, "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0", Toast.LENGTH_SHORT).show();
            return;
        }
        btnPayment.setEnabled(false);
        btnDeposit.setEnabled(false);

        // Tính tổng tiền của các slot
        int overallTotalPrice = 0;
        for (ConfirmOrder o : confirmOrders) {
            overallTotalPrice += o.getDailyPrice();
        }
        int n = confirmOrders.size();
        int totalAmount = overallTotalPrice;
        int depositAmount = 0;
        int paymentAmount = 0;
        String paymentStatus = "";

        if (n == 1) {
            depositAmount = totalAmount;
            paymentAmount = totalAmount;
            paymentStatus = "Chưa thanh toán";
        } else {
            int pricePerSlot = overallTotalPrice / n;
            int depositSlots = (int) Math.ceil(n / 3.0);
            depositAmount = pricePerSlot * depositSlots;
            if (isDeposit) {
                paymentAmount = depositAmount;
                paymentStatus = "Chưa đặt cọc";
            } else {
                paymentAmount = totalAmount;
                paymentStatus = "Chưa thanh toán";
            }
        }

        final int finalTotalAmount = totalAmount;
        final int finalDepositAmount = depositAmount;
        final int finalPaymentAmount = paymentAmount;

        // Tạo request đặt đơn hàng
        CreateOrderRequest req = new CreateOrderRequest();
        req.setCourtId(clubId);
        req.setCourtName(tvStadiumName.getText().toString());
        req.setAddress(tvAddress.getText().toString());
        req.setBookingDate(selectedDate);
        req.setCustomerName(name);
        req.setUserId(userId);
        req.setPhoneNumber(phone);
        req.setTotalAmount(finalTotalAmount);
        req.setDiscountCode(null);
        req.setNote(note.isEmpty() ? null : note);
        req.setDiscountAmount(0);

        req.setPaymentAmount(finalPaymentAmount);
        req.setPaymentStatus(paymentStatus);
        req.setDepositAmount(String.valueOf(finalDepositAmount));

        req.setSignature(genSignature(
                String.valueOf(finalTotalAmount),
                String.valueOf(finalPaymentAmount),
                String.valueOf(finalDepositAmount),
                selectedDate));

        // Thiết lập danh sách OrderDetail từ confirmOrders
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (ConfirmOrder o : confirmOrders) {
            OrderDetail d = new OrderDetail();
            d.setCourtSlotId(o.getCourtSlotId());
            d.setCourtSlotName(o.getCourtSlotName());
            d.setStartTime(o.getStartTime());
            d.setEndTime(o.getEndTime());
            d.setPrice((int)o.getDailyPrice());
            orderDetails.add(d);
        }
        req.setOrderDetails(orderDetails);

        // Gọi API tạo đơn hàng
        ApiService api = RetrofitClient.getApiService(ConfirmActivity.this);
        NetworkUtils.callApi(api.createOrder(req), this, new NetworkUtils.ApiCallback<CreateOrderResponse>() {
            @Override
            public void onSuccess(CreateOrderResponse r) {
                if (r != null) {
                    if (r.getQrcode() != null && !r.getQrcode().isEmpty()) {
                        try {
                            // Parse paymentTimeout từ chuỗi ISO sang thời gian milliseconds
                            String paymentTimeoutStr = r.getPaymentTimeout();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSS", Locale.getDefault());
                            sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
                            Date timeoutDate = sdf.parse(paymentTimeoutStr);
                            long timeoutTimeMillis = timeoutDate.getTime();

                            // Khởi tạo Intent chuyển sang QRCodeActivity và truyền timeoutTimeMillis từ API
                            Intent i = new Intent(ConfirmActivity.this, QRCodeActivity.class);
                            i.putExtra("qrCodeData", r.getQrcode());
                            i.putExtra("timeoutTimeMillis", timeoutTimeMillis);
                            i.putExtra("orderId", r.getId());
                            i.putExtra("overallTotalPrice", finalTotalAmount);
                            i.putExtra("depositAmount", finalDepositAmount);
                            i.putExtra("isDeposit", isDeposit);
                            i.putExtra("totalTime", totalTime);
                            i.putExtra("selectedDate", selectedDate);
                            i.putExtra("totalPrice", finalTotalAmount);
                            Orders newOrder = new Orders();
                            newOrder.setId(r.getId());
                            newOrder.setBookingDate(selectedDate);
                            newOrder.setTotalAmount(finalTotalAmount);
                            registerNotification();
                            startActivity(i);
                        } catch (Exception e) {
                            Toast.makeText(ConfirmActivity.this, "Lỗi khi parse paymentTimeout: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            btnPayment.setEnabled(true);
                            btnDeposit.setEnabled(true);
                        }
                    } else {
                        Toast.makeText(ConfirmActivity.this, "Tạo đơn thất bại: QR code không có", Toast.LENGTH_SHORT).show();
                        btnPayment.setEnabled(true);
                        btnDeposit.setEnabled(true);
                    }
                } else {
                    Toast.makeText(ConfirmActivity.this, "Tạo đơn thất bại!", Toast.LENGTH_SHORT).show();
                    btnPayment.setEnabled(true);
                    btnDeposit.setEnabled(true);
                }
            }

            @Override
            public void onError(String e) {
                Toast.makeText(ConfirmActivity.this, "Tạo đơn thất bại: " + e, Toast.LENGTH_SHORT).show();
                btnPayment.setEnabled(true);
                btnDeposit.setEnabled(true);
            }
        });
    }

    private void fetchCourtDetails(String id) {
        ApiService apiService = RetrofitClient.getApiService(this);
        NetworkUtils.callApi(apiService.getCourtById(id), this, new NetworkUtils.ApiCallback<Courts>() {
            @Override
            public void onSuccess(Courts c) {
                if (c != null) {
                    courtName = c.getName();
                    courtAddress = c.getAddress();

                    tvStadiumName.setText("Tên sân: " + courtName);
                    tvAddress.setText("Địa chỉ: " + courtAddress);
                    TextView tvContact = findViewById(R.id.tvContact);
                    if (c.getPhone() != null) {
                        tvContact.setText("Liên hệ: " + c.getPhone());
                    } else {
                        tvContact.setText("Liên hệ: Chưa cập nhật");
                    }
                }
            }
            @Override
            public void onError(String e) {
                Toast.makeText(ConfirmActivity.this, "Lỗi khi lấy thông tin sân", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int toMinutes(String time) {
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        return hour * 60 + minute;
    }

    private String formatMoney(int amount) {
        return new java.text.DecimalFormat("#,###").format(amount) + " ₫";
    }

    private void buildConfirmOrdersUI() {
        if (confirmOrders == null || confirmOrders.isEmpty()) {
            return;
        }
        TreeMap<String, List<ConfirmOrder>> ordersByDay = new TreeMap<>();
        for (ConfirmOrder o : confirmOrders) {
            String day = o.getDayBooking();
            if (day == null || day.isEmpty()) {
                day = "9999-12-31";
            }
            if (!ordersByDay.containsKey(day)) {
                ordersByDay.put(day, new ArrayList<>());
            }
            ordersByDay.get(day).add(o);
        }
        layoutConfirmOrders.removeAllViews();
        int totalPrice = 0;
        int totalMinutes = 0;
        for (Map.Entry<String, List<ConfirmOrder>> e : ordersByDay.entrySet()) {
            String d = e.getKey();
            List<ConfirmOrder> list = e.getValue();
            list.sort((o1, o2) -> Integer.compare(toMinutes(o1.getStartTime()), toMinutes(o2.getStartTime())));
            TextView tvDayHeader = new TextView(this);
            tvDayHeader.setTextColor(getResources().getColor(android.R.color.white));
            tvDayHeader.setTextSize(16);
            tvDayHeader.setText("Ngày: " + d);
            layoutConfirmOrders.addView(tvDayHeader);
            for (ConfirmOrder o : list) {
                totalPrice += o.getDailyPrice();
                totalMinutes += (toMinutes(o.getEndTime()) - toMinutes(o.getStartTime()));
                TextView tvSlot = new TextView(this);
                tvSlot.setTextColor(getResources().getColor(android.R.color.white));
                tvSlot.setTextSize(14);
                String detail = "   - " + o.getCourtSlotName() + ": " + o.getStartTime()
                        + " - " + o.getEndTime() + " | " + formatMoney((int)o.getDailyPrice());
                tvSlot.setText(detail);
                layoutConfirmOrders.addView(tvSlot);
            }
        }
        int hours = totalMinutes / 60;
        int mins = totalMinutes % 60;
        String timeStr = String.format(Locale.getDefault(), "%dh%02d", hours, mins);
        totalTime = timeStr;
        String moneyHtml = "Tổng tiền: <b>" + formatMoney(totalPrice) + "</b>";
        tvTotalPriceLine.setText(Html.fromHtml(moneyHtml));
        tvTotalTimeLine.setText("Tổng thời gian chơi: " + timeStr);
    }

    public static String genSignature(String totalAmount, String paymentAmount, String depositAmount, String bookingDate) {
        try {
            String data = totalAmount + "|" + paymentAmount + "|" + depositAmount + "|" + bookingDate;
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKey = new SecretKeySpec(SALT.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error while generating signature", e);
        }
    }

    //=================== PHẦN ĐĂNG KÝ FCM TOKEN VÀ GỬI LÊN SERVER ===================//
    private void registerNotification() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("FCM", "Fetching FCM token failed", task.getException());
                return;
            }
            String fcmToken = task.getResult();
            Log.d("FCM_TOKEN", "FCM Token: " + fcmToken);
            sendDeviceTokenToServer(fcmToken);
        });
    }

    private void sendDeviceTokenToServer(String fcmToken) {
        SessionManager sessionManager = new SessionManager(this);
        String key = sessionManager.getUserId();
        NotificationRequest request = new NotificationRequest(key, fcmToken);
        ApiService apiService = RetrofitClient.getApiService(ConfirmActivity.this);
        apiService.registerNotification(request).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()){
                    Log.d("Notify", "Đăng ký FCM token thành công");
                } else {
                    try {
                        String error = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e("Notify", "Đăng ký token thất bại: " + response.code() + " - " + error);
                    } catch (Exception e) {
                        Log.e("Notify", "Lỗi khi đọc errorBody", e);
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                Log.e("Notify", "Lỗi khi đăng ký FCM token", t);
            }
        });
    }
}
