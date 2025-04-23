package UI.Activity;

import Data.Model.Role;
import Data.Network.ApiService;
import Data.Network.NetworkUtils;
import Data.Network.RetrofitClient;
import Data.Model.BookingSlot;
import Data.Model.ConfirmOrder;
import Data.Model.CourtSlot;
import Data.Model.MyInfo;
import Data.Model.MyInfoResponse;
import SEP490.G9.R;
import Data.Session.SessionManager;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

import java.text.NumberFormat;
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
    private ListView lvSelectedBookings;
    private ImageButton btnBack;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat sdfFull = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    private String selectedDate = "";
    private String courtId, tvPhone;

    private Map<String, List<CourtSlot>> courtsByDate = new HashMap<>();
    private Map<String, Map<String, ConfirmOrder>> selectedOrdersByDate = new HashMap<>();
    private ArrayList<String> selectedBookingsList = new ArrayList<>();
    private ArrayAdapter<String> selectedBookingsAdapter;
    private List<String> allTimesCurrentDay = new ArrayList<>();
    private List<String> startTimesCurrentDay = new ArrayList<>();

    private boolean isStudent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_table);

        tableLayout = findViewById(R.id.tableLayout);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnNext = findViewById(R.id.btnNext);
        lvSelectedBookings = findViewById(R.id.lvSelectedBookings);
        btnBack = findViewById(R.id.btnBack);

        selectedBookingsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, selectedBookingsList);
        lvSelectedBookings.setAdapter(selectedBookingsAdapter);

        courtId = getIntent().getStringExtra("club_id");
        tvPhone = getIntent().getStringExtra("tvPhone");

        if (courtId == null) {
            //Toast.makeText(this, "Không có club_id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Calendar c = Calendar.getInstance();
        selectedDate = sdf.format(c.getTime());
        btnSelectDate.setText("Date: " + selectedDate);

        selectedOrdersByDate.put(selectedDate, new HashMap<>());

        fetchUserInfo();

        btnSelectDate.setOnClickListener(v -> showDatePickerDialog());


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
                    if (myInfo != null && myInfo.getRoles() != null) {
                        isStudent = myInfo.getRoles().stream().anyMatch(role -> "STUDENT".equals(role.getName()));
                    } else {
                        isStudent = false;
                    }
                    fetchBookingSlots(courtId, selectedDate);
                } else {
                    //Toast.makeText(BookingTableActivity.this, "Không thể lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                    isStudent = false;
                    fetchBookingSlots(courtId, selectedDate);
                }
            }

            @Override
            public void onFailure(Call<MyInfoResponse> call, Throwable t) {
                //Toast.makeText(BookingTableActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                isStudent = false;
                fetchBookingSlots(courtId, selectedDate);
            }
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
                    String newSelectedDate = sdf.format(tmp.getTime());
                    if (!selectedOrdersByDate.containsKey(newSelectedDate)) {
                        selectedOrdersByDate.put(newSelectedDate, new HashMap<>());
                    }
                    selectedDate = newSelectedDate;
                    btnSelectDate.setText("Date: " + selectedDate);
                    fetchBookingSlots(courtId, selectedDate);
                },
                year, month, day
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

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
                            collectAllTimesForCurrentDay(courtSlots);
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

    /** Thu thập tất cả thời gian (startTime và endTime) cho tiêu đề, và chỉ startTime cho ô tương tác */
    private void collectAllTimesForCurrentDay(List<CourtSlot> courtList) {
        HashMap<String, Boolean> allTimesMap = new HashMap<>();
        HashMap<String, Boolean> startTimesMap = new HashMap<>();
        for (CourtSlot court : courtList) {
            for (BookingSlot slot : court.getBookingSlots()) {
                Log.d("BookingTableActivity", "Slot: start=" + slot.getStartTime() + ", end=" + slot.getEndTime());
                allTimesMap.put(slot.getStartTime(), true);
                allTimesMap.put(slot.getEndTime(), true);
                startTimesMap.put(slot.getStartTime(), true);
            }
        }
        allTimesCurrentDay = new ArrayList<>(allTimesMap.keySet());
        startTimesCurrentDay = new ArrayList<>(startTimesMap.keySet());
        allTimesCurrentDay.sort((t1, t2) -> Integer.compare(toMinutes(t1), toMinutes(t2)));
        startTimesCurrentDay.sort((t1, t2) -> Integer.compare(toMinutes(t1), toMinutes(t2)));

        Log.d("BookingTableActivity", "All Times for " + selectedDate + ": " + allTimesCurrentDay);
        Log.d("BookingTableActivity", "Start Times for " + selectedDate + ": " + startTimesCurrentDay);
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
    }

    private void buildTableForDate(String date) {
        tableLayout.removeAllViews();

        List<CourtSlot> courtSlots = courtsByDate.get(date);
        if (courtSlots == null) {
            Log.e("BookingTableActivity", "No court slots for date: " + date);
            return;
        }

        // Tạo hàng tiêu đề với tất cả thời gian (bao gồm endTime)
        TableRow headerRow = new TableRow(this);
        headerRow.addView(createCell("Court", true, true));
        for (String time : allTimesCurrentDay) {
            String displayTime = time.substring(0, 5);
            headerRow.addView(createCell(displayTime, true, false));
        }
        tableLayout.addView(headerRow);

        Map<String, ConfirmOrder> selectedOrdersForThisDate = selectedOrdersByDate.get(date);
        if (selectedOrdersForThisDate == null);
        {
            selectedOrdersForThisDate = new HashMap<>();
            selectedOrdersByDate.put(date, selectedOrdersForThisDate);
        }


        if (courtSlots.isEmpty() || startTimesCurrentDay.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Không có dữ liệu");
            tableLayout.addView(tvEmpty);
            Log.w("BookingTableActivity", "No court slots or start times available");
            return;
        }

        int displayedCourts = 0; // Đếm số sân được hiển thị
        int totalCellsDisplayed = 0; // Tổng số ô được hiển thị
        String lastTime = allTimesCurrentDay.get(allTimesCurrentDay.size() - 1); // endTime cuối cùng

        for (CourtSlot court : courtSlots) {


            displayedCourts++;
            TableRow row = new TableRow(this);
            row.addView(createCell(court.getCourtSlotName(), false, true));

            Map<String, BookingSlot> slotMap = new HashMap<>();
            for (BookingSlot slot : court.getBookingSlots()) {
                slotMap.put(slot.getStartTime(), slot);
            }

            int cellsInRow = 0; // Đếm số ô trong hàng này
            for (int i = 0; i < allTimesCurrentDay.size(); i++) {
                String time = allTimesCurrentDay.get(i);
                // Không vẽ ô cho endTime cuối cùng
                if (time.equals(lastTime)) {
                    continue;
                }

                TextView cell = createCell("", false, false);
                cell.setBackground(createCellBackground());
                cellsInRow++;
                totalCellsDisplayed++;

                // Nếu là startTime, vẽ ô tương tác
                if (startTimesCurrentDay.contains(time)) {
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
                } else {
                    // Ô trắng cho khoảng ngắt quãng, khóa không cho chọn
                    setCellColor(cell, Color.parseColor("#b2b2b2"));
                    cell.setOnClickListener(null);
                }
                row.addView(cell);
            }
            tableLayout.addView(row, new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
            ));

            // Kiểm tra số ô trong hàng
            int expectedCellsPerRow = allTimesCurrentDay.size() - 1; // Trừ endTime cuối
            if (cellsInRow != expectedCellsPerRow) {
                Log.w("BookingTableActivity", "Row for court " + court.getCourtSlotName() + " has " + cellsInRow +
                        " cells, expected " + expectedCellsPerRow);
            }
        }

        // Kiểm tra tổng số ô
        int expectedTotalCells = displayedCourts * (allTimesCurrentDay.size() - 1);
        if (totalCellsDisplayed != expectedTotalCells) {
            Log.w("BookingTableActivity", "Total cells displayed: " + totalCellsDisplayed +
                    ", expected: " + expectedTotalCells);
        } else {
            Log.d("BookingTableActivity", "Total cells displayed matches expected: " + totalCellsDisplayed);
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
        tv.setSingleLine(true);
        tv.setEllipsize(TextUtils.TruncateAt.END);
        if (isHeader && !isFirstColumn) {
            tv.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        } else {
            tv.setGravity(Gravity.CENTER);
        }

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
                // Sử dụng NumberFormat với Locale Việt Nam
                NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                // Định dạng giá mà không cần ép kiểu int, đảm bảo giá trị chính xác
                String formattedPrice = formatter.format(order.getDailyPrice());
                String info = "Date: " + order.getDayBooking()
                        + ", Court: " + order.getCourtSlotName()
                        + ", Time slot: " + order.getStartTime().substring(0, 5)
                        + " - " + order.getEndTime().substring(0, 5)
                        + ", Price: " + formattedPrice;
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
            Toast.makeText(this, "Please select at least one timeslot!", Toast.LENGTH_SHORT).show();
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