package SEP490.G9;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import Api.ApiService;
import Api.RetrofitClient;
import Model.CheckInvalidSlotsRequest;
import Model.CheckInvalidSlotsResponse;
import retrofit2.Call;

public class BookingRegularTableActivity extends AppCompatActivity {
    private String courtId;
    private List<String> selectedDays = new ArrayList<>();
    private String[] daysOfWeekVietnamese = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ Nhật"};
    private String[] daysOfWeekEnglish = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};
    private String startDate, endDate, startTime, endTime;
    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private ApiService apiService;
    private LinearLayout resultContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_booking_regular_table);

        courtId = getIntent().getStringExtra("club_id");
        if (courtId == null) {
            Toast.makeText(this, "Không có club_id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService = RetrofitClient.getApiService(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnSelectDays = findViewById(R.id.btn_select_days);
        Button btnStartDate = findViewById(R.id.btn_start_date);
        Button btnEndDate = findViewById(R.id.btn_end_date);
        Button btnStartTime = findViewById(R.id.btn_start_time);
        Button btnEndTime = findViewById(R.id.btn_end_time);
        Button btnSubmit = findViewById(R.id.btn_submit);
        resultContainer = findViewById(R.id.result_container);

        btnSelectDays.setOnClickListener(v -> showDaySelectionDialog());

        btnStartDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        startDate = dateFormat.format(calendar.getTime());
                        btnStartDate.setText("Ngày bắt đầu: " + startDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });

        btnEndDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        endDate = dateFormat.format(calendar.getTime());
                        btnEndDate.setText("Ngày kết thúc: " + endDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            if (startDate != null) {
                try {
                    Calendar minDate = Calendar.getInstance();
                    minDate.setTime(dateFormat.parse(startDate));
                    datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            datePickerDialog.show();
        });

        btnStartTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        startTime = timeFormat.format(calendar.getTime());
                        btnStartTime.setText("Giờ bắt đầu: " + startTime);
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
            );
            timePickerDialog.show();
        });

        btnEndTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        endTime = timeFormat.format(calendar.getTime());
                        btnEndTime.setText("Giờ kết thúc: " + endTime);
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
            );
            timePickerDialog.show();
        });

        btnSubmit.setOnClickListener(v -> {
            if (selectedDays.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất một ngày trong tuần", Toast.LENGTH_SHORT).show();
                return;
            }
            if (startDate == null || endDate == null || startTime == null || endTime == null) {
                Toast.makeText(this, "Vui lòng chọn đầy đủ ngày và giờ", Toast.LENGTH_SHORT).show();
                return;
            }
            String daysOfWeek = String.join(",", selectedDays);
            checkInvalidSlots(courtId, daysOfWeek, startDate, endDate, startTime, endTime);
        });
    }

    private void showDaySelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn ngày trong tuần");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        List<CheckBox> checkBoxes = new ArrayList<>();
        for (String day : daysOfWeekVietnamese) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(day);
            checkBoxes.add(checkBox);
            layout.addView(checkBox);
        }
        builder.setView(layout);

        builder.setPositiveButton("OK", (dialog, which) -> {
            selectedDays.clear();
            for (int i = 0; i < checkBoxes.size(); i++) {
                if (checkBoxes.get(i).isChecked()) {
                    selectedDays.add(daysOfWeekEnglish[i]);
                }
            }
            Toast.makeText(this, "Đã chọn: " + String.join(", ", selectedDays), Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void checkInvalidSlots(String courtId, String daysOfWeek, String startDate, String endDate, String startTime, String endTime) {
        CheckInvalidSlotsRequest request = new CheckInvalidSlotsRequest(courtId, daysOfWeek, startDate, endDate, startTime, endTime);
        Call<CheckInvalidSlotsResponse> call = apiService.checkInvalidSlots(request);
        call.enqueue(new retrofit2.Callback<CheckInvalidSlotsResponse>() {
            @Override
            public void onResponse(Call<CheckInvalidSlotsResponse> call, retrofit2.Response<CheckInvalidSlotsResponse> response) {
                if (response.isSuccessful()) {
                    CheckInvalidSlotsResponse responseData = response.body();
                    handleApiResponse(responseData);
                } else {
                    Toast.makeText(BookingRegularTableActivity.this, "Lỗi API: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CheckInvalidSlotsResponse> call, Throwable t) {
                Toast.makeText(BookingRegularTableActivity.this, "Lỗi khi gọi API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleApiResponse(CheckInvalidSlotsResponse response) {
        resultContainer.removeAllViews();
        if (response.getInvalidCourtSlots().isEmpty()) {
            showAutoBooking(response.getAvailableCourtSlots());
        } else {
            showOptimalBooking(response.getInvalidCourtSlots(), response.getAvailableCourtSlots());
        }
    }

    private void showAutoBooking(List<String> availableCourtSlots) {
        LinearLayout courtLayout = new LinearLayout(this);
        courtLayout.setOrientation(LinearLayout.VERTICAL);
        List<String> selectedCourtSlots = new ArrayList<>();
        for (String court : availableCourtSlots) {
            Button courtButton = new Button(this);
            courtButton.setText(court);
            courtButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.darker_gray));
            courtButton.setTextColor(getResources().getColor(android.R.color.black));
            courtButton.setOnClickListener(v -> {
                if (selectedCourtSlots.contains(court)) {
                    selectedCourtSlots.remove(court);
                    courtButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.darker_gray));
                } else {
                    selectedCourtSlots.add(court);
                    courtButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_green_dark));
                }
            });
            courtLayout.addView(courtButton);
        }
        resultContainer.addView(courtLayout);

        Button btnAutoBooking = new Button(this);
        btnAutoBooking.setText("Đặt lịch tự động");
        btnAutoBooking.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_green_dark));
        btnAutoBooking.setTextColor(getResources().getColor(android.R.color.white));
        resultContainer.addView(btnAutoBooking);

        btnAutoBooking.setOnClickListener(v -> {
            if (selectedCourtSlots.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất một sân", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(BookingRegularTableActivity.this, ConfirmRegularActivity.class);
            intent.putExtra("courtId", courtId);
            intent.putStringArrayListExtra("selectedDays", new ArrayList<>(selectedDays));
            intent.putExtra("startDate", startDate);
            intent.putExtra("endDate", endDate);
            intent.putExtra("startTime", startTime + ":00");
            intent.putExtra("endTime", endTime + ":00");
            intent.putStringArrayListExtra("selectedCourtSlots", new ArrayList<>(selectedCourtSlots));
            startActivity(intent);
        });
    }

    private void showOptimalBooking(Map<String, Object> invalidCourtSlots, List<String> availableCourtSlots) {
        Button btnOptimalBooking = new Button(this);
        btnOptimalBooking.setText("Đặt lịch tối ưu");
        btnOptimalBooking.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_orange_light));
        btnOptimalBooking.setTextColor(getResources().getColor(android.R.color.black));
        resultContainer.addView(btnOptimalBooking);

        TextView bookedCourtsTitle = new TextView(this);
        bookedCourtsTitle.setText("Sân đã được đặt");
        bookedCourtsTitle.setTextSize(18);
        bookedCourtsTitle.setTextColor(getResources().getColor(android.R.color.black));
        resultContainer.addView(bookedCourtsTitle);

        Map<String, String> flexibleCourtSlotFixes = new HashMap<>();
        for (Map.Entry<String, Object> entry : invalidCourtSlots.entrySet()) {
            String courtName = entry.getKey();
            List<String> invalidDates = (List<String>) entry.getValue();

            TextView courtText = new TextView(this);
            courtText.setText(courtName);
            courtText.setTextSize(16);
            resultContainer.addView(courtText);

            LinearLayout datesLayout = new LinearLayout(this);
            datesLayout.setOrientation(LinearLayout.VERTICAL);
            for (String date : invalidDates) {
                Button dateButton = new Button(this);
                dateButton.setText(date);
                dateButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_red_dark));
                dateButton.setTextColor(getResources().getColor(android.R.color.white));
                dateButton.setOnClickListener(v -> showReplacementDialog(date, availableCourtSlots, flexibleCourtSlotFixes));
                datesLayout.addView(dateButton);
            }
            resultContainer.addView(datesLayout);
        }

        TextView availableCourtsTitle = new TextView(this);
        availableCourtsTitle.setText("Các sân có thể đặt");
        availableCourtsTitle.setTextSize(18);
        availableCourtsTitle.setTextColor(getResources().getColor(android.R.color.black));
        resultContainer.addView(availableCourtsTitle);

        LinearLayout availableLayout = new LinearLayout(this);
        availableLayout.setOrientation(LinearLayout.VERTICAL);
        for (String court : availableCourtSlots) {
            TextView courtText = new TextView(this);
            courtText.setText(court);
            courtText.setTextSize(16);
            courtText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            availableLayout.addView(courtText);
        }
        resultContainer.addView(availableLayout);

        btnOptimalBooking.setOnClickListener(v -> {
            Intent intent = new Intent(BookingRegularTableActivity.this, ConfirmRegularActivity.class);
            intent.putExtra("courtId", courtId);
            intent.putStringArrayListExtra("selectedDays", new ArrayList<>(selectedDays));
            intent.putExtra("startDate", startDate);
            intent.putExtra("endDate", endDate);
            intent.putExtra("startTime", startTime + ":00");
            intent.putExtra("endTime", endTime + ":00");
            intent.putStringArrayListExtra("selectedCourtSlots", new ArrayList<>(availableCourtSlots));
            intent.putExtra("flexibleCourtSlotFixes", new Gson().toJson(flexibleCourtSlotFixes));
            startActivity(intent);
        });
    }

    private void showReplacementDialog(String date, List<String> availableCourtSlots, Map<String, String> flexibleCourtSlotFixes) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Bạn có thể thay thế ngày " + date);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        List<Button> courtButtons = new ArrayList<>();
        for (String court : availableCourtSlots) {
            Button courtButton = new Button(this);
            courtButton.setText(court);
            courtButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_green_dark));
            courtButton.setTextColor(getResources().getColor(android.R.color.white));
            courtButton.setOnClickListener(v -> {
                flexibleCourtSlotFixes.put(date, court);
                Toast.makeText(this, "Đã chọn " + court + " cho ngày " + date, Toast.LENGTH_SHORT).show();
            });
            courtButtons.add(courtButton);
            layout.addView(courtButton);
        }
        builder.setView(layout);

        builder.setPositiveButton("OK", null);
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }
}