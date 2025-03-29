package SEP490.G9;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
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
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmActivity extends AppCompatActivity {

    private TextView tvHeader, tvStadiumName, tvAddress, tvDate, tvTotalPriceLine, tvTotalTimeLine;
    private LinearLayout layoutConfirmOrders;
    private EditText etName, etPhone, etNote;
    private Button btnPayment, btnDeposit;
    private ImageButton btnBack;
    private int overallTotalPrice = 0;
    private String clubId, selectedDate, confirmOrdersJson, userId = null, orderId;
    private List<ConfirmOrder> confirmOrders;
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String SALT = "dHRzX2phdmFfMDFAaHlwZXJsb2d5LmNvbTpIeXBlckAxMjN0dHNfamF2YV8wMUBoeXBlcmxvZ3kuY29t";

    private SessionManager sessionManager;
    private String courtName;
    private String courtAddress;
    private String totalTime;

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

        sessionManager = new SessionManager(this);
        clubId = getIntent().getStringExtra("club_id");
        selectedDate = getIntent().getStringExtra("selectedDate");
        confirmOrdersJson = getIntent().getStringExtra("confirmOrdersJson");
        orderId = getIntent().getStringExtra("orderId");

        if (selectedDate == null || selectedDate.trim().isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            selectedDate = sdf.format(new Date());
        }
        tvDate.setText("Thông tin chi tiết:");

        String token = sessionManager.getToken();
        ApiService apiService = RetrofitClient.getApiService(this);
        if (token != null && !token.isEmpty()) {
            String authHeader = "Bearer " + token;
            NetworkUtils.callApi(apiService.getMyInfo(authHeader), this, new NetworkUtils.ApiCallback<MyInfoResponse>() {
                @Override
                public void onSuccess(MyInfoResponse r) {
                    if (r != null && r.getResult() != null) {
                        String fullName = r.getResult().getFirstName() + " " + r.getResult().getLastName();
                        etName.setText(fullName);
                        etPhone.setText(r.getResult().getPhoneNumber());
                        userId = r.getResult().getId();
                        sessionManager.saveUserId(userId);
                        registerNotification();
                    }
                }
                @Override
                public void onError(String e) {
                    Toast.makeText(ConfirmActivity.this, "Lấy thông tin thất bại!", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Bạn đang đặt sân với vai trò là Guest!", Toast.LENGTH_SHORT).show();
            List<String> guestPhones = sessionManager.getGuestPhones();
            if (!guestPhones.isEmpty()) {
                etPhone.setText(guestPhones.get(0)); // Hiển thị số điện thoại đầu tiên trong danh sách
            }
        }

        confirmOrders = new Gson().fromJson(confirmOrdersJson, new TypeToken<List<ConfirmOrder>>(){}.getType());
        if (clubId != null && !clubId.isEmpty()) {
            fetchCourtDetails(clubId);
        }
        buildConfirmOrdersUI();

        btnPayment.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            if (!validateInput(phone)) return;
            if (userId == null || userId.isEmpty()) {
                sessionManager.addGuestPhone(phone); // Thêm số điện thoại vào danh sách
                registerNotification();
            }
            processOrder(false);
        });

        btnDeposit.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            if (!validateInput(phone)) return;
            if (userId == null || userId.isEmpty()) {
                sessionManager.addGuestPhone(phone); // Thêm số điện thoại vào danh sách
                registerNotification();
            }
            processOrder(true);
        });

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, BookingTableActivity.class);
            intent.putExtra("club_id", clubId);
            startActivity(intent);
            finish();
        });
    }

    private boolean validateInput(String phone) {
        if (phone.isEmpty() || !phone.matches("0\\d{9}")) {
            Toast.makeText(this, "Vui lòng nhập số điện thoại hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void registerNotification() {
        // Nếu là user đã đăng nhập, sử dụng userId
        if (userId != null && !userId.isEmpty()) {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String fcmToken = task.getResult();
                    sendDeviceTokenToServer(userId, fcmToken);
                }
            });
        } else {
            // Nếu là guest, đăng ký thông báo cho tất cả số điện thoại trong danh sách
            List<String> guestPhones = sessionManager.getGuestPhones();
            if (guestPhones.isEmpty()) {
                Log.e("FCM", "Không có số điện thoại guest để đăng ký FCM token");
                return;
            }

            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String fcmToken = task.getResult();
                    // Đăng ký FCM token cho từng số điện thoại trong danh sách
                    for (String phone : guestPhones) {
                        sendDeviceTokenToServer(phone, fcmToken);
                    }
                }
            });
        }
    }

    private void sendDeviceTokenToServer(String key, String fcmToken) {
        NotificationRequest request = new NotificationRequest(key, fcmToken);
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.registerNotification(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    Log.e("Notify", "Đăng ký token thất bại cho key " + key + ": " + response.code());
                } else {
                    Log.d("Notify", "Đăng ký token thành công cho key: " + key);
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("Notify", "Lỗi khi đăng ký FCM token cho key " + key, t);
            }
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

        btnPayment.setEnabled(false);
        btnDeposit.setEnabled(false);

        overallTotalPrice = 0;
        for (ConfirmOrder o : confirmOrders) {
            overallTotalPrice += o.getDailyPrice();
        }
        int n = confirmOrders.size();
        int totalAmount = overallTotalPrice;
        int depositAmount = (n == 1) ? totalAmount : (overallTotalPrice / n) * (int) Math.ceil(n / 3.0);

        CreateOrderRequest req = new CreateOrderRequest();
        req.setCourtId(clubId);
        req.setCourtName(courtName);
        req.setAddress(courtAddress);
        req.setBookingDate(selectedDate);
        req.setCustomerName(name);
        req.setUserId(userId);
        req.setPhoneNumber(phone);
        req.setTotalAmount(totalAmount);
        req.setDiscountCode(null);
        req.setNote(note.isEmpty() ? null : note);
        req.setDiscountAmount(0);

        List<OrderDetail> orderDetails = new ArrayList<>();
        for (ConfirmOrder o : confirmOrders) {
            OrderDetail d = new OrderDetail();
            d.setCourtSlotId(o.getCourtSlotId());
            d.setCourtSlotName(o.getCourtSlotName());
            d.setStartTime(o.getStartTime());
            d.setEndTime(o.getEndTime());
            d.setPrice((int) o.getDailyPrice());
            orderDetails.add(d);
        }
        req.setOrderDetails(orderDetails);

        ApiService api = RetrofitClient.getApiService(this);
        int paymentAmount = isDeposit ? depositAmount : totalAmount;
        String paymentStatus = isDeposit && n > 1 ? "Chưa đặt cọc" : "Chưa thanh toán";
        req.setPaymentAmount(paymentAmount);
        req.setPaymentStatus(paymentStatus);
        req.setDepositAmount(String.valueOf(depositAmount));
        req.setSignature(genSignature(String.valueOf(totalAmount), String.valueOf(paymentAmount), String.valueOf(depositAmount), selectedDate));

        Call<CreateOrderResponse> call = (orderId != null && !orderId.isEmpty()) ? api.changeOrder(orderId, req) : api.createOrder(req);
        NetworkUtils.callApi(call, this, new NetworkUtils.ApiCallback<CreateOrderResponse>() {
            @Override
            public void onSuccess(CreateOrderResponse r) {
                if (r != null && r.getQrcode() != null && !r.getQrcode().isEmpty()) {
                    Intent i = new Intent(ConfirmActivity.this, QRCodeActivity.class);
                    i.putExtra("qrCodeData", r.getQrcode());
                    i.putExtra("paymentTimeout", r.getPaymentTimeout());
                    i.putExtra("orderId", r.getId());
                    i.putExtra("overallTotalPrice", totalAmount);
                    i.putExtra("depositAmount", depositAmount);
                    i.putExtra("isDeposit", isDeposit);
                    i.putExtra("totalTime", totalTime);
                    i.putExtra("selectedDate", selectedDate);
                    i.putExtra("totalPrice", totalAmount);
                    i.putExtra("courtId", clubId);
                    startActivity(i);
                    finish();
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
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    private String formatMoney(int amount) {
        return new java.text.DecimalFormat("#,###").format(amount) + " ₫";
    }

    private void buildConfirmOrdersUI() {
        if (confirmOrders == null || confirmOrders.isEmpty()) return;
        TreeMap<String, List<ConfirmOrder>> ordersByDay = new TreeMap<>();
        for (ConfirmOrder o : confirmOrders) {
            String day = o.getDayBooking() != null ? o.getDayBooking() : "9999-12-31";
            ordersByDay.computeIfAbsent(day, k -> new ArrayList<>()).add(o);
        }
        layoutConfirmOrders.removeAllViews();
        overallTotalPrice = 0;
        int totalMinutes = 0;
        for (Map.Entry<String, List<ConfirmOrder>> e : ordersByDay.entrySet()) {
            TextView tvDayHeader = new TextView(this);
            tvDayHeader.setTextColor(getResources().getColor(android.R.color.white));
            tvDayHeader.setTextSize(16);
            tvDayHeader.setText("Ngày: " + e.getKey());
            layoutConfirmOrders.addView(tvDayHeader);
            List<ConfirmOrder> list = e.getValue();
            list.sort((o1, o2) -> Integer.compare(toMinutes(o1.getStartTime()), toMinutes(o2.getStartTime())));
            for (ConfirmOrder o : list) {
                overallTotalPrice += o.getDailyPrice();
                totalMinutes += (toMinutes(o.getEndTime()) - toMinutes(o.getStartTime()));
                TextView tvSlot = new TextView(this);
                tvSlot.setTextColor(getResources().getColor(android.R.color.white));
                tvSlot.setTextSize(14);
                String detail = "   - " + o.getCourtSlotName() + ": " + o.getStartTime() + " - " + o.getEndTime() + " | " + formatMoney((int)o.getDailyPrice());
                tvSlot.setText(detail);
                layoutConfirmOrders.addView(tvSlot);
            }
        }
        int hours = totalMinutes / 60;
        int mins = totalMinutes % 60;
        totalTime = String.format(Locale.getDefault(), "%dh%02d", hours, mins);
        tvTotalPriceLine.setText(Html.fromHtml("Tổng tiền: <b>" + formatMoney(overallTotalPrice) + "</b>"));
        tvTotalTimeLine.setText("Tổng thời gian chơi: " + totalTime);
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
}