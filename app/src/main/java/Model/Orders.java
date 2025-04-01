package Model;

import java.util.List;
import java.util.Locale;

public class Orders {
    private String id;
    private String courtId;
    private String courtName;
    private String address;
    private String bookingDate;
    private String userId;
    private String customerName;
    private String phoneNumber;
    private String note;
    private String orderType;
    private String orderStatus;
    private String paymentStatus;
    private String discountCode;
    private int totalAmount;
    private int discountAmount;
    private int paymentAmount;
    private int amountPaid;
    private int amountRefund;
    private String paymentTimeout;
    private List<OrderDetailGroup> orderDetails; // Đổi sang List<OrderDetailGroup>
    private String qrcode;
    private String createdAt;
    private int depositAmount; // Đổi sang int

    // Constructors, Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCourtId() { return courtId; }
    public void setCourtId(String courtId) { this.courtId = courtId; }
    public String getCourtName() { return courtName; }
    public void setCourtName(String courtName) { this.courtName = courtName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getDiscountCode() { return discountCode; }
    public void setDiscountCode(String discountCode) { this.discountCode = discountCode; }
    public int getTotalAmount() { return totalAmount; }
    public void setTotalAmount(int totalAmount) { this.totalAmount = totalAmount; }
    public int getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(int discountAmount) { this.discountAmount = discountAmount; }
    public int getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(int paymentAmount) { this.paymentAmount = paymentAmount; }
    public int getAmountPaid() { return amountPaid; }
    public void setAmountPaid(int amountPaid) { this.amountPaid = amountPaid; }
    public int getAmountRefund() { return amountRefund; }
    public void setAmountRefund(int amountRefund) { this.amountRefund = amountRefund; }
    public String getPaymentTimeout() { return paymentTimeout; }
    public void setPaymentTimeout(String paymentTimeout) { this.paymentTimeout = paymentTimeout; }
    public List<OrderDetailGroup> getOrderDetails() { return orderDetails; }
    public void setOrderDetails(List<OrderDetailGroup> orderDetails) { this.orderDetails = orderDetails; }
    public String getQrcode() { return qrcode; }
    public void setQrcode(String qrcode) { this.qrcode = qrcode; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public int getDepositAmount() { return depositAmount; }
    public void setDepositAmount(int depositAmount) { this.depositAmount = depositAmount; }

    public String getTotalTime() {
        if (orderDetails == null || orderDetails.isEmpty()) {
            return "0h00";
        }
        int totalMinutes = 0;
        for (OrderDetailGroup group : orderDetails) {
            for (OrderDetail detail : group.getBookingSlots()) {
                totalMinutes += calculateMinutes(detail.getStartTime(), detail.getEndTime());
            }
        }
        int hours = totalMinutes / 60;
        int mins = totalMinutes % 60;
        return String.format(Locale.getDefault(), "%dh%02d", hours, mins);
    }

    private int calculateMinutes(String startTime, String endTime) {
        String[] startParts = startTime.split(":");
        String[] endParts = endTime.split(":");
        int startHour = Integer.parseInt(startParts[0]);
        int startMin = Integer.parseInt(startParts[1]);
        int endHour = Integer.parseInt(endParts[0]);
        int endMin = Integer.parseInt(endParts[1]);
        int startTotalMin = startHour * 60 + startMin;
        int endTotalMin = endHour * 60 + endMin;
        return endTotalMin - startTotalMin;
    }
}