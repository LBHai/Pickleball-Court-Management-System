package SEP490.G9;

import Api.ApiService;
import Api.NetworkUtils;
import Api.RetrofitClient;
import Model.BookingSlot;
import Model.ConfirmOrder;
import Model.CourtSlot;
import Model.MyInfo;
import Model.MyInfoResponse;
import Session.SessionManager;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BookingTableActivity extends AppCompatActivity {

    private TableLayout tableLayout;
    private Button btnSelectDate, btnNext;
    private Spinner spinnerCourt;
    private ListView lvSelectedBookings;
    private ImageButton btnBack;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat sdfFull = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    private String selectedDate = "";
    private String courtId,tvPhone;

    private Map<String, List<CourtSlot>> courtsByDate = new HashMap<>();
    private Map<String, Map<String, ConfirmOrder>> selectedOrdersByDate = new HashMap<>();
    private ArrayList<String> selectedBookingsList = new ArrayList<>();
    private ArrayAdapter<String> selectedBookingsAdapter;
    private List<String> allTimesCurrentDay = new ArrayList<>();

    private boolean isStudent = false; // Biến xác định người dùng có phải sinh viên không

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_table);

        // Khởi tạo các view
        tableLayout = findViewById(R.id.tableLayout);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        spinnerCourt = findViewById(R.id.spinnerCourt);
        btnNext = findViewById(R.id.btnNext);
        lvSelectedBookings = findViewById(R.id.lvSelectedBookings);
        btnBack = findViewById(R.id.btnBack);

        // Thiết lập adapter cho danh sách slot đã chọn
        selectedBookingsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, selectedBookingsList);
        lvSelectedBookings.setAdapter(selectedBookingsAdapter);

        // Lấy courtId từ Intent
        courtId = getIntent().getStringExtra("club_id");
        tvPhone = getIntent().getStringExtra("tvPhone");

        if (courtId == null) {
            Toast.makeText(this, "Không có club_id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Thiết lập ngày mặc định
        Calendar c = Calendar.getInstance();
        selectedDate = sdf.format(c.getTime());
        btnSelectDate.setText("Ngày: " + selectedDate);

        // Khởi tạo selectedOrdersByDate cho ngày mặc định
        selectedOrdersByDate.put(selectedDate, new HashMap<>());

        // Lấy thông tin người dùng trước khi tải slot
        fetchUserInfo();

        // Sự kiện nút chọn ngày
        btnSelectDate.setOnClickListener(v -> showDatePickerDialog());

        // Sự kiện chọn sân từ spinner
        spinnerCourt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                buildTableForDate(selectedDate);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Sự kiện xem chi tiết sân và giá
        TextView tvViewCourtsAndPrices = findViewById(R.id.tvViewCourtsAndPrices);
        tvViewCourtsAndPrices.setOnClickListener(v -> {
            Intent intent = new Intent(BookingTableActivity.this, DetailPriceCourtActivity.class);
            intent.putExtra("club_id", courtId);
            startActivity(intent);
        });

        // Sự kiện nút tiếp tục
        btnNext.setOnClickListener(v -> goToNextScreen());

        // Sự kiện nút quay lại
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(BookingTableActivity.this, MainActivity.class);
            intent.putExtra("showFragment", "courts");
            startActivity(intent);
            finish();
        });
    }

    /** Lấy thông tin người dùng từ API để xác định isStudent */
    private void fetchUserInfo() {
        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getToken();
        if (token == null) {
            isStudent = false;
            fetchBookingSlots(courtId, selectedDate);
            return;
        }

        ApiService apiService = RetrofitClient.getApiService(this);
        Call<MyInfoResponse> call = apiService.getMyInfo("Bearer " + token);
        call.enqueue(new Callback<MyInfoResponse>() {
            @Override
            public void onResponse(Call<MyInfoResponse> call, Response<MyInfoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MyInfo myInfo = response.body().getResult();
                    if (myInfo != null) {
                        isStudent = myInfo.isStudent();
                    } else {
                        isStudent = false;
                    }
                    fetchBookingSlots(courtId, selectedDate);
                } else {
                    Toast.makeText(BookingTableActivity.this, "Không thể lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                    isStudent = false;
                    fetchBookingSlots(courtId, selectedDate);
                }
            }

            @Override
            public void onFailure(Call<MyInfoResponse> call, Throwable t) {
                Toast.makeText(BookingTableActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                isStudent = false;
                fetchBookingSlots(courtId, selectedDate);
            }
        });
    }

    /** Hiển thị dialog chọn ngày */
    private void showDatePickerDialog() {
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(sdf.parse(selectedDate));
        } catch (Exception e) {
            e.printStackTrace();
        }
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (DatePicker view, int y, int m, int d) -> {
                    Calendar tmp = Calendar.getInstance();
                    tmp.set(y, m, d);
                    String newSelectedDate = sdf.format(tmp.getTime());
                    if (!selectedOrdersByDate.containsKey(newSelectedDate)) {
                        selectedOrdersByDate.put(newSelectedDate, new HashMap<>());
                    }
                    selectedDate = newSelectedDate;
                    btnSelectDate.setText("Ngày: " + selectedDate);
                    fetchBookingSlots(courtId, selectedDate);
                },
                year, month, day
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    /** Lấy dữ liệu slot từ API */
    private void fetchBookingSlots(String clubId, String dateBooking) {
        ApiService apiService = RetrofitClient.getApiService(this);
        NetworkUtils.callApi(
                apiService.getBookingSlots(clubId, dateBooking),
                this,
                new NetworkUtils.ApiCallback<List<CourtSlot>>() {
                    @Override
                    public void onSuccess(List<CourtSlot> courtSlots) {
                        if (courtSlots != null) {
                            courtsByDate.put(dateBooking, courtSlots);
                            collectAllStartTimesForCurrentDay(courtSlots);
                            setupCourtSpinner(courtSlots);
                            buildTableForDate(dateBooking);
                            updateSelectedBookings();
                        } else {
                            Toast.makeText(BookingTableActivity.this, "Không có dữ liệu slot", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {}
                }
        );
    }

    /** Thu thập tất cả thời gian bắt đầu */
    private void collectAllStartTimesForCurrentDay(List<CourtSlot> courtList) {
        HashMap<String, Boolean> timeMap = new HashMap<>();
        for (CourtSlot court : courtList) {
            for (BookingSlot slot : court.getBookingSlots()) {
                timeMap.put(slot.getStartTime(), true);
            }
        }
        allTimesCurrentDay = new ArrayList<>(timeMap.keySet());
        allTimesCurrentDay.sort((t1, t2) -> Integer.compare(toMinutes(t1), toMinutes(t2)));
    }

    private int toMinutes(String hhmmss) {
        String[] parts = hhmmss.split(":");
        int hh = Integer.parseInt(parts[0]);
        int mm = Integer.parseInt(parts[1]);
        return hh * 60 + mm;
    }

    /** Thiết lập spinner chọn sân */
    private void setupCourtSpinner(List<CourtSlot> courtList) {
        List<String> spinnerItems = new ArrayList<>();
        spinnerItems.add("Tất cả");
        for (CourtSlot c : courtList) {
            spinnerItems.add(c.getCourtSlotName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourt.setAdapter(adapter);
    }

    /** Xây dựng bảng slot */
    private void buildTableForDate(String date) {
        tableLayout.removeAllViews();

        List<CourtSlot> courtSlots = courtsByDate.get(date);
        if (courtSlots == null) {
            return;
        }

        // Tạo hàng tiêu đề
        TableRow headerRow = new TableRow(this);
        headerRow.addView(createCell("Court", true, true));
        for (String time : allTimesCurrentDay) {
            String displayTime = time.substring(0, 5);
            headerRow.addView(createCell(displayTime, true, false));
        }
        tableLayout.addView(headerRow);

        Map<String, ConfirmOrder> selectedOrdersForThisDate = selectedOrdersByDate.get(date);
        if (selectedOrdersForThisDate == null) {
            selectedOrdersForThisDate = new HashMap<>();
            selectedOrdersByDate.put(date, selectedOrdersForThisDate);
        }

        String selectedCourtName = spinnerCourt.getSelectedItem().toString();

        if (courtSlots.isEmpty() || allTimesCurrentDay.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Không có dữ liệu");
            tableLayout.addView(tvEmpty);
            return;
        }

        for (CourtSlot court : courtSlots) {
            if (!selectedCourtName.equals("Tất cả") && !court.getCourtSlotName().equals(selectedCourtName)) {
                continue;
            }

            TableRow row = new TableRow(this);
            row.addView(createCell(court.getCourtSlotName(), false, true));

            Map<String, BookingSlot> slotMap = new HashMap<>();
            for (BookingSlot slot : court.getBookingSlots()) {
                slotMap.put(slot.getStartTime(), slot);
            }

            for (String time : allTimesCurrentDay) {
                TextView cell = createCell("", false, false);
                cell.setBackground(createCellBackground());

                BookingSlot slot = slotMap.get(time);
                if (slot != null) {
                    final String finalKey = court.getCourtSlotId() + "_" + slot.getStartTime();
                    final TextView finalCell = cell;
                    final CourtSlot finalCourt = court;
                    final BookingSlot finalSlot = slot;
                    final String finalDate = date;
                    final Map<String, ConfirmOrder> finalSelectedOrdersForThisDate = selectedOrdersForThisDate;

                    if (isSlotEntirelyPast(finalDate, finalSlot.getEndTime())) {
                        switch (slot.getStatus()) {
                            case "LOCK_BOOKED":
                                setCellColor(cell, Color.parseColor("#b24646"));
                                break;
                            case "AVAILABLE":
                            case "LOCKED":
                                setCellColor(cell, Color.parseColor("#b2b2b2"));
                                break;
                            default:
                                setCellColor(cell, Color.parseColor("#E0E0E0"));
                                break;
                        }
                        cell.setOnClickListener(null);
                    } else {
                        switch (slot.getStatus()) {
                            case "AVAILABLE":
                                cell.setOnClickListener(v -> {
                                    if (finalSelectedOrdersForThisDate.containsKey(finalKey)) {
                                        finalSelectedOrdersForThisDate.remove(finalKey);
                                        setCellColor(finalCell, Color.WHITE);
                                    } else {
                                        ConfirmOrder order = new ConfirmOrder();
                                        order.setCourtSlotId(finalCourt.getCourtSlotId());
                                        order.setCourtSlotName(finalCourt.getCourtSlotName());
                                        order.setStartTime(finalSlot.getStartTime());
                                        order.setEndTime(finalSlot.getEndTime());
                                        double price = isStudent ? finalSlot.getStudentPrice() : finalSlot.getDailyPrice();
                                        order.setDailyPrice(price);
                                        order.setDayBooking(finalDate);
                                        finalSelectedOrdersForThisDate.put(finalKey, order);
                                        setCellColor(finalCell, Color.parseColor("#A5D6A7"));
                                    }
                                    updateSelectedBookings();
                                });
                                if (selectedOrdersForThisDate.containsKey(finalKey)) {
                                    setCellColor(cell, Color.parseColor("#A5D6A7"));
                                } else {
                                    setCellColor(cell, Color.WHITE);
                                }
                                break;
                            case "BOOKED":
                                setCellColor(cell, Color.parseColor("#ff6464"));
                                cell.setOnClickListener(null);
                                break;
                            case "LOCKED":
                                setCellColor(cell, Color.parseColor("#b2b2b2"));
                                cell.setOnClickListener(null);
                                break;
                            default:
                                setCellColor(cell, Color.parseColor("#E0E0E0"));
                                cell.setOnClickListener(null);
                                break;
                        }
                    }
                } else {
                    setCellColor(cell, Color.parseColor("#E0E0E0"));
                    cell.setOnClickListener(null);
                }
                row.addView(cell);
            }
            tableLayout.addView(row, new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
            ));
        }
    }

    private boolean isSlotEntirelyPast(String dateBooking, String endTime) {
        try {
            String dateTimeString = dateBooking + " " + endTime;
            Date slotEnd = sdfFull.parse(dateTimeString);
            Date now = new Date();
            if (slotEnd != null) {
                return now.after(slotEnd);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    private TextView createCell(String text, boolean isHeader, boolean isFirstColumn) {
        TextView tv = new TextView(this);
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        tv.setLayoutParams(params);

        int minWidth = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                isFirstColumn ? 120 : 80,
                getResources().getDisplayMetrics()
        );
        tv.setMinWidth(minWidth);

        int padding = dpToPx(4);
        tv.setPadding(padding, padding, padding, padding);
        tv.setText(text);
        tv.setGravity(Gravity.CENTER);
        tv.setSingleLine(true);
        tv.setEllipsize(TextUtils.TruncateAt.END);

        if (isHeader) {
            tv.setTypeface(null, Typeface.BOLD);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            tv.setBackgroundColor(Color.parseColor("#EEEEEE"));
        } else if (isFirstColumn) {
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            GradientDrawable courtDrawable = new GradientDrawable();
            courtDrawable.setColor(Color.parseColor("#FFCC80"));
            courtDrawable.setStroke(dpToPx(2), Color.BLACK);
            tv.setBackground(courtDrawable);
        } else {
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        }
        return tv;
    }

    private GradientDrawable createCellBackground() {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.TRANSPARENT);
        gd.setStroke(2, Color.BLACK);
        return gd;
    }

    private void setCellColor(TextView cell, int color) {
        GradientDrawable bg = (GradientDrawable) cell.getBackground();
        bg.setColor(color);
    }

    /** Cập nhật danh sách slot đã chọn */
    private void updateSelectedBookings() {
        selectedBookingsList.clear();
        for (String date : selectedOrdersByDate.keySet()) {
            Map<String, ConfirmOrder> ordersMap = selectedOrdersByDate.get(date);
            if (ordersMap == null) continue;
            for (ConfirmOrder order : ordersMap.values()) {
                String info = "Ngày: " + order.getDayBooking()
                        + ", Sân: " + order.getCourtSlotName()
                        + ", Thời gian: " + order.getStartTime().substring(0, 5)
                        + " - " + order.getEndTime().substring(0, 5)
                        + ", Giá: " + String.format("%.0f", order.getDailyPrice()) + " VNĐ";
                selectedBookingsList.add(info);
            }
        }
        selectedBookingsAdapter.notifyDataSetChanged();
    }

    private void goToNextScreen() {
        List<ConfirmOrder> confirmOrders = new ArrayList<>();
        for (Map<String, ConfirmOrder> ordersMap : selectedOrdersByDate.values()) {
            if (ordersMap != null) {
                confirmOrders.addAll(ordersMap.values());
            }
        }
        if (confirmOrders.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất 1 slot", Toast.LENGTH_SHORT).show();
            return;
        }
        Gson gson = new Gson();
        String confirmOrdersJson = gson.toJson(confirmOrders);
        Intent intent = new Intent(BookingTableActivity.this, ConfirmActivity.class);
        intent.putExtra("tvPhone", tvPhone);

        intent.putExtra("confirmOrdersJson", confirmOrdersJson);
        intent.putExtra("club_id", courtId);
        intent.putExtra("selectedDate", selectedDate);
        String orderId = getIntent().getStringExtra("orderId");
        if (orderId != null && !orderId.isEmpty()) {
            intent.putExtra("orderId", orderId);
        }
        startActivity(intent);
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }
}