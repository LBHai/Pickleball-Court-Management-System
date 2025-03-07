package SEP490.G9;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.Map;
import Api.ApiService;
import Api.RetrofitClient;
import Model.ConfirmOrder;
import Model.Courts;
import Model.CreateOrderRequest;
import Model.CreateOrderResponse;
import Model.MyInfoResponse;
import Model.OrderDetail;
import Session.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmActivity extends AppCompatActivity {

    private TextView tvHeader, tvStadiumName, tvAddress, tvDate;
    private TextView tvTotalPriceLine, tvTotalTimeLine; // 2 TextView mới
    private LinearLayout layoutConfirmOrders;
    private EditText etName, etPhone, etNote;
    private Button btnConfirm;

    private String clubId;
    private String selectedDate;
    private String confirmOrdersJson;
    private List<ConfirmOrder> confirmOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        // Ánh xạ View
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

        // Lấy dữ liệu từ Intent
        clubId = getIntent().getStringExtra("club_id");
        selectedDate = getIntent().getStringExtra("selectedDate");
        confirmOrdersJson = getIntent().getStringExtra("confirmOrdersJson");

        // Nếu selectedDate null hoặc rỗng, gán ngày hiện tại
        if (selectedDate == null || selectedDate.trim().isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            selectedDate = sdf.format(new Date());
        }

        // Label "Thông tin chi tiết:"
        tvDate.setText("Thông tin chi tiết:");

        // Kiểm tra token => Nếu có token, lấy thông tin người dùng
        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getToken();
        if (token != null && !token.isEmpty()) {
            String authHeader = "Bearer " + token;
            // Lấy ApiService từ RetrofitClient
            ApiService apiService = RetrofitClient.getApiService(ConfirmActivity.this);
            apiService.getMyInfo(authHeader).enqueue(new Callback<MyInfoResponse>() {
                @Override
                public void onResponse(Call<MyInfoResponse> call, Response<MyInfoResponse> response) {
                    if (response.isSuccessful()) {
                        MyInfoResponse myInfoResponse = response.body();
                        if (myInfoResponse != null && myInfoResponse.getResult() != null) {
                            String fullName = myInfoResponse.getResult().getFirstName() + " " +
                                    myInfoResponse.getResult().getLastName();
                            etName.setText(fullName);
                            etPhone.setText(myInfoResponse.getResult().getPhoneNumber());
                        } else {
                            Toast.makeText(ConfirmActivity.this, "Dữ liệu trả về không hợp lệ", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ConfirmActivity.this, "Bạn đang đặt sân với tư cách là Guest", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<MyInfoResponse> call, Throwable t) {
                    Toast.makeText(ConfirmActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(ConfirmActivity.this, "Bạn đang đặt sân với tư cách là Guest", Toast.LENGTH_SHORT).show();
        }

        // Parse JSON -> danh sách ConfirmOrder
        confirmOrders = new Gson().fromJson(confirmOrdersJson, new TypeToken<List<ConfirmOrder>>(){}.getType());

        // Gọi API lấy thông tin sân nếu cần
        if (clubId != null && !clubId.isEmpty()) {
            fetchCourtDetails(clubId);
        }

        // Xây dựng UI hiển thị slot
        buildConfirmOrdersUI();

        // Xử lý nút Xác nhận
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = etName.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                String note = etNote.getText().toString().trim();

                // Kiểm tra thông tin bắt buộc
                if (name.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(ConfirmActivity.this, "Vui lòng nhập đủ Tên và Số điện thoại", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate tên: chỉ chứa chữ và khoảng trắng, ít nhất 2 ký tự
                if (!name.matches("^[\\p{L}\\s]+$") || name.length() < 2) {
                    Toast.makeText(ConfirmActivity.this, "Tên chỉ chứa chữ cái và khoảng trắng, ít nhất 2 ký tự", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Chặn từ nhạy cảm (danh sách omitted)
                String[] bannedWords = { /* ... danh sách từ nhạy cảm ... */ };
                for (String bannedWord : bannedWords) {
                    if (name.toLowerCase().contains(bannedWord.toLowerCase())) {
                        Toast.makeText(ConfirmActivity.this, "Tên chứa từ nhạy cảm, vui lòng nhập lại", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // Validate số điện thoại: 10 chữ số, bắt đầu bằng 0
                if (!phone.matches("0\\d{9}")) {
                    Toast.makeText(ConfirmActivity.this, "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Tính tổng tiền từ tất cả các slot
                int overallTotalPrice = 0;
                for (ConfirmOrder order : confirmOrders) {
                    overallTotalPrice += order.getDailyPrice();
                }

                // Tạo đối tượng CreateOrderRequest
                CreateOrderRequest createOrderRequest = new CreateOrderRequest();
                createOrderRequest.setCourtId(clubId);
                createOrderRequest.setCourtName(tvStadiumName.getText().toString());
                createOrderRequest.setAddress(tvAddress.getText().toString());
                createOrderRequest.setBookingDate(selectedDate);
                createOrderRequest.setCustomerName(name);
                createOrderRequest.setPhoneNumber(phone);
                createOrderRequest.setTotalAmount(overallTotalPrice);
                createOrderRequest.setDiscountCode(null);
                createOrderRequest.setNote(note);
                createOrderRequest.setDiscountAmount(0);
                createOrderRequest.setPaymentAmount(2000); // Giá trị test
                createOrderRequest.setPaymentStatus("Đặt cọc");

                // Xây dựng danh sách OrderDetail
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

                // Lấy đối tượng ApiService từ RetrofitClient và gọi API tạo đơn hàng
                ApiService apiService = RetrofitClient.getApiService(ConfirmActivity.this);
                apiService.createOrder(createOrderRequest).enqueue(new Callback<CreateOrderResponse>() {
                    @Override
                    public void onResponse(Call<CreateOrderResponse> call, Response<CreateOrderResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            CreateOrderResponse orderResponse = response.body();
                            Log.d("CreateOrder", "Order ID: " + orderResponse.getId());
                            if (orderResponse.getQrcode() != null && !orderResponse.getQrcode().isEmpty()) {
                                long timeoutDuration = 5 * 60 * 1000; // 5 phút
                                long timeoutTimeMillis = System.currentTimeMillis() + timeoutDuration;
                                orderResponse.setPaymentTimeout(timeoutTimeMillis + "");

                                Intent intent = new Intent(ConfirmActivity.this, QRCodeActivity.class);
                                intent.putExtra("qrCodeData", orderResponse.getQrcode());
                                intent.putExtra("timeoutTimeMillis", timeoutTimeMillis);
                                startActivity(intent);
                            } else {
                                Toast.makeText(ConfirmActivity.this, "Tạo đơn thất bại: QR code không có", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String errorBody = "null";
                            try {
                                if (response.errorBody() != null) {
                                    errorBody = response.errorBody().string();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                errorBody = e.getMessage();
                            }
                            Log.e("CreateOrder", "Response lỗi: " + errorBody);
                            Toast.makeText(ConfirmActivity.this, "Tạo đơn thất bại: " + errorBody, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CreateOrderResponse> call, Throwable t) {
                        Log.e("CreateOrder", "onFailure: " + t.getMessage());
                        Toast.makeText(ConfirmActivity.this, "Tạo đơn thất bại: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // Gọi API lấy thông tin sân
    private void fetchCourtDetails(String clubId) {
        ApiService apiService = RetrofitClient.getApiService(ConfirmActivity.this);
        apiService.getCourtById(clubId).enqueue(new Callback<Courts>() {
            @Override
            public void onResponse(Call<Courts> call, Response<Courts> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Courts court = response.body();
                    tvStadiumName.setText(court.getName());
                    tvAddress.setText("Địa chỉ: " + court.getAddress());

                    // Hiển thị liên hệ (giả sử tvContact có trong layout)
                    TextView tvContact = findViewById(R.id.tvContact);
                    if (court.getPhone() != null) {
                        tvContact.setText("Liên hệ: " + court.getPhone());
                    } else {
                        tvContact.setText("Liên hệ: Chưa cập nhật");
                    }
                } else {
                    Toast.makeText(ConfirmActivity.this, "Lỗi khi lấy thông tin sân", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Courts> call, Throwable t) {
                Toast.makeText(ConfirmActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm parse "HH:mm" => số phút
    private int toMinutes(String time) {
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        return hour * 60 + minute;
    }

    // Định dạng tiền theo kiểu "300.000 ₫"
    private String formatMoney(int amount) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(amount) + " ₫";
    }

    /**
     * Xây dựng UI "Thông tin chi tiết":
     *   - Nhóm theo dayBooking, sắp xếp ngày và giờ
     *   - Hiển thị slot
     *   - Tính tổng giá và tổng giờ => hiển thị ở 2 dòng (tvTotalPriceLine, tvTotalTimeLine)
     */
    private void buildConfirmOrdersUI() {
        if (confirmOrders == null || confirmOrders.isEmpty()) {
            return;
        }

        // Gom các ConfirmOrder theo ngày (dayBooking) và sắp xếp tăng dần theo ngày
        TreeMap<String, List<ConfirmOrder>> ordersByDay = new TreeMap<>();
        for (ConfirmOrder order : confirmOrders) {
            String day = order.getDayBooking();
            if (day == null || day.isEmpty()) {
                day = "9999-12-31"; // Nếu không có ngày, đưa về cuối danh sách
            }
            if (!ordersByDay.containsKey(day)) {
                ordersByDay.put(day, new ArrayList<>());
            }
            ordersByDay.get(day).add(order);
        }

        // Xoá view cũ
        layoutConfirmOrders.removeAllViews();

        // Biến tính tổng cho tất cả các ngày
        int overallTotalPrice = 0;
        int overallTotalMinutes = 0;

        // Lặp qua từng ngày đã sắp xếp
        for (Map.Entry<String, List<ConfirmOrder>> entry : ordersByDay.entrySet()) {
            String day = entry.getKey();
            List<ConfirmOrder> dayOrders = entry.getValue();

            // Sắp xếp slot theo giờ bắt đầu tăng dần
            dayOrders.sort((o1, o2) -> Integer.compare(toMinutes(o1.getStartTime()), toMinutes(o2.getStartTime())));

            // Thêm header ngày
            TextView tvDayHeader = new TextView(this);
            tvDayHeader.setTextColor(getResources().getColor(android.R.color.white));
            tvDayHeader.setTextSize(16);
            tvDayHeader.setText("Ngày: " + day);
            layoutConfirmOrders.addView(tvDayHeader);

            // Hiển thị các slot của ngày
            for (ConfirmOrder order : dayOrders) {
                overallTotalPrice += order.getDailyPrice();
                overallTotalMinutes += (toMinutes(order.getEndTime()) - toMinutes(order.getStartTime()));

                TextView tvSlot = new TextView(this);
                tvSlot.setTextColor(getResources().getColor(android.R.color.white));
                tvSlot.setTextSize(14);
                String detail = "   - " + order.getCourtSlotName()
                        + ": " + order.getStartTime()
                        + " - " + order.getEndTime()
                        + " | " + formatMoney((int)order.getDailyPrice());
                tvSlot.setText(detail);
                layoutConfirmOrders.addView(tvSlot);
            }
        }

        // Tính tổng thời gian chơi
        int hours = overallTotalMinutes / 60;
        int mins = overallTotalMinutes % 60;
        String timeStr = String.format(Locale.getDefault(), "%dh%02d", hours, mins);

        // Hiển thị 2 dòng tổng tiền và tổng thời gian chơi
        String moneyHtml = "Tổng tiền: <b>" + formatMoney(overallTotalPrice) + "</b>";
        tvTotalPriceLine.setText(Html.fromHtml(moneyHtml));
        tvTotalTimeLine.setText("Tổng thời gian chơi: " + timeStr);
    }
}
