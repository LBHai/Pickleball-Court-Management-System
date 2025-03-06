package SEP490.G9;

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
    // HashSet lưu các ô mà người dùng đã chọn (trạng thái "đang đặt") vĩnh viễn khi đang sử dụng app
    private Set<String> selectedCells = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_table);

        tableLayout = findViewById(R.id.tableLayout);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        tvSelectedDate = findViewById(R.id.btnSelectDate);
        spinnerCourt = findViewById(R.id.spinnerCourt);
        btnNext = findViewById(R.id.btnNext);

        // Lấy club_id từ Intent
        courtId = getIntent().getStringExtra("club_id");
        if (courtId == null) {
            Toast.makeText(this, "Không có club_id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Thiết lập ngày hiện tại làm mặc định
        Calendar c = Calendar.getInstance();
        selectedDate = sdf.format(c.getTime());
        tvSelectedDate.setText("Ngày: " + selectedDate);

        // Gọi API lấy dữ liệu slot cho ngày và sân được chọn
        fetchBookingSlots(courtId, selectedDate);

        // Chọn ngày (các ngày quá khứ bị vô hiệu hóa)
        btnSelectDate.setOnClickListener(v -> showDatePickerDialog());

        // Khi thay đổi sân qua spinner, xây dựng lại bảng
        spinnerCourt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                buildTable();
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Nút Next: chuyển sang màn hình xác nhận
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
        // Chỉ cho phép chọn ngày hiện tại và tương lai (ngày quá khứ bị vô hiệu hóa và xám đi)
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
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
                    // Lưu ý: Không xóa selectedCells để giữ trạng thái đã chọn trong suốt phiên làm việc của app
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
        // Sắp xếp theo thứ tự thời gian
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

        // Hàng header
        TableRow headerRow = new TableRow(this);
        headerRow.addView(createCell("Court", true, true));

        for (String time : allTimes) {
            String displayTime = time.substring(0, 5);
            headerRow.addView(createCell(displayTime, true, false));
        }
        tableLayout.addView(headerRow);

        String selectedCourtName = spinnerCourt.getSelectedItem().toString();

        // Kiểm tra dữ liệu đầu vào
        if (allCourts.isEmpty() || allTimes.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Không có dữ liệu");
            tableLayout.addView(tvEmpty);
            return;
        }

        for (CourtSlot court : allCourts) {
            if (!selectedCourtName.equals("Tất cả") &&
                    !court.getCourtSlotName().equals(selectedCourtName)) {
                continue;
            }

            TableRow row = new TableRow(this);
            row.addView(createCell(court.getCourtSlotName(), false, true));

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
                    switch (slot.getStatus()) {
                        case "AVAILABLE":
                            // Cho phép người dùng chọn/bỏ chọn ô đặt sân
                            cell.setOnClickListener(v -> {
                                if (selectedCells.contains(key)) {
                                    selectedCells.remove(key);
                                    setCellColor(cell, Color.WHITE);
                                } else {
                                    selectedCells.add(key);
                                    setCellColor(cell, Color.parseColor("#A5D6A7"));
                                }
                            });
                            // Hiển thị màu theo trạng thái đã chọn
                            if (selectedCells.contains(key)) {
                                setCellColor(cell, Color.parseColor("#A5D6A7"));
                            } else {
                                setCellColor(cell, Color.WHITE);
                            }
                            break;
                        case "BOOKED":
                            setCellColor(cell, Color.RED);
                            cell.setOnClickListener(null);
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
                    // Nếu không có slot thì ô hiển thị màu xám nhạt
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

    private TextView createCell(String text, boolean isHeader, boolean isFirstColumn) {
        TextView tv = new TextView(this);

        // Thiết lập LayoutParams
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        tv.setLayoutParams(params);

        // Thiết lập kích thước tối thiểu
        int minWidth = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                isFirstColumn ? 120 : 80,
                getResources().getDisplayMetrics()
        );
        tv.setMinWidth(minWidth);

        // Thiết lập padding
        int padding = dpToPx(4);
        tv.setPadding(padding, padding, padding, padding);

        // Thiết lập text và căn giữa
        tv.setText(text);
        tv.setGravity(Gravity.CENTER);
        tv.setSingleLine(true);
        tv.setEllipsize(TextUtils.TruncateAt.END);

        // Thiết lập style
        if (isHeader) {
            tv.setTypeface(null, Typeface.BOLD);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            tv.setBackgroundColor(Color.parseColor("#EEEEEE"));
        } else if (isFirstColumn) {
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            // Tạo drawable cho cell của cột sân với màu nền #FFCC80 và border đen
            GradientDrawable courtDrawable = new GradientDrawable();
            courtDrawable.setColor(Color.parseColor("#FFCC80")); // Màu nền dễ nhìn cho tên sân
            courtDrawable.setStroke(dpToPx(2), Color.BLACK);      // Đường viền dày 2dp, màu đen
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

    private void goToNextScreen() {
        // Lấy danh sách slot mà người dùng đã chọn
        List<ConfirmOrder> confirmOrders = new ArrayList<>();
        for (String key : selectedCells) {
            // Tách key thành courtSlotId và startTime
            String[] parts = key.split("_");
            String courtSlotId = parts[0];
            String startTime = parts[1];

            // Tìm courtSlot và bookingSlot tương ứng
            for (CourtSlot court : allCourts) {
                if (court.getCourtSlotId().equals(courtSlotId)) {
                    for (BookingSlot slot : court.getBookingSlots()) {
                        if (slot.getStartTime().equals(startTime)) {
                            ConfirmOrder order = new ConfirmOrder();
                            order.setCourtSlotId(courtSlotId); // <-- Thêm dòng này
                            order.setCourtSlotName(court.getCourtSlotName());
                            order.setStartTime(slot.getStartTime());
                            order.setEndTime(slot.getEndTime());
                            order.setDailyPrice(slot.getDailyPrice());
                            confirmOrders.add(order);
                            break;
                        }
                    }
                    break;
                }
            }
        }

        if (confirmOrders.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn lịch đặt", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chuyển sang màn hình xác nhận
        Gson gson = new Gson();
        String confirmOrdersJson = gson.toJson(confirmOrders);
        Intent intent = new Intent(BookingTableActivity.this, ConfirmActivity.class);
        intent.putExtra("selectedDate", selectedDate);
        intent.putExtra("confirmOrdersJson", confirmOrdersJson);
        intent.putExtra("club_id", courtId);

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
