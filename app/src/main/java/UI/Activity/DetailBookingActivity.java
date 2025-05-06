package UI.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import android.content.SharedPreferences;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import Data.Model.OrderDetail;
import Data.Model.Orders;
import Data.Model.Service;
import Data.Model.ServiceDetail;
import Data.Model.Transaction;
import Data.Network.ApiService;
import Data.Network.NetworkUtils;
import Data.Network.RetrofitClient;
import Data.Holder.DataHolder;
import Data.Holder.OrderServiceHolder;
import SEP490.G9.R;
import Data.Session.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailBookingActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvTitleMain;
    private TextView tvTabBookingInfo;
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
    private Button btnPayRemaining;
    private boolean isFirstClick = true;
    private String currentQRCode; // Lưu QR code hiện tại
    private String currentPaymentTimeout; // Lưu paymentTimeout hiện tại
    private static final String PREF_NAME = "QRCodePrefs";
    private static final String KEY_QR_CODE = "currentQRCode";
    private static final String KEY_TIMEOUT = "currentPaymentTimeout";
    private SharedPreferences sharedPreferences;
    private static final String KEY_INITIAL_PAYMENT_DONE = "initialPaymentDone"; // Theo dõi trạng thái thanh toán lần đầu
    private ImageView imgAvatar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_booking);
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Khởi tạo UI
        btnBack = findViewById(R.id.btnBack);
        tvTitleMain = findViewById(R.id.tvTitleMain);
        imgAvatar = findViewById(R.id.imgAvatar);
        tvTabBookingInfo = findViewById(R.id.tvTabBookingInfo);
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
        tvRefundAmount = findViewById(R.id.tvRefundAmount);
        btnCancelBooking = findViewById(R.id.btnCancelBooking);
        btnChangeBooking = findViewById(R.id.btnChangeBooking);
        layoutBookingSlots = findViewById(R.id.layoutBookingSlots);
        btnPayRemaining = findViewById(R.id.btnPayRemaining);
        sessionManager = new SessionManager(this);

        String savedQRCode = sharedPreferences.getString(KEY_QR_CODE, null);
        String savedTimeout = sharedPreferences.getString(KEY_TIMEOUT, null);

        if (savedQRCode != null && savedTimeout != null) {
            Log.d("DetailBookingActivity", "Restored from SharedPreferences - QRCode: " + savedQRCode + ", Timeout: " + savedTimeout);
        } else {
            Log.d("DetailBookingActivity", "No saved QR code or timeout found in SharedPreferences");
        }
        btnCancelBooking.setOnClickListener(v -> {
            if (paymentStatus == null) {
                Toast.makeText(this, "Đang tải dữ liệu, vui lòng thử lại sau", Toast.LENGTH_SHORT).show();
                return;
            }
            if ("Đã đặt cọc".equals(paymentStatus) || "Đã thanh toán".equals(paymentStatus)) {
                new AlertDialog.Builder(this)
                        .setTitle("Cảnh báo")
                        .setMessage("Khi bạn hủy đặt lịch, đồng nghĩa với việc bạn sẽ mất tiền cọc sân. Bạn có chắc chắn muốn tiếp tục?")
                        .setPositiveButton("Có", (dialog, which) -> showConfirmCancelDialog())
                        .setNegativeButton("Không", null)
                        .show();
            } else {
                showConfirmCancelDialog();
            }
        });

        btnBack.setOnClickListener(v -> finish());

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
        String avatarUrl = getIntent().getStringExtra("avatarUrl");
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .into(imgAvatar);
        } else {
            imgAvatar.setImageResource(R.drawable.avatar);
        }
        boolean isStudent = getIntent().getBooleanExtra("isStudent", false);
        //Log.d("DetailBookingActivity", "Giá trị isStudent nhận được: " + isStudent);
        // Hiển thị subject
        TextView tvSubject = findViewById(R.id.subject);
        if (isStudent) {
            tvSubject.setText("Subject: " + getString(R.string.student));
        } else {
            tvSubject.setText("Subject: " + getString(R.string.adult));
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
    }

    private void fetchUserInfo() {
        // Lấy thông tin từ Intent
        customerName = getIntent().getStringExtra("customerName");
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        note = getIntent().getStringExtra("note");

        // Hiển thị thông tin
        tvName.setText("Khách Hàng: " + (customerName != null ? customerName : "N/A"));
        tvPhonenumber.setText("SDT: " + (phoneNumber != null ? phoneNumber : "N/A"));
        String displayNote = (note == null || note.trim().isEmpty()) ? "Không có" : note;
        tvNote.setText("Khách hàng ghi chú: " + displayNote);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentQRCode", currentQRCode);
        outState.putString("currentPaymentTimeout", currentPaymentTimeout);
        Log.d("DetailBookingActivity", "Saving state - QRCode: " + currentQRCode + ", Timeout: " + currentPaymentTimeout);
    }
    private void fetchOrderDetails(String orderId) {
        Call<Orders> call = RetrofitClient.getApiService(this).getOrderById(orderId);
        NetworkUtils.callApi(call, this, new NetworkUtils.ApiCallback<Orders>() {
            @Override
            public void onSuccess(Orders order) {
                if (order == null) {
                    Log.e("DetailBookingActivity", "Order is null from API");
                    runOnUiThread(() -> {
                        updatePaymentButton();
                        Toast.makeText(DetailBookingActivity.this, "Không thể tải dữ liệu đơn hàng", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                String courtName = order.getCourtName();
                String address = order.getAddress();
                paymentStatus = order.getPaymentStatus();
                String updatedOrderStatus = order.getOrderStatus();
                orderType = order.getOrderType();

                slotPrices = getIntent().getIntegerArrayListExtra("slotPrices");
                if (slotPrices == null || slotPrices.isEmpty()) {
                    slotPrices = new ArrayList<>(getSlotPricesFromOrder(order));
                }

                ApiService apiService = RetrofitClient.getApiService(DetailBookingActivity.this);
                Call<List<Transaction>> transactionCall = apiService.getTransactionHistory(orderId);
                transactionCall.enqueue(new Callback<List<Transaction>>() {
                    @Override
                    public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Transaction> transactions = response.body();
                            double totalPaid = 0;
                            double refundAmount = 0;
                            boolean hasRefund = false;

                            for (Transaction t : transactions) {
                                if ("Đã thanh toán".equals(t.getPaymentStatus()) || "Đã đặt cọc".equals(t.getPaymentStatus())) {
                                    totalPaid += t.getAmount();
                                } else if ("Hoàn tiền".equals(t.getPaymentStatus())) {
                                    refundAmount += t.getAmount();
                                    hasRefund = true;
                                }
                            }
                            int finalTotalPaid = (int) totalPaid;
                            int finalRefundAmount = (int) refundAmount;
                            boolean finalHasRefund = hasRefund;

                            runOnUiThread(() -> {
                                if (finalHasRefund) {
                                    tvAmountPaid.setText("Số tiền đã hoàn trả: " + formatMoney(finalRefundAmount));
                                } else if ("Đã thanh toán".equals(paymentStatus) || "Đã đặt cọc".equals(paymentStatus)) {
                                    tvAmountPaid.setText("Số tiền đã trả: " + formatMoney(finalTotalPaid));
                                } else {
                                    tvAmountPaid.setText("Số tiền đã trả: 0đ");
                                }
                                tvPaymentStatus.setText("Trạng thái thanh toán: " + (paymentStatus != null ? paymentStatus : "N/A"));
                                tvStadiumName.setText("Tên sân: " + (courtName != null ? courtName : "N/A"));
                                tvAddress.setText("Địa chỉ: " + (address != null ? address : "N/A"));
                                tvTotalPrice.setText("Tổng tiền: " + formatMoney(order.getTotalAmount()));

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
                                updatePaymentButton();
                            });
                        } else {
                            runOnUiThread(() -> {
                                tvAmountPaid.setText("Số tiền đã trả: 0đ");
                                tvPaymentStatus.setText("Trạng thái thanh toán: " + (paymentStatus != null ? paymentStatus : "N/A"));
                                updatePaymentButton();
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Transaction>> call, Throwable t) {
                        runOnUiThread(() -> {
                            updateButtonsBasedOnStatus(updatedOrderStatus);
                            tvAmountPaid.setText("Số tiền đã trả: 0đ");
                            tvPaymentStatus.setText("Trạng thái thanh toán: " + (paymentStatus != null ? paymentStatus : "N/A"));
                            updatePaymentButton();
                        });
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
    private void updatePaymentButton() {
        if ("Đặt lịch thành công".equals(orderStatus) && "Đã đặt cọc".equals(paymentStatus) ||"Đặt lịch thành công".equals(orderStatus) && "Thanh toán sau đặt cọc thất bại".equals(paymentStatus) ||"Đặt lịch thành công".equals(orderStatus) && "Chờ thanh toán sau đặt cọc".equals(paymentStatus)) {
            btnPayRemaining.setVisibility(View.VISIBLE);
            btnPayRemaining.setOnClickListener(v -> {
                Log.d("DetailBookingActivity", "btnPayRemaining clicked");
                createQRForRemainingPayment(orderId);
            });            Log.d("DetailBookingActivity", "orderStatus: " + orderStatus + ", paymentStatus: " + paymentStatus);
        } else {
            btnPayRemaining.setVisibility(View.GONE);
        }
    }

    private void displayServiceDetails(String serviceDetailsJson) {
        Log.d("DetailBookingActivity", "serviceDetailsJson trong displayServiceDetails: " + serviceDetailsJson);
        if (serviceDetailsJson != null && !serviceDetailsJson.isEmpty()) {
            Gson gson = new Gson();
            List<ServiceDetail> serviceDetails = gson.fromJson(serviceDetailsJson, new TypeToken<List<ServiceDetail>>(){}.getType());

            String serviceListJson = getIntent().getStringExtra("serviceListJson");
            if (serviceListJson == null || serviceListJson.isEmpty()) {
                serviceListJson = OrderServiceHolder.getInstance().getServiceListJson(orderId);
            }

            List<Service> serviceList = new ArrayList<>();
            if (serviceListJson != null && !serviceListJson.isEmpty()) {
                serviceList = gson.fromJson(serviceListJson, new TypeToken<List<Service>>(){}.getType());
            } else {
                Log.w("DetailBookingActivity", "serviceListJson không có, hiển thị tên mặc định");
            }

            layoutBookingSlots.removeAllViews();
            for (ServiceDetail detail : serviceDetails) {
                TextView tvService = new TextView(this);
                String serviceName = "Dịch vụ không xác định";
                if (!serviceList.isEmpty()) {
                    for (Service service : serviceList) {
                        if (service.getId() != null && service.getId().equals(detail.getCourtServiceId())) {
                            serviceName = service.getName();
                            break;
                        }
                    }
                }
                String serviceText = serviceName + " x" + detail.getQuantity() + " : " + formatMoney((int) (detail.getPrice() * detail.getQuantity()));
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

    private List<Integer> getSlotPricesFromOrder(Orders order) {
        List<Integer> slotPrices = new ArrayList<>();
        if (order.getOrderDetails() != null) {
            for (OrderDetail detail : order.getOrderDetails()) {
                slotPrices.add(detail.getPrice());
            }
        }
        return slotPrices;
    }

    private void updateButtonsBasedOnStatus(String status) {
        Log.d("DetailBookingActivity", "updateButtonsBasedOnStatus - status: " + status +
                ", orderType: " + orderType +
                ", paymentStatus: " + paymentStatus);
        if (status == null || paymentStatus == null || orderType == null) {
            Log.w("DetailBookingActivity", "Một trong các giá trị null: status=" + status +
                    ", paymentStatus=" + paymentStatus +
                    ", orderType=" + orderType);
            btnCancelBooking.setVisibility(View.GONE);
            btnChangeBooking.setVisibility(View.GONE);
            return;
        }
        if ("Đổi lịch thất bại".equals(status)) {
            btnCancelBooking.setVisibility(View.GONE);
            btnChangeBooking.setVisibility(View.GONE);
            return;
        }
        if ("Đơn dịch vụ".equals(orderType)) {
            // Kiểm tra trạng thái Hủy trước
            if ("Hủy đặt lịch".equals(status) || "Hủy đặt lịch do quá giờ thanh toán".equals(status)) {
                btnCancelBooking.setVisibility(View.GONE);
                btnChangeBooking.setVisibility(View.GONE);
            } else if ("Chưa thanh toán".equals(paymentStatus)) {
                btnCancelBooking.setText("Hủy đặt dịch vụ");
                btnCancelBooking.setVisibility(View.VISIBLE);
                btnChangeBooking.setText("Thanh toán");
                btnChangeBooking.setVisibility(View.VISIBLE);
                btnChangeBooking.setOnClickListener(v -> handlePaymentForServiceOrder(orderId));
            } else {
                btnCancelBooking.setVisibility(View.GONE);
                btnChangeBooking.setVisibility(View.GONE);
            }
        } else {
            Log.d("DetailBookingActivity", "Không phải Đơn dịch vụ, orderType: " + orderType);
            if ("Đơn cố định".equals(orderType) && "Đã thanh toán".equals(paymentStatus) &&
                    ("Đang xử lý".equals(status) || "Đặt lịch thành công".equals(status) || "Đã hoàn thành".equals(status))) {
                btnCancelBooking.setVisibility(View.GONE);
                btnChangeBooking.setVisibility(View.GONE);
                return;
            }
            if ("Hủy đặt lịch".equals(status) ||
                    "Hủy đặt lịch do quá giờ thanh toán".equals(status) ||
                    "Thay đổi lịch đặt thành công".equals(status) || "Đã hoàn thành".equals(status)) {
                btnCancelBooking.setVisibility(View.GONE);
                btnChangeBooking.setVisibility(View.GONE);
            } else if ("Thay đổi lịch đặt".equals(status) || "Đang xử lý".equals(status) && ("Chưa thanh toán".equals(paymentStatus) || "Chưa đặt cọc".equals(paymentStatus))) {
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
            } else if ("Đang xử lý".equals(status) || "Đặt lịch thành công".equals(status)) {
                btnChangeBooking.setText("Thay đổi lịch đặt");
                btnChangeBooking.setVisibility(View.VISIBLE);
                btnCancelBooking.setVisibility(View.VISIBLE);
                btnChangeBooking.setOnClickListener(v -> {
                    // Tạo hộp thoại tùy chỉnh
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_booking, null);
                    builder.setView(dialogView);

                    // Ánh xạ các thành phần trong dialog
                    TextView tvWarning = dialogView.findViewById(R.id.tvWarning);
                    CheckBox cbAccept = dialogView.findViewById(R.id.cbAccept);
                    Button btnNo = dialogView.findViewById(R.id.btnNo);
                    Button btnYes = dialogView.findViewById(R.id.btnYes);

                    // Thiết lập tiêu đề
                    builder.setTitle("Xác nhận");

                    // Tạo và hiển thị dialog
                    AlertDialog dialog = builder.create();

                    // Xử lý sự kiện nút "Không"
                    btnNo.setOnClickListener(view -> dialog.dismiss());

                    // Xử lý sự kiện nút "Có"
                    btnYes.setOnClickListener(view -> {
                        if (cbAccept.isChecked()) {
                            // Người dùng đã tích checkbox, chuyển đến BookingTableActivity
                            Intent intent = new Intent(DetailBookingActivity.this, BookingTableActivity.class);
                            intent.putExtra("orderId", orderId);
                            intent.putExtra("club_id", courtId);
                            startActivity(intent);
                            dialog.dismiss();
                        } else {
                            // Hiển thị thông báo nếu chưa tích checkbox
                            Toast.makeText(DetailBookingActivity.this, "Bạn phải chấp nhận điều khoản để tiếp tục", Toast.LENGTH_SHORT).show();
                        }
                    });

                    // Hiển thị dialog
                    dialog.show();
                });
            } else {
                btnChangeBooking.setText("Thay đổi lịch đặt");
                btnChangeBooking.setVisibility(View.VISIBLE);
                btnCancelBooking.setVisibility(View.VISIBLE);
                btnChangeBooking.setOnClickListener(v -> {
                    // Tạo hộp thoại tùy chỉnh
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_booking, null);
                    builder.setView(dialogView);

                    // Ánh xạ các thành phần trong dialog
                    TextView tvWarning = dialogView.findViewById(R.id.tvWarning);
                    CheckBox cbAccept = dialogView.findViewById(R.id.cbAccept);
                    Button btnNo = dialogView.findViewById(R.id.btnNo);
                    Button btnYes = dialogView.findViewById(R.id.btnYes);

                    // Thiết lập tiêu đề
                    builder.setTitle("Xác nhận");

                    // Tạo và hiển thị dialog
                    AlertDialog dialog = builder.create();

                    // Xử lý sự kiện nút "Không"
                    btnNo.setOnClickListener(view -> dialog.dismiss());

                    // Xử lý sự kiện nút "Có"
                    btnYes.setOnClickListener(view -> {
                        if (cbAccept.isChecked()) {
                            // Người dùng đã tích checkbox, chuyển đến BookingTableActivity
                            Intent intent = new Intent(DetailBookingActivity.this, BookingTableActivity.class);
                            intent.putExtra("orderId", orderId);
                            intent.putExtra("club_id", courtId);
                            startActivity(intent);
                            dialog.dismiss();
                        } else {
                            // Hiển thị thông báo nếu chưa tích checkbox
                            Toast.makeText(DetailBookingActivity.this, "Bạn phải chấp nhận điều khoản để tiếp tục", Toast.LENGTH_SHORT).show();
                        }
                    });

                    // Hiển thị dialog
                    dialog.show();
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
            if (slotPrices == null || slotPrices.isEmpty()) {
                slotPrices = new ArrayList<>(getSlotPricesFromOrder(order));
                Log.d("DetailBookingActivity", "slotPrices từ Orders: " + slotPrices);
            }
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
                    String slotInfo ="   - " + detail.getCourtSlotName() + ": " +
                            detail.getStartTime().substring(0, 5) + " - " +
                            detail.getEndTime().substring(0, 5);
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
                    // Thêm log để kiểm tra slotPrice
                    Log.d("DetailBookingActivity", "Slot: " + detail.getCourtSlotName() + ", SlotPrice from source: " + slotPrice);
                    Log.d("DetailBookingActivity", "SlotPrice from OrderDetail: " + detail.getPrice() + ", from slotPrices: " + (slotPrices != null && priceIndex <= slotPrices.size() ? slotPrices.get(priceIndex - 1) : "N/A"));
                    String formattedPrice = formatMoney(slotPrice);
                    Log.d("DetailBookingActivity", "Slot: " + detail.getCourtSlotName() + ", Formatted price: " + formattedPrice);
                    String slotInfo = "   - " + detail.getCourtSlotName() + ": " +
                            detail.getStartTime().substring(0, 5) + " - " +
                            detail.getEndTime().substring(0, 5) + " | " +
                            formattedPrice;
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
    private void createQRForRemainingPayment(String orderId) {
        Log.d("DetailBookingActivity", "Starting createQRForRemainingPayment with orderId: " + orderId);
        String savedQRCode = sharedPreferences.getString(KEY_QR_CODE, null);
        String savedTimeout = sharedPreferences.getString(KEY_TIMEOUT, null);
        boolean initialPaymentDone = sharedPreferences.getBoolean(KEY_INITIAL_PAYMENT_DONE, false);

        Call<Orders> orderCall = RetrofitClient.getApiService(this).getOrderById(orderId);
        orderCall.enqueue(new Callback<Orders>() {
            @Override
            public void onResponse(Call<Orders> call, Response<Orders> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Orders order = response.body();
                    int totalAmount = order.getTotalAmount();

                    Call<List<Transaction>> transactionCall = RetrofitClient.getApiService(DetailBookingActivity.this).getTransactionHistory(orderId);
                    transactionCall.enqueue(new Callback<List<Transaction>>() {
                        @Override
                        public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                List<Transaction> transactions = response.body();
                                int depositAmount = 0;
                                for (Transaction t : transactions) {
                                    if ("Đã đặt cọc".equals(t.getPaymentStatus())) {
                                        depositAmount = (int) t.getAmount();
                                        break;
                                    }
                                }

                                if (savedQRCode != null && savedTimeout != null && initialPaymentDone) {
                                    try {
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSS", Locale.getDefault());
                                        sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
                                        Date timeoutDate = sdf.parse(savedTimeout);
                                        long timeoutTimeMillis = timeoutDate.getTime();
                                        long currentTimeMillis = System.currentTimeMillis();

                                        if (currentTimeMillis < timeoutTimeMillis) {
                                            Log.d("DetailBookingActivity", "Using existing QR code: " + savedQRCode);
                                            Intent intent = new Intent(DetailBookingActivity.this, QRCodeActivity.class);
                                            intent.putExtra("qrCodeData", savedQRCode);
                                            intent.putExtra("paymentTimeout", savedTimeout);
                                            intent.putExtra("orderId", orderId);
                                            intent.putExtra("isRemainingPayment", true);
                                            intent.putExtra("totalTime", totalTime);
                                            intent.putExtra("totalPrice", totalAmount);
                                            intent.putExtra("depositAmount", depositAmount);
                                            intent.putExtra("courtId", courtId);
                                            intent.putIntegerArrayListExtra("slotPrices", slotPrices);
                                            intent.putExtra("customerName", customerName);
                                            intent.putExtra("phoneNumber", phoneNumber);
                                            intent.putExtra("note", note);
                                            startActivity(intent);
                                            return;
                                        }
                                    } catch (Exception e) {
                                        Log.e("DetailBookingActivity", "Lỗi parse paymentTimeout: " + e.getMessage());
                                    }
                                }

                                generateNewQRCode(orderId, totalAmount, depositAmount);
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Transaction>> call, Throwable t) {
                            Log.e("DetailBookingActivity", "Failed to fetch transactions: " + t.getMessage());
                            Toast.makeText(DetailBookingActivity.this, "Lỗi khi lấy lịch sử giao dịch", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<Orders> call, Throwable t) {
                Log.e("DetailBookingActivity", "Failed to fetch order: " + t.getMessage());
                Toast.makeText(DetailBookingActivity.this, "Lỗi khi lấy thông tin đơn hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void generateNewQRCode(String orderId, int totalAmount, int depositAmount) {
        Call<String> paymentCall = RetrofitClient.getApiService(this).createPaymentForRemaining(orderId);
        paymentCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Call<Orders> orderCall = RetrofitClient.getApiService(DetailBookingActivity.this).getOrderById(orderId);
                    orderCall.enqueue(new Callback<Orders>() {
                        @Override
                        public void onResponse(Call<Orders> call, Response<Orders> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Orders order = response.body();
                                String newQRCode = order.getQrcode();
                                String newPaymentTimeout = order.getPaymentTimeout();

                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(KEY_QR_CODE, newQRCode);
                                editor.putString(KEY_TIMEOUT, newPaymentTimeout);
                                editor.putBoolean(KEY_INITIAL_PAYMENT_DONE, true);
                                editor.apply();

                                Log.d("DetailBookingActivity", "New QR code generated: " + newQRCode);
                                Log.d("DetailBookingActivity", "New paymentTimeout: " + newPaymentTimeout);

                                Intent intent = new Intent(DetailBookingActivity.this, QRCodeActivity.class);
                                intent.putExtra("qrCodeData", newQRCode);
                                intent.putExtra("paymentTimeout", newPaymentTimeout);
                                intent.putExtra("orderId", orderId);
                                intent.putExtra("isRemainingPayment", true);
                                intent.putExtra("totalTime", totalTime);
                                intent.putExtra("totalPrice", totalAmount);
                                intent.putExtra("depositAmount", depositAmount);
                                intent.putExtra("courtId", courtId);
                                intent.putIntegerArrayListExtra("slotPrices", slotPrices);
                                intent.putExtra("customerName", customerName);
                                intent.putExtra("phoneNumber", phoneNumber);
                                intent.putExtra("note", note);
                                startActivity(intent);
                            } else {
                                Log.e("DetailBookingActivity", "getOrderById failed. Code: " + response.code());
                                Toast.makeText(DetailBookingActivity.this, "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Orders> call, Throwable t) {
                            Log.e("DetailBookingActivity", "getOrderById failed: " + t.getMessage());
                            Toast.makeText(DetailBookingActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.e("DetailBookingActivity", "createPaymentForRemaining failed. Code: " + response.code());
                    Toast.makeText(DetailBookingActivity.this, "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("DetailBookingActivity", "createPaymentForRemaining failed: " + t.getMessage());
                Toast.makeText(DetailBookingActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkAndHideButtons(Orders order) {
        if (order == null) {
            btnCancelBooking.setVisibility(View.GONE);
            btnChangeBooking.setVisibility(View.GONE);
            Log.w("DetailBookingActivity", "Order is null");
            return;
        }

        if (!"Đơn dịch vụ".equals(orderType)) {
            if (order.getOrderDetails() == null || order.getOrderDetails().isEmpty()) {
                btnCancelBooking.setVisibility(View.GONE);
                btnChangeBooking.setVisibility(View.GONE);
                Log.w("DetailBookingActivity", "OrderDetails null hoặc trống cho orderType: " + orderType);
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
                Log.w("DetailBookingActivity", "Không có booking dates cho orderType: " + orderType);
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
                Log.w("DetailBookingActivity", "Không có ngày đặt hợp lệ cho orderType: " + orderType);
                return;
            }

            LocalDate minBookingDate = Collections.min(bookingLocalDates);
            if (currentDate.isAfter(minBookingDate)) {
                btnCancelBooking.setVisibility(View.GONE);
                btnChangeBooking.setVisibility(View.GONE);
                Log.d("DetailBookingActivity", "Ẩn nút vì ngày hiện tại > ngày đặt nhỏ nhất: " + minBookingDate);
            } else {
                Log.d("DetailBookingActivity", "Gọi updateButtonsBasedOnStatus cho orderType: " + orderType);
                updateButtonsBasedOnStatus(order.getOrderStatus());
            }
        } else {
            Log.d("DetailBookingActivity", "Không ẩn nút cho Đơn dịch vụ");
            updateButtonsBasedOnStatus(order.getOrderStatus());
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

    private String formatMoney(int amount) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(amount) + " ₫";
    }

    private int toMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }
    private void showConfirmCancelDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận")
                .setMessage("Bạn có chắc chắn là muốn hủy đơn không?")
                .setPositiveButton("Có", (dialog, which) -> cancelOrder(orderId))
                .setNegativeButton("Không", null)
                .show();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
        // Xóa dữ liệu SharedPreferences khi thoát ứng dụng
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}