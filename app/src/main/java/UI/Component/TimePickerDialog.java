package UI.Component;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import SEP490.G9.R;

public class TimePickerDialog {

    public interface OnTimeSelectedListener {
        void onTimeSelected(String time);
    }

    private final Context context;
    private final OnTimeSelectedListener listener;
    private final String initialTime;
    private final boolean isStartTime;

    private String selectedHour = "00";
    private String selectedMinute = "00";

    public TimePickerDialog(Context context, OnTimeSelectedListener listener, String initialTime, boolean isStartTime) {
        this.context = context;
        this.listener = listener;
        this.initialTime = initialTime;
        this.isStartTime = isStartTime;

        if (initialTime != null && initialTime.contains(":")) {
            String[] parts = initialTime.split(":");
            if (parts.length >= 2) {
                selectedHour = parts[0];
                selectedMinute = parts[1];
            }
        }
    }

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(isStartTime ? "Chọn giờ bắt đầu" : "Chọn giờ kết thúc");

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_time_picker, null);
        builder.setView(dialogView);

        ListView hourListView = dialogView.findViewById(R.id.listViewHours);
        ListView minuteListView = dialogView.findViewById(R.id.listViewMinutes);
        TextView tvHourLabel = dialogView.findViewById(R.id.tvHourLabel);
        TextView tvMinuteLabel = dialogView.findViewById(R.id.tvMinuteLabel);

        // Cấu hình danh sách giờ (6-23)
        List<String> hours = new ArrayList<>();
        for (int i = 0; i <= 23; i++) {
            hours.add(String.format("%02d", i));
        }

        // Cấu hình danh sách phút (00, 30)
        List<String> minutes = new ArrayList<>();
        minutes.add("00");
        minutes.add("30");

        // Tạo adapter cho danh sách giờ và phút
        TimeItemAdapter hourAdapter = new TimeItemAdapter(context, hours, selectedHour);
        TimeItemAdapter minuteAdapter = new TimeItemAdapter(context, minutes, selectedMinute);

        hourListView.setAdapter(hourAdapter);
        minuteListView.setAdapter(minuteAdapter);

        // Scrolling đến vị trí được chọn
        int hourPosition = hours.indexOf(selectedHour);
        int minutePosition = minutes.indexOf(selectedMinute);

        if (hourPosition >= 0) {
            hourListView.setSelection(hourPosition);
        }

        if (minutePosition >= 0) {
            minuteListView.setSelection(minutePosition);
        }

        // Xử lý sự kiện nhấn chọn giờ
        hourListView.setOnItemClickListener((parent, view, position, id) -> {
            selectedHour = hours.get(position);
            hourAdapter.setSelectedItem(selectedHour);
            hourAdapter.notifyDataSetChanged();
        });

        // Xử lý sự kiện nhấn chọn phút
        minuteListView.setOnItemClickListener((parent, view, position, id) -> {
            selectedMinute = minutes.get(position);
            minuteAdapter.setSelectedItem(selectedMinute);
            minuteAdapter.notifyDataSetChanged();
        });

        // Nút xác nhận và hủy
        MaterialButton btnConfirm = dialogView.findViewById(R.id.btnConfirmTime);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelTime);

        AlertDialog dialog = builder.create();

        btnConfirm.setOnClickListener(v -> {
            String selectedTime = selectedHour + ":" + selectedMinute;
            listener.onTimeSelected(selectedTime);
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * Adapter tùy chỉnh cho danh sách giờ và phút
     */
    static class TimeItemAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final List<String> items;
        private String selectedItem;

        public TimeItemAdapter(Context context, List<String> items, String selectedItem) {
            super(context, 0, items);
            this.context = context;
            this.items = items;
            this.selectedItem = selectedItem;
        }

        public void setSelectedItem(String item) {
            this.selectedItem = item;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.item_time_selection, parent, false);
            }

            String item = getItem(position);
            CardView cardView = view.findViewById(R.id.cardViewTime);
            TextView textView = view.findViewById(R.id.tvTimeValue);

            textView.setText(item);

            // Đánh dấu mục đã chọn
            if (item.equals(selectedItem)) {
                cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
                textView.setTextColor(Color.WHITE);
                textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
            } else {
                cardView.setCardBackgroundColor(Color.WHITE);
                textView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
                textView.setTypeface(textView.getTypeface(), Typeface.NORMAL);
            }

            return view;
        }
    }
}