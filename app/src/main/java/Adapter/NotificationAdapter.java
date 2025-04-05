package Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
import Holder.DataHolder;
import Model.Notification;
import Model.NotificationData;
import SEP490.G9.DetailBookingActivity;
import SEP490.G9.R;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private Context context;
    private List<Notification> notificationList;
    private OnNotificationItemClickListener listener;

    public interface OnNotificationItemClickListener {
        void onCloseClick(Notification notification);
        void onItemClick(Notification notification);
    }

    public NotificationAdapter(Context context, List<Notification> notificationList, OnNotificationItemClickListener listener) {
        this.context = context;
        this.notificationList = notificationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.ViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        holder.tvTitle.setText(notification.getTitle());
        holder.tvDescription.setText(notification.getDescription());

        // Định dạng thời gian từ createAt
        String timeString = notification.getCreateAt();
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
            Date date = isoFormat.parse(timeString);
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy, 'lúc' HH:mm", Locale.getDefault());
            String formattedTime = displayFormat.format(date);
            holder.tvTime.setText(formattedTime);
        } catch (ParseException e) {
            holder.tvTime.setText(timeString);
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvTime;

        public ViewHolder(@NonNull View itemView, OnNotificationItemClickListener listener) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTime = itemView.findViewById(R.id.tvTime);

            // Xử lý click vào toàn bộ item
            itemView.setOnClickListener(v -> {
                Notification notification = notificationList.get(getAdapterPosition());
                listener.onItemClick(notification);

                // Tạo Intent chuyển sang DetailBookingActivity
                Intent intent = new Intent(context, DetailBookingActivity.class);

                // Truyền thông tin cơ bản của thông báo
                intent.putExtra("notificationId", notification.getId());
                intent.putExtra("notificationTitle", notification.getTitle());
                intent.putExtra("notificationDescription", notification.getDescription());
                intent.putExtra("notificationTime", notification.getCreateAt());
                intent.putExtra("notificationStatus", notification.getStatus());

                // Nếu có thông tin đặt lịch trong NotificationData thì truyền thêm các thông tin chi tiết
                NotificationData data = notification.getNotificationData();
                if (data != null) {
                    intent.putExtra("dateBooking", data.getDateBooking());
                    intent.putExtra("orderId", data.getOrderId());
                    if (data.getTotalTime() != null && !data.getTotalTime().isEmpty()) {
                        intent.putExtra("totalTime", data.getTotalTime());
                    } else {
                        intent.putExtra("totalTime", DataHolder.getInstance().getTotalTime());
                    }
                    intent.putExtra("totalPrice", data.getTotalPrice());
                    intent.putExtra("orderStatus", data.getOrderStatus());
                    intent.putExtra("courtId", data.getCourtId());
                    if (data.getSlotPrices() != null) {
                        intent.putIntegerArrayListExtra("slotPrices", data.getSlotPrices());
                    }
                } else {
                    intent.putExtra("totalTime", DataHolder.getInstance().getTotalTime());
                }

                context.startActivity(intent);
            });
        }
    }
}
