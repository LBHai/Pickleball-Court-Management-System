package Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import Model.Notification;
import SEP490.G9.R;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private Context context;
    private List<Notification> notificationList;
    private OnNotificationItemClickListener listener;

    public interface OnNotificationItemClickListener {
        void onCloseClick(int position);
        void onItemClick(int position);
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

        // Gán dữ liệu
        holder.tvTitle.setText(notification.getTitle());
        holder.tvDescription.setText(notification.getDescription());
        holder.tvTime.setText(notification.getCreateAt());
        // Icon bên trái => holder.imgNotificationIcon.setImageResource(...) nếu cần
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageButton btnBack;
        TextView tvTitle, tvDescription, tvTime;
        ImageButton btnCloseNotification;

        public ViewHolder(@NonNull View itemView, OnNotificationItemClickListener listener) {
            super(itemView);
            btnBack = itemView.findViewById(R.id.btnBack);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTime = itemView.findViewById(R.id.tvTime);
            btnCloseNotification = itemView.findViewById(R.id.btnCloseNotification);

            // Click cả item
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(getAdapterPosition());
                }
            });

            // Click nút X
            btnCloseNotification.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCloseClick(getAdapterPosition());
                }
            });
        }
    }
}
