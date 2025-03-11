package SEP490.G9;

import Api.RetrofitClient;
import androidx.appcompat.app.AppCompatActivity;

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

import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import java.util.Locale;
import java.util.Map;
import Api.ApiService;
import Fragment.CourtsFragment;
import Fragment.MapFragment;
import Model.BookingSlot;
import Model.CourtSlot;
import Model.ConfirmOrder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingTableActivity extends AppCompatActivity {

    private TableLayout tableLayout;
    private Button btnSelectDate, btnNext;
    private Spinner spinnerCourt;
    private ListView lvSelectedBookings;
    private ImageButton btnBack;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    // Định dạng đầy đủ để so sánh giờ
    private SimpleDateFormat sdfFull = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    private String selectedDate = "";   // Ngày đang hiển thị
    private String courtId;             // ID của Club (truyền qua Intent)

    // Lưu danh sách CourtSlot cho mỗi ngày
    private Map<String, List<CourtSlot>> courtsByDate = new HashMap<>();

    // Lưu các ConfirmOrder đã chọn
    private Map<String, Map<String, ConfirmOrder>> selectedOrdersByDate = new HashMap<>();

    // Hiển thị danh sách slot đã chọn
    private ArrayList<String> selectedBookingsList = new ArrayList<>();
    private ArrayAdapter<String> selectedBookingsAdapter;

    // Lưu tất cả startTime cho ngày hiện tại (để vẽ cột)
    private List<String> allTimesCurrentDay = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_table);

        tableLayout = findViewById(R.id.tableLayout);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        spinnerCourt = findViewById(R.id.spinnerCourt);
        btnNext = findViewById(R.id.btnNext);
        lvSelectedBookings = findViewById(R.id.lvSelectedBookings);
        btnBack = findViewById(R.id.btnBack);
        selectedBookingsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, selectedBookingsList);
        lvSelectedBookings.setAdapter(selectedBookingsAdapter);

        // Lấy club_id từ Intent
        courtId = getIntent().getStringExtra("club_id");
        if (courtId == null) {
            Toast.makeText(this, "Không có club_id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Ngày mặc định = hôm nay
        Calendar c = Calendar.getInstance();
        selectedDate = sdf.format(c.getTime());
        btnSelectDate.setText("Ngày: " + selectedDate);

        // Khởi tạo map cho ngày này
        if (!selectedOrdersByDate.containsKey(selectedDate)) {
            selectedOrdersByDate.put(selectedDate, new HashMap<>());
        }

        // Gọi API ban đầu
        fetchBookingSlots(courtId, selectedDate);

        // Nút chọn ngày
        btnSelectDate.setOnClickListener(v -> showDatePickerDialog());

        // Khi chọn spinner => build lại bảng
        spinnerCourt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                buildTableForDate(selectedDate);
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        TextView tvViewCourtsAndPrices = findViewById(R.id.tvViewCourtsAndPrices);
        tvViewCourtsAndPrices.setOnClickListener(v -> {
            Intent intent = new Intent(BookingTableActivity.this, DetailPriceCourtActivity.class);
            intent.putExtra("club_id", courtId);
            startActivity(intent);
        });

        btnNext.setOnClickListener(v -> goToNextScreen());

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(BookingTableActivity.this, MainActivity.class);
            intent.putExtra("showFragment", "courts");
            startActivity(intent);
            finish();
        });
    }

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
                    selectedDate = sdf.format(tmp.getTime());
                    btnSelectDate.setText("Ngày: " + selectedDate);

                    if (!selectedOrdersByDate.containsKey(selectedDate)) {
                        selectedOrdersByDate.put(selectedDate, new HashMap<>());
                    }
                    fetchBookingSlots(courtId, selectedDate);
                },
                year, month, day
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    // Gọi API lấy BookingSlots
    private void fetchBookingSlots(String clubId, String dateBooking) {
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.getBookingSlots(clubId, dateBooking).enqueue(new Callback<List<CourtSlot>>() {
            @Override
            public void onResponse(Call<List<CourtSlot>> call, Response<List<CourtSlot>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    courtsByDate.put(dateBooking, response.body());
                    collectAllStartTimesForCurrentDay(response.body());
                    setupCourtSpinner(response.body());
                    buildTableForDate(dateBooking);
                    updateSelectedBookings();
                } else {
                    Toast.makeText(BookingTableActivity.this, "Lỗi khi lấy dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<CourtSlot>> call, Throwable t) {
                Toast.makeText(BookingTableActivity.this, "Kết nối thất bại: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Thu thập tất cả startTime
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

    // "HH:mm" -> tổng phút
    private int toMinutes(String hhmmss) {
        String[] parts = hhmmss.split(":");
        int hh = Integer.parseInt(parts[0]);
        int mm = Integer.parseInt(parts[1]);
        return hh * 60 + mm;
    }

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

    private void buildTableForDate(String date) {
        tableLayout.removeAllViews();

        List<CourtSlot> courtSlots = courtsByDate.get(date);
        if (courtSlots == null) {
            return;
        }

        // Header
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
            if (!selectedCourtName.equals("Tất cả") &&
                    !court.getCourtSlotName().equals(selectedCourtName)) {
                continue;
            }

            TableRow row = new TableRow(this);
            // Tên sân
            row.addView(createCell(court.getCourtSlotName(), false, true));

            // Map startTime -> BookingSlot
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
                        // Slot đã qua giờ
                        // 1) Nếu BOOKED => #b24646
                        // 2) Nếu AVAILABLE => LOCKED => #b2b2b2
                        // 3) LOCKED => #b2b2b2
                        // 4) Khác => xám nhạt
                        switch (slot.getStatus()) {
                            case "BOOKED":
                                setCellColor(cell, Color.parseColor("#b24646")); // đỏ sẫm
                                break;
                            case "AVAILABLE":
                            case "LOCKED":
                                setCellColor(cell, Color.parseColor("#b2b2b2")); // xám
                                break;
                            default:
                                setCellColor(cell, Color.parseColor("#E0E0E0")); // xám nhạt
                                break;
                        }
                        cell.setOnClickListener(null);
                    } else {
                        // Slot chưa qua giờ
                        switch (slot.getStatus()) {
                            case "AVAILABLE":
                                // Trắng => có thể chọn => xanh lá
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
                                        order.setDailyPrice(finalSlot.getDailyPrice());
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
                                // Chưa qua giờ => đỏ (#ff6464)
                                setCellColor(cell, Color.parseColor("#ff6464"));
                                cell.setOnClickListener(null);
                                break;
                            case "LOCKED":
                                // #bdbdbd
                                setCellColor(cell, Color.parseColor("#BDBDBD"));
                                cell.setOnClickListener(null);
                                break;
                            default:
                                // #e0e0e0
                                setCellColor(cell, Color.parseColor("#E0E0E0"));
                                cell.setOnClickListener(null);
                                break;
                        }
                    }
                } else {
                    // Không có slot => xám nhạt
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

    /**
     * Kiểm tra nếu bây giờ đã qua endTime => slotEntirelyPast
     * dateBooking = "yyyy-MM-dd", endTime = "HH:mm"
     */
    private boolean isSlotEntirelyPast(String dateBooking, String endTime) {
        try {
            String dateTimeString = dateBooking + " " + endTime; // "2023-09-25 10:00"
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

    private void updateSelectedBookings() {
        selectedBookingsList.clear();
        for (String date : selectedOrdersByDate.keySet()) {
            Map<String, ConfirmOrder> ordersMap = selectedOrdersByDate.get(date);
            if (ordersMap == null) continue;
            for (ConfirmOrder order : ordersMap.values()) {
                String info = "Ngày: " + order.getDayBooking()
                        + ", Sân: " + order.getCourtSlotName()
                        + ", Thời gian: " + order.getStartTime().substring(0,5)
                        + " - " + order.getEndTime().substring(0,5);
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
        intent.putExtra("confirmOrdersJson", confirmOrdersJson);
        intent.putExtra("club_id", courtId);
        intent.putExtra("selectedDate", selectedDate);
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
