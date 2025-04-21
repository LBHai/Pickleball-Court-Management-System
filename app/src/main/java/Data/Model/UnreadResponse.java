package Data.Model;

import com.google.gson.annotations.SerializedName;

public class UnreadResponse {

    @SerializedName("unreadCount")
    private int unreadCount;

    // Nếu server trả thêm thông tin khác, bổ sung thêm các trường

    // Getters và Setters
    public int getUnreadCount() {
        return unreadCount;
    }
    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}
