package Adapter;

import android.content.Context;
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
import Model.NotificationItem;
import SEP490.G9.R;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private Context context;
    private List<NotificationItem> notificationList;
    private OnNotificationItemClickListener listener;

    public interface OnNotificationItemClickListener {
        // Callback khi click vào toàn bộ item (để Activity xử lý gọi API và chuyển màn hình)
        void onItemClick(NotificationItem notification);
        // Callback khi click nút đóng (xóa thông báo khỏi danh sách)
        void onCloseClick(NotificationItem notification);
    }

    public NotificationAdapter(Context context, List<NotificationItem> notificationList,
                               OnNotificationItemClickListener listener) {
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
        NotificationItem notification = notificationList.get(position);
        holder.tvTitle.setText(notification.getTitle());
        holder.tvDescription.setText(notification.getDescription());

        // Định dạng thời gian từ trường createAt
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

        // Kiểm tra trạng thái thông báo để cập nhật giao diện
        if ("read".equalsIgnoreCase(notification.getStatus())) {
            // Nếu đã đọc, làm mờ item (giảm độ mờ về 50%)
            holder.itemView.setAlpha(0.5f);
        } else {
            // Nếu chưa đọc, hiển thị bình thường (độ mờ 100%)
            holder.itemView.setAlpha(1f);
        }
    }


    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvDescription, tvTime;
        // Nếu có nút đóng (x) để xóa thông báo, bạn có thể khai báo và xử lý ở đây
        // ImageButton btnClose;

        public ViewHolder(@NonNull View itemView, OnNotificationItemClickListener listener) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTime = itemView.findViewById(R.id.tvTime);

            // Xử lý click vào toàn bộ item
            itemView.setOnClickListener(v -> {
                NotificationItem notification = notificationList.get(getAdapterPosition());
                listener.onItemClick(notification);
            });

            // Nếu bạn có nút đóng để xóa thông báo thì uncomment đoạn dưới
            /*
            btnClose = itemView.findViewById(R.id.btnClose);
            btnClose.setOnClickListener(v -> {
                NotificationItem notification = notificationList.get(getAdapterPosition());
                listener.onCloseClick(notification);
            });
            */
        }
    }
}
