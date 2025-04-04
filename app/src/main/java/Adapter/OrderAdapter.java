package Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import Model.Orders;
import Model.OrderDetail;
import SEP490.G9.DetailBookingActivity;
import SEP490.G9.R;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
    private List<Orders> orderList;
    private Context context;

    public OrderAdapter(List<Orders> orderList, Context context) {
        this.orderList = orderList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderAdapter.ViewHolder holder, int position) {
        Orders order = orderList.get(position);

        holder.tvDonNgay.setText(order.getOrderType());
        holder.tvStatus.setText(order.getOrderStatus());
        holder.tvTitle.setText(order.getCourtName());
        holder.tvAddress.setText(order.getAddress());

        // Kiểm tra và hiển thị thông tin chi tiết
        if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
            OrderDetail firstDetail = order.getOrderDetails().get(0); // Lấy slot đầu tiên
            String detailText = "Chi tiết: " + firstDetail.getCourtSlotName()
                    + " " + firstDetail.getStartTime().substring(0, 5)
                    + " - " + firstDetail.getEndTime().substring(0, 5);
            holder.tvDetail.setText(detailText);
        } else {
            holder.tvDetail.setText("Chi tiết: Không có");
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailBookingActivity.class);
            intent.putExtra("orderId", order.getId());
            // Xử lý selectedDate từ OrderDetail thay vì Orders
            if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
                OrderDetail firstDetail = order.getOrderDetails().get(0);
                if (firstDetail.getBookingDates() != null && !firstDetail.getBookingDates().isEmpty()) {
                    intent.putExtra("selectedDate", firstDetail.getBookingDates().get(0));
                }
            }
            intent.putExtra("totalPrice", order.getTotalAmount());
            intent.putExtra("totalTime", order.getTotalTime());
            intent.putExtra("orderStatus", order.getOrderStatus());
            intent.putExtra("courtId", order.getCourtId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvDonNgay, tvStatus, tvTitle, tvDetail, tvAddress;

        public ViewHolder(View itemView) {
            super(itemView);
            tvDonNgay = itemView.findViewById(R.id.tvDonNgay);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDetail = itemView.findViewById(R.id.tvDetail);
            tvAddress = itemView.findViewById(R.id.tvAddress);
        }
    }
}