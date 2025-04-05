package SEP490.G9;

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
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

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
import Model.OrderDetailGroup;
import Model.Orders;
import Session.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmRegularActivity extends AppCompatActivity {

    private TextView tvHeader, tvStadiumName, tvAddress, tvDate, tvTotalPriceLine, tvTotalTimeLine;
    private LinearLayout layoutConfirmOrders;
    private EditText etName, etPhone, etNote;
    private Button btnPayment, btnDeposit;
    private ImageButton btnBack;
    private int overallTotalPrice = 0;
    private String courtId, userId = null;
    private List<String> selectedDays;
    private String startDate, endDate, startTime, endTime;
    private List<String> selectedCourtSlots;
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
        setContentView(R.layout.activity_confirm_regular); // Sử dụng layout tương tự ConfirmActivity

        // Khởi tạo các view
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
        courtId = getIntent().getStringExtra("courtId");
        selectedDays = getIntent().getStringArrayListExtra("selectedDays");
        startDate = getIntent().getStringExtra("startDate");
        endDate = getIntent().getStringExtra("endDate");
        startTime = getIntent().getStringExtra("startTime");
        endTime = getIntent().getStringExtra("endTime");
        selectedCourtSlots = getIntent().getStringArrayListExtra("selectedCourtSlots");

        // Kiểm tra dữ liệu đầu vào
        if (courtId == null || selectedDays == null || selectedDays.isEmpty() || startDate == null || endDate == null || startTime == null || endTime == null || selectedCourtSlots == null || selectedCourtSlots.isEmpty()) {
            Toast.makeText(this, "Thiếu dữ liệu đặt lịch!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvHeader.setText("Xác nhận đặt lịch cố định");
        tvDate.setText("Thông tin chi tiết:");

        // Lấy thông tin người dùng
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
                    Toast.makeText(ConfirmRegularActivity.this, "Lấy thông tin thất bại!", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Bạn đang đặt sân với vai trò là Guest!", Toast.LENGTH_SHORT).show();
            List<String> guestPhones = sessionManager.getGuestPhones();
            if (!guestPhones.isEmpty()) {
                etPhone.setText(guestPhones.get(0));
            }
        }

        // Tạo danh sách confirmOrders từ dữ liệu nhận được
        confirmOrders = new ArrayList<>();
        for (String slot : selectedCourtSlots) {
            ConfirmOrder order = new ConfirmOrder();
            order.setCourtSlotName(slot);
            order.setStartTime(startTime);
            order.setEndTime(endTime);
            order.setDailyPrice(100000); // Giá tạm thời, cần lấy từ API thực tế
            confirmOrders.add(order);
        }

        if (courtId != null && !courtId.isEmpty()) {
            fetchCourtDetails(courtId);
        }
        buildConfirmOrdersUI();

        // Xử lý sự kiện nút Thanh toán
        btnPayment.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            if (!validateInput(phone)) return;
            if (userId == null || userId.isEmpty()) {
                sessionManager.addGuestPhone(phone);
                registerNotification();
            }
            processOrder(false);
        });

        // Xử lý sự kiện nút Đặt cọc
        btnDeposit.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            if (!validateInput(phone)) return;
            if (userId == null || userId.isEmpty()) {
                sessionManager.addGuestPhone(phone);
                registerNotification();
            }
            processOrder(true);
        });

        // Quay lại màn hình trước
        btnBack.setOnClickListener(v -> finish());
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
        if (!phone.matches("0\\d{9}")) {
            Toast.makeText(this, "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0", Toast.LENGTH_SHORT).show();
            return;
        }
        btnPayment.setEnabled(false);
        btnDeposit.setEnabled(false);

        // Tính tổng tiền và thời gian
        overallTotalPrice = 0;
        for (ConfirmOrder o : confirmOrders) {
            overallTotalPrice += o.getDailyPrice();
        }
        int n = confirmOrders.size();
        int totalAmount = overallTotalPrice * selectedDays.size();
        int depositAmount = 0;

        if (n == 1) {
            depositAmount = totalAmount;
        } else {
            List<Integer> slotPrices = new ArrayList<>();
            for (ConfirmOrder o : confirmOrders) {
                slotPrices.add((int) o.getDailyPrice());
            }
            Collections.sort(slotPrices);
            int k = (int) Math.ceil(n / 3.0);
            for (int i = 0; i < k && i < slotPrices.size(); i++) {
                depositAmount += slotPrices.get(i) * selectedDays.size();
            }
        }

        final int finalTotalAmount = totalAmount;
        final int finalDepositAmount = depositAmount;

        CreateOrderRequest req = new CreateOrderRequest();
        req.setCourtId(courtId);
        req.setCourtName(courtName);
        req.setAddress(courtAddress);
        req.setCustomerName(name);
        req.setUserId(userId);
        req.setPhoneNumber(phone);
        req.setTotalAmount(finalTotalAmount);
        req.setDiscountCode(null);
        req.setNote(note.isEmpty() ? null : note);
        req.setDiscountAmount(0);
        req.setOrderType("REGULAR");

        // Tạo danh sách chi tiết đơn hàng
        Map<String, List<OrderDetail>> orderDetailsByDay = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        try {
            Date end = sdf.parse(endDate); // Parse endDate một lần duy nhất
            calendar.setTime(sdf.parse(startDate));
            while (!calendar.getTime().after(end)) {
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                String dayName = getDayName(dayOfWeek);
                if (selectedDays.contains(dayName)) {
                    String bookingDate = sdf.format(calendar.getTime());
                    for (ConfirmOrder o : confirmOrders) {
                        OrderDetail d = new OrderDetail();
                        d.setCourtSlotName(o.getCourtSlotName());
                        d.setStartTime(o.getStartTime());
                        d.setEndTime(o.getEndTime());
                        d.setPrice((int) o.getDailyPrice());
                        orderDetailsByDay.computeIfAbsent(bookingDate, k -> new ArrayList<>()).add(d);
                    }
                }
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi xử lý ngày tháng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            btnPayment.setEnabled(true);
            btnDeposit.setEnabled(true);
            return;
        }

        // Tạo danh sách OrderDetailGroup từ orderDetailsByDay
        List<OrderDetailGroup> orderDetailGroups = new ArrayList<>();
        for (Map.Entry<String, List<OrderDetail>> entry : orderDetailsByDay.entrySet()) {
            OrderDetailGroup group = new OrderDetailGroup();
            group.setBookingDate(entry.getKey());
            group.setBookingSlots(entry.getValue());
            orderDetailGroups.add(group);
        }
        req.setOrderDetails(orderDetailGroups);

        ArrayList<Integer> slotPrices = new ArrayList<>();
        for (ConfirmOrder o : confirmOrders) {
            slotPrices.add((int) o.getDailyPrice());
        }

        ApiService api = RetrofitClient.getApiService(this);
        final int paymentAmount = isDeposit ? finalDepositAmount : finalTotalAmount;
        final String paymentStatus = isDeposit ? ((n == 1) ? "Chưa thanh toán" : "Chưa đặt cọc") : "Chưa thanh toán";
        final int amountPaid = 0;

        req.setPaymentAmount(paymentAmount);
        req.setPaymentStatus(paymentStatus);
        req.setDepositAmount(finalDepositAmount);
        req.setSignature(genSignature(
                String.valueOf(finalTotalAmount),
                String.valueOf(paymentAmount),
                String.valueOf(finalDepositAmount),
                phone));

        Call<CreateOrderResponse> call = api.createOrder(req);
        NetworkUtils.callApi(call, this, new NetworkUtils.ApiCallback<CreateOrderResponse>() {
            @Override
            public void onSuccess(CreateOrderResponse r) {
                if (r != null && r.getQrcode() != null && !r.getQrcode().isEmpty()) {
                    Intent i = new Intent(ConfirmRegularActivity.this, QRCodeActivity.class);
                    i.putExtra("qrCodeData", r.getQrcode());
                    i.putExtra("paymentTimeout", r.getPaymentTimeout());
                    i.putExtra("orderId", r.getId());
                    i.putExtra("overallTotalPrice", finalTotalAmount);
                    i.putExtra("depositAmount", finalDepositAmount);
                    i.putExtra("paymentAmount", paymentAmount);
                    i.putExtra("amountPaid", amountPaid);
                    i.putExtra("isDeposit", isDeposit);
                    i.putExtra("totalTime", totalTime);
                    i.putExtra("courtId", courtId);
                    i.putStringArrayListExtra("selectedDays", new ArrayList<>(selectedDays));
                    i.putExtra("startDate", startDate);
                    i.putExtra("endDate", endDate);
                    i.putExtra("startTime", startTime);
                    i.putExtra("endTime", endTime);
                    i.putStringArrayListExtra("selectedCourtSlots", new ArrayList<>(selectedCourtSlots));
                    i.putIntegerArrayListExtra("slotPrices", slotPrices);
                    startActivity(i);
                    finish();
                } else {
                    Intent i = new Intent(ConfirmRegularActivity.this, PaymentSuccessActivity.class);
                    i.putExtra("orderId", r.getId());
                    i.putExtra("totalTime", totalTime);
                    i.putExtra("totalPrice", finalTotalAmount);
                    i.putExtra("orderStatus", r.getOrderStatus());
                    i.putExtra("courtId", courtId);
                    i.putStringArrayListExtra("selectedDays", new ArrayList<>(selectedDays));
                    i.putExtra("startDate", startDate);
                    i.putExtra("endDate", endDate);
                    i.putExtra("startTime", startTime);
                    i.putExtra("endTime", endTime);
                    i.putStringArrayListExtra("selectedCourtSlots", new ArrayList<>(selectedCourtSlots));
                    i.putIntegerArrayListExtra("slotPrices", slotPrices);
                    startActivity(i);
                    finish();
                }
            }

            @Override
            public void onError(String e) {
                Toast.makeText(ConfirmRegularActivity.this, "Tạo đơn thất bại: " + e, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(ConfirmRegularActivity.this, "Lỗi khi lấy thông tin sân", Toast.LENGTH_SHORT).show();
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
        layoutConfirmOrders.removeAllViews();
        overallTotalPrice = 0;
        int totalMinutes = 0;

        // Hiển thị thông tin các ngày trong tuần đã chọn
        TextView tvDaysHeader = new TextView(this);
        tvDaysHeader.setTextColor(getResources().getColor(android.R.color.white));
        tvDaysHeader.setTextSize(16);
        tvDaysHeader.setText("Ngày trong tuần: " + String.join(", ", selectedDays));
        layoutConfirmOrders.addView(tvDaysHeader);

        TextView tvDateRange = new TextView(this);
        tvDateRange.setTextColor(getResources().getColor(android.R.color.white));
        tvDateRange.setTextSize(16);
        tvDateRange.setText("Từ " + startDate + " đến " + endDate);
        layoutConfirmOrders.addView(tvDateRange);

        // Hiển thị thông tin sân và thời gian
        for (ConfirmOrder o : confirmOrders) {
            overallTotalPrice += o.getDailyPrice() * selectedDays.size();
            totalMinutes = (toMinutes(o.getEndTime()) - toMinutes(o.getStartTime()));
            TextView tvSlot = new TextView(this);
            tvSlot.setTextColor(getResources().getColor(android.R.color.white));
            tvSlot.setTextSize(14);
            String detail = "   - " + o.getCourtSlotName() + ": " + o.getStartTime().substring(0, 5) + " - " + o.getEndTime().substring(0, 5) + " | " + formatMoney((int) o.getDailyPrice());
            tvSlot.setText(detail);
            layoutConfirmOrders.addView(tvSlot);
        }

        int hours = totalMinutes / 60;
        int mins = totalMinutes % 60;
        totalTime = String.format(Locale.getDefault(), "%dh%02d", hours, mins);
        tvTotalPriceLine.setText(Html.fromHtml("Tổng tiền: <b>" + formatMoney(overallTotalPrice) + "</b>"));
        tvTotalTimeLine.setText("Tổng thời gian chơi mỗi ngày: " + totalTime);
    }

    private String getDayName(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.MONDAY: return "MONDAY";
            case Calendar.TUESDAY: return "TUESDAY";
            case Calendar.WEDNESDAY: return "WEDNESDAY";
            case Calendar.THURSDAY: return "THURSDAY";
            case Calendar.FRIDAY: return "FRIDAY";
            case Calendar.SATURDAY: return "SATURDAY";
            case Calendar.SUNDAY: return "SUNDAY";
            default: return "";
        }
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

    private boolean validateInput(String phone) {
        if (phone.isEmpty() || !phone.matches("0\\d{9}")) {
            Toast.makeText(this, "Vui lòng nhập số điện thoại hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void registerNotification() {
        if (userId != null && !userId.isEmpty()) {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String fcmToken = task.getResult();
                    sendDeviceTokenToServer(userId, fcmToken);
                }
            });
        } else {
            List<String> guestPhones = sessionManager.getGuestPhones();
            if (guestPhones.isEmpty()) {
                return;
            }
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String fcmToken = task.getResult();
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
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("Notify", "Lỗi khi đăng ký FCM token cho key " + key, t);
            }
        });
    }
    private boolean isValidDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdf.setLenient(false);
        try {
            sdf.parse(dateStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}