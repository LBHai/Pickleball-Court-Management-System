package SEP490.G9;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;

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
import retrofit2.Callback;
import retrofit2.Response;

public class BookingRegularTableActivity extends AppCompatActivity {
    private static final String TAG = "BookingRegularTableActivity";
    private String courtId;
    private String startDate, endDate, startTime, endTime;
    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private ApiService apiService;
    private LinearLayout resultContainer;
    private MaterialCheckBox cbMonday, cbTuesday, cbWednesday, cbThursday, cbFriday, cbSaturday, cbSunday;
    private Map<String, String> flexibleCourtSlotFixes = new HashMap<>();
    private Map<String, TextView> dateReplacementTextViews = new HashMap<>();
    private List<String> selectedCourts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_booking_regular_table);

        apiService = RetrofitClient.getApiService(this);

        courtId = getIntent().getStringExtra("club_id");
        if (courtId == null) {
            Log.e(TAG, "onCreate: Không có club_id trong Intent");
            Toast.makeText(this, "Không có club_id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Log.d(TAG, "onCreate: Nhận được club_id = " + courtId);

        // Khởi tạo các thành phần UI
        cbMonday = findViewById(R.id.cbMonday);
        cbTuesday = findViewById(R.id.cbTuesday);
        cbWednesday = findViewById(R.id.cbWednesday);
        cbThursday = findViewById(R.id.cbThursday);
        cbFriday = findViewById(R.id.cbFriday);
        cbSaturday = findViewById(R.id.cbSaturday);
        cbSunday = findViewById(R.id.cbSunday);

        MaterialButton btnStartDate = findViewById(R.id.btnStartDate);
        MaterialButton btnEndDate = findViewById(R.id.btnEndDate);
        MaterialButton btnStartTime = findViewById(R.id.btnStartTime);
        MaterialButton btnEndTime = findViewById(R.id.btnEndTime);
        MaterialButton btnCheckAvailability = findViewById(R.id.btnCheckAvailability);
        resultContainer = findViewById(R.id.resultContainer);

        // Sự kiện nhấn nút chọn ngày bắt đầu
        btnStartDate.setOnClickListener(v -> {
            Log.d(TAG, "btnStartDate: Mở DatePickerDialog");
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        startDate = dateFormat.format(calendar.getTime());
                        btnStartDate.setText("Ngày bắt đầu: " + startDate);
                        Log.d(TAG, "btnStartDate: Đã chọn ngày bắt đầu = " + startDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });

        // Sự kiện nhấn nút chọn ngày kết thúc
        btnEndDate.setOnClickListener(v -> {
            Log.d(TAG, "btnEndDate: Mở DatePickerDialog");
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        endDate = dateFormat.format(calendar.getTime());
                        btnEndDate.setText("Ngày kết thúc: " + endDate);
                        Log.d(TAG, "btnEndDate: Đã chọn ngày kết thúc = " + endDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });

        // Sự kiện nhấn nút chọn giờ bắt đầu
        btnStartTime.setOnClickListener(v -> {
            Log.d(TAG, "btnStartTime: Mở TimePickerDialog");
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        startTime = timeFormat.format(calendar.getTime());
                        btnStartTime.setText("Giờ bắt đầu: " + startTime);
                        Log.d(TAG, "btnStartTime: Đã chọn giờ bắt đầu = " + startTime);
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true);
            timePickerDialog.show();
        });

        // Sự kiện nhấn nút chọn giờ kết thúc
        btnEndTime.setOnClickListener(v -> {
            Log.d(TAG, "btnEndTime: Mở TimePickerDialog");
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        endTime = timeFormat.format(calendar.getTime());
                        btnEndTime.setText("Giờ kết thúc: " + endTime);
                        Log.d(TAG, "btnEndTime: Đã chọn giờ kết thúc = " + endTime);
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true);
            timePickerDialog.show();
        });

        // Sự kiện nhấn nút kiểm tra lịch trống
        btnCheckAvailability.setOnClickListener(v -> {
            Log.d(TAG, "btnCheckAvailability: Nhấn nút kiểm tra lịch trống");
            checkAvailability();
        });
    }

    private void checkAvailability() {
        List<String> selectedDaysEnglish = getSelectedDays();
        String daysOfWeek = String.join(",", selectedDaysEnglish);
        Log.d(TAG, "checkAvailability: Ngày trong tuần đã chọn = " + daysOfWeek);

        if (daysOfWeek.isEmpty() || startDate == null || endDate == null || startTime == null || endTime == null) {
            Log.w(TAG, "checkAvailability: Thiếu thông tin, không thể kiểm tra");
            Toast.makeText(this, "Vui lòng chọn đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        CheckInvalidSlotsRequest request = new CheckInvalidSlotsRequest(courtId, daysOfWeek, startDate, endDate, startTime, endTime);
        Log.d(TAG, "checkAvailability: Gửi yêu cầu API với courtId = " + courtId + ", daysOfWeek = " + daysOfWeek + ", startDate = " + startDate + ", endDate = " + endDate + ", startTime = " + startTime + ", endTime = " + endTime);

        Call<CheckInvalidSlotsResponse> call = apiService.checkInvalidSlots(request);
        call.enqueue(new Callback<CheckInvalidSlotsResponse>() {
            @Override
            public void onResponse(Call<CheckInvalidSlotsResponse> call, Response<CheckInvalidSlotsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "onResponse: Nhận phản hồi thành công từ API");
                    handleCheckInvalidSlotsResponse(response.body());
                } else {
                    Log.e(TAG, "onResponse: Phản hồi không thành công hoặc body null, mã lỗi = " + response.code());
                    Toast.makeText(BookingRegularTableActivity.this, "Lỗi khi kiểm tra lịch trống", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CheckInvalidSlotsResponse> call, Throwable t) {
                Log.e(TAG, "onFailure: Lỗi mạng khi gọi API, chi tiết: " + t.getMessage());
                Toast.makeText(BookingRegularTableActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleCheckInvalidSlotsResponse(CheckInvalidSlotsResponse res) {
        Log.d(TAG, "handleCheckInvalidSlotsResponse: Xử lý phản hồi từ API");
        resultContainer.removeAllViews();
        resultContainer.setVisibility(View.VISIBLE);
        selectedCourts.clear(); // Reset danh sách sân đã chọn

        if (res.getInvalidCourtSlots().isEmpty()) {
            if (res.getAvailableCourtSlots().isEmpty()) {
                Log.w(TAG, "handleCheckInvalidSlotsResponse: Không có sân nào khả dụng");
                Toast.makeText(this, "Không có sân nào khả dụng cho khoảng thời gian đã chọn", Toast.LENGTH_LONG).show();
                resultContainer.setVisibility(View.GONE);
                return;
            }
            Log.d(TAG, "handleCheckInvalidSlotsResponse: Không có xung đột, hiển thị tùy chọn đặt lịch tự động");

            // **Tạo header với icon và text (bỏ background)**
            LinearLayout headerLayout = new LinearLayout(this);
            headerLayout.setOrientation(LinearLayout.HORIZONTAL);
            headerLayout.setPadding(16, 16, 16, 16);

            ImageView ivCheck = new ImageView(this);
            ivCheck.setImageResource(R.drawable.check); // Giả sử icon checkmark có sẵn trong drawable
            headerLayout.addView(ivCheck);

            TextView tvHeader = new TextView(this);
            tvHeader.setText("Sân có sẵn (Có thể chọn nhiều):");
            tvHeader.setTextSize(20); // Tăng kích thước chữ
            tvHeader.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            headerLayout.addView(tvHeader);

            resultContainer.addView(headerLayout);

            // **Hiển thị các sân có sẵn dưới dạng box**
            LinearLayout llCourts = new LinearLayout(this);
            llCourts.setOrientation(LinearLayout.VERTICAL);

            List<String> availableCourts = res.getAvailableCourtSlots();

            for (String court : availableCourts) {
                CardView cardView = new CardView(this);
                cardView.setCardElevation(4);
                cardView.setRadius(8); // Góc bo tròn 8dp
                cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.light_blue)); // Màu mặc định
                cardView.setTag(court);

                // Thiết lập layout params để ô sân chiếm 90% chiều rộng
                int screenWidth = getResources().getDisplayMetrics().widthPixels;
                int cardWidth = (int) (screenWidth * 0.9);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        cardWidth,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 0, 20); // Khoảng cách dọc 5dp giữa các ô
                cardView.setLayoutParams(params);

                TextView tvCourt = new TextView(this);
                tvCourt.setText(court);
                tvCourt.setTextColor(ContextCompat.getColor(this, android.R.color.black));
                tvCourt.setTextSize(18); // Tăng kích thước chữ
                tvCourt.setGravity(android.view.Gravity.CENTER); // Căn giữa văn bản
                tvCourt.setPadding(16, 16, 16, 16); // Padding bên trong ô
                cardView.addView(tvCourt);

                // Sự kiện nhấn để chọn/bỏ chọn sân
                cardView.setOnClickListener(v -> {
                    CardView cv = (CardView) v;
                    String selectedCourt = (String) v.getTag();
                    if (selectedCourts.contains(selectedCourt)) {
                        selectedCourts.remove(selectedCourt);
                        cv.setCardBackgroundColor(ContextCompat.getColor(this, R.color.light_blue)); // Màu khi bỏ chọn
                        Log.d(TAG, "CardView clicked: Bỏ chọn sân " + selectedCourt);
                    } else {
                        selectedCourts.add(selectedCourt);
                        cv.setCardBackgroundColor(ContextCompat.getColor(this, R.color.light_green)); // Màu khi chọn
                        Log.d(TAG, "CardView clicked: Chọn sân " + selectedCourt);
                    }
                });

                llCourts.addView(cardView);
            }
            resultContainer.addView(llCourts);

            // **Nút "Đặt lịch tự động"**
            MaterialButton btnBookAuto = new MaterialButton(this);
            btnBookAuto.setText("Đặt lịch tự động");
            btnBookAuto.setTextSize(18); // Tăng kích thước chữ
            btnBookAuto.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_green_dark));
            btnBookAuto.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            btnBookAuto.setCornerRadius(8);
            btnBookAuto.setPadding(16, 16, 16, 16);
            resultContainer.addView(btnBookAuto);

            btnBookAuto.setOnClickListener(v -> {
                Log.d(TAG, "btnBookAuto: Nhấn nút Đặt lịch tự động");
                if (selectedCourts.isEmpty()) {
                    Toast.makeText(this, "Vui lòng chọn ít nhất một sân", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(this, ConfirmActivity.class);
                intent.putExtra("courtId", courtId);
                intent.putExtra("selectedDays", String.join(",", getSelectedDays()));
                intent.putExtra("startDate", startDate);
                intent.putExtra("endDate", endDate);
                intent.putExtra("startTime", startTime);
                intent.putExtra("endTime", endTime);
                intent.putStringArrayListExtra("selectedCourtSlots", new ArrayList<>(selectedCourts));
                intent.putExtra("orderType", "Đơn cố định");
                startActivity(intent);
            });
        } else {
            Log.d(TAG, "handleCheckInvalidSlotsResponse: Có xung đột, hiển thị tùy chọn đặt lịch tối ưu");

            TextView tvConflictingCourts = new TextView(this);
            tvConflictingCourts.setText("Sân đã được đặt:");
            tvConflictingCourts.setTextSize(20); // Tăng kích thước chữ
            tvConflictingCourts.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            tvConflictingCourts.setPadding(0, 16, 0, 8);
            resultContainer.addView(tvConflictingCourts);

            for (Map.Entry<String, Object> entry : res.getInvalidCourtSlots().entrySet()) {
                String court = entry.getKey();
                List<String> dates = (List<String>) entry.getValue();

                TextView tvCourt = new TextView(this);
                tvCourt.setText(court);
                tvCourt.setTextSize(18); // Tăng kích thước chữ
                tvCourt.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                tvCourt.setPadding(0, 8, 0, 4);
                resultContainer.addView(tvCourt);

                for (String date : dates) {
                    LinearLayout llDate = new LinearLayout(this);
                    llDate.setOrientation(LinearLayout.HORIZONTAL);
                    TextView tvDate = new TextView(this);
                    tvDate.setText("  - " + date);
                    tvDate.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                    llDate.addView(tvDate);

                    TextView tvReplacement = new TextView(this);
                    tvReplacement.setText(" (Thay thế: Chưa chọn)");
                    tvReplacement.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
                    llDate.addView(tvReplacement);
                    dateReplacementTextViews.put(date, tvReplacement);

                    resultContainer.addView(llDate);
                    tvDate.setOnClickListener(v -> {
                        Log.d(TAG, "tvDate: Nhấn vào ngày " + date + " để thay thế sân");
                        showAlternativeDialog(date, res.getAvailableCourtSlots(), court);
                    });
                }
            }

            // **Tạo header với icon và text cho sân có sẵn (bỏ background)**
            LinearLayout headerLayout = new LinearLayout(this);
            headerLayout.setOrientation(LinearLayout.HORIZONTAL);
            headerLayout.setPadding(16, 16, 16, 16);

            ImageView ivCheck = new ImageView(this);
            ivCheck.setImageResource(R.drawable.checked);
            headerLayout.addView(ivCheck);

            TextView tvHeader = new TextView(this);
            tvHeader.setText("Sân có sẵn (Có thể chọn nhiều):");
            tvHeader.setTextSize(20); // Tăng kích thước chữ
            tvHeader.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            headerLayout.addView(tvHeader);

            resultContainer.addView(headerLayout);

            // **Hiển thị các sân có sẵn dưới dạng box**
            LinearLayout llCourts = new LinearLayout(this);
            llCourts.setOrientation(LinearLayout.VERTICAL);
            List<String> allCourts = new ArrayList<>(res.getAvailableCourtSlots());
            for (String court : res.getInvalidCourtSlots().keySet()) {
                if (!allCourts.contains(court)) allCourts.add(court);
            }

            for (String court : allCourts) {
                CardView cardView = new CardView(this);
                cardView.setCardElevation(4);
                cardView.setRadius(8); // Góc bo tròn 8dp
                cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.light_blue)); // Màu mặc định
                cardView.setTag(court);

                // Thiết lập layout params để ô sân chiếm 90% chiều rộng
                int screenWidth = getResources().getDisplayMetrics().widthPixels;
                int cardWidth = (int) (screenWidth * 0.9);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        cardWidth,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 0, 5); // Khoảng cách dọc 5dp giữa các ô
                cardView.setLayoutParams(params);

                TextView tvCourt = new TextView(this);
                tvCourt.setText(court);
                tvCourt.setTextColor(ContextCompat.getColor(this, android.R.color.black));
                tvCourt.setTextSize(18); // Tăng kích thước chữ
                tvCourt.setGravity(android.view.Gravity.CENTER); // Căn giữa văn bản
                tvCourt.setPadding(16, 16, 16, 16); // Padding bên trong ô
                cardView.addView(tvCourt);

                // Sự kiện nhấn để chọn/bỏ chọn sân
                cardView.setOnClickListener(v -> {
                    CardView cv = (CardView) v;
                    String selectedCourt = (String) v.getTag();
                    if (selectedCourts.contains(selectedCourt)) {
                        selectedCourts.remove(selectedCourt);
                        cv.setCardBackgroundColor(ContextCompat.getColor(this, R.color.gray)); // Màu khi bỏ chọn
                        Log.d(TAG, "CardView clicked: Bỏ chọn sân " + selectedCourt);
                    } else {
                        selectedCourts.add(selectedCourt);
                        cv.setCardBackgroundColor(ContextCompat.getColor(this, R.color.light_green)); // Màu khi chọn
                        Log.d(TAG, "CardView clicked: Chọn sân " + selectedCourt);
                    }
                });

                llCourts.addView(cardView);
            }
            resultContainer.addView(llCourts);

            // **Nút "Đặt lịch tối ưu"**
            MaterialButton btnOptimizeBook = new MaterialButton(this);
            btnOptimizeBook.setText("Đặt lịch tối ưu");
            btnOptimizeBook.setTextSize(18); // Tăng kích thước chữ
            btnOptimizeBook.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_orange_dark));
            btnOptimizeBook.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            btnOptimizeBook.setCornerRadius(8);
            btnOptimizeBook.setPadding(16, 16, 16, 16);
            resultContainer.addView(btnOptimizeBook);

            btnOptimizeBook.setOnClickListener(v -> {
                Log.d(TAG, "btnOptimizeBook: Nhấn nút Đặt lịch tối ưu");
                if (selectedCourts.isEmpty()) {
                    Toast.makeText(this, "Vui lòng chọn ít nhất một sân", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(BookingRegularTableActivity.this, ConfirmActivity.class);
                intent.putExtra("courtId", courtId);
                intent.putExtra("selectedDays", String.join(",", getSelectedDays()));
                intent.putExtra("startDate", startDate);
                intent.putExtra("endDate", endDate);
                intent.putExtra("startTime", startTime + ":00");
                intent.putExtra("endTime", endTime + ":00");
                intent.putStringArrayListExtra("selectedCourtSlots", new ArrayList<>(selectedCourts));
                intent.putExtra("flexibleCourtSlotFixes", new HashMap<>(flexibleCourtSlotFixes));
                intent.putExtra("orderType", "Đơn cố định");
                startActivity(intent);
            });
        }
    }

    private void showAlternativeDialog(String date, List<String> availableCourts, String selectedCourt) {
        Log.d(TAG, "showAlternativeDialog: Mở dialog thay thế cho ngày " + date);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn sân thay thế cho ngày " + date);

        RadioGroup rgAlternatives = new RadioGroup(this);
        rgAlternatives.setOrientation(RadioGroup.VERTICAL);
        rgAlternatives.setPadding(16, 16, 16, 16);

        for (String court : availableCourts) {
            RadioButton rb = new RadioButton(this);
            rb.setText(court);
            rb.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            rb.setPadding(8, 8, 8, 8);
            rgAlternatives.addView(rb);
        }

        builder.setView(rgAlternatives);
        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            int selectedId = rgAlternatives.getCheckedRadioButtonId();
            if (selectedId != -1) {
                RadioButton selectedRb = rgAlternatives.findViewById(selectedId);
                String chosenCourt = selectedRb.getText().toString();
                String replacementText = " (Thay thế: " + chosenCourt + ")";
                dateReplacementTextViews.get(date).setText(replacementText);
                flexibleCourtSlotFixes.put(date, chosenCourt);
                Log.d(TAG, "showAlternativeDialog: Đã chọn sân thay thế cho ngày " + date + ": " + chosenCourt);
            } else {
                Log.w(TAG, "showAlternativeDialog: Không có sân nào được chọn cho ngày " + date);
                Toast.makeText(this, "Vui lòng chọn một sân thay thế", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private List<String> getSelectedDays() {
        List<String> selectedDaysEnglish = new ArrayList<>();
        if (cbMonday.isChecked()) selectedDaysEnglish.add("MONDAY");
        if (cbTuesday.isChecked()) selectedDaysEnglish.add("TUESDAY");
        if (cbWednesday.isChecked()) selectedDaysEnglish.add("WEDNESDAY");
        if (cbThursday.isChecked()) selectedDaysEnglish.add("THURSDAY");
        if (cbFriday.isChecked()) selectedDaysEnglish.add("FRIDAY");
        if (cbSaturday.isChecked()) selectedDaysEnglish.add("SATURDAY");
        if (cbSunday.isChecked()) selectedDaysEnglish.add("SUNDAY");
        Log.d(TAG, "getSelectedDays: Ngày đã chọn = " + selectedDaysEnglish);
        return selectedDaysEnglish;
    }
}