package UI.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import Utils.DebouncedOnClickListener;
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

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
    private TextInputLayout tilPhone,tilName, tilNote;
    private LinearLayout layoutConfirmOrders;
    private TextInputEditText etName, etPhone, etNote;
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
        tilPhone = findViewById(R.id.tilPhone);
        tilName = findViewById(R.id.tilName);
        tilNote = findViewById(R.id.tilNote);

        sessionManager = new SessionManager(this);

        Intent intent = getIntent();
        orderType = intent.getStringExtra("orderType");
        String tvPhoneFromIntent = intent.getStringExtra("tvPhone");
        if (tvPhoneFromIntent != null) {
            tvContact.setText(getString(R.string.stadium_contact) + tvPhoneFromIntent);
            Log.d("tvContact", tvPhoneFromIntent != null ? tvPhoneFromIntent : "Phone number not provided");
        }

        if ("Đơn cố định".equals(orderType)) {
            btnDeposit.setVisibility(View.GONE);
            btnPayment.setText("PAYMENT");
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
                Toast.makeText(this, getString(R.string.error_list_service_label), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            Log.e("ConfirmActivity", "serviceListJson is null or empty");
            Toast.makeText(this, getString(R.string.error_nodata_service_label), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(this, getString(R.string.empty_service_label), Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            } catch (Exception e) {
                Log.e("ConfirmActivity", "Lỗi parse serviceDetailsJson: " + e.getMessage());
                Toast.makeText(this, getString(R.string.error_data_service_label) + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            Log.e("ConfirmActivity", "serviceDetailsJson is null or empty");
            Toast.makeText(this, getString(R.string.no_data_service_label), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        final List<ServiceDetail> finalServiceDetails = serviceDetails;
        final List<Service> finalServiceList = serviceList;
        final double paymentAmount = calculatePaymentAmount(serviceDetails);

        // Hiển thị thông tin lên giao diện
        tvStadiumName.setText(getString(R.string.label_stadium_name, courtName));
        tvAddress.setText(getString(R.string.label_address, courtAddress));
        tvDate.setText(getString(R.string.infomation_service_label));

        layoutConfirmOrders.removeAllViews();
        for (ServiceDetail detail : serviceDetails) {
            TextView tvService = new TextView(this);
            // Tìm Service tương ứng với courtServiceId
            String serviceName = getString(R.string.unknown_service_label);
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

        tvTotalPriceLine.setText(Html.fromHtml(
                getString(R.string.total_price_html, formatMoney((int) paymentAmount))
        ));

        // Ẩn nút đặt cọc và đặt tên nút thanh toán
        btnDeposit.setVisibility(View.GONE);
        btnPayment.setText("PAYMENT");

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
                    Toast.makeText(ConfirmActivity.this, getString(R.string.error_get_infor), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, getString(R.string.order_service_guest), Toast.LENGTH_SHORT).show();
            etName.setText("");
            etPhone.setText("");
            String guestPhone = sessionManager.getGuestPhone();
            if (guestPhone != null && !guestPhone.isEmpty()) {
                etPhone.setText(guestPhone);
            }
        }

        // Trong ConfirmActivity.java, sửa phần xử lý nút btnPayment trong handleServiceOrder
        btnPayment.setOnClickListener(new DebouncedOnClickListener(1000) {
            private boolean isProcessing = false;

            @Override
            public void onDebouncedClick(View v) {
                if (isProcessing) {
                    Toast.makeText(ConfirmActivity.this, getString(R.string.watting), Toast.LENGTH_SHORT).show();
                    return;
                }

                isProcessing = true;
                btnPayment.setEnabled(false);

                boolean isNameValid = validateName();
                boolean isPhoneValid = validatePhoneNumber();
                boolean isNoteValid = validateNote();
                // Proceed only if both validations pass
                if (!isNameValid || !isPhoneValid) {
                    isProcessing = false;
                    btnPayment.setEnabled(true);
                    return;
                }

                String name = etName.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                String note = etNote.getText().toString().trim();

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
                            Log.d("ConfirmActivity", "Đơn hàng được tạo - Order ID: " + orderId);
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
                            Toast.makeText(ConfirmActivity.this, getString(R.string.gen_service_fail), Toast.LENGTH_SHORT).show();
                            isProcessing = false;
                            btnPayment.setEnabled(true);
                        }
                    }

                    @Override
                    public void onFailure(Call<CreateOrderResponse> call, Throwable t) {
                        Log.e("ConfirmActivity", "Lỗi mạng khi tạo đơn dịch vụ: " + t.getMessage(), t);
                        ///Toast.makeText(ConfirmActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, getString(R.string.no_data_booked), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        int totalMinutes = calculateTotalTimeForFixedOrder();
        int hours = totalMinutes / 60;
        int mins = totalMinutes % 60;
        totalTime = String.format(Locale.getDefault(), "%dh%02d", hours, mins);
        tvTotalTimeLine.setText(getString(R.string.total_booked) + totalTime);

        String formattedStartTime = startTime.contains(":") && startTime.split(":").length == 2 ? startTime + ":00" : startTime;
        String formattedEndTime = endTime.contains(":") && endTime.split(":").length == 2 ? endTime + ":00" : endTime;

        ApiService apiService = RetrofitClient.getApiService(this);
        Call<Double> call = apiService.getPaymentValue(courtId, selectedDays, startDate, endDate, formattedStartTime, formattedEndTime);
        final List<String> finalSelectedCourtSlots = selectedCourtSlots;
        call.enqueue(new Callback<Double>() {
            @Override
            public void onResponse(Call<Double> call, Response<Double> response) {
                if (response.isSuccessful() && response.body() != null) {
                    double paymentValue = response.body();
                    if (paymentValue == 0) {
                        // Hiển thị dialog thông báo lỗi
                        AlertDialog.Builder builder = new AlertDialog.Builder(ConfirmActivity.this);
                        builder.setTitle(getString(R.string.dialog_title_invalid_request));
                        builder.setMessage(getString(R.string.dialog_message_invalid_request));
                        builder.setPositiveButton(getString(R.string.dialog_button_retry), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(ConfirmActivity.this, BookingRegularTableActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });

                        builder.setCancelable(false); // Không cho phép đóng dialog bằng nút back
                        builder.show();
                    } else {
                        totalPriceFixedOrder = response.body().intValue();
                        int numberOfCourts = finalSelectedCourtSlots.size();
                        int finalTotalPrice = totalPriceFixedOrder * numberOfCourts;
                        String totalPriceText = getString(R.string.total_price_html, formatMoney(finalTotalPrice));
                        tvTotalPriceLine.setText(Html.fromHtml(totalPriceText));
                    }
                } else {
                    Toast.makeText(ConfirmActivity.this, getString(R.string.error_get_total), Toast.LENGTH_SHORT).show();
                    tvTotalPriceLine.setText("Tổng tiền: Lỗi");
                }
            }

            @Override
            public void onFailure(Call<Double> call, Throwable t) {
                //Toast.makeText(ConfirmActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                //tvTotalPriceLine.setText("Tổng tiền: Lỗi mạng");
            }
        });

        fetchCourtDetails(courtId);

        tvDate.setText(getString(R.string.date_range, startDate, endDate, startTime, endTime, selectedDays));

        layoutConfirmOrders.removeAllViews();
        if (selectedCourtSlots != null) {
            for (String courtSlot : selectedCourtSlots) {
                TextView tvCourtSlot = new TextView(this);
                tvCourtSlot.setText(getString(R.string.court_selected)+ courtSlot);
                tvCourtSlot.setTextColor(getResources().getColor(android.R.color.white));
                tvCourtSlot.setTextSize(14);
                layoutConfirmOrders.addView(tvCourtSlot);
            }
        }
        for (Map.Entry<String, String> entry : flexibleCourtSlotFixes.entrySet()) {
            String date = entry.getKey();
            String alternativeCourt = entry.getValue();
            TextView tvReplacement = new TextView(this);
            tvReplacement.setText(getString(R.string.replacement_info, date, alternativeCourt));
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
                    Toast.makeText(ConfirmActivity.this, getString(R.string.error_get_infor), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, getString(R.string.order_court_guest), Toast.LENGTH_SHORT).show();
            etName.setText("");
            etPhone.setText("");
            etNote.setText("");
            String guestPhone = sessionManager.getGuestPhone();
            if (guestPhone != null && !guestPhone.isEmpty()) {
                etPhone.setText(guestPhone);
            }
        }

        btnPayment.setOnClickListener(new DebouncedOnClickListener(1000) {
            @Override
            public void onDebouncedClick(View v) {
                boolean isNameValid = validateName();
                boolean isPhoneValid = validatePhoneNumber();
                boolean isNoteValid = validateNote();
                if (!isNameValid || !isPhoneValid || !isNoteValid) {
                    return;
                }
                String name = etName.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                String note = etNote.getText().toString().trim();
                createFixedOrder(false, totalPriceFixedOrder);
            }
        });

        btnDeposit.setOnClickListener(new DebouncedOnClickListener(1000) {
            @Override
            public void onDebouncedClick(View v) {
                boolean isNameValid = validateName();
                boolean isPhoneValid = validatePhoneNumber();
                boolean isNoteValid = validateNote();
                if (!isNameValid || !isPhoneValid || !isNoteValid) {
                    return;
                }
                String name = etName.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                String note = etNote.getText().toString().trim();
                createFixedOrder(true, totalPriceFixedOrder);
            }
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
        tvDate.setText(getString(R.string.detail_infor));

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
                    Toast.makeText(ConfirmActivity.this, getString(R.string.error_get_infor), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, getString(R.string.order_court_guest), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, getString(R.string.no_data_booked), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
// Thêm log để kiểm tra giá trị dailyPrice ngay sau khi parse confirmOrders
        for (ConfirmOrder o : confirmOrders) {
            Log.d("ConfirmActivity", "ConfirmOrder - Slot: " + o.getCourtSlotName() + ", DailyPrice: " + o.getDailyPrice());
        }
        if (clubId != null && !clubId.isEmpty()) {
            fetchCourtDetails(clubId);
        }
        buildConfirmOrdersUI();

        btnPayment.setOnClickListener(new DebouncedOnClickListener(1000) {
            @Override
            public void onDebouncedClick(View v) {
                boolean isNameValid = validateName();
                boolean isPhoneValid = validatePhoneNumber();
                boolean isNoteValid = validateNote();
                if (!isNameValid || !isPhoneValid || !isNoteValid) {
                    return;
                }
                String name = etName.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                String note = etNote.getText().toString().trim();
                if (userId == null || userId.isEmpty()) {
                    sessionManager.setGuestPhone(phone);
                    registerNotification();
                }
                processOrder(false);
            }
        });

        btnDeposit.setOnClickListener(new DebouncedOnClickListener(1000) {
            @Override
            public void onDebouncedClick(View v) {
                boolean isNameValid = validateName();
                boolean isPhoneValid = validatePhoneNumber();
                boolean isNoteValid = validateNote();
                if (!isNameValid || !isPhoneValid || !isNoteValid) {
                    return;
                }
                String name = etName.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                String note = etNote.getText().toString().trim();
                if (userId == null || userId.isEmpty()) {
                    sessionManager.setGuestPhone(phone);
                    registerNotification();
                }
                processOrder(true);
            }
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
                    tvStadiumName.setText(getString(R.string.label_stadium_name) + courtName);
                    tvAddress.setText(getString(R.string.label_address) + courtAddress);
                }
            }
            @Override
            public void onError(String e) {
                Toast.makeText(ConfirmActivity.this, getString(R.string.error_fetch_court_infor), Toast.LENGTH_SHORT).show();
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
            tvDayHeader.setText(getString(R.string.data_label) + e.getKey());
            layoutConfirmOrders.addView(tvDayHeader);
            List<ConfirmOrder> list = e.getValue();
            list.sort((o1, o2) -> Integer.compare(toMinutes(o1.getStartTime()), toMinutes(o2.getStartTime())));
            for (ConfirmOrder o : list) {
                overallTotalPrice += (int) o.getDailyPrice();
                totalMinutes += (toMinutes(o.getEndTime()) - toMinutes(o.getStartTime()));
                TextView tvSlot = new TextView(this);
                tvSlot.setTextColor(getResources().getColor(android.R.color.white));
                tvSlot.setTextSize(14);
                // Thêm log trước khi hiển thị giá
                int price = (int) o.getDailyPrice();
                Log.d("ConfirmActivity", "Slot: " + o.getCourtSlotName() + ", Price before format: " + price);
                String formattedPrice = formatMoney(price);
                Log.d("ConfirmActivity", "Slot: " + o.getCourtSlotName() + ", Formatted price: " + formattedPrice);
                String detail = "   - " + o.getCourtSlotName() + ": " + o.getStartTime().substring(0, 5) + " - " + o.getEndTime().substring(0, 5) + " | " + formattedPrice;
                tvSlot.setText(detail);
                layoutConfirmOrders.addView(tvSlot);
            }
        }
        int hours = totalMinutes / 60;
        int mins = totalMinutes % 60;
        totalTime = String.format(Locale.getDefault(), "%dh%02d", hours, mins);
        tvTotalPriceLine.setText(Html.fromHtml(getString(R.string.total_price_html, formatMoney(overallTotalPrice))));
        tvTotalTimeLine.setText(getString(R.string.total_play_time, totalTime));

    }
    private void createFixedOrder(boolean isDeposit, int totalPriceFixedOrder) {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String note = etNote.getText().toString().trim();

        CreateOrderRegularRequest req = new CreateOrderRegularRequest();
        req.setCourtId(courtId);
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

//        Log.d("CreateFixedOrder", "=== Thông tin đơn cố định ===");
//        Log.d("CreateFixedOrder", "Court ID: " + courtId);
//        Log.d("CreateFixedOrder", "User ID: " + userId);
//        Log.d("CreateFixedOrder", "Customer Name: " + name);
//        Log.d("CreateFixedOrder", "Phone Number: " + phone);
//        Log.d("CreateFixedOrder", "Note: " + note);
//        Log.d("CreateFixedOrder", "Payment Status: Chưa thanh toán");
//        Log.d("CreateFixedOrder", "Order Type: Đơn cố định");
//        Log.d("CreateFixedOrder", "Start Date: " + startDate);
//        Log.d("CreateFixedOrder", "End Date: " + endDate);
//        Log.d("CreateFixedOrder", "Start Time: " + startTime);
//        Log.d("CreateFixedOrder", "End Time: " + endTime);
//        Log.d("CreateFixedOrder", "Selected Days: " + selectedDays);
//        Log.d("CreateFixedOrder", "Selected Court Slots: " + (selectedCourtSlots != null ? selectedCourtSlots.toString() : "[]"));
//        Log.d("CreateFixedOrder", "Flexible Court Slot Fixes: " + flexibleCourtSlotFixes);
//        Log.d("CreateFixedOrder", "Number of Courts: " + numberOfCourts);
//        Log.d("CreateFixedOrder", "Total Price: " + finalTotalPrice);
//        Log.d("CreateFixedOrder", "Is Deposit: " + isDeposit);
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
                    i.putStringArrayListExtra("selectedCourtSlots", new ArrayList<>(selectedCourtSlots)); // Thêm dòng này
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(ConfirmActivity.this, getString(R.string.gen_court_fail), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CreateOrderResponse> call, Throwable t) {
                //Toast.makeText(ConfirmActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(ConfirmActivity.this, getString(R.string.order_not_exist), Toast.LENGTH_SHORT).show();
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
//                                Log.d("ConfirmActivity", "API changeOrder success");
//                                Log.d("ConfirmActivity", "computedPaymentAmount: " + computedPaymentAmount);
//                                Log.d("ConfirmActivity", "qrcode: " + r.getQrcode());
//                                Log.d("ConfirmActivity", "paymentTimeout: " + r.getPaymentTimeout());

                                // **Sửa đổi**: Lấy paymentTimeout mới từ response
                                String newPaymentTimeout = r.getPaymentTimeout();
                                if (newPaymentTimeout == null || newPaymentTimeout.isEmpty()) {
                                    Log.e("ConfirmActivity", "paymentTimeout từ changeOrder là null hoặc rỗng");
                                    Toast.makeText(ConfirmActivity.this, getString(R.string.error_timeout_invalid), Toast.LENGTH_SHORT).show();
                                    btnPayment.setEnabled(true);
                                    btnDeposit.setEnabled(true);
                                    return;
                                }

                                Intent i = (computedPaymentAmount > 0 && r.getQrcode() != null && !r.getQrcode().isEmpty())
                                        ? new Intent(ConfirmActivity.this, QRCodeActivity.class)
                                        : new Intent(ConfirmActivity.this, PaymentSuccessActivity.class);
                                if (i.getComponent().getClassName().equals(QRCodeActivity.class.getName())) {
                                    i.putExtra("qrCodeData", r.getQrcode());
                                    // **Sửa đổi**: Truyền paymentTimeout mới sang QRCodeActivity
                                    i.putExtra("paymentTimeout", newPaymentTimeout);
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
                                    i.putIntegerArrayListExtra("slotPrices", new ArrayList<>(slotPricesList)); // Đảm bảo truyền slotPrices
                                    if (i.getComponent().getClassName().equals(QRCodeActivity.class.getName())) {
                                        i.putExtra("qrCodeData", r.getQrcode());
                                        i.putExtra("paymentTimeout", r.getPaymentTimeout());
                                        i.putExtra("paymentAmount", computedPaymentAmount);
                                        i.putExtra("amountPaid", amountPaid);
                                        i.putExtra("depositAmount", finalDepositAmount);
                                        i.putExtra("isDeposit", isDeposit);
                                    }
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
                                Toast.makeText(ConfirmActivity.this, getString(R.string.update_schedule_failed), Toast.LENGTH_SHORT).show();
                                btnPayment.setEnabled(true);
                                btnDeposit.setEnabled(true);
                            }
                        }

                        @Override
                        public void onError(String e) {
                            Toast.makeText(ConfirmActivity.this, getString(R.string.update_schedule_failed_with_error) + e, Toast.LENGTH_SHORT).show();
                            btnPayment.setEnabled(true);
                            btnDeposit.setEnabled(true);
                        }
                    });
                }

                @Override
                public void onError(String e) {
                    Toast.makeText(ConfirmActivity.this, getString(R.string.get_old_order_failed_with_error) + e, Toast.LENGTH_SHORT).show();
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
                        String paymentTimeout = r.getPaymentTimeout();
                        Intent i = new Intent(ConfirmActivity.this, QRCodeActivity.class);
                        i.putExtra("qrCodeData", r.getQrcode());
                        i.putExtra("paymentTimeout", paymentTimeout);
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
        // Sử dụng NumberFormat với Locale Việt Nam
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        // Định dạng số tiền
        String formattedAmount = formatter.format(amount);
        // Thay thế để đảm bảo định dạng "₫ 80.000"
        formattedAmount = formattedAmount.replace(" ₫", "").replace("₫", "₫ ");
        return formattedAmount;
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

    public static class BadWordsLoader {
        private static Set<String> englishBadWords = null;
        private static Set<String> vietnameseBadWords = null;

        public static Set<String> loadEnglishBadWords(Context context) {
            if (englishBadWords != null) {
                return englishBadWords;
            }

            englishBadWords = new HashSet<>();
            try {
                InputStream inputStream = context.getResources().openRawResource(R.raw.bad_words_en);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        englishBadWords.add(line.toLowerCase());
                    }
                }
                reader.close();
            } catch (Exception e) {
                //Log.e("BadWordsLoader", "Lỗi khi tải danh sách từ tục tĩu tiếng Anh: " + e.getMessage());
            }
            return englishBadWords;
        }

        public static Set<String> loadVietnameseBadWords(Context context) {
            if (vietnameseBadWords != null) {
                return vietnameseBadWords;
            }

            vietnameseBadWords = new HashSet<>();
            try {
                InputStream inputStream = context.getResources().openRawResource(R.raw.vn_offensive_words);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        vietnameseBadWords.add(line.toLowerCase());
                    }
                }
                reader.close();
            } catch (Exception e) {
                //Log.e("BadWordsLoader", "Lỗi khi tải danh sách từ tục tĩu tiếng Việt: " + e.getMessage());
            }
            return vietnameseBadWords;
        }
    }

    private boolean containsBadWords(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        Set<String> badWordsEn = BadWordsLoader.loadEnglishBadWords(this);
        Set<String> badWordsVi = BadWordsLoader.loadVietnameseBadWords(this);
        Set<String> allBadWords = new HashSet<>();
        allBadWords.addAll(badWordsEn);
        allBadWords.addAll(badWordsVi);

        String lowerInput = input.toLowerCase();
        String[] words = lowerInput.split("\\s+");

        for (String word : words) {
            if (allBadWords.contains(word)) {
                return true;
            }
        }

        for (String badWord : allBadWords) {
            if (lowerInput.contains(badWord)) {
                return true;
            }
        }

        return false;
    }

    private boolean validateName() {
        String name = etName.getText().toString().trim();

        // Kiểm tra bad words trước tiên
        if (containsBadWords(name)) {
            tilName.setError(getString(R.string.name_contains_bad_words));
            return false;
        }

        // Kiểm tra tên trống
        if (name.isEmpty()) {
            tilName.setError(getString(R.string.name_empty));
            return false;
        }

        // Kiểm tra ký tự hợp lệ và độ dài
        String namePattern = "^[\\p{L}][\\p{L}\\s]{1,29}$";
        if (!name.matches(namePattern)) {
            tilName.setError(getString(R.string.name_invalid));
            return false;
        }

        tilName.setError(null);
        return true;
    }

    private boolean validateNote() {
        String note = etNote.getText().toString().trim();

        // Bỏ qua kiểm tra cho đơn dịch vụ
        if ("Đơn dịch vụ".equals(orderType)) {
            return true; // Note được đặt tự động, không cần kiểm tra
        }

        // Kiểm tra bad words trước tiên
        if (containsBadWords(note)) {
            tilNote.setError(getString(R.string.note_contains_bad_words));
            return false;
        }

        // Note có thể trống, không cần báo lỗi nếu trống
        if (note.isEmpty()) {
            tilNote.setError(null);
            return true;
        }

        // Kiểm tra ký tự hợp lệ và độ dài
        String notePattern = "^[a-zA-Z0-9\\s.,!?]{0,100}$";
        if (!note.matches(notePattern)) {
            tilNote.setError(getString(R.string.note_invalid));
            return false;
        }

        tilNote.setError(null);
        return true;
    }

    private boolean validatePhoneNumber() {
        String phone = etPhone.getText().toString().trim();
        if (phone.isEmpty()) {
            tilPhone.setError(getString(R.string.phone_empty));
            return false;
        }
        if (!phone.matches("^(\\+84|0)(3|5|7|8|9)[0-9]{8}$")) {
            tilPhone.setError(getString(R.string.phone_invalid));
            return false;
        }
        tilPhone.setError(null);
        return true;
    }

}