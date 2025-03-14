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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.Map;
import Api.ApiService;
import Api.NetworkUtils;
import Api.RetrofitClient;
import Model.ConfirmOrder;
import Model.Courts;
import Model.CreateOrderRequest;
import Model.CreateOrderResponse;
import Model.MyInfoResponse;
import Model.OrderDetail;
import Session.SessionManager;

public class ConfirmActivity extends AppCompatActivity {

    private TextView tvHeader, tvStadiumName, tvAddress, tvDate;
    private TextView tvTotalPriceLine, tvTotalTimeLine;
    private LinearLayout layoutConfirmOrders;
    private EditText etName, etPhone, etNote;
    private Button btnConfirm;
    private ImageButton btnBack;

    private String clubId;
    private String selectedDate;
    private String confirmOrdersJson;
    private List<ConfirmOrder> confirmOrders;

    // Nếu người dùng đăng nhập, userId sẽ có giá trị; ngược lại, null.
    private String userId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        // Ánh xạ view
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
        btnConfirm = findViewById(R.id.btnConfirm);
        btnBack = findViewById(R.id.btnBack);

        // Lấy dữ liệu từ Intent (đảm bảo Activity khởi chạy ConfirmActivity đã truyền club_id)
        clubId = getIntent().getStringExtra("club_id");
        Log.d("ConfirmActivity", "club_id nhận được: " + clubId);
        selectedDate = getIntent().getStringExtra("selectedDate");
        confirmOrdersJson = getIntent().getStringExtra("confirmOrdersJson");

        // Nếu selectedDate null hoặc rỗng, gán ngày hiện tại
        if (selectedDate == null || selectedDate.trim().isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            selectedDate = sdf.format(new Date());
        }
        tvDate.setText("Thông tin chi tiết:");

        // Lấy thông tin người dùng nếu có token
        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getToken();
        ApiService apiService = RetrofitClient.getApiService(ConfirmActivity.this);
        if (token != null && !token.isEmpty()) {
            String authHeader = "Bearer " + token;
            NetworkUtils.callApi(apiService.getMyInfo(authHeader), ConfirmActivity.this, new NetworkUtils.ApiCallback<MyInfoResponse>() {
                @Override
                public void onSuccess(MyInfoResponse myInfoResponse) {
                    if (myInfoResponse != null && myInfoResponse.getResult() != null) {
                        String fullName = myInfoResponse.getResult().getFirstName() + " " +
                                myInfoResponse.getResult().getLastName();
                        etName.setText(fullName);
                        etPhone.setText(myInfoResponse.getResult().getPhoneNumber());
                        userId = myInfoResponse.getResult().getId();
                    } else {
                        Toast.makeText(ConfirmActivity.this, "Lấy thông tin thất bại, vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(ConfirmActivity.this, "Lấy thông tin thất bại, vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(ConfirmActivity.this, "Token không tồn tại, vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
        }

        // Parse JSON -> danh sách ConfirmOrder
        confirmOrders = new Gson().fromJson(confirmOrdersJson, new TypeToken<List<ConfirmOrder>>(){}.getType());

        // Gọi API lấy thông tin sân nếu cần
        if (clubId != null && !clubId.isEmpty()) {
            fetchCourtDetails(clubId);
        }

        // Xây dựng UI hiển thị slot đã chọn
        buildConfirmOrdersUI();

        // Xử lý nút xác nhận
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = etName.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                String note = etNote.getText().toString().trim();

                if (name.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(ConfirmActivity.this, "Vui lòng nhập đủ Tên và Số điện thoại", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!name.matches("^[\\p{L}\\s]+$") || name.length() < 2) {
                    Toast.makeText(ConfirmActivity.this, "Tên chỉ chứa chữ cái và khoảng trắng, ít nhất 2 ký tự", Toast.LENGTH_SHORT).show();
                    return;
                }
                String[] bannedWords = { /* ... danh sách từ nhạy cảm ... */ };
                for (String bannedWord : bannedWords) {
                    if (name.toLowerCase().contains(bannedWord.toLowerCase())) {
                        Toast.makeText(ConfirmActivity.this, "Tên chứa từ nhạy cảm, vui lòng nhập lại", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if (!phone.matches("0\\d{9}")) {
                    Toast.makeText(ConfirmActivity.this, "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                int overallTotalPrice = 0;
                for (ConfirmOrder order : confirmOrders) {
                    overallTotalPrice += (int) order.getDailyPrice();
                }

                CreateOrderRequest createOrderRequest = new CreateOrderRequest();
                createOrderRequest.setCourtId(clubId);
                createOrderRequest.setCourtName(tvStadiumName.getText().toString());
                createOrderRequest.setAddress(tvAddress.getText().toString());
                createOrderRequest.setBookingDate(selectedDate);
                createOrderRequest.setCustomerName(name);
                createOrderRequest.setUserId(userId);
                createOrderRequest.setPhoneNumber(phone);
                createOrderRequest.setTotalAmount(2000); // demo value
                createOrderRequest.setDiscountCode(null);
                createOrderRequest.setNote(note.isEmpty() ? null : note);
                createOrderRequest.setDiscountAmount(0);
                createOrderRequest.setPaymentStatus("Đặt cọc");
                createOrderRequest.setPaymentAmount(2000); // demo value

                List<OrderDetail> orderDetails = new ArrayList<>();
                for (ConfirmOrder order : confirmOrders) {
                    OrderDetail detail = new OrderDetail();
                    detail.setCourtSlotId(order.getCourtSlotId());
                    detail.setCourtSlotName(order.getCourtSlotName());
                    detail.setStartTime(order.getStartTime());
                    detail.setEndTime(order.getEndTime());
                    detail.setPrice((int) order.getDailyPrice());
                    orderDetails.add(detail);
                }
                createOrderRequest.setOrderDetails(orderDetails);

                Log.d("CreateOrder", "Request: " + new Gson().toJson(createOrderRequest));

                NetworkUtils.callApi(apiService.createOrder(createOrderRequest), ConfirmActivity.this, new NetworkUtils.ApiCallback<CreateOrderResponse>() {
                    @Override
                    public void onSuccess(CreateOrderResponse orderResponse) {
                        if (orderResponse != null) {
                            Log.d("CreateOrder", "Order ID: " + orderResponse.getId());
                            if (orderResponse.getQrcode() != null && !orderResponse.getQrcode().isEmpty()) {
                                long timeoutDuration = 5 * 60 * 1000; // 5 phút
                                long timeoutTimeMillis = System.currentTimeMillis() + timeoutDuration;
                                orderResponse.setPaymentTimeout(timeoutTimeMillis + "");
                                Intent intent = new Intent(ConfirmActivity.this, QRCodeActivity.class);
                                intent.putExtra("qrCodeData", orderResponse.getQrcode());
                                intent.putExtra("timeoutTimeMillis", timeoutTimeMillis);
                                intent.putExtra("orderId",  String.valueOf(orderResponse.getId()));
                                startActivity(intent);
                            } else {
                                Toast.makeText(ConfirmActivity.this, "Tạo đơn thất bại: QR code không có", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ConfirmActivity.this, "Tạo đơn thất bại!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(ConfirmActivity.this, "Tạo đơn thất bại: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ConfirmActivity.this, BookingTableActivity.class);
            intent.putExtra("club_id", clubId);
            startActivity(intent);
            finish();
        });
    }

    private void fetchCourtDetails(String clubId) {
        ApiService apiService = RetrofitClient.getApiService(ConfirmActivity.this);
        NetworkUtils.callApi(apiService.getCourtById(clubId), ConfirmActivity.this, new NetworkUtils.ApiCallback<Courts>() {
            @Override
            public void onSuccess(Courts court) {
                if (court != null) {
                    tvStadiumName.setText(court.getName());
                    tvAddress.setText("Địa chỉ: " + court.getAddress());
                    TextView tvContact = findViewById(R.id.tvContact);
                    if (court.getPhone() != null) {
                        tvContact.setText("Liên hệ: " + court.getPhone());
                    } else {
                        tvContact.setText("Liên hệ: Chưa cập nhật");
                    }
                }
            }
            @Override
            public void onError(String errorMessage) {
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
        for (ConfirmOrder order : confirmOrders) {
            String day = order.getDayBooking();
            if (day == null || day.isEmpty()) {
                day = "9999-12-31";
            }
            if (!ordersByDay.containsKey(day)) {
                ordersByDay.put(day, new ArrayList<>());
            }
            ordersByDay.get(day).add(order);
        }
        layoutConfirmOrders.removeAllViews();
        int overallTotalPrice = 0;
        int overallTotalMinutes = 0;
        for (Map.Entry<String, List<ConfirmOrder>> entry : ordersByDay.entrySet()) {
            String day = entry.getKey();
            List<ConfirmOrder> dayOrders = entry.getValue();
            dayOrders.sort((o1, o2) -> Integer.compare(toMinutes(o1.getStartTime()), toMinutes(o2.getStartTime())));
            TextView tvDayHeader = new TextView(this);
            tvDayHeader.setTextColor(getResources().getColor(android.R.color.white));
            tvDayHeader.setTextSize(16);
            tvDayHeader.setText("Ngày: " + day);
            layoutConfirmOrders.addView(tvDayHeader);
            for (ConfirmOrder order : dayOrders) {
                overallTotalPrice += order.getDailyPrice();
                overallTotalMinutes += (toMinutes(order.getEndTime()) - toMinutes(order.getStartTime()));
                TextView tvSlot = new TextView(this);
                tvSlot.setTextColor(getResources().getColor(android.R.color.white));
                tvSlot.setTextSize(14);
                String detail = "   - " + order.getCourtSlotName() + ": " + order.getStartTime()
                        + " - " + order.getEndTime() + " | " + formatMoney((int) order.getDailyPrice());
                tvSlot.setText(detail);
                layoutConfirmOrders.addView(tvSlot);
            }
        }
        int hours = overallTotalMinutes / 60;
        int mins = overallTotalMinutes % 60;
        String timeStr = String.format(Locale.getDefault(), "%dh%02d", hours, mins);
        String moneyHtml = "Tổng tiền: <b>" + formatMoney(overallTotalPrice) + "</b>";
        tvTotalPriceLine.setText(Html.fromHtml(moneyHtml));
        tvTotalTimeLine.setText("Tổng thời gian chơi: " + timeStr);
    }
}
