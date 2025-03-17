package Model;

import com.google.gson.annotations.SerializedName;

public class Notification {
    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("status")
    private String status;

    @SerializedName("createAt")
    private String createAt;

    @SerializedName("notificationData")
    private NotificationData notificationData;

    public String getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public String getStatus() {
        return status;
    }
    public String getCreateAt() {
        return createAt;
    }
    public NotificationData getNotificationData() {
        return notificationData;
    }
}
