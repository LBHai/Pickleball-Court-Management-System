package SEP490.G9;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import Api.ApiService;
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

    private TextView tvHeader, tvStadiumName, tvAddress, tvDate, tvGameType, tvTotalPrice;
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
        tvGameType = findViewById(R.id.tvGameType);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        layoutConfirmOrders = findViewById(R.id.layoutConfirmOrders);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etNote = findViewById(R.id.etNote);
        btnConfirm = findViewById(R.id.btnConfirm);

        // Lấy dữ liệu từ Intent
        clubId = getIntent().getStringExtra("club_id");
        selectedDate = getIntent().getStringExtra("selectedDate");
        confirmOrdersJson = getIntent().getStringExtra("confirmOrdersJson");

        // Nếu có token thì lấy thông tin người dùng
        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getToken();
        if (token != null && !token.isEmpty()) {
            String authHeader = "Bearer " + token;
            ApiService.apiService.getMyInfo(authHeader).enqueue(new Callback<MyInfoResponse>() {
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
                        Toast.makeText(ConfirmActivity.this, "Lấy thông tin thất bại: HTTP " + response.code() + " - " + response.message(), Toast.LENGTH_SHORT).show();
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

        // Parse JSON -> danh sách slot đã chọn
        confirmOrders = new Gson().fromJson(confirmOrdersJson, new TypeToken<List<ConfirmOrder>>(){}.getType());

        tvDate.setText("Ngày: " + selectedDate);
        tvGameType.setText("Pickleball, Sân 5 người");

        if (clubId != null && !clubId.isEmpty()) {
            fetchCourtDetails(clubId);
        }

        buildConfirmOrdersUI();

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

                // Tính tổng giá tiền và xây dựng danh sách OrderDetail
                int totalAmount = 0;
                List<OrderDetail> orderDetails = new ArrayList<>();
                for (ConfirmOrder order : confirmOrders) {
                    totalAmount += (int) order.getDailyPrice();  // ép kiểu nếu cần
                    OrderDetail detail = new OrderDetail();
                    detail.setCourtSlotId(order.getCourtSlotId());
                    detail.setCourtSlotName(order.getCourtSlotName());
                    detail.setStartTime(order.getStartTime());
                    detail.setEndTime(order.getEndTime());
                    detail.setPrice((int) order.getDailyPrice());  // ép kiểu chuyển double sang int nếu cần
                    orderDetails.add(detail);
                }

                // Xây dựng đối tượng CreateOrderRequest
                CreateOrderRequest createOrderRequest = new CreateOrderRequest();
                createOrderRequest.setCourtId(clubId);
                createOrderRequest.setCourtName(tvStadiumName.getText().toString());
                createOrderRequest.setAddress(tvAddress.getText().toString());
                createOrderRequest.setBookingDate(selectedDate);
                createOrderRequest.setCustomerName(name);
                createOrderRequest.setPhoneNumber(phone);
                createOrderRequest.setTotalAmount(totalAmount);
                createOrderRequest.setDiscountCode(null); // nếu không có
                createOrderRequest.setNote(note);
                createOrderRequest.setDiscountAmount(0);
                //totalAmount
                createOrderRequest.setPaymentAmount(2000); // giá trị fake để test
                createOrderRequest.setPaymentStatus("Đặt cọc");
                createOrderRequest.setOrderDetails(orderDetails);

                // Log request để kiểm tra
                Log.d("CreateOrder", "Request: " + new Gson().toJson(createOrderRequest));

                // Gọi API tạo đơn hàng
                ApiService.apiService.createOrder(createOrderRequest).enqueue(new Callback<CreateOrderResponse>() {
                    @Override
                    public void onResponse(Call<CreateOrderResponse> call, Response<CreateOrderResponse> response) {
                        // Trong onResponse của API tạo đơn hàng
                        if (response.isSuccessful() && response.body() != null) {
                            CreateOrderResponse orderResponse = response.body();
                            Log.d("CreateOrder", "Order ID: " + orderResponse.getId());
                            if (orderResponse.getQrcode() != null && !orderResponse.getQrcode().isEmpty()) {
                                Intent intent = new Intent(ConfirmActivity.this, QRCodeActivity.class);
                                intent.putExtra("qrCodeData", orderResponse.getQrcode());
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

    private void fetchCourtDetails(String clubId) {
        ApiService.apiService.getCourtById(clubId).enqueue(new Callback<Courts>() {
            @Override
            public void onResponse(Call<Courts> call, Response<Courts> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Courts court = response.body();
                    tvStadiumName.setText(court.getName());
                    tvAddress.setText("Địa chỉ: " + court.getAddress());
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

    private void buildConfirmOrdersUI() {
        if (confirmOrders == null || confirmOrders.isEmpty()) return;
        int totalPrice = 0;
        for (ConfirmOrder order : confirmOrders) {
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_confirm_order, layoutConfirmOrders, false);
            TextView tvCourtSlotName = itemView.findViewById(R.id.tvCourtSlotName);
            TextView tvTime = itemView.findViewById(R.id.tvTime);
            TextView tvPrice = itemView.findViewById(R.id.tvPrice);
            tvCourtSlotName.setText("Sân: " + order.getCourtSlotName());
            tvTime.setText("Thời gian: " + order.getStartTime() + " - " + order.getEndTime());
            tvPrice.setText("Giá: " + order.getDailyPrice() + " đ");
            totalPrice += order.getDailyPrice();
            layoutConfirmOrders.addView(itemView);
        }
        tvTotalPrice.setText("Tổng giá: " + totalPrice + " đ");
    }
}
