package Model;

import com.google.gson.annotations.SerializedName;

public class NotificationItem {

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
    private Object notificationData;

    public NotificationItem() {
    }

    public NotificationItem(String id, String title, String description, String status, String createAt, Object notificationData) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.createAt = createAt;
        this.notificationData = notificationData;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreateAt() {
        return createAt;
    }

    public void setCreateAt(String createAt) {
        this.createAt = createAt;
    }

    public Object getNotificationData() {
        return notificationData;
    }

    public void setNotificationData(Object notificationData) {
        this.notificationData = notificationData;
    }
}
