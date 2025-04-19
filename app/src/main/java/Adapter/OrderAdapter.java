package Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Model.Orders;
import Model.OrderDetail;
import SEP490.G9.DetailBookingActivity;
import SEP490.G9.R;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
    private List<Orders> orderList;
    private Context context;
    private OnItemClickListener onItemClickListener; // Thêm interface để xử lý click

    // Interface để xử lý sự kiện click
    public interface OnItemClickListener {
        void onItemClick(Orders order);
    }

    public OrderAdapter(List<Orders> orderList, Context context) {
        this.orderList = orderList;
        this.context = context;
    }

    // Setter cho listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
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
        holder.tvAddress.setText("Địa chỉ: " + order.getAddress());

        // Tạo chuỗi thông tin chi tiết
        String detailInfo = "Chi tiết: ";
        if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
            OrderDetail firstDetail = order.getOrderDetails().get(0);
            //String slotName = firstDetail.getCourtSlotName();
            String startTime = firstDetail.getStartTime();
            String endTime = firstDetail.getEndTime();
//            if (startTime != null && startTime.length() >= 5 && endTime != null && endTime.length() >= 5) {
//                detailInfo += slotName + " : " + startTime.substring(0, 5) + " - " + endTime.substring(0, 5);
//            } else {
//                detailInfo += "Không hợp lệ";
//            }
        } else {
            detailInfo += "Không có";
        }

        // Format ngày tạo sang định dạng dd/MM/yyyy
        String createdAtStr = order.getCreatedAt();
        String formattedDate = createdAtStr; // fallback nếu parse lỗi
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(createdAtStr);
            formattedDate = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Gộp thông tin chi tiết và ngày tạo theo định dạng mong muốn
        String finalText = detailInfo + "  |  " + formattedDate;
        holder.tvDetail.setText(finalText);

        // Nếu bạn không sử dụng tvCreatedAt riêng, có thể ẩn nó đi
        holder.tvCreatedAt.setVisibility(View.GONE);

        // Thiết lập màu sắc cho tvStatus dựa trên trạng thái
        String status = order.getOrderStatus();
        if (status.equals("Hủy đặt lịch") || status.equals("Hủy đặt lịch do quá giờ thanh toán") || status.equals("Đổi lịch thất bại") || status.equals("Không sử dụng lịch đặt")) {
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_light));
        } else if (status.equals("Đang xử lý")) {
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_orange_light));
        } else if (status.equals("Đã hoàn thành") || status.equals("Đã sử dụng lịch đặt") ||
                status.equals("Đặt dịch vụ tại sân") || status.equals("Đặt lịch thành công") || status.equals("Thay đổi lịch đặt thành công")) {
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_light));
        } else {
            // Màu mặc định nếu trạng thái không xác định
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.black));
        }

        // Xử lý sự kiện click
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(order);
            } else {
                // Giữ logic click cũ nếu không có listener
                Intent intent = new Intent(context, DetailBookingActivity.class);
                intent.putExtra("orderId", order.getId());
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
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvDonNgay, tvStatus, tvTitle, tvDetail, tvAddress, tvCreatedAt;

        public ViewHolder(View itemView) {
            super(itemView);
            tvDonNgay = itemView.findViewById(R.id.tvDonNgay);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDetail = itemView.findViewById(R.id.tvDetail);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
        }
    }
}