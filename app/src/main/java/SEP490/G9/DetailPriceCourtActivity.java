package SEP490.G9;

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

import Api.ApiService;
import Api.RetrofitClient;
import Model.CourtPrice;
import Model.TimeSlot;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailPriceCourtActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private String clubId;

    // Thay vì 1 TableLayout chung, ta tách riêng 2 bảng con:
    private TableLayout tableWeekday;
    private TableLayout tableWeekend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_price_court);

        // Lấy dữ liệu club_id truyền sang
        clubId = getIntent().getStringExtra("club_id");

        // Ánh xạ các view
        btnBack = findViewById(R.id.btnBack);
        tableWeekday = findViewById(R.id.tableWeekday);
        tableWeekend = findViewById(R.id.tableWeekend);

        // Xử lý nút Back
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(DetailPriceCourtActivity.this, BookingTableActivity.class);
            intent.putExtra("club_id", clubId);
            startActivity(intent);
            finish();
        });

        // Gọi API lấy dữ liệu bảng giá
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

                    // Đổ dữ liệu T2 - T6
                    fillTimeSlots(tableWeekday, cp.getWeekdayTimeSlots());

                    // Đổ dữ liệu T7 - CN
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

    /**
     * Đổ danh sách slots vào một TableLayout con.
     * Mỗi dòng: 3 cột [Khung giờ | Học sinh | Người lớn].
     * Nếu không có slot => hiển thị 1 dòng trống với dấu "-"
     */
    private void fillTimeSlots(TableLayout tableLayout, List<TimeSlot> slots) {
        // Tạo 1 row header (nếu muốn hiển thị tiêu đề cột cho mỗi bảng)
        TableRow headerRow = new TableRow(this);
        headerRow.addView(createCell("Khung giờ", true));
        headerRow.addView(createCell("Học sinh", true));
        headerRow.addView(createCell("Người lớn", true));
        tableLayout.addView(headerRow);

        if (slots == null || slots.isEmpty()) {
            TableRow emptyRow = new TableRow(this);
            emptyRow.addView(createCell("-", false));
            emptyRow.addView(createCell("-", false));
            emptyRow.addView(createCell("-", false));
            tableLayout.addView(emptyRow);
            return;
        }

        // Nếu có dữ liệu
        for (TimeSlot slot : slots) {
            TableRow row = new TableRow(this);

            // Cột 1: Khung giờ
            String timeRange = slot.getStartTime().substring(0, 5)
                    + " - " + slot.getEndTime().substring(0, 5);
            row.addView(createCell(timeRange, false));

            // Cột 2: Giá học sinh
            row.addView(createCell(formatMoney(slot.getStudentPrice()), false));

            // Cột 3: Giá người lớn
            row.addView(createCell(formatMoney(slot.getDailyPrice()), false));

            tableLayout.addView(row);
        }
    }

    /**
     * Tạo một TextView làm cell, có viền, canh giữa.
     * isHeader = true => font to hơn, in đậm hơn (nếu muốn).
     */
    private TextView createCell(String text, boolean isHeader) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(16, 16, 16, 16);
        tv.setGravity(Gravity.CENTER);

        // LayoutParams cho TableRow, chia đều các cột
        tv.setLayoutParams(new TableRow.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        ));
        // Đặt viền cho ô
        tv.setBackgroundResource(R.drawable.cell_border);

        if (isHeader) {
            tv.setTextSize(16);
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            tv.setTextSize(14);
        }
        tv.setTextColor(getResources().getColor(android.R.color.black));

        return tv;
    }

    /**
     * Định dạng tiền tệ. Nếu amount <= 0 => trả về "-"
     */
    private String formatMoney(double amount) {
        if (amount <= 0) return "-";
        return String.format(Locale.getDefault(), "%,.0f đ", amount);
    }
}
