package Model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

public class NotificationData {
    @SerializedName("orderId")
    private String orderId;

    @SerializedName("dateBooking")
    private String dateBooking;

    // Các thông tin bổ sung để hiển thị chi tiết đặt lịch
    @SerializedName("totalTime")
    private String totalTime;

    @SerializedName("totalPrice")
    private String totalPrice;

    @SerializedName("orderStatus")
    private String orderStatus;

    @SerializedName("courtId")
    private String courtId;

    @SerializedName("slotPrices")
    private ArrayList<Integer> slotPrices;

    public NotificationData() {
    }

    public NotificationData(String orderId, String dateBooking, String totalTime, String totalPrice, String orderStatus, String courtId, ArrayList<Integer> slotPrices) {
        this.orderId = orderId;
        this.dateBooking = dateBooking;
        this.totalTime = totalTime;
        this.totalPrice = totalPrice;
        this.orderStatus = orderStatus;
        this.courtId = courtId;
        this.slotPrices = slotPrices;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getDateBooking() {
        return dateBooking;
    }

    public void setDateBooking(String dateBooking) {
        this.dateBooking = dateBooking;
    }

    public String getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(String totalTime) {
        this.totalTime = totalTime;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getCourtId() {
        return courtId;
    }

    public void setCourtId(String courtId) {
        this.courtId = courtId;
    }

    public ArrayList<Integer> getSlotPrices() {
        return slotPrices;
    }

    public void setSlotPrices(ArrayList<Integer> slotPrices) {
        this.slotPrices = slotPrices;
    }
}