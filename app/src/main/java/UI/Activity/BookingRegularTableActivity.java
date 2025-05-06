package UI.Activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import Data.Network.ApiService;
import Data.Network.RetrofitClient;
import Data.Model.CheckInvalidSlotsRequest;
import Data.Model.CheckInvalidSlotsResponse;
import SEP490.G9.R;
import UI.Component.TimePickerDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingRegularTableActivity extends AppCompatActivity {
    private static final String TAG = "BookingRegularTableActivity";
    private String courtId, tvPhone;
    private String startDate, endDate, startTime, endTime;
    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private ApiService apiService;
    private LinearLayout resultContainer;
    private MaterialCheckBox cbMonday, cbTuesday, cbWednesday, cbThursday, cbFriday, cbSaturday, cbSunday;
    private Map<String, String> flexibleCourtSlotFixes = new HashMap<>();
    private Map<String, TextView> courtDateReplacementTextViews = new HashMap<>();
    private List<String> selectedCourtSlots = new ArrayList<>();
    private List<String> selectedCourts = new ArrayList<>();
    private MaterialButton btnStartTime, btnEndTime;
    private CheckInvalidSlotsResponse currentResponse;
    private Map<String, String> selectedReplacementPerDate = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_regular_table);

        apiService = RetrofitClient.getApiService(this);

        courtId = getIntent().getStringExtra("club_id");
        tvPhone = getIntent().getStringExtra("tvPhone");

        if (courtId == null) {
            Log.e(TAG, "onCreate: Không có club_id trong Intent");
            //Toast.makeText(this, "Không có club_id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        cbMonday = findViewById(R.id.cbMonday);
        cbTuesday = findViewById(R.id.cbTuesday);
        cbWednesday = findViewById(R.id.cbWednesday);
        cbThursday = findViewById(R.id.cbThursday);
        cbFriday = findViewById(R.id.cbFriday);
        cbSaturday = findViewById(R.id.cbSaturday);
        cbSunday = findViewById(R.id.cbSunday);

        MaterialButton btnStartDate = findViewById(R.id.btnStartDate);
        MaterialButton btnEndDate = findViewById(R.id.btnEndDate);
        btnStartTime = findViewById(R.id.btnStartTime);
        btnEndTime = findViewById(R.id.btnEndTime);
        resultContainer = findViewById(R.id.resultContainer);

        // Listener for date and time selections
        btnStartDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        startDate = dateFormat.format(calendar.getTime());
                        btnStartDate.setText(getString(R.string.start_date_label) + startDate);
                        tryCheckAvailability();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });

        btnEndDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        endDate = dateFormat.format(calendar.getTime());
                        btnEndDate.setText(getString(R.string.end_date_label)+ endDate);
                        tryCheckAvailability();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            // Set minimum date for endDate to be startDate if startDate is selected
            if (startDate != null) {
                try {
                    Date start = dateFormat.parse(startDate);
                    Calendar minDate = Calendar.getInstance();
                    minDate.setTime(start);
                    datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing startDate: " + e.getMessage());
                }
            }
            datePickerDialog.show();
        });

        btnStartTime.setOnClickListener(v -> {
            TimePickerDialog dialog = new TimePickerDialog(
                    BookingRegularTableActivity.this,
                    selectedTime -> {
                        startTime = selectedTime;
                        btnStartTime.setText(getString(R.string.start_time_label) + startTime);
                        tryCheckAvailability();
                    },
                    startTime,
                    true
            );
            dialog.show();
        });

        btnEndTime.setOnClickListener(v -> {
            TimePickerDialog dialog = new TimePickerDialog(
                    BookingRegularTableActivity.this,
                    selectedTime -> {
                        endTime = selectedTime;
                        btnEndTime.setText(getString(R.string.end_time_label) + endTime);
                        tryCheckAvailability();
                    },
                    endTime,
                    false
            );
            dialog.show();
        });

        // Listeners for day checkboxes
        View.OnClickListener checkBoxListener = v -> tryCheckAvailability();
        cbMonday.setOnClickListener(checkBoxListener);
        cbTuesday.setOnClickListener(checkBoxListener);
        cbWednesday.setOnClickListener(checkBoxListener);
        cbThursday.setOnClickListener(checkBoxListener);
        cbFriday.setOnClickListener(checkBoxListener);
        cbSaturday.setOnClickListener(checkBoxListener);
        cbSunday.setOnClickListener(checkBoxListener);
    }

    private void tryCheckAvailability() {
        // Check if all required fields are filled
        if (startDate != null && endDate != null && startTime != null && endTime != null && !getSelectedDays().isEmpty()) {
            checkAvailability();
        }
    }

    private void checkAvailability() {
        List<String> selectedDaysEnglish = getSelectedDays();
        String daysOfWeek = String.join(", ", selectedDaysEnglish);

        // Validate dates
        try {
            Date start = dateFormat.parse(startDate);
            Date end = dateFormat.parse(endDate);
            if (start.after(end)) {
                Toast.makeText(this, getString(R.string.error_finish_before_start_date), Toast.LENGTH_SHORT).show();
                resultContainer.setVisibility(View.GONE);
                return;
            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error_invalid_date_format), Toast.LENGTH_SHORT).show();
            resultContainer.setVisibility(View.GONE);
            return;
        }

        // Validate times
        try {
            Date start = timeFormat.parse(startTime);
            Date end = timeFormat.parse(endTime);
            if (start.compareTo(end) >= 0) {
                Toast.makeText(this, getString(R.string.error_finish_before_start_time), Toast.LENGTH_SHORT).show();
                resultContainer.setVisibility(View.GONE);
                return;
            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error_invalid_time_format), Toast.LENGTH_SHORT).show();
            resultContainer.setVisibility(View.GONE);
            return;
        }

        CheckInvalidSlotsRequest request = new CheckInvalidSlotsRequest(courtId, daysOfWeek, startDate, endDate, startTime, endTime);
        Call<CheckInvalidSlotsResponse> call = apiService.checkInvalidSlots(request);
        call.enqueue(new Callback<CheckInvalidSlotsResponse>() {
            @Override
            public void onResponse(Call<CheckInvalidSlotsResponse> call, Response<CheckInvalidSlotsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentResponse = response.body();
                    handleCheckInvalidSlotsResponse(response.body());
                } else {
                    Toast.makeText(BookingRegularTableActivity.this, getString(R.string.toast_no_available_slots), Toast.LENGTH_SHORT).show();
                    resultContainer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<CheckInvalidSlotsResponse> call, Throwable t) {
                Toast.makeText(BookingRegularTableActivity.this, getString(R.string.toast_network_error) + t.getMessage(), Toast.LENGTH_SHORT).show();
                resultContainer.setVisibility(View.GONE);
            }
        });
    }

    private void handleCheckInvalidSlotsResponse(CheckInvalidSlotsResponse res) {
        resultContainer.removeAllViews();
        resultContainer.setVisibility(View.VISIBLE);
        selectedCourts.clear();
        flexibleCourtSlotFixes.clear();
        courtDateReplacementTextViews.clear();
        selectedCourtSlots.clear();
        selectedReplacementPerDate.clear();

        if (res.getInvalidCourtSlots().isEmpty()) {
            if (res.getAvailableCourtSlots().isEmpty()) {
                Toast.makeText(this, getString(R.string.toast_no_courts_available), Toast.LENGTH_LONG).show();
                resultContainer.setVisibility(View.GONE);
                return;
            }

            LinearLayout headerLayout = new LinearLayout(this);
            headerLayout.setOrientation(LinearLayout.HORIZONTAL);
            headerLayout.setPadding(16, 16, 16, 16);

            ImageView ivCheck = new ImageView(this);
            ivCheck.setImageResource(R.drawable.check);
            headerLayout.addView(ivCheck);

            TextView tvHeader = new TextView(this);
            tvHeader.setText(getString(R.string.header_available_courts));
            tvHeader.setTextSize(20);
            tvHeader.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            headerLayout.addView(tvHeader);

            resultContainer.addView(headerLayout);

            LinearLayout llCourts = new LinearLayout(this);
            llCourts.setOrientation(LinearLayout.VERTICAL);

            List<String> availableCourts = res.getAvailableCourtSlots();

            for (String court : availableCourts) {
                CardView cardView = new CardView(this);
                cardView.setCardElevation(4);
                cardView.setRadius(8);
                cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.light_blue));
                cardView.setTag(court);

                int screenWidth = getResources().getDisplayMetrics().widthPixels;
                int cardWidth = (int) (screenWidth * 0.9);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(cardWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 0, 20);
                cardView.setLayoutParams(params);

                TextView tvCourt = new TextView(this);
                tvCourt.setText(court);
                tvCourt.setTextColor(ContextCompat.getColor(this, android.R.color.black));
                tvCourt.setTextSize(18);
                tvCourt.setGravity(android.view.Gravity.CENTER);
                tvCourt.setPadding(16, 16, 16, 16);
                cardView.addView(tvCourt);

                cardView.setOnClickListener(v -> {
                    CardView cv = (CardView) v;
                    String selectedCourt = (String) v.getTag();
                    if (selectedCourts.contains(selectedCourt)) {
                        selectedCourts.remove(selectedCourt);
                        cv.setCardBackgroundColor(ContextCompat.getColor(this, R.color.light_blue));
                    } else {
                        selectedCourts.add(selectedCourt);
                        cv.setCardBackgroundColor(ContextCompat.getColor(this, R.color.light_green));
                    }
                });

                llCourts.addView(cardView);
            }
            resultContainer.addView(llCourts);

            MaterialButton btnBookFixed = new MaterialButton(this);
            btnBookFixed.setText(getString(R.string.btn_book_fixed));
            btnBookFixed.setTextSize(18);
            btnBookFixed.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_green_dark));
            btnBookFixed.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            btnBookFixed.setCornerRadius(8);
            btnBookFixed.setPadding(16, 16, 16, 16);
            resultContainer.addView(btnBookFixed);

            btnBookFixed.setOnClickListener(v -> {
                if (selectedCourts.isEmpty()) {
                    Toast.makeText(this, getString(R.string.prompt_select_at_least_one), Toast.LENGTH_SHORT).show();
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
                intent.putExtra("tvPhone", tvPhone);
                Gson gson = new Gson();
                String flexibleCourtSlotFixesJson = gson.toJson(new HashMap<String, String>());
                intent.putExtra("flexibleCourtSlotFixes", flexibleCourtSlotFixesJson);
                intent.putExtra("orderType", "Đơn cố định");
                startActivity(intent);
            });
        } else {
            TextView tvConflictingCourts = new TextView(this);
            tvConflictingCourts.setText(getString(R.string.court_booked_label));
            tvConflictingCourts.setTextSize(20);
            tvConflictingCourts.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            tvConflictingCourts.setPadding(0, 16, 0, 8);
            resultContainer.addView(tvConflictingCourts);

            for (Map.Entry<String, Object> entry : res.getInvalidCourtSlots().entrySet()) {
                String court = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof List<?>) {
                    List<?> list = (List<?>) value;
                    if (list.isEmpty() || list.get(0) instanceof String) {
                        List<String> dates = (List<String>) list;

                        TextView tvCourt = new TextView(this);
                        tvCourt.setText(court);
                        tvCourt.setTextSize(18);
                        tvCourt.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                        tvCourt.setPadding(0, 8, 0, 4);
                        resultContainer.addView(tvCourt);

                        for (String date : dates) {
                            String courtDateKey = court + "_" + date;

                            LinearLayout llCourtDate = new LinearLayout(this);
                            llCourtDate.setOrientation(LinearLayout.HORIZONTAL);
                            llCourtDate.setPadding(16, 0, 0, 0);

                            TextView tvCourtDate = new TextView(this);
                            tvCourtDate.setText(getString(R.string._day_label) + date);
                            tvCourtDate.setTextSize(16);
                            tvCourtDate.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                            llCourtDate.addView(tvCourtDate);

                            TextView tvReplacementLabel = new TextView(this);
                            tvReplacementLabel.setText(getString(R.string.replace_label));
                            tvReplacementLabel.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
                            llCourtDate.addView(tvReplacementLabel);

                            TextView tvReplacement = new TextView(this);
                            if (flexibleCourtSlotFixes.containsKey(date)) {
                                tvReplacement.setText(flexibleCourtSlotFixes.get(date));
                            } else {
                                tvReplacement.setText(getString(R.string.checkbox_none));
                            }
                            tvReplacement.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
                            llCourtDate.addView(tvReplacement);
                            courtDateReplacementTextViews.put(courtDateKey, tvReplacement);

                            resultContainer.addView(llCourtDate);

                            tvReplacement.setOnClickListener(v -> {
                                List<String> availableCourtsForDate = getAvailableCourtsForDate(res, date);
                                showAlternativeDialog(court, date, availableCourtsForDate, courtDateKey);
                            });
                        }
                    }
                }
            }

            LinearLayout headerLayout = new LinearLayout(this);
            headerLayout.setOrientation(LinearLayout.HORIZONTAL);
            headerLayout.setPadding(16, 16, 16, 16);

            ImageView ivCheck = new ImageView(this);
            ivCheck.setImageResource(R.drawable.check);
            headerLayout.addView(ivCheck);

            TextView tvHeader = new TextView(this);
            tvHeader.setText(getString(R.string.header_available_courts));
            tvHeader.setTextSize(20);
            tvHeader.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            headerLayout.addView(tvHeader);

            resultContainer.addView(headerLayout);

            LinearLayout llCourts = new LinearLayout(this);
            llCourts.setOrientation(LinearLayout.VERTICAL);

            List<String> availableCourts = res.getAvailableCourtSlots();

            for (String court : availableCourts) {
                CardView cardView = new CardView(this);
                cardView.setCardElevation(4);
                cardView.setRadius(8);
                cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.light_blue));
                cardView.setTag(court);

                int screenWidth = getResources().getDisplayMetrics().widthPixels;
                int cardWidth = (int) (screenWidth * 0.9);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(cardWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 0, 5);
                cardView.setLayoutParams(params);

                TextView tvCourt = new TextView(this);
                tvCourt.setText(court);
                tvCourt.setTextColor(ContextCompat.getColor(this, android.R.color.black));
                tvCourt.setTextSize(18);
                tvCourt.setGravity(android.view.Gravity.CENTER);
                tvCourt.setPadding(16, 16, 16, 16);
                cardView.addView(tvCourt);

                cardView.setOnClickListener(v -> {
                    CardView cv = (CardView) v;
                    String selectedCourt = (String) v.getTag();
                    if (selectedCourts.contains(selectedCourt)) {
                        selectedCourts.remove(selectedCourt);
                        cv.setCardBackgroundColor(ContextCompat.getColor(this, R.color.light_blue));
                    } else {
                        selectedCourts.add(selectedCourt);
                        cv.setCardBackgroundColor(ContextCompat.getColor(this, R.color.light_green));
                    }
                });

                llCourts.addView(cardView);
            }
            resultContainer.addView(llCourts);

            MaterialButton btnBookFixed = new MaterialButton(this);
            btnBookFixed.setText(getString(R.string.btn_book_optimized));
            btnBookFixed.setTextSize(18);
            btnBookFixed.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_orange_dark));
            btnBookFixed.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            btnBookFixed.setCornerRadius(8);
            btnBookFixed.setPadding(16, 16, 16, 16);
            resultContainer.addView(btnBookFixed);

            btnBookFixed.setOnClickListener(v -> {
                if (selectedCourts.isEmpty() && selectedCourtSlots.isEmpty()) {
                    Toast.makeText(this, getString(R.string.prompt_select_at_least_one_or_replacement), Toast.LENGTH_SHORT).show();
                    return;
                }

                List<String> allSelectedCourts = new ArrayList<>(selectedCourts);
                for (String court : selectedCourtSlots) {
                    if (!allSelectedCourts.contains(court)) {
                        allSelectedCourts.add(court);
                    }
                }

                Intent intent = new Intent(BookingRegularTableActivity.this, ConfirmActivity.class);
                intent.putExtra("courtId", courtId);
                intent.putExtra("tvPhone", tvPhone);
                intent.putExtra("selectedDays", String.join(",", getSelectedDays()));
                intent.putExtra("startDate", startDate);
                intent.putExtra("endDate", endDate);
                intent.putExtra("startTime", startTime);
                intent.putExtra("endTime", endTime);
                intent.putStringArrayListExtra("selectedCourtSlots", new ArrayList<>(allSelectedCourts));
                Gson gson = new Gson();
                String flexibleCourtSlotFixesJson = gson.toJson(flexibleCourtSlotFixes);
                intent.putExtra("flexibleCourtSlotFixes", flexibleCourtSlotFixesJson);
                intent.putExtra("orderType", "Đơn cố định");
                startActivity(intent);
            });
        }
    }

    private List<String> getAvailableCourtsForDate(CheckInvalidSlotsResponse res, String date) {
        List<String> allCourts = new ArrayList<>(res.getAvailableCourtSlots());
        for (String court : res.getInvalidCourtSlots().keySet()) {
            if (!allCourts.contains(court)) allCourts.add(court);
        }
        List<String> availableCourtsForDate = new ArrayList<>();
        for (String court : allCourts) {
            if (res.getAvailableCourtSlots().contains(court)) {
                availableCourtsForDate.add(court);
            } else if (res.getInvalidCourtSlots().containsKey(court)) {
                List<String> bookedDates = (List<String>) res.getInvalidCourtSlots().get(court);
                if (!bookedDates.contains(date)) {
                    availableCourtsForDate.add(court);
                }
            }
        }
        return availableCourtsForDate;
    }

    private int countCourtDates(String court) {
        int count = 0;
        for (Map.Entry<String, Object> entry : currentResponse.getInvalidCourtSlots().entrySet()) {
            if (entry.getKey().equals(court) && entry.getValue() instanceof List<?>) {
                count = ((List<?>) entry.getValue()).size();
            }
        }
        return count;
    }

    private void showAlternativeDialog(String court, String date, List<String> availableCourts, String courtDateKey) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_title_choose_replacement) + court + getString(R.string._day_label2) + date);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        String currentSelection = flexibleCourtSlotFixes.get(date);
        List<CheckBox> checkBoxes = new ArrayList<>();
        boolean isSingleDateCourt = countCourtDates(court) == 1;
        String selectedReplacementForDate = selectedReplacementPerDate.get(date);

        CheckBox cbNone = new CheckBox(this);
        cbNone.setText(getString(R.string.checkbox_none));
        cbNone.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        cbNone.setPadding(8, 8, 8, 8);
        cbNone.setChecked(currentSelection == null);
        checkBoxes.add(cbNone);
        layout.addView(cbNone);

        for (String availableCourt : availableCourts) {
            CheckBox cb = new CheckBox(this);
            cb.setText(availableCourt);
            cb.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            cb.setPadding(8, 8, 8, 8);
            cb.setChecked(availableCourt.equals(currentSelection));

            if (selectedReplacementPerDate.containsKey(date) && !availableCourt.equals(selectedReplacementPerDate.get(date))) {
                cb.setEnabled(false);
                cb.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
            }

            checkBoxes.add(cb);
            layout.addView(cb);
        }

        for (CheckBox cb : checkBoxes) {
            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    for (CheckBox otherCb : checkBoxes) {
                        if (otherCb != cb) {
                            otherCb.setChecked(false);
                        }
                    }
                }
            });
        }

        builder.setView(layout);
        builder.setPositiveButton(getString(R.string.btn_confirm), (dialog, which) -> {
            String chosenCourt = null;
            for (CheckBox cb : checkBoxes) {
                if (cb.isChecked() && !cb.getText().toString().equals(getString(R.string.checkbox_none))) {
                    chosenCourt = cb.getText().toString();
                    break;
                }
            }

            if (chosenCourt != null) {
                flexibleCourtSlotFixes.put(date, chosenCourt);
                selectedReplacementPerDate.put(date, chosenCourt);
                if (!selectedCourtSlots.contains(court)) {
                    selectedCourtSlots.add(court);
                }
                courtDateReplacementTextViews.get(courtDateKey).setText(chosenCourt);
            } else {
                flexibleCourtSlotFixes.remove(date);
                if (isSingleDateCourt || !hasOtherCourtsOnSameDate(date, court)) {
                    selectedReplacementPerDate.remove(date);
                }
                courtDateReplacementTextViews.get(courtDateKey).setText(getString(R.string.checkbox_none));
                if (!hasReplacementForCourt(court)) {
                    selectedCourtSlots.remove(court);
                }
            }
        });
        builder.setNegativeButton(getString(R.string.btn_cancel), null);
        builder.show();
    }

    private boolean hasOtherCourtsOnSameDate(String date, String currentCourt) {
        for (Map.Entry<String, Object> entry : currentResponse.getInvalidCourtSlots().entrySet()) {
            String court = entry.getKey();
            if (!court.equals(currentCourt) && entry.getValue() instanceof List<?>) {
                List<String> dates = (List<String>) entry.getValue();
                if (dates.contains(date) && flexibleCourtSlotFixes.containsKey(date)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasReplacementForCourt(String court) {
        for (String key : flexibleCourtSlotFixes.keySet()) {
            if (courtDateReplacementTextViews.containsKey(court + "_" + key)) {
                return true;
            }
        }
        return false;
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
        return selectedDaysEnglish;
    }
}