package Data.Model;

import com.google.gson.annotations.SerializedName;

public class NotificationData {
    @SerializedName("orderId")
    private String orderId;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
