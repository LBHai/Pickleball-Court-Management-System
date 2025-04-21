package UI.Component;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

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

public class DisplayInforCourt extends Fragment {
    private static final String ARG_CLUB_ID = "club_id";

    private String clubId;
    private TableLayout tableWeekday;
    private TableLayout tableWeekend;

    public static DisplayInforCourt newInstance(String clubId) {
        DisplayInforCourt fragment = new DisplayInforCourt();
        Bundle args = new Bundle();
        args.putString(ARG_CLUB_ID, clubId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate layout for this fragment
        View view = inflater.inflate(R.layout.fragment_displayinfo_court, container, false);

        // Lấy clubId từ arguments
        if (getArguments() != null) {
            clubId = getArguments().getString(ARG_CLUB_ID);
        }

        tableWeekday = view.findViewById(R.id.tableWeekday);
        tableWeekend = view.findViewById(R.id.tableWeekend);

        fetchCourtPrice(clubId);

        return view;
    }

    private void fetchCourtPrice(String courtId) {
        if (courtId == null || getContext() == null) return;

        ApiService api = RetrofitClient.getApiService(requireContext());
        Call<CourtPrice> call = api.getCourtPriceByCourtId(courtId);
        call.enqueue(new Callback<CourtPrice>() {
            @Override
            public void onResponse(Call<CourtPrice> call, Response<CourtPrice> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CourtPrice cp = response.body();
                    fillTimeSlots(tableWeekday, cp.getWeekdayTimeSlots());
                    fillTimeSlots(tableWeekend, cp.getWeekendTimeSlots());
                } else {
                    Toast.makeText(requireContext(), "Không có dữ liệu!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CourtPrice> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(requireContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fillTimeSlots(TableLayout tableLayout, List<TimeSlot> slots) {
        if (getContext() == null) return;

        tableLayout.removeAllViews(); // Xóa các view cũ trước khi thêm mới

        TableRow headerRow = new TableRow(requireContext());
        // Ô "Khung giờ" có trọng số lớn hơn (ví dụ 3f) để mở rộng theo chiều ngang
        headerRow.addView(createCell("Khung giờ", true, 1.5f));
        headerRow.addView(createCell("Học sinh", true, 1.2f));
        headerRow.addView(createCell("Người lớn", true, 1.2f));
        tableLayout.addView(headerRow);

        if (slots == null || slots.isEmpty()) {
            TableRow emptyRow = new TableRow(requireContext());
            emptyRow.addView(createCell("-", false, 1.5f));
            emptyRow.addView(createCell("-", false, 1.2f));
            emptyRow.addView(createCell("-", false, 1.2f));
            tableLayout.addView(emptyRow);
            return;
        }

        for (TimeSlot slot : slots) {
            TableRow row = new TableRow(requireContext());
            String timeRange = formatTimeSlot(slot.getStartTime() + "-" + slot.getEndTime());
            row.addView(createCell(timeRange, false, 1.5f));
            row.addView(createCell(formatMoney(slot.getStudentPrice()), false, 1.2f));
            row.addView(createCell(formatMoney(slot.getDailyPrice()), false, 1.2f));
            tableLayout.addView(row);
        }
    }

    private TextView createCell(String text, boolean isHeader, float weight) {
        TextView tv = new TextView(requireContext());
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
        tv.setTextColor(requireContext().getResources().getColor(android.R.color.black));
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
