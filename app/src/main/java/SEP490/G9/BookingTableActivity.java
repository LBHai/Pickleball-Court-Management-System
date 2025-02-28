package SEP490.G9;

import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;
import java.util.Set;
import Api.ApiService;
import Model.BookingSlot;
import Model.CourtSlot;
import Model.ConfirmOrder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingTableActivity extends AppCompatActivity {
    private TableLayout tableLayout;
    private Button btnSelectDate, btnNext;
    private TextView tvSelectedDate;
    private Spinner spinnerCourt;
    private String courtId;
    private String selectedDate = "";
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private List<CourtSlot> allCourts = new ArrayList<>();
    private List<String> allTimes = new ArrayList<>();
    private Set<String> selectedCells = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_table);
        tableLayout = findViewById(R.id.tableLayout);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        spinnerCourt = findViewById(R.id.spinnerCourt);
        btnNext = findViewById(R.id.btnNext);
        courtId = getIntent().getStringExtra("club_id");
        if (courtId == null) {
            Toast.makeText(this, "Không có club_id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Calendar c = Calendar.getInstance();
        selectedDate = sdf.format(c.getTime());
        tvSelectedDate.setText("Ngày: " + selectedDate);
        fetchBookingSlots(courtId, selectedDate);
        btnSelectDate.setOnClickListener(v -> showDatePickerDialog());
        spinnerCourt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                buildTable();
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        btnNext.setOnClickListener(v -> goToNextScreen());
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
                    tvSelectedDate.setText("Ngày: " + selectedDate);
                    fetchBookingSlots(courtId, selectedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void fetchBookingSlots(String courtId, String dateBooking) {
        Call<List<CourtSlot>> call = ApiService.apiService.getBookingSlots(courtId, dateBooking);
        call.enqueue(new Callback<List<CourtSlot>>() {
            public void onResponse(Call<List<CourtSlot>> call, Response<List<CourtSlot>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allCourts = response.body();
                    collectAllStartTimes(allCourts);
                    setupCourtSpinner(allCourts);
                    selectedCells.clear();
                    buildTable();
                } else {
                    Toast.makeText(BookingTableActivity.this, "Lỗi khi lấy dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }
            public void onFailure(Call<List<CourtSlot>> call, Throwable t) {
                Toast.makeText(BookingTableActivity.this, "Kết nối thất bại: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void collectAllStartTimes(List<CourtSlot> courtList) {
        Set<String> timeSet = new HashSet<>();
        for (CourtSlot court : courtList) {
            for (BookingSlot slot : court.getBookingSlots()) {
                timeSet.add(slot.getStartTime());
            }
        }
        allTimes = new ArrayList<>(timeSet);
        allTimes.sort((t1, t2) -> Integer.compare(toMinutes(t1), toMinutes(t2)));
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

    private void buildTable() {
        tableLayout.removeAllViews();
        TableRow headerRow = new TableRow(this);
        TextView cornerCell = createCell("Court", true, true);
        headerRow.addView(cornerCell);
        for (String time : allTimes) {
            String displayTime = time.substring(0, 5);
            TextView cell = createCell(displayTime, true, false);
            headerRow.addView(cell);
        }
        tableLayout.addView(headerRow);
        String selectedCourtName = spinnerCourt.getSelectedItem().toString();
        for (CourtSlot court : allCourts) {
            if (!selectedCourtName.equals("Tất cả") && !court.getCourtSlotName().equals(selectedCourtName))
                continue;
            TableRow row = new TableRow(this);
            TextView courtNameCell = createCell(court.getCourtSlotName(), false, true);
            row.addView(courtNameCell);
            Map<String, BookingSlot> slotMap = new HashMap<>();
            for (BookingSlot slot : court.getBookingSlots()) {
                slotMap.put(slot.getStartTime(), slot);
            }
            for (String time : allTimes) {
                TextView cell = createCell("", false, false);
                cell.setBackground(createCellBackground());
                BookingSlot slot = slotMap.get(time);
                if (slot != null) {
                    String key = court.getCourtSlotId() + "_" + slot.getStartTime();
                    if ("AVAILABLE".equals(slot.getStatus())) {
                        cell.setOnClickListener(v -> {
                            if (selectedCells.contains(key)) {
                                selectedCells.remove(key);
                                setCellColor(cell, Color.WHITE);
                            } else {
                                selectedCells.add(key);
                                setCellColor(cell, Color.parseColor("#A5D6A7"));
                            }
                        });
                        if (selectedCells.contains(key)) {
                            setCellColor(cell, Color.parseColor("#A5D6A7"));
                        } else {
                            setCellColor(cell, Color.WHITE);
                        }
                    } else if ("BOOKED".equals(slot.getStatus())) {
                        setCellColor(cell, Color.parseColor("#EF9A9A"));
                        cell.setOnClickListener(null);
                    } else if ("LOCKED".equals(slot.getStatus())) {
                        setCellColor(cell, Color.parseColor("#BDBDBD"));
                        cell.setOnClickListener(null);
                    } else {
                        setCellColor(cell, Color.parseColor("#E0E0E0"));
                        cell.setOnClickListener(null);
                    }
                } else {
                    setCellColor(cell, Color.parseColor("#E0E0E0"));
                    cell.setOnClickListener(null);
                }
                row.addView(cell);
            }
            tableLayout.addView(row);
        }
    }

    private TextView createCell(String text, boolean isHeader, boolean isFirstColumn) {
        TextView tv = new TextView(this);
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        tv.setLayoutParams(params);
        tv.setPadding(16, 16, 16, 16);
        tv.setText(text);
        tv.setGravity(Gravity.CENTER);
        tv.setSingleLine(true);
        tv.setEllipsize(TextUtils.TruncateAt.END);
        if (isFirstColumn) {
            tv.setWidth(440);
        } else {
            tv.setWidth(220);
        }
        if (isHeader) {
            tv.setTypeface(null, Typeface.BOLD);
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

    private void goToNextScreen() {
        List<ConfirmOrder> confirmOrders = new ArrayList<>();
        for (CourtSlot court : allCourts) {
            for (BookingSlot slot : court.getBookingSlots()) {
                String key = court.getCourtSlotId() + "_" + slot.getStartTime();
                if (selectedCells.contains(key)) {
                    ConfirmOrder order = new ConfirmOrder();
                    order.setCourtSlotName(court.getCourtSlotName());
                    order.setStartTime(slot.getStartTime());
                    order.setEndTime(slot.getEndTime());
                    order.setDailyPrice(slot.getDailyPrice());
                    confirmOrders.add(order);
                }
            }
        }
        if (confirmOrders.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn lịch đặt", Toast.LENGTH_SHORT).show();
            return;
        }
        Gson gson = new Gson();
        String confirmOrdersJson = gson.toJson(confirmOrders);
        Intent intent = new Intent(BookingTableActivity.this, ConfirmActivity.class);
        intent.putExtra("selectedDate", selectedDate);
        intent.putExtra("confirmOrdersJson", confirmOrdersJson);
        startActivity(intent);
    }
}
