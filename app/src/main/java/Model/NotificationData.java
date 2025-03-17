package Model;

import com.google.gson.annotations.SerializedName;

public class NotificationData {
    @SerializedName("orderId")
    private String orderId;

    @SerializedName("dateBooking")
    private String dateBooking;

    public String getOrderId() {
        return orderId;
    }

    public String getDateBooking() {
        return dateBooking;
    }
}
