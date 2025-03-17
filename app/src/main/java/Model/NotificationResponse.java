package Model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NotificationResponse {
    @SerializedName("totalCount")
    private int totalCount;

    @SerializedName("unreadCount")
    private int unreadCount;

    @SerializedName("notifications")
    private List<Notification> notifications;

    public int getTotalCount() {
        return totalCount;
    }
    public int getUnreadCount() {
        return unreadCount;
    }
    public List<Notification> getNotifications() {
        return notifications;
    }
}
