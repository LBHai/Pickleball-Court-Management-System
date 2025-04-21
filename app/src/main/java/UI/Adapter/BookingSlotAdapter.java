package UI.Adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import Data.Model.BookingSlot;
import SEP490.G9.R;

public class BookingSlotAdapter extends RecyclerView.Adapter<BookingSlotAdapter.BookingSlotViewHolder> {

    private List<BookingSlot> bookingSlotList;

    public interface OnBookingSlotClickListener {
        void onBookingSlotClick(int position);
    }

    private OnBookingSlotClickListener clickListener;

    public BookingSlotAdapter(List<BookingSlot> bookingSlotList, OnBookingSlotClickListener clickListener) {
        this.bookingSlotList = bookingSlotList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public BookingSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking_slot, parent, false);
        return new BookingSlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingSlotViewHolder holder, int position) {
        BookingSlot slot = bookingSlotList.get(position);
        holder.bind(slot);
    }

    @Override
    public int getItemCount() {
        return bookingSlotList == null ? 0 : bookingSlotList.size();
    }

    class BookingSlotViewHolder extends RecyclerView.ViewHolder {
        TextView tvTimeRange, tvPrice;
        View container; // Layout gốc

        public BookingSlotViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.booking_slot_container);
            tvTimeRange = itemView.findViewById(R.id.tvTimeRange);
            tvPrice = itemView.findViewById(R.id.tvPrice);

            // Xử lý click
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onBookingSlotClick(getAdapterPosition());
                }
            });
        }

        void bind(BookingSlot slot) {
            // Hiển thị "06:00 - 06:30" hoặc tuỳ ý
            String timeRange = slot.getStartTime() + " - " + slot.getEndTime();
            tvTimeRange.setText(timeRange);

            // Hiển thị giá (regularPrice), tuỳ ý format
            tvPrice.setText(String.valueOf(slot.getRegularPrice()));

            // Đổi màu theo status
            switch (slot.getStatus()) {
                case "AVAILABLE":
                    container.setBackgroundColor(Color.WHITE);
                    break;
                case "BOOKED":
                    container.setBackgroundColor(Color.RED);
                    break;
                case "LOCKED":
                    container.setBackgroundColor(Color.GRAY);
                    break;
                default:
                    container.setBackgroundColor(Color.LTGRAY);
                    break;
            }

            if (slot.isSelected()) {
                container.setBackgroundColor(Color.YELLOW); // Ví dụ: tô vàng khi chọn
            }
        }
    }
}
