package Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import Model.BookingSlot;
import Model.CourtSlot;
import SEP490.G9.R;

public class CourtSlotAdapter extends RecyclerView.Adapter<CourtSlotAdapter.CourtViewHolder> {

    private List<CourtSlot> courtSlots;

    // Callback khi click vào bookingSlot
    public interface OnCourtSlotInteraction {
        void onBookingSlotSelected(CourtSlot court, BookingSlot slot);
    }

    private OnCourtSlotInteraction listener;

    public CourtSlotAdapter(List<CourtSlot> courtSlots, OnCourtSlotInteraction listener) {
        this.courtSlots = courtSlots;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CourtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_court_slot, parent, false);
        return new CourtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourtViewHolder holder, int position) {
        CourtSlot courtSlot = courtSlots.get(position);
        holder.bind(courtSlot);
    }

    @Override
    public int getItemCount() {
        return courtSlots == null ? 0 : courtSlots.size();
    }

    class CourtViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourtName;
        RecyclerView rvBookingSlots;

        public CourtViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourtName = itemView.findViewById(R.id.tvCourtName);
            rvBookingSlots = itemView.findViewById(R.id.rvBookingSlots);
        }

        void bind(final CourtSlot courtSlot) {
            // Hiển thị tên sân
            tvCourtName.setText(courtSlot.getCourtSlotName());

            // Sử dụng một mảng để giữ instance của BookingSlotAdapter
            final BookingSlotAdapter[] adapterHolder = new BookingSlotAdapter[1];

            BookingSlotAdapter bookingSlotAdapter = new BookingSlotAdapter(
                    courtSlot.getBookingSlots(),
                    new BookingSlotAdapter.OnBookingSlotClickListener(){
                        @Override
                        public void onBookingSlotClick(int position) {
                            BookingSlot clickedSlot = courtSlot.getBookingSlots().get(position);

                            // Nếu slot AVAILABLE => toggle selected và cập nhật adapter
                            if ("AVAILABLE".equals(clickedSlot.getStatus())) {
                                boolean current = clickedSlot.isSelected();
                                clickedSlot.setSelected(!current);
                                adapterHolder[0].notifyItemChanged(position);
                            }

                            // Gọi callback nếu cần
                            if (listener != null) {
                                listener.onBookingSlotSelected(courtSlot, clickedSlot);
                            }
                        }
                    }
            );

            // Gán instance cho adapterHolder
            adapterHolder[0] = bookingSlotAdapter;

            rvBookingSlots.setLayoutManager(new LinearLayoutManager(itemView.getContext(),
                    LinearLayoutManager.HORIZONTAL, false));
            rvBookingSlots.setAdapter(bookingSlotAdapter);
        }
    }
}

