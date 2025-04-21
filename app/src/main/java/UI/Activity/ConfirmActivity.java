package UI.Activity;

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

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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

import Data.Network.ApiService;
import Data.Network.NetworkUtils;
import Data.Network.RetrofitClient;
import Data.Holder.OrderServiceHolder;
import Data.Model.ConfirmOrder;
import Data.Model.Courts;
import Data.Model.CreateOrderRegularRequest;
import Data.Model.CreateOrderRequest;
import Data.Model.CreateOrderResponse;
import Data.Model.MyInfoResponse;
import Data.Model.NotificationRequest;
import Data.Model.OrderDetail;
import Data.Model.OrderDetailGroup;
import Data.Model.Orders;
import Data.Model.Service;
import Data.Model.ServiceDetail;
import Data.Model.ServiceOrderRequest;
import SEP490.G9.R;
import Data.Session.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmActivity extends AppCompatActivity {

    private TextView tvHeader, tvStadiumName, tvAddress, tvDate, tvTotalPriceLine, tvTotalTimeLine, tvContact;
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
    private Map<String, String> flexibleCourtSlotFixes;
    private String courtId, selectedDays, startDate, endDate, startTime, endTime;
    private List<String> selectedCourtSlots;
    private int totalPriceFixedOrder;
    private String orderType;
    private boolean isPaymentButtonClicked = false;

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
        tvContact = findViewById(R.id.tvContact);
        sessionManager = new SessionManager(this);

        Intent intent = getIntent();
        orderType = intent.getStringExtra("orderType");
        String tvPhoneFromIntent = intent.getStringExtra("tvPhone");
        if (tvPhoneFromIntent != null) {
            tvContact.setText("Liên hệ: " + tvPhoneFromIntent);
            Log.d("tvContact", tvPhoneFromIntent != null ? tvPhoneFromIntent : "Phone number not provided");
        }

        if ("Đơn cố định".equals(orderType)) {
            btnDeposit.setVisibility(View.GONE);
            btnPayment.setText("Thanh toán");
        }

        if ("Đơn cố định".equals(orderType)) {
            handleFixedOrder(intent);
        } else if ("Đơn dịch vụ".equals(orderType)) {
            courtName = intent.getStringExtra("courtName");
            etNote.setText(courtName);
            etNote.setEnabled(false); // Không cho người dùng chỉnh sửa note
            handleServiceOrder(intent);
        } else {
            handleRegularOrder(intent);
        }
    }

    private void handleServiceOrder(Intent intent) {
        // Lấy dữ liệu từ Intent
        courtName = intent.getStringExtra("courtName");
        courtAddress = intent.getStringExtra("address");
        String serviceDetailsJson = intent.getStringExtra("serviceDetailsJson");
        String serviceListJson = intent.getStringExtra("serviceListJson"); // Thêm dòng này để lấy danh sách Service
        orderId = intent.getStringExtra("orderId");

        // Parse danh sách Service từ JSON
        List<Service> serviceList = new ArrayList<>();
        if (serviceListJson != null && !serviceListJson.isEmpty()) {
            try {
                Gson gson = new Gson();
                serviceList = gson.fromJson(serviceListJson, new TypeToken<List<Service>>(){}.getType());
            } catch (Exception e) {
                Log.e("ConfirmActivity", "Lỗi parse serviceListJson: " + e.getMessage());
                Toast.makeText(this, "Lỗi dữ liệu danh sách dịch vụ", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            Log.e("ConfirmActivity", "serviceListJson is null or empty");
            Toast.makeText(this, "Không có dữ liệu danh sách dịch vụ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Kiểm tra và parse dữ liệu serviceDetailsJson
        List<ServiceDetail> serviceDetails = new ArrayList<>();
        if (serviceDetailsJson != null && !serviceDetailsJson.isEmpty()) {
            try {
                Gson gson = new Gson();
                serviceDetails = gson.fromJson(serviceDetailsJson, new TypeToken<List<ServiceDetail>>(){}.getType());
                if (serviceDetails == null || serviceDetails.isEmpty()) {
                    Toast.makeText(this, "Danh sách dịch vụ trống", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            } catch (Exception e) {
                Log.e("ConfirmActivity", "Lỗi parse serviceDetailsJson: " + e.getMessage());
                Toast.makeText(this, "Lỗi dữ liệu dịch vụ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            Log.e("ConfirmActivity", "serviceDetailsJson is null or empty");
            Toast.makeText(this, "Không có dữ liệu dịch vụ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        final List<ServiceDetail> finalServiceDetails = serviceDetails;
        final List<Service> finalServiceList = serviceList;
        final double paymentAmount = calculatePaymentAmount(serviceDetails);

        // Hiển thị thông tin lên giao diện
        tvStadiumName.setText("Tên sân: " + courtName);
        tvAddress.setText("Địa chỉ: " + courtAddress);
        tvDate.setText("Thông tin đơn dịch vụ:");

        layoutConfirmOrders.removeAllViews();
        for (ServiceDetail detail : serviceDetails) {
            TextView tvService = new TextView(this);
            // Tìm Service tương ứng với courtServiceId
            String serviceName = "Dịch vụ không xác định";
            for (Service service : finalServiceList) {
                if (service.getId() != null && service.getId().equals(detail.getCourtServiceId())) {
                    serviceName = service.getName();
                    break;
                }
            }
            // Sử dụng serviceName từ service.getName()
            String serviceText = serviceName + " x" + detail.getQuantity() + " : " + formatMoney((int) (detail.getPrice() * detail.getQuantity()));
            tvService.setText(serviceText);
            tvService.setTextColor(getResources().getColor(android.R.color.white));
            tvService.setTextSize(14);
            layoutConfirmOrders.addView(tvService);
        }

        tvTotalPriceLine.setText(Html.fromHtml("Tổng tiền: <b>" + formatMoney((int) paymentAmount) + "</b>"));

        // Ẩn nút đặt cọc và đặt tên nút thanh toán
        btnDeposit.setVisibility(View.GONE);
        btnPayment.setText("Thanh toán");

        // Kiểm tra thông tin người dùng
        ApiService apiService = RetrofitClient.getApiService(this);
        String token = sessionManager.getToken();
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
            Toast.makeText(this, "Bạn đang đặt dịch vụ với vai trò là Guest!", Toast.LENGTH_SHORT).show();
            etName.setText("");
            etPhone.setText("");
            String guestPhone = sessionManager.getGuestPhone();
            if (guestPhone != null && !guestPhone.isEmpty()) {
                etPhone.setText(guestPhone);
            }
        }

        // Trong ConfirmActivity.java, sửa phần xử lý nút btnPayment trong handleServiceOrder
        btnPayment.setOnClickListener(new View.OnClickListener() {
            private boolean isProcessing = false;

            @Override
            public void onClick(View v) {
                if (isProcessing) {
                    Toast.makeText(ConfirmActivity.this, "Đang xử lý, vui lòng chờ...", Toast.LENGTH_SHORT).show();
                    return;
                }

                isProcessing = true;
                btnPayment.setEnabled(false);

                String name = etName.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                String note = etNote.getText().toString().trim();

                if (name.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(ConfirmActivity.this, "Vui lòng nhập đủ Tên và Số điện thoại", Toast.LENGTH_SHORT).show();
                    isProcessing = false;
                    btnPayment.setEnabled(true);
                    return;
                }
                if (!name.matches("^[\\p{L}\\s]+$") || name.length() < 2) {
                    Toast.makeText(ConfirmActivity.this, "Tên chỉ chứa chữ cái và khoảng trắng, ít nhất 2 ký tự", Toast.LENGTH_SHORT).show();
                    isProcessing = false;
                    btnPayment.setEnabled(true);
                    return;
                }
                if (!phone.matches("0\\d{9}")) {
                    Toast.makeText(ConfirmActivity.this, "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0", Toast.LENGTH_SHORT).show();
                    isProcessing = false;
                    btnPayment.setEnabled(true);
                    return;
                }

                if (userId == null || userId.isEmpty()) {
                    sessionManager.setGuestPhone(phone);
                    registerNotification();
                }

                ServiceOrderRequest request = new ServiceOrderRequest();
                request.setCourtId(intent.getStringExtra("courtId"));
                request.setUserId(userId);
                request.setPaymentAmount(paymentAmount);
                request.setCustomerName(name);
                request.setPhoneNumber(phone);
                request.setNote(courtName);
                request.setServiceDetails(finalServiceDetails);

                Call<CreateOrderResponse> call = apiService.createServiceOrder(request);
                call.enqueue(new Callback<CreateOrderResponse>() {
                    @Override
                    public void onResponse(Call<CreateOrderResponse> call, Response<CreateOrderResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            CreateOrderResponse orderResponse = response.body();
                            String orderId = orderResponse.getId();
                            String qrCodeData = orderResponse.getQrcode();
                            String paymentTimeout = orderResponse.getPaymentTimeout();

                            // Lưu cả serviceDetailsJson và serviceListJson vào OrderServiceHolder
                            OrderServiceHolder.getInstance().addOrderDetail(orderId, serviceDetailsJson, serviceListJson);

                            String totalTime = String.format(Locale.getDefault(), "%dh%02d", 0, 0);

                            Intent i = new Intent(ConfirmActivity.this, QRCodeActivity.class);
                            i.putExtra("orderType", "Đơn dịch vụ");
                            i.putExtra("orderId", orderId);
                            i.putExtra("qrCodeData", qrCodeData);
                            i.putExtra("paymentTimeout", paymentTimeout);
                            i.putExtra("overallTotalPrice", (int) paymentAmount);
                            i.putExtra("paymentAmount", (int) paymentAmount);
                            i.putExtra("serviceDetailsJson", serviceDetailsJson);
                            i.putExtra("serviceListJson", serviceListJson); // Truyền thêm serviceListJson
                            i.putExtra("isDeposit", false);
                            i.putExtra("customerName", name);
                            i.putExtra("phoneNumber", phone);
                            i.putExtra("note", note);
                            i.putExtra("totalTime", totalTime);
                            startActivity(i);
                            finish();
                        } else {
                            Log.e("ConfirmActivity", "Tạo đơn dịch vụ thất bại. Mã trạng thái HTTP: " + response.code());
                            Toast.makeText(ConfirmActivity.this, "Tạo đơn dịch vụ thất bại!", Toast.LENGTH_SHORT).show();
                            isProcessing = false;
                            btnPayment.setEnabled(true);
                        }
                    }

                    @Override
                    public void onFailure(Call<CreateOrderResponse> call, Throwable t) {
                        Log.e("ConfirmActivity", "Lỗi mạng khi tạo đơn dịch vụ: " + t.getMessage(), t);
                        Toast.makeText(ConfirmActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        isProcessing = false;
                        btnPayment.setEnabled(true);
                    }
                });
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }
    private void handleFixedOrder(Intent intent) {
        courtId = intent.getStringExtra("courtId");
        selectedDays = intent.getStringExtra("selectedDays");
        startDate = intent.getStringExtra("startDate");
        endDate = intent.getStringExtra("endDate");
        startTime = intent.getStringExtra("startTime");
        endTime = intent.getStringExtra("endTime");
        selectedCourtSlots = intent.getStringArrayListExtra("selectedCourtSlots");
        String flexibleCourtSlotFixesJson = intent.getStringExtra("flexibleCourtSlotFixes");

        if (flexibleCourtSlotFixesJson != null) {
            Gson gson = new Gson();
            flexibleCourtSlotFixes = gson.fromJson(flexibleCourtSlotFixesJson, new TypeToken<Map<String, String>>(){}.getType());
        } else {
            flexibleCourtSlotFixes = new HashMap<>();
        }

        if (courtId == null || courtId.isEmpty() ||
                selectedDays == null || selectedDays.isEmpty() ||
                startDate == null || endDate == null ||
                startTime == null || endTime == null ||
                (selectedCourtSlots == null || selectedCourtSlots.isEmpty()) && flexibleCourtSlotFixes.isEmpty()) {
            Toast.makeText(this, "Không có dữ liệu đặt sân!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        int totalMinutes = calculateTotalTimeForFixedOrder();
        int hours = totalMinutes / 60;
        int mins = totalMinutes % 60;
        totalTime = String.format(Locale.getDefault(), "%dh%02d", hours, mins);
        tvTotalTimeLine.setText("Tổng thời gian chơi: " + totalTime);

        String formattedStartTime = startTime.contains(":") && startTime.split(":").length == 2 ? startTime + ":00" : startTime;
        String formattedEndTime = endTime.contains(":") && endTime.split(":").length == 2 ? endTime + ":00" : endTime;

        ApiService apiService = RetrofitClient.getApiService(this);
        Call<Double> call = apiService.getPaymentValue(courtId, selectedDays, startDate, endDate, formattedStartTime, formattedEndTime);
        final List<String> finalSelectedCourtSlots = selectedCourtSlots;
        call.enqueue(new Callback<Double>() {
            @Override
            public void onResponse(Call<Double> call, Response<Double> response) {
                if (response.isSuccessful() && response.body() != null) {
                    totalPriceFixedOrder = response.body().intValue();
                    int numberOfCourts = finalSelectedCourtSlots.size();
                    int finalTotalPrice = totalPriceFixedOrder * numberOfCourts;
                    tvTotalPriceLine.setText(Html.fromHtml("Tổng tiền: <b>" + formatMoney(finalTotalPrice) + "</b>"));
                } else {
                    Toast.makeText(ConfirmActivity.this, "Lỗi khi lấy tổng tiền", Toast.LENGTH_SHORT).show();
                    tvTotalPriceLine.setText("Tổng tiền: Lỗi");
                }
            }

            @Override
            public void onFailure(Call<Double> call, Throwable t) {
                Toast.makeText(ConfirmActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                tvTotalPriceLine.setText("Tổng tiền: Lỗi mạng");
            }
        });

        fetchCourtDetails(courtId);

        tvDate.setText("Ngày " + startDate + " đến ngày " + endDate + "\nThời gian: " + startTime + " - " + endTime + "\nTrong: " + selectedDays);

        layoutConfirmOrders.removeAllViews();
        if (selectedCourtSlots != null) {
            for (String courtSlot : selectedCourtSlots) {
                TextView tvCourtSlot = new TextView(this);
                tvCourtSlot.setText("Sân đang chọn: " + courtSlot);
                tvCourtSlot.setTextColor(getResources().getColor(android.R.color.white));
                tvCourtSlot.setTextSize(14);
                layoutConfirmOrders.addView(tvCourtSlot);
            }
        }
        for (Map.Entry<String, String> entry : flexibleCourtSlotFixes.entrySet()) {
            String date = entry.getKey();
            String alternativeCourt = entry.getValue();
            TextView tvReplacement = new TextView(this);
            tvReplacement.setText("Ngày " + date + ": Thay thế bằng " + alternativeCourt);
            tvReplacement.setTextColor(getResources().getColor(android.R.color.white));
            tvReplacement.setTextSize(14);
            layoutConfirmOrders.addView(tvReplacement);
        }

        String token = sessionManager.getToken();
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
            etName.setText("");
            etPhone.setText("");
            etNote.setText("");
            String guestPhone = sessionManager.getGuestPhone();
            if (guestPhone != null && !guestPhone.isEmpty()) {
                etPhone.setText(guestPhone);
            }
        }

        btnPayment.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String note = etNote.getText().toString().trim();

            if (token == null || token.isEmpty()) {
                if (name.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập đủ Tên và Số điện thoại", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
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
            createFixedOrder(false, totalPriceFixedOrder);
        });

        btnDeposit.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String note = etNote.getText().toString().trim();

            if (token == null || token.isEmpty()) {
                if (name.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập đủ Tên và Số điện thoại", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
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
            createFixedOrder(true, totalPriceFixedOrder);
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void handleRegularOrder(Intent intent) {
        clubId = intent.getStringExtra("club_id");
        selectedDate = intent.getStringExtra("selectedDate");
        confirmOrdersJson = intent.getStringExtra("confirmOrdersJson");
        orderId = intent.getStringExtra("orderId");

        if (selectedDate == null || selectedDate.trim().isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            selectedDate = sdf.format(new Date());
        }
        tvDate.setText("Thông tin chi tiết:");

        ApiService apiService = RetrofitClient.getApiService(this);
        String token = sessionManager.getToken();
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
            etName.setText("");
            etPhone.setText("");
            etNote.setText("");
            String guestPhone = sessionManager.getGuestPhone();
            if (guestPhone != null && !guestPhone.isEmpty()) {
                etPhone.setText(guestPhone);
            }
        }

        confirmOrders = new Gson().fromJson(confirmOrdersJson, new TypeToken<List<ConfirmOrder>>(){}.getType());
        if (confirmOrders == null || confirmOrders.isEmpty()) {
            Toast.makeText(this, "Không có dữ liệu đặt sân!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (clubId != null && !clubId.isEmpty()) {
            fetchCourtDetails(clubId);
        }
        buildConfirmOrdersUI();

        btnPayment.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String note = etNote.getText().toString().trim();

            if (token == null || token.isEmpty()) {
                if (name.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập đủ Tên và Số điện thoại", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
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
            if (userId == null || userId.isEmpty()) {
                sessionManager.setGuestPhone(phone);
                registerNotification();
            }
            processOrder(false);
        });

        btnDeposit.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String note = etNote.getText().toString().trim();

            if (token == null || token.isEmpty()) {
                if (name.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập đủ Tên và Số điện thoại", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
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
            if (userId == null || userId.isEmpty()) {
                sessionManager.setGuestPhone(phone);
                registerNotification();
            }
            processOrder(true);
        });

        btnBack.setOnClickListener(v -> finish());
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

    private void buildConfirmOrdersUI() {
        if (confirmOrders == null || confirmOrders.isEmpty()) return;
        TreeMap<String, List<ConfirmOrder>> ordersByDay = new TreeMap<>();
        for (ConfirmOrder o : confirmOrders) {
            String day = o.getDayBooking() != null ? o.getDayBooking() : selectedDate;
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
                overallTotalPrice += (int) o.getDailyPrice();
                totalMinutes += (toMinutes(o.getEndTime()) - toMinutes(o.getStartTime()));
                TextView tvSlot = new TextView(this);
                tvSlot.setTextColor(getResources().getColor(android.R.color.white));
                tvSlot.setTextSize(14);
                String detail = "   - " + o.getCourtSlotName() + ": " + o.getStartTime().substring(0, 5) + " - " + o.getEndTime().substring(0, 5) + " | " + formatMoney((int) o.getDailyPrice());
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

    private void createFixedOrder(boolean isDeposit, int totalPriceFixedOrder) {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String note = etNote.getText().toString().trim();

        CreateOrderRegularRequest req = new CreateOrderRegularRequest();
        req.setCourtId(courtId);
        req.setCourtName(courtName);
        req.setAddress(courtAddress);
        req.setUserId(userId);
        req.setCustomerName(name);
        req.setPhoneNumber(phone);
        req.setNote(note);
        req.setPaymentStatus("Chưa thanh toán");
        req.setOrderType("Đơn cố định");
        req.setStartDate(startDate);
        req.setEndDate(endDate);
        req.setStartTime(startTime);
        req.setEndTime(endTime);
        req.setSelectedDays(selectedDays);
        req.setSelectedCourtSlots(selectedCourtSlots != null ? selectedCourtSlots : new ArrayList<>());
        req.setFlexibleCourtSlotFixes(flexibleCourtSlotFixes);

        int numberOfCourts = selectedCourtSlots != null ? selectedCourtSlots.size() : 0;
        int finalTotalPrice = totalPriceFixedOrder * numberOfCourts;

        ApiService api = RetrofitClient.getApiService(this);
        Call<CreateOrderResponse> call = api.createFixedOrder(req);
        call.enqueue(new Callback<CreateOrderResponse>() {
            @Override
            public void onResponse(Call<CreateOrderResponse> call, Response<CreateOrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CreateOrderResponse r = response.body();
                    Intent i = new Intent(ConfirmActivity.this, QRCodeActivity.class);
                    i.putExtra("orderType", "Đơn cố định");
                    i.putExtra("orderId", r.getId());
                    i.putExtra("totalPriceFixedOrder", finalTotalPrice);
                    i.putExtra("qrCodeData", r.getQrcode());
                    i.putExtra("overallTotalPrice", finalTotalPrice);
                    i.putExtra("depositAmount", r.getDepositAmount());
                    i.putExtra("isDeposit", isDeposit);
                    i.putExtra("totalTime", totalTime);
                    i.putExtra("courtId", courtId);
                    i.putExtra("customerName", name);
                    i.putExtra("phoneNumber", phone);
                    i.putExtra("note", note);
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(ConfirmActivity.this, "Tạo đơn thất bại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CreateOrderResponse> call, Throwable t) {
                Toast.makeText(ConfirmActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processOrder(boolean isDeposit) {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String note = etNote.getText().toString().trim();

        btnPayment.setEnabled(false);
        btnDeposit.setEnabled(false);

        overallTotalPrice = 0;
        for (ConfirmOrder o : confirmOrders) {
            overallTotalPrice += (int) o.getDailyPrice();
        }
        int n = confirmOrders.size();
        int totalAmount = overallTotalPrice;
        int depositAmount = 0;

        if (n == 1) {
            depositAmount = totalAmount;
        } else {
            List<Integer> slotPricesList = new ArrayList<>();
            for (ConfirmOrder o : confirmOrders) {
                slotPricesList.add((int) o.getDailyPrice());
            }
            Collections.sort(slotPricesList);
            int k = (int) Math.ceil(n / 3.0);
            for (int i = 0; i < k && i < slotPricesList.size(); i++) {
                depositAmount += slotPricesList.get(i);
            }
        }

        final int finalTotalAmount = totalAmount;
        final int finalDepositAmount = depositAmount;

        CreateOrderRequest req = new CreateOrderRequest();
        req.setCourtId(clubId);
//        req.setCourtName(courtName);
//        req.setAddress(courtAddress);
        req.setCustomerName(name);
        req.setUserId(userId);
        req.setPhoneNumber(phone);
        req.setTotalAmount(finalTotalAmount);
        req.setDiscountCode(null);
        req.setNote(note.isEmpty() ? null : note);
        req.setDiscountAmount(0);
        req.setOrderType(null);

        Map<String, List<OrderDetail>> orderDetailsByDate = new HashMap<>();
        for (ConfirmOrder o : confirmOrders) {
            String date = o.getDayBooking() != null ? o.getDayBooking() : selectedDate;
            OrderDetail d = new OrderDetail();
            d.setCourtSlotId(o.getCourtSlotId());
            //d.setCourtSlotName(o.getCourtSlotName());
            d.setStartTime(o.getStartTime());
            d.setEndTime(o.getEndTime());
            d.setPrice((int) o.getDailyPrice());
            orderDetailsByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(d);
        }

        List<OrderDetailGroup> orderDetailGroups = new ArrayList<>();
        for (Map.Entry<String, List<OrderDetail>> entry : orderDetailsByDate.entrySet()) {
            OrderDetailGroup group = new OrderDetailGroup();
            group.setBookingDate(entry.getKey());
            group.setBookingSlots(entry.getValue());
            orderDetailGroups.add(group);
        }
        req.setOrderDetails(orderDetailGroups);

        ApiService api = RetrofitClient.getApiService(this);

        List<Integer> slotPricesList = new ArrayList<>();
        for (ConfirmOrder o : confirmOrders) {
            slotPricesList.add((int) o.getDailyPrice());
        }

        if (orderId != null && !orderId.isEmpty()) {
            Call<Orders> orderCall = api.getOrderById(orderId);
            NetworkUtils.callApi(orderCall, this, new NetworkUtils.ApiCallback<Orders>() {
                @Override
                public void onSuccess(Orders order) {
                    if (order == null) {
                        Toast.makeText(ConfirmActivity.this, "Đơn hàng không tồn tại", Toast.LENGTH_SHORT).show();
                        btnPayment.setEnabled(true);
                        btnDeposit.setEnabled(true);
                        return;
                    }

                    int amountPaid = order.getAmountPaid();
                    int initialPaymentAmount;
                    String paymentStatus;

                    if (isDeposit) {
                        initialPaymentAmount = finalDepositAmount - amountPaid;
                        paymentStatus = (initialPaymentAmount > 0) ? ((n == 1) ? "Chưa thanh toán" : "Chưa đặt cọc")
                                : ((n == 1) ? "Đã thanh toán" : "Đã đặt cọc");
                    } else {
                        initialPaymentAmount = finalTotalAmount - amountPaid;
                        paymentStatus = (initialPaymentAmount > 0) ? "Chưa thanh toán" : "Đã thanh toán";
                    }

                    int oldDepositAmount = order.getDepositAmount();
                    int maxAllowedRefund = amountPaid - oldDepositAmount;

                    final int computedPaymentAmount;
                    final int computedRefundAmount;
                    if (initialPaymentAmount >= 0) {
                        computedPaymentAmount = initialPaymentAmount;
                        computedRefundAmount = 0;
                    } else {
                        int tempRefund = Math.abs(initialPaymentAmount);
                        if (tempRefund > maxAllowedRefund) {
                            computedPaymentAmount = -maxAllowedRefund;
                            computedRefundAmount = maxAllowedRefund;
                            Toast.makeText(ConfirmActivity.this, "Số tiền hoàn lại tối đa là " + formatMoney(maxAllowedRefund), Toast.LENGTH_SHORT).show();
                        } else {
                            computedPaymentAmount = initialPaymentAmount;
                            computedRefundAmount = tempRefund;
                        }
                    }

                    req.setPaymentAmount(computedPaymentAmount);
                    req.setPaymentStatus(paymentStatus);
                    req.setDepositAmount(finalDepositAmount);
                    req.setSignature(genSignature(
                            String.valueOf(finalTotalAmount),
                            String.valueOf(computedPaymentAmount),
                            String.valueOf(finalDepositAmount),
                            phone));

                    Call<CreateOrderResponse> call = api.changeOrder(orderId, req);
                    NetworkUtils.callApi(call, ConfirmActivity.this, new NetworkUtils.ApiCallback<CreateOrderResponse>() {
                        @Override
                        public void onSuccess(CreateOrderResponse r) {
                            if (r != null) {
                                Intent i = (computedPaymentAmount > 0 && r.getQrcode() != null && !r.getQrcode().isEmpty())
                                        ? new Intent(ConfirmActivity.this, QRCodeActivity.class)
                                        : new Intent(ConfirmActivity.this, PaymentSuccessActivity.class);
                                if (i.getComponent().getClassName().equals(QRCodeActivity.class.getName())) {
                                    i.putExtra("qrCodeData", r.getQrcode());
                                    i.putExtra("paymentTimeout", r.getPaymentTimeout());
                                    i.putExtra("overallTotalPrice", r.getTotalAmount());
                                    i.putExtra("paymentAmount", computedPaymentAmount);
                                    i.putExtra("amountPaid", amountPaid);
                                    i.putExtra("depositAmount", finalDepositAmount);
                                    i.putExtra("isDeposit", isDeposit);
                                    i.putExtra("courtId", clubId);
                                    i.putExtra("customerName", name);
                                    i.putExtra("phoneNumber", phone);
                                    i.putExtra("note", note);
                                    i.putExtra("orderType", getIntent().getStringExtra("orderType"));
                                    i.putIntegerArrayListExtra("slotPrices", new ArrayList<>(slotPricesList));
                                }
                                i.putExtra("orderId", r.getId());
                                i.putExtra("totalTime", totalTime);
                                i.putExtra("selectedDate", selectedDate);
                                i.putExtra("totalPrice", r.getTotalAmount());
                                i.putExtra("courtId", clubId);
                                i.putExtra("customerName", name);
                                i.putExtra("phoneNumber", phone);
                                i.putExtra("note", note);
                                i.putExtra("refundAmount", computedRefundAmount);
                                i.putExtra("confirmOrdersJson", confirmOrdersJson);
                                i.putExtra("orderType", getIntent().getStringExtra("orderType"));
                                i.putIntegerArrayListExtra("slotPrices", new ArrayList<>(slotPricesList));
                                startActivity(i);
                                finish();
                            } else {
                                Toast.makeText(ConfirmActivity.this, "Thay đổi lịch thất bại!", Toast.LENGTH_SHORT).show();
                                btnPayment.setEnabled(true);
                                btnDeposit.setEnabled(true);
                            }
                        }

                        @Override
                        public void onError(String e) {
                            Toast.makeText(ConfirmActivity.this, "Thay đổi lịch thất bại: " + e, Toast.LENGTH_SHORT).show();
                            btnPayment.setEnabled(true);
                            btnDeposit.setEnabled(true);
                        }
                    });
                }

                @Override
                public void onError(String e) {
                    Toast.makeText(ConfirmActivity.this, "Không thể lấy thông tin đơn cũ: " + e, Toast.LENGTH_SHORT).show();
                    btnPayment.setEnabled(true);
                    btnDeposit.setEnabled(true);
                }
            });
        } else {
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
            NetworkUtils.callApi(call, ConfirmActivity.this, new NetworkUtils.ApiCallback<CreateOrderResponse>() {
                @Override
                public void onSuccess(CreateOrderResponse r) {
                    if (r != null && r.getQrcode() != null && !r.getQrcode().isEmpty()) {
                        Intent i = new Intent(ConfirmActivity.this, QRCodeActivity.class);
                        i.putExtra("qrCodeData", r.getQrcode());
                        i.putExtra("paymentTimeout", r.getPaymentTimeout());
                        i.putExtra("orderId", r.getId());
                        i.putExtra("overallTotalPrice", finalTotalAmount);
                        i.putExtra("depositAmount", finalDepositAmount);
                        i.putExtra("paymentAmount", paymentAmount);
                        i.putExtra("amountPaid", amountPaid);
                        i.putExtra("customerName", name);
                        i.putExtra("phoneNumber", phone);
                        i.putExtra("note", note);
                        i.putExtra("isDeposit", isDeposit);
                        i.putExtra("totalTime", totalTime);
                        i.putExtra("selectedDate", selectedDate);
                        i.putExtra("totalPrice", finalTotalAmount);
                        i.putExtra("courtId", clubId);
                        i.putExtra("confirmOrdersJson", confirmOrdersJson);
                        i.putExtra("orderType", getIntent().getStringExtra("orderType"));
                        i.putIntegerArrayListExtra("slotPrices", new ArrayList<>(slotPricesList));
                        startActivity(i);
                        finish();
                    } else {
                        Intent i = new Intent(ConfirmActivity.this, PaymentSuccessActivity.class);
                        i.putExtra("orderId", r.getId());
                        i.putExtra("totalTime", totalTime);
                        i.putExtra("selectedDate", selectedDate);
                        i.putExtra("totalPrice", finalTotalAmount);
                        i.putExtra("courtId", clubId);
                        i.putExtra("customerName", name);
                        i.putExtra("phoneNumber", phone);
                        i.putExtra("note", note);
                        i.putExtra("confirmOrdersJson", confirmOrdersJson);
                        i.putExtra("orderType", getIntent().getStringExtra("orderType"));
                        i.putIntegerArrayListExtra("slotPrices", new ArrayList<>(slotPricesList));
                        startActivity(i);
                        finish();
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
    }

    private int toMinutes(String time) {
        if (time == null || !time.contains(":")) {
            Log.e("ConfirmActivity", "Thời gian không hợp lệ: " + time);
            return 0;
        }
        String[] parts = time.split(":");
        if (parts.length < 2) {
            Log.e("ConfirmActivity", "Thời gian không đúng định dạng: " + time);
            return 0;
        }
        try {
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            return hours * 60 + minutes;
        } catch (NumberFormatException e) {
            Log.e("ConfirmActivity", "Lỗi chuyển đổi số: " + e.getMessage());
            return 0;
        }
    }

    private String formatMoney(int amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setGroupingSeparator('.');
        DecimalFormat formatter = new DecimalFormat("#,###", symbols);
        return formatter.format(amount) + "đ";
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

    private void registerNotification() {
        if (userId != null && !userId.isEmpty()) {
            // Nếu là user đăng nhập, đăng ký thông báo với userId
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String fcmToken = task.getResult();
                    sendDeviceTokenToServer(userId, fcmToken);
                }
            });
        } else {
            // Nếu là guest, chỉ đăng ký với số điện thoại mới nhất
            String guestPhone = sessionManager.getGuestPhone();
            if (guestPhone != null && !guestPhone.isEmpty()) {
                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String fcmToken = task.getResult();
                        sendDeviceTokenToServer(guestPhone, fcmToken);
                    }
                });
            }
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

    private int calculateTotalTimeForFixedOrder() {
        int minutesPerDay = toMinutes(endTime) - toMinutes(startTime);
        if (minutesPerDay <= 0) {
            Log.e("ConfirmActivity", "Thời gian không hợp lệ: endTime phải sau startTime");
            return 0;
        }

        int numberOfDays = calculateNumberOfDays(startDate, endDate, selectedDays);
        if (numberOfDays <= 0) {
            Log.e("ConfirmActivity", "Không có ngày nào được chọn trong khoảng thời gian");
            return 0;
        }

        int numberOfCourts = selectedCourtSlots != null ? selectedCourtSlots.size() : 0;
        if (numberOfCourts == 0) {
            Log.e("ConfirmActivity", "Không có sân nào được chọn");
            return 0;
        }

        return minutesPerDay * numberOfDays * numberOfCourts;
    }

    private int calculateNumberOfDays(String startDate, String endDate, String selectedDays) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);

            if (start == null || end == null || start.after(end)) {
                Log.e("ConfirmActivity", "Ngày không hợp lệ");
                return 0;
            }

            String[] daysArray = selectedDays.split(",");
            List<String> selectedDaysList = Arrays.asList(daysArray);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(start);
            int count = 0;

            while (!calendar.getTime().after(end)) {
                String dayName = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(calendar.getTime());
                if (selectedDaysList.contains(dayName.toUpperCase())) {
                    count++;
                }
                calendar.add(Calendar.DATE, 1);
            }
            return count;
        } catch (Exception e) {
            Log.e("ConfirmActivity", "Lỗi tính số ngày: " + e.getMessage());
            return 0;
        }
    }

    private double calculatePaymentAmount(List<ServiceDetail> serviceDetails) {
        double amount = 0;
        for (ServiceDetail detail : serviceDetails) {
            amount += detail.getPrice() * detail.getQuantity();
        }
        return amount;
    }
}