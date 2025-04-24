package UI.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Data.Model.NotificationItem;
import SEP490.G9.R;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private Context context;
    private List<NotificationItem> notificationList;
    private OnNotificationItemClickListener listener;
    private static final SimpleDateFormat ISO_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
    private static final SimpleDateFormat DISPLAY_FORMAT = new SimpleDateFormat("dd/MM/yyyy, 'lúc' HH:mm", Locale.getDefault());

    public interface OnNotificationItemClickListener {
        void onItemClick(NotificationItem notification);
        void onCloseClick(NotificationItem notification);
    }

    public NotificationAdapter(Context context, List<NotificationItem> notificationList,
                               OnNotificationItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        setNotifications(notificationList);
    }

    public void setNotifications(List<NotificationItem> list) {
        if (list != null) {
            Collections.sort(list, new Comparator<NotificationItem>() {
                @Override
                public int compare(NotificationItem o1, NotificationItem o2) {
                    try {
                        Date d1 = ISO_FORMAT.parse(o1.getCreateAt());
                        Date d2 = ISO_FORMAT.parse(o2.getCreateAt());
                        // d2 trước d1 để mới nhất lên đầu
                        return d2.compareTo(d1);
                    } catch (ParseException e) {
                        return 0;
                    }
                }
            });
        }
        this.notificationList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationItem notification = notificationList.get(position);
        holder.tvTitle.setText(notification.getTitle());
        holder.tvDescription.setText(notification.getDescription());

        // Định dạng thời gian hiển thị
        try {
            Date date = ISO_FORMAT.parse(notification.getCreateAt());
            holder.tvTime.setText(DISPLAY_FORMAT.format(date));
        } catch (ParseException e) {
            holder.tvTime.setText(notification.getCreateAt());
        }

        // Đánh dấu đã đọc
        holder.itemView.setAlpha(
                "read".equalsIgnoreCase(notification.getStatus()) ? 0.5f : 1f
        );
    }

    @Override
    public int getItemCount() {
        return notificationList != null ? notificationList.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvTime;

        ViewHolder(@NonNull View itemView, OnNotificationItemClickListener listener) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTime = itemView.findViewById(R.id.tvTime);

            itemView.setOnClickListener(v ->
                    listener.onItemClick(notificationList.get(getAdapterPosition()))
            );

        }
    }
}
