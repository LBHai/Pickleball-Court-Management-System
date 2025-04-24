package UI.Activity;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

import Data.Network.ApiService;
import Data.Network.RetrofitClient;
import Data.Model.CourtPrice;
import Data.Model.TimeSlot;
import SEP490.G9.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailPriceCourtActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private String clubId;
    private TableLayout tableWeekday;
    private TableLayout tableWeekend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_price_court);

        clubId = getIntent().getStringExtra("club_id");

        btnBack = findViewById(R.id.btnBack);
        tableWeekday = findViewById(R.id.tableWeekday);
        tableWeekend = findViewById(R.id.tableWeekend);

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(DetailPriceCourtActivity.this, BookingTableActivity.class);
            intent.putExtra("club_id", clubId);
            startActivity(intent);
            finish();
        });

        fetchCourtPrice(clubId);
    }

    private void fetchCourtPrice(String courtId) {
        ApiService api = RetrofitClient.getApiService(DetailPriceCourtActivity.this);
        Call<CourtPrice> call = api.getCourtPriceByCourtId(courtId);
        call.enqueue(new Callback<CourtPrice>() {
            @Override
            public void onResponse(Call<CourtPrice> call, Response<CourtPrice> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CourtPrice cp = response.body();
                    fillTimeSlots(tableWeekday, cp.getWeekdayTimeSlots());
                    fillTimeSlots(tableWeekend, cp.getWeekendTimeSlots());
                } else {
                    Toast.makeText(DetailPriceCourtActivity.this, "Không có dữ liệu!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CourtPrice> call, Throwable t) {
                Toast.makeText(DetailPriceCourtActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fillTimeSlots(TableLayout tableLayout, List<TimeSlot> slots) {
        TableRow headerRow = new TableRow(this);
        headerRow.addView(createCell("Khung giờ", true, 1.2f));
        headerRow.addView(createCell("Học sinh", true, 1f));
        headerRow.addView(createCell("Người lớn", true, 1f));
        tableLayout.addView(headerRow);

        if (slots == null || slots.isEmpty()) {
            TableRow emptyRow = new TableRow(this);
            emptyRow.addView(createCell("-", false, 1.5f));
            emptyRow.addView(createCell("-", false, 1.2f));
            emptyRow.addView(createCell("-", false, 1.2f));
            tableLayout.addView(emptyRow);
            return;
        }

        for (TimeSlot slot : slots) {
            TableRow row = new TableRow(this);
            String timeRange = formatTimeSlot(slot.getStartTime() + "-" + slot.getEndTime());
            row.addView(createCell(timeRange, false, 1.5f));
            row.addView(createCell(formatMoney(slot.getStudentPrice()), false, 1.2f));
            row.addView(createCell(formatMoney(slot.getDailyPrice()), false, 1.2f));
            tableLayout.addView(row);
        }
    }

    private TextView createCell(String text, boolean isHeader, float weight) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(16, 16, 16, 16);
        tv.setGravity(Gravity.CENTER);
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weight);
        tv.setLayoutParams(params);
        tv.setBackgroundResource(R.drawable.cell_border);
        if (isHeader) {
            tv.setTextSize(14);
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            tv.setTextSize(12);
        }
        tv.setTextColor(getResources().getColor(android.R.color.black));
        return tv;
    }

    private String formatMoney(double amount) {
        if (amount <= 0) return "-";
        return String.format(Locale.getDefault(), "%,.0f đ", amount);
    }

    private String formatTimeSlot(String timeSlot) {
        // Tách chuỗi thành thời gian bắt đầu và kết thúc
        String[] parts = timeSlot.split("-");
        if (parts.length != 2) {
            return timeSlot; // Trả về chuỗi gốc nếu định dạng không đúng
        }

        // Chuyển đổi thời gian bắt đầu
        String startTime = formatTime(parts[0]);
        // Chuyển đổi thời gian kết thúc
        String endTime = formatTime(parts[1]);

        // Kết hợp lại với "h"
        return startTime + "h-" + endTime + "h";
    }

    private String formatTime(String time) {
        // Tách chuỗi thời gian bằng dấu ":"
        String[] timeParts = time.split(":");
        if (timeParts.length >= 1) {
            try {
                // Lấy phần giờ (phần đầu tiên) và chuyển thành số nguyên để bỏ số 0 đầu
                int hour = Integer.parseInt(timeParts[0]);
                return String.valueOf(hour);
            } catch (NumberFormatException e) {
                return time; // Trả về chuỗi gốc nếu không parse được
            }
        }
        return time; // Trả về chuỗi gốc nếu không có dấu ":"
    }
}
