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
    public OrderAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderAdapter.ViewHolder holder, int position) {
        Orders order = orderList.get(position);

        // Gán dữ liệu cho các TextView trong item_order.xml
        holder.tvDonNgay.setText(order.getOrderType());        // Ví dụ: "Đơn ngày"
        holder.tvStatus.setText(order.getOrderStatus());        // Ví dụ: "Hủy do quá giờ thanh toán"
        holder.tvTitle.setText(order.getCourtName());           // Tên sân hoặc CLB
        holder.tvAddress.setText(order.getAddress());           // Địa chỉ

        if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
            String detailText = "Chi tiết: " + order.getOrderDetails().get(0).getCourtSlotName()
                    + " " + order.getOrderDetails().get(0).getStartTime()
                    + " - " + order.getOrderDetails().get(0).getEndTime();
            holder.tvDetail.setText(detailText);
        } else {
            holder.tvDetail.setText("Chi tiết: Không có");
        }

        // Xử lý click vào item: mở DetailBookingActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailBookingActivity.class);
            // Truyền dữ liệu cần thiết qua Intent
            intent.putExtra("orderId", order.getId());
            // Nếu bạn muốn truyền thêm dữ liệu như ngày đặt, tổng tiền, tổng thời gian...
            intent.putExtra("selectedDate", order.getBookingDate());
            intent.putExtra("totalPrice", String.valueOf(order.getTotalAmount()));
            // Nếu có trường tổng thời gian, truyền nó. Ví dụ:
            // intent.putExtra("totalTime", order.getTotalTime());
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
