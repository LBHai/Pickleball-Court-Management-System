package SEP490.G9;

import Api.ApiService;
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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
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

    // Định dạng ngày và giờ
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat sdfFull = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    private String selectedDate = "";   // Ngày đang hiển thị
    private String courtId;             // ID của Club (truyền qua Intent)

    // Lưu danh sách CourtSlot cho mỗi ngày
    private Map<String, List<CourtSlot>> courtsByDate = new HashMap<>();
    // Lưu thông tin ConfirmOrder đã chọn cho mỗi ngày
    private Map<String, Map<String, ConfirmOrder>> selectedOrdersByDate = new HashMap<>();

    // Hiển thị danh sách đã chọn
    private ArrayList<String> selectedBookingsList = new ArrayList<>();
    private ArrayAdapter<String> selectedBookingsAdapter;

    // Danh sách tất cả giờ (startTime) của ngày hiện tại
    private List<String> allTimesCurrentDay = new ArrayList<>();

    // Ngưỡng nội suy màu cho BOOKED đã qua giờ (ví dụ 2 giờ = 120 phút)
    private final long BOOKED_FADE_THRESHOLD = 120 * 60 * 1000; // 120 phút tính theo millis

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_table);

        tableLayout = findViewById(R.id.tableLayout);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        spinnerCourt = findViewById(R.id.spinnerCourt);
        btnNext = findViewById(R.id.btnNext);
        lvSelectedBookings = findViewById(R.id.lvSelectedBookings);

        selectedBookingsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, selectedBookingsList);
        lvSelectedBookings.setAdapter(selectedBookingsAdapter);

        // Lấy club_id từ Intent
        courtId = getIntent().getStringExtra("club_id");
        if (courtId == null) {
            Toast.makeText(this, "Không có club_id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Ngày mặc định là ngày hiện tại
        Calendar cal = Calendar.getInstance();
        selectedDate = sdf.format(cal.getTime());
        btnSelectDate.setText("Ngày: " + selectedDate);
        if (!selectedOrdersByDate.containsKey(selectedDate)) {
            selectedOrdersByDate.put(selectedDate, new HashMap<>());
        }

        fetchBookingSlots(courtId, selectedDate);

        btnSelectDate.setOnClickListener(v -> showDatePickerDialog());

        spinnerCourt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                buildTableForDate(selectedDate);
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnNext.setOnClickListener(v -> goToNextScreen());
    }

    private void showDatePickerDialog() {
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(sdf.parse(selectedDate));
        } catch (ParseException e) {
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

    // Thu thập tất cả các startTime từ danh sách slot
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

        // Tạo header cho bảng: cột "Court" + các cột giờ
        TableRow headerRow = new TableRow(this);
        headerRow.addView(createCell("Court", true, true));
        for (String time : allTimesCurrentDay) {
            String displayTime = time.substring(0, 5);
            headerRow.addView(createCell(displayTime, true, false));
        }
        tableLayout.addView(headerRow);

        // Lấy danh sách ConfirmOrder đã chọn cho ngày hiện tại
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
            // Cột đầu tiên: tên sân
            row.addView(createCell(court.getCourtSlotName(), false, true));

            // Tạo map từ startTime đến BookingSlot
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

                    switch (slot.getStatus()) {
                        case "AVAILABLE":
                            // Nếu slot AVAILABLE nhưng đã qua giờ thì hiển thị màu xám (không thể chọn)
                            if (isPast(finalDate, slot.getEndTime())) {
                                setCellColor(cell, Color.parseColor("#BDBDBD"));
                                cell.setOnClickListener(null);
                            } else {
                                // Còn lại, AVAILABLE: cho phép chọn/hủy
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
                                if (finalSelectedOrdersForThisDate.containsKey(finalKey)) {
                                    setCellColor(cell, Color.parseColor("#A5D6A7"));
                                } else {
                                    setCellColor(cell, Color.WHITE);
                                }
                            }
                            break;

                        case "BOOKED":
                            // Nếu slot BOOKED: kiểm tra thời gian
                            cell.setOnClickListener(null);
                            if (!isPast(finalDate, finalSlot.getEndTime())) {
                                // Chưa hết giờ: tô đỏ hoàn toàn (#ff6464)
                                setCellColor(cell, Color.parseColor("#ff6464"));
                            } else {
                                // Đã qua giờ: nội suy màu dựa trên khoảng cách thời gian so với slot end
                                int bookedColor = interpolateBookedColor(finalDate, finalSlot.getEndTime());
                                setCellColor(cell, bookedColor);
                            }
                            break;

                        case "LOCKED":
                            setCellColor(cell, Color.parseColor("#BDBDBD"));
                            cell.setOnClickListener(null);
                            break;

                        default:
                            setCellColor(cell, Color.parseColor("#E0E0E0"));
                            cell.setOnClickListener(null);
                            break;
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

    // Hàm kiểm tra xem slot đã qua giờ hay chưa
    private boolean isPast(String dateBooking, String endTime) {
        try {
            Date slotEnd = sdfFull.parse(dateBooking + " " + endTime);
            return slotEnd != null && new Date().after(slotEnd);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Nếu slot BOOKED đã qua giờ, nội suy màu từ đỏ ban đầu (#ff6464)
     * đến màu đỏ tối hơn (#b24646) dựa trên thời gian đã qua kể từ khi slot kết thúc.
     * Giả sử nếu đã qua 2 giờ trở lên, màu đạt mức tối đa (#b24646).
     */
    private int interpolateBookedColor(String dateBooking, String endTime) {
        try {
            Date slotEnd = sdfFull.parse(dateBooking + " " + endTime);
            Date now = new Date();
            if (slotEnd == null || now.before(slotEnd)) {
                return Color.parseColor("#ff6464");
            }
            long diff = now.getTime() - slotEnd.getTime();
            float fraction = Math.min(1f, (float) diff / (float) BOOKED_FADE_THRESHOLD);
            return interpolateColor(Color.parseColor("#ff6464"), Color.parseColor("#b24646"), fraction);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return Color.parseColor("#ff6464");
    }

    // Hàm nội suy màu giữa hai màu với tỷ lệ fraction (0..1)
    private int interpolateColor(int colorStart, int colorEnd, float fraction) {
        int startA = (colorStart >> 24) & 0xff;
        int startR = (colorStart >> 16) & 0xff;
        int startG = (colorStart >> 8) & 0xff;
        int startB = colorStart & 0xff;

        int endA = (colorEnd >> 24) & 0xff;
        int endR = (colorEnd >> 16) & 0xff;
        int endG = (colorEnd >> 8) & 0xff;
        int endB = colorEnd & 0xff;

        int a = (int)(startA + (endA - startA) * fraction);
        int r = (int)(startR + (endR - startR) * fraction);
        int g = (int)(startG + (endG - startG) * fraction);
        int b = (int)(startB + (endB - startB) * fraction);

        return (a << 24) | (r << 16) | (g << 8) | b;
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
