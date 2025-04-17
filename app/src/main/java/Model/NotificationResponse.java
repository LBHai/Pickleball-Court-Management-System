package Model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NotificationResponse {

    @SerializedName("totalCount")
    private int totalCount;

    @SerializedName("unreadCount")
    private int unreadCount;

    @SerializedName("notifications")
    private List<NotificationItem> notifications;

    // Getters và Setters
    public int getTotalCount() {
        return totalCount;
    }
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getUnreadCount() {
        return unreadCount;
    }
    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public List<NotificationItem> getNotifications() {
        return notifications;
    }
    public void setNotifications(List<NotificationItem> notifications) {
        this.notifications = notifications;
    }
}
