package SEP490.G9;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import Api.ApiService;
import Api.NetworkUtils;
import Api.RetrofitClient;
import Holder.DataHolder;
import Holder.OrderServiceHolder;
import Model.MyInfoResponse;
import Model.OrderDetail;
import Model.Orders;
import Model.ServiceDetail;
import Model.Transaction;
import Session.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailBookingActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvTitleMain;
    private TextView tvTabBookingInfo, tvTabServiceDetail;
    private View lineBookingInfo, lineServiceDetail;
    private ScrollView layoutBookingInfo, layoutServiceDetail;
    private TextView tvStadiumName, tvAddress, tvTotalTime, tvTotalPrice, tvPaymentStatus, tvPhonenumber, tvName, tvNote;
    private TextView tvAmountPaid, tvPaymentAmount, tvRefundAmount;
    private Button btnCancelBooking, btnChangeBooking;
    private LinearLayout layoutBookingSlots;
    private String orderId, totalTime, orderStatus, courtId, orderType, paymentStatus, customerName, note, phoneNumber;
    private int totalPrice;
    private ArrayList<Integer> slotPrices;
    private Handler handler = new Handler();
    private Runnable checkOrderStatusRunnable;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_booking);

        // Khởi tạo UI
        btnBack = findViewById(R.id.btnBack);
        tvTitleMain = findViewById(R.id.tvTitleMain);
        tvTabBookingInfo = findViewById(R.id.tvTabBookingInfo);
        tvTabServiceDetail = findViewById(R.id.tvTabServiceDetail);
        lineBookingInfo = findViewById(R.id.lineBookingInfo);
        lineServiceDetail = findViewById(R.id.lineServiceDetail);
        layoutBookingInfo = findViewById(R.id.layoutBookingInfo);
        layoutServiceDetail = findViewById(R.id.layoutServiceDetail);
        tvStadiumName = findViewById(R.id.tvStadiumName);
        tvAddress = findViewById(R.id.tvAddress);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvPaymentStatus = findViewById(R.id.tvPaymentStatus);
        tvName = findViewById(R.id.tvName);
        tvPhonenumber = findViewById(R.id.tvPhonenumber);
        tvNote = findViewById(R.id.tvNote);
        tvAmountPaid = findViewById(R.id.tvAmountPaid);
        tvPaymentAmount = findViewById(R.id.tvPaymentAmount);
        tvRefundAmount = findViewById(R.id.tvRefundAmount);
        btnCancelBooking = findViewById(R.id.btnCancelBooking);
        btnChangeBooking = findViewById(R.id.btnChangeBooking);
        layoutBookingSlots = findViewById(R.id.layoutBookingSlots);

        sessionManager = new SessionManager(this);

        btnCancelBooking.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận")
                    .setMessage("Bạn có chắc chắn là muốn hủy đặt dịch vụ không?")
                    .setPositiveButton("Có", (dialog, which) -> cancelOrder(orderId))
                    .setNegativeButton("Không", null)
                    .show();
        });

        btnBack.setOnClickListener(v -> goBackToMainActivity());

        // Lấy dữ liệu từ Intent
        orderId = getIntent().getStringExtra("orderId");
        totalTime = getIntent().getStringExtra("totalTime");
        totalPrice = getIntent().getIntExtra("totalPrice", 0);
        orderStatus = getIntent().getStringExtra("orderStatus");
        courtId = getIntent().getStringExtra("courtId");
        orderType = getIntent().getStringExtra("orderType");
        slotPrices = getIntent().getIntegerArrayListExtra("slotPrices");
        customerName = getIntent().getStringExtra("customerName");
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        Log.d("DetailBookingActivity", "phoneNumber" + phoneNumber);

        note = getIntent().getStringExtra("note");
        if (orderId == null || orderId.isEmpty()) {
            Log.d("DetailBookingActivity", "orderId is null or empty");
            Toast.makeText(this, "Không có orderId", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Gọi hàm để lấy thông tin người dùng từ API
        fetchUserInfo();

        // Xử lý UI ban đầu dựa trên orderType
        if ("Đơn dịch vụ".equals(orderType)) {
            tvTotalTime.setVisibility(View.GONE);
            tvRefundAmount.setVisibility(View.GONE);
            layoutBookingSlots.setVisibility(View.VISIBLE);
            String serviceDetailsJson = getIntent().getStringExtra("serviceDetailsJson");
            if (serviceDetailsJson == null || serviceDetailsJson.isEmpty()) {
                serviceDetailsJson = OrderServiceHolder.getInstance().getServiceDetailsJson(orderId);
            }
            Log.d("DetailBookingActivity", "serviceDetailsJson trong onCreate: " + serviceDetailsJson);
            displayServiceDetails(serviceDetailsJson);
        } else {
            tvTotalTime.setText("Tổng thời gian: " + (totalTime != null ? totalTime : "N/A"));
            tvTotalPrice.setText("Tổng tiền: " + formatMoney(totalPrice));
            updateButtonsBasedOnStatus(orderStatus);
        }

        // Gọi API để lấy thông tin chi tiết đơn hàng
        fetchOrderDetails(orderId);

        checkOrderStatusRunnable = () -> {
            fetchOrderDetails(orderId);
            handler.postDelayed(checkOrderStatusRunnable, 10000);
        };

        tvTabBookingInfo.setOnClickListener(v -> showBookingInfoTab());
        tvTabServiceDetail.setOnClickListener(v -> showServiceDetailTab());
    }

    private void fetchUserInfo() {
        // Lấy thông tin từ Intent
        customerName = getIntent().getStringExtra("customerName");
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        note = getIntent().getStringExtra("note");

        // Hiển thị thông tin
        tvName.setText("Khách Hàng: " + (customerName != null ? customerName : "N/A"));
        tvPhonenumber.setText("SDT: " + (phoneNumber != null ? phoneNumber : "N/A"));
        tvNote.setText("Khách hàng ghi chú: " + (note != null ? note : "N/A"));
    }

    private void displayServiceDetails(String serviceDetailsJson) {
        Log.d("DetailBookingActivity", "serviceDetailsJson trong displayServiceDetails: " + serviceDetailsJson);
        if (serviceDetailsJson != null && !serviceDetailsJson.isEmpty()) {
            Gson gson = new Gson();
            List<ServiceDetail> serviceDetails = gson.fromJson(serviceDetailsJson, new TypeToken<List<ServiceDetail>>(){}.getType());
            layoutBookingSlots.removeAllViews();
            for (ServiceDetail detail : serviceDetails) {
                TextView tvService = new TextView(this);
                String serviceText = detail.getCourtServiceName() + " x" + detail.getQuantity() + " : " + formatMoney((int) (detail.getPrice() * detail.getQuantity()));
                tvService.setText(serviceText);
                tvService.setTextColor(Color.WHITE);
                tvService.setTextSize(14);
                layoutBookingSlots.addView(tvService);
                Log.d("DetailBookingActivity", "Hiển thị dịch vụ: " + serviceText);
            }
        } else {
            TextView tvNoService = new TextView(this);
            tvNoService.setText("Không có thông tin dịch vụ");
            tvNoService.setTextColor(Color.WHITE);
            tvNoService.setTextSize(14);
            layoutBookingSlots.addView(tvNoService);
            Log.d("DetailBookingActivity", "Không có thông tin dịch vụ để hiển thị");
        }
    }

    private void handlePaymentForServiceOrder(String orderId) {
        String serviceDetailsJson = getIntent().getStringExtra("serviceDetailsJson");
        if (serviceDetailsJson == null || serviceDetailsJson.isEmpty()) {
            serviceDetailsJson = OrderServiceHolder.getInstance().getServiceDetailsJson(orderId);
        }
        final String finalServiceDetailsJson = serviceDetailsJson;

        Call<Orders> call = RetrofitClient.getApiService(this).getOrderById(orderId);
        NetworkUtils.callApi(call, this, new NetworkUtils.ApiCallback<Orders>() {
            @Override
            public void onSuccess(Orders order) {
                if (order == null) {
                    Log.e("DetailBookingActivity", "Order is null from API");
                    runOnUiThread(() -> Toast.makeText(DetailBookingActivity.this, "Không thể tải dữ liệu đơn hàng", Toast.LENGTH_SHORT).show());
                    return;
                }

                String customerName = order.getCustomerName();
                String phoneNumber = order.getPhoneNumber();
                String note = order.getNote();

                Intent intent = new Intent(DetailBookingActivity.this, QRCodeActivity.class);
                intent.putExtra("orderId", orderId);
                intent.putExtra("totalPrice", order.getTotalAmount());
                intent.putExtra("courtId", courtId);
                intent.putExtra("customerName", customerName);
                intent.putExtra("phoneNumber", phoneNumber);
                intent.putExtra("note", note);
                intent.putExtra("serviceDetailsJson", finalServiceDetailsJson);
                startActivity(intent);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("DetailBookingActivity", "Lỗi khi lấy dữ liệu: " + errorMessage);
                runOnUiThread(() -> Toast.makeText(DetailBookingActivity.this, "Lỗi khi lấy dữ liệu đơn hàng", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void fetchOrderDetails(String orderId) {
        Log.d("DetailBookingActivity", "Calling getOrderById with orderId: " + orderId);
        Call<Orders> call = RetrofitClient.getApiService(this).getOrderById(orderId);
        NetworkUtils.callApi(call, this, new NetworkUtils.ApiCallback<Orders>() {
            @Override
            public void onSuccess(Orders order) {
                if (order == null) {
                    Log.e("DetailBookingActivity", "Order is null from API");
                    runOnUiThread(() -> Toast.makeText(DetailBookingActivity.this, "Không thể tải dữ liệu đơn hàng", Toast.LENGTH_SHORT).show());
                    return;
                }

                String courtName = order.getCourtName();
                String address = order.getAddress();
                paymentStatus = order.getPaymentStatus();
                String updatedOrderStatus = order.getOrderStatus();
                int paymentAmount = order.getPaymentAmount();
                orderType = order.getOrderType();

                // Gọi API getTransactionHistory
                ApiService apiService = RetrofitClient.getApiService(DetailBookingActivity.this);
                Call<List<Transaction>> transactionCall = apiService.getTransactionHistory(orderId);
                transactionCall.enqueue(new Callback<List<Transaction>>() {
                    @Override
                    public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Transaction> transactions = response.body();
                            double totalPaid = 0;
                            for (Transaction t : transactions) {
                                if ("Thành công".equals(t.getStatus())) {
                                    totalPaid += t.getAmount();
                                }
                            }
                            int finalTotalPaid = (int) totalPaid;
                            runOnUiThread(() -> {
                                tvAmountPaid.setText("Số tiền đã trả: " + formatMoney(finalTotalPaid));
                                tvStadiumName.setText("Tên sân: " + (courtName != null ? courtName : "N/A"));
                                tvAddress.setText("Địa chỉ: " + (address != null ? address : "N/A"));
                                tvPaymentStatus.setText("Trạng thái thanh toán: " + (paymentStatus != null ? paymentStatus : "N/A"));
                                tvTotalPrice.setText("Tổng tiền: " + formatMoney(order.getTotalAmount()));
                                tvPaymentAmount.setText("Số tiền cần thanh toán: " + formatMoney(paymentAmount));

                                if (!"Đơn dịch vụ".equals(orderType)) {
                                    buildBookingSlotsUI(order);
                                } else {
                                    String serviceDetailsJson = getIntent().getStringExtra("serviceDetailsJson");
                                    if (serviceDetailsJson == null || serviceDetailsJson.isEmpty()) {
                                        serviceDetailsJson = OrderServiceHolder.getInstance().getServiceDetailsJson(orderId);
                                    }
                                    displayServiceDetails(serviceDetailsJson);
                                }
                                checkAndHideButtons(order);

                                if (!updatedOrderStatus.equals(orderStatus)) {
                                    orderStatus = updatedOrderStatus;
                                    updateButtonsBasedOnStatus(orderStatus);
                                }
                            });
                        } else {
                            Log.d("DetailBookingActivity", "API transaction trả về rỗng hoặc lỗi");
                            runOnUiThread(() -> tvAmountPaid.setText("Số tiền đã trả: " + formatMoney(0)));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Transaction>> call, Throwable t) {
                        Log.e("DetailBookingActivity", "Lỗi gọi API transaction: " + t.getMessage());
                        runOnUiThread(() -> tvAmountPaid.setText("Số tiền đã trả: " + formatMoney(0)));
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("DetailBookingActivity", "Lỗi khi lấy dữ liệu: " + errorMessage);
                runOnUiThread(() -> Toast.makeText(DetailBookingActivity.this, "Lỗi khi lấy dữ liệu đơn hàng", Toast.LENGTH_LONG).show());
            }
        });
    }

    private void updateButtonsBasedOnStatus(String status) {
        if (status == null || paymentStatus == null || orderType == null) {
            btnCancelBooking.setVisibility(View.GONE);
            btnChangeBooking.setVisibility(View.GONE);
            return;
        }

        if ("Đơn dịch vụ".equals(orderType)) {
            if ("Chưa thanh toán".equals(paymentStatus)) {
                btnCancelBooking.setText("Hủy đặt dịch vụ");
                btnCancelBooking.setVisibility(View.VISIBLE);
                btnChangeBooking.setText("Thanh toán");
                btnChangeBooking.setVisibility(View.VISIBLE);
                btnChangeBooking.setOnClickListener(v -> handlePaymentForServiceOrder(orderId));
            } else if ("Hủy đặt lịch".equals(status) || "Hủy đặt lịch do quá giờ thanh toán".equals(status)) {
                btnCancelBooking.setVisibility(View.GONE);
                btnChangeBooking.setVisibility(View.GONE);
            } else {
                btnCancelBooking.setVisibility(View.GONE);
                btnChangeBooking.setVisibility(View.GONE);
            }
        } else {
            if ("Đơn cố định".equals(orderType) && "Đã thanh toán".equals(paymentStatus) &&
                    ("Đang xử lý".equals(status) || "Đặt lịch thành công".equals(status) || "Đã hoàn thành".equals(status))) {
                btnCancelBooking.setVisibility(View.GONE);
                btnChangeBooking.setVisibility(View.GONE);
                return;
            }
            if ("Hủy đặt lịch".equals(status) ||
                    "Hủy đặt lịch do quá giờ thanh toán".equals(status) ||
                    "Thay đổi lịch đặt".equals(status) || "Thay đổi lịch đặt thành công".equals(status) || "Đã hoàn thành".equals(status)) {
                btnCancelBooking.setVisibility(View.GONE);
                btnChangeBooking.setVisibility(View.GONE);
            } else if ("Đang xử lý".equals(status) && ("Chưa thanh toán".equals(paymentStatus) || "Chưa đặt cọc".equals(paymentStatus))) {
                btnChangeBooking.setText("Thanh toán");
                btnChangeBooking.setVisibility(View.VISIBLE);
                btnCancelBooking.setVisibility(View.VISIBLE);
                btnChangeBooking.setOnClickListener(v -> {
                    Intent intent = new Intent(DetailBookingActivity.this, QRCodeActivity.class);
                    intent.putExtra("orderId", orderId);
                    intent.putExtra("totalTime", totalTime);
                    intent.putExtra("totalPrice", totalPrice);
                    intent.putExtra("courtId", courtId);
                    intent.putIntegerArrayListExtra("slotPrices", slotPrices);
                    startActivity(intent);
                });
            } else if ("Đang xử lý".equals(status)) {
                btnChangeBooking.setText("Thay đổi lịch đặt");
                btnChangeBooking.setVisibility(View.VISIBLE);
                btnCancelBooking.setVisibility(View.VISIBLE);
                btnChangeBooking.setOnClickListener(v -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Xác nhận")
                            .setMessage("Bạn có chắc chắn là muốn thay đổi lịch đặt không?")
                            .setPositiveButton("Có", (dialog, which) -> {
                                Intent intent = new Intent(DetailBookingActivity.this, BookingTableActivity.class);
                                intent.putExtra("orderId", orderId);
                                intent.putExtra("club_id", courtId);
                                startActivity(intent);
                            })
                            .setNegativeButton("Không", null)
                            .show();
                });
            } else if ("Đặt lịch thành công".equals(status)) {
                btnChangeBooking.setText("Thay đổi lịch đặt");
                btnChangeBooking.setVisibility(View.VISIBLE);
                btnCancelBooking.setVisibility(View.VISIBLE);
                btnChangeBooking.setOnClickListener(v -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Xác nhận")
                            .setMessage("Bạn có chắc chắn là muốn thay đổi lịch đặt không?")
                            .setPositiveButton("Có", (dialog, which) -> {
                                Intent intent = new Intent(DetailBookingActivity.this, BookingTableActivity.class);
                                intent.putExtra("orderId", orderId);
                                intent.putExtra("club_id", courtId);
                                startActivity(intent);
                            })
                            .setNegativeButton("Không", null)
                            .show();
                });
            } else {
                btnChangeBooking.setText("Thay đổi lịch đặt");
                btnChangeBooking.setVisibility(View.VISIBLE);
                btnCancelBooking.setVisibility(View.VISIBLE);
                btnChangeBooking.setOnClickListener(v -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Xác nhận")
                            .setMessage("Bạn có chắc chắn là muốn thay đổi lịch đặt không?")
                            .setPositiveButton("Có", (dialog, which) -> {
                                Intent intent = new Intent(DetailBookingActivity.this, BookingTableActivity.class);
                                intent.putExtra("orderId", orderId);
                                intent.putExtra("club_id", courtId);
                                startActivity(intent);
                            })
                            .setNegativeButton("Không", null)
                            .show();
                });
            }
        }
    }

    private void buildBookingSlotsUI(Orders order) {
        layoutBookingSlots.removeAllViews();
        if (order == null || order.getOrderDetails() == null || order.getOrderDetails().isEmpty()) {
            TextView tvNoSlots = new TextView(this);
            tvNoSlots.setTextColor(Color.WHITE);
            tvNoSlots.setTextSize(14);
            tvNoSlots.setText("Không có slot nào được chọn");
            layoutBookingSlots.addView(tvNoSlots);
            Log.d("DetailBookingActivity", "Không có order hoặc orderDetails");
            return;
        }

        slotPrices = getIntent().getIntegerArrayListExtra("slotPrices");
        if (slotPrices == null || slotPrices.isEmpty()) {
            Log.d("DetailBookingActivity", "slotPrices từ Intent là null hoặc rỗng");
            slotPrices = DataHolder.getInstance().getSlotPrices();
            Log.d("DetailBookingActivity", "slotPrices từ DataHolder: " + slotPrices);
        } else {
            Log.d("DetailBookingActivity", "slotPrices từ Intent: " + slotPrices);
        }

        if ("Đơn cố định".equals(order.getOrderType())) {
            Map<String, List<OrderDetail>> slotsByDate = new HashMap<>();
            for (OrderDetail detail : order.getOrderDetails()) {
                if (detail.getBookingDates() != null) {
                    for (String date : detail.getBookingDates()) {
                        slotsByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(detail);
                    }
                }
            }

            Set<String> sortedDates = new TreeSet<>(slotsByDate.keySet());
            int priceIndex = 0;
            for (String date : sortedDates) {
                TextView tvDayHeader = new TextView(this);
                tvDayHeader.setTextColor(Color.WHITE);
                tvDayHeader.setTextSize(16);
                tvDayHeader.setText("Ngày: " + date);
                layoutBookingSlots.addView(tvDayHeader);

                List<OrderDetail> slots = slotsByDate.get(date);
                Collections.sort(slots, (o1, o2) -> toMinutes(o1.getStartTime()) - toMinutes(o2.getStartTime()));

                for (OrderDetail detail : slots) {
                    TextView tvSlot = new TextView(this);
                    tvSlot.setTextColor(Color.WHITE);
                    tvSlot.setTextSize(14);

                    int slotPrice = (slotPrices != null && priceIndex < slotPrices.size()) ? slotPrices.get(priceIndex++) : detail.getPrice();
                    if (slotPrice == 0) {
                        Log.w("DetailBookingActivity", "slotPrice là 0, kiểm tra slotPrices hoặc detail.getPrice()");
                    }
                    //"   - " + detail.getCourtSlotName() + ": " +
                    String slotInfo =
                            detail.getStartTime().substring(0, 5) + " - " +
                                    detail.getEndTime().substring(0, 5) + " | " +
                                    formatMoney(slotPrice);
                    tvSlot.setText(slotInfo);
                    layoutBookingSlots.addView(tvSlot);
                    Log.d("DetailBookingActivity", "Slot hiển thị: " + slotInfo);
                }
            }
        } else {
            Map<String, List<OrderDetail>> slotsByDate = new HashMap<>();
            for (OrderDetail detail : order.getOrderDetails()) {
                if (detail.getBookingDates() != null) {
                    for (String date : detail.getBookingDates()) {
                        slotsByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(detail);
                    }
                }
            }

            int priceIndex = 0;
            for (Map.Entry<String, List<OrderDetail>> entry : slotsByDate.entrySet()) {
                String date = entry.getKey();
                List<OrderDetail> slots = entry.getValue();

                TextView tvDayHeader = new TextView(this);
                tvDayHeader.setTextColor(Color.WHITE);
                tvDayHeader.setTextSize(16);
                tvDayHeader.setText("Ngày: " + date);
                layoutBookingSlots.addView(tvDayHeader);

                Collections.sort(slots, (o1, o2) -> toMinutes(o1.getStartTime()) - toMinutes(o2.getStartTime()));

                for (OrderDetail detail : slots) {
                    TextView tvSlot = new TextView(this);
                    tvSlot.setTextColor(Color.WHITE);
                    tvSlot.setTextSize(14);

                    int slotPrice = (slotPrices != null && priceIndex < slotPrices.size()) ? slotPrices.get(priceIndex++) : detail.getPrice();
                    if (slotPrice == 0) {
                        Log.w("DetailBookingActivity", "slotPrice là 0, kiểm tra slotPrices hoặc detail.getPrice()");
                    }
                    //"   - " + detail.getCourtSlotName() + ": " +
                    String slotInfo =
                            detail.getStartTime().substring(0, 5) + " - " +
                                    detail.getEndTime().substring(0, 5) + " | " +
                                    formatMoney(slotPrice);
                    tvSlot.setText(slotInfo);
                    layoutBookingSlots.addView(tvSlot);
                    Log.d("DetailBookingActivity", "Slot hiển thị: " + slotInfo);
                }
            }
        }
    }

    private void cancelOrder(String orderId) {
        Call<Orders> call = RetrofitClient.getApiService(this).cancelOrder(orderId);
        NetworkUtils.callApi(call, this, new NetworkUtils.ApiCallback<Orders>() {
            @Override
            public void onSuccess(Orders order) {
                if (order != null && "Hủy đặt lịch".equals(order.getOrderStatus())) {
                    runOnUiThread(() -> {
                        btnCancelBooking.setVisibility(View.GONE);
                        btnChangeBooking.setVisibility(View.GONE);
                        tvPaymentStatus.setText("Trạng thái thanh toán: " + order.getOrderStatus());
                        Toast.makeText(DetailBookingActivity.this, "Đã hủy đặt dịch vụ", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("DetailBookingActivity", "Lỗi khi hủy đặt dịch vụ: " + errorMessage);
                runOnUiThread(() -> Toast.makeText(DetailBookingActivity.this, "Lỗi khi hủy đặt dịch vụ", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void checkAndHideButtons(Orders order) {
        if (order == null || order.getOrderDetails() == null || order.getOrderDetails().isEmpty()) {
            btnCancelBooking.setVisibility(View.GONE);
            btnChangeBooking.setVisibility(View.GONE);
            Log.w("DetailBookingActivity", "Order hoặc OrderDetails null hoặc trống");
            return;
        }

        List<String> bookingDates = new ArrayList<>();
        for (OrderDetail detail : order.getOrderDetails()) {
            if (detail.getBookingDates() != null) {
                bookingDates.addAll(detail.getBookingDates());
            }
        }

        if (bookingDates.isEmpty()) {
            btnCancelBooking.setVisibility(View.GONE);
            btnChangeBooking.setVisibility(View.GONE);
            Log.w("DetailBookingActivity", "Không có booking dates");
            return;
        }

        LocalDate currentDate = LocalDate.now();
        List<LocalDate> bookingLocalDates = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (String dateStr : bookingDates) {
            try {
                LocalDate date = LocalDate.parse(dateStr, formatter);
                bookingLocalDates.add(date);
            } catch (Exception e) {
                Log.e("DetailBookingActivity", "Lỗi định dạng ngày: " + dateStr, e);
            }
        }

        if (bookingLocalDates.isEmpty()) {
            btnCancelBooking.setVisibility(View.GONE);
            btnChangeBooking.setVisibility(View.GONE);
            Log.w("DetailBookingActivity", "Không có ngày đặt hợp lệ");
            return;
        }

        LocalDate minBookingDate = Collections.min(bookingLocalDates);
        if (currentDate.isAfter(minBookingDate)) {
            btnCancelBooking.setVisibility(View.GONE);
            btnChangeBooking.setVisibility(View.GONE);
        } else {
            updateButtonsBasedOnStatus(order.getOrderStatus());
        }
    }

    private void showBookingInfoTab() {
        tvTabBookingInfo.setTextColor(Color.WHITE);
        tvTabServiceDetail.setTextColor(Color.DKGRAY);
        lineBookingInfo.setVisibility(View.VISIBLE);
        lineServiceDetail.setVisibility(View.INVISIBLE);
        layoutBookingInfo.setVisibility(View.VISIBLE);
        layoutServiceDetail.setVisibility(View.GONE);
    }

    private void showServiceDetailTab() {
        tvTabBookingInfo.setTextColor(Color.DKGRAY);
        tvTabServiceDetail.setTextColor(Color.WHITE);
        lineBookingInfo.setVisibility(View.INVISIBLE);
        lineServiceDetail.setVisibility(View.VISIBLE);
        layoutBookingInfo.setVisibility(View.GONE);
        layoutServiceDetail.setVisibility(View.VISIBLE);
    }

    private String formatMoney(int amount) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(amount) + " ₫";
    }

    private int toMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goBackToMainActivity();
    }

    private void goBackToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchOrderDetails(orderId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(checkOrderStatusRunnable);
        }
    }
}