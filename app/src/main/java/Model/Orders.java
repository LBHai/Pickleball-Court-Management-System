package Model;

import java.util.List;
import java.util.Locale;

public class Orders {
    private String id;
    private String courtId; // Thêm trường courtId
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
    private List<OrderDetail> orderDetails;
    private String qrcode;
    private String createdAt;

    public Orders() {
    }

    public Orders(String totalTime, String id, String courtId, String courtName, String address, String bookingDate, String userId,
                  String customerName, String phoneNumber, String note, String orderType, String orderStatus, String paymentStatus,
                  String discountCode, int totalAmount, int discountAmount, int paymentAmount, int amountPaid, int amountRefund,
                  String paymentTimeout, List<OrderDetail> orderDetails, String qrcode, String createdAt) {
        this.id = id;
        this.courtId = courtId; // Thêm vào constructor
        this.courtName = courtName;
        this.address = address;
        this.bookingDate = bookingDate;
        this.userId = userId;
        this.customerName = customerName;
        this.phoneNumber = phoneNumber;
        this.note = note;
        this.orderType = orderType;
        this.orderStatus = orderStatus;
        this.paymentStatus = paymentStatus;
        this.discountCode = discountCode;
        this.totalAmount = totalAmount;
        this.discountAmount = discountAmount;
        this.paymentAmount = paymentAmount;
        this.amountPaid = amountPaid;
        this.amountRefund = amountRefund;
        this.paymentTimeout = paymentTimeout;
        this.orderDetails = orderDetails;
        this.qrcode = qrcode;
        this.createdAt = createdAt;
    }

    // Getter và Setter cho courtId
    public String getCourtId() {
        return courtId;
    }

    public void setCourtId(String courtId) {
        this.courtId = courtId;
    }

    // Các getter/setter khác giữ nguyên
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
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
    public List<OrderDetail> getOrderDetails() { return orderDetails; }
    public void setOrderDetails(List<OrderDetail> orderDetails) { this.orderDetails = orderDetails; }
    public String getQrcode() { return qrcode; }
    public void setQrcode(String qrcode) { this.qrcode = qrcode; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getTotalTime() {
        if (orderDetails == null || orderDetails.isEmpty()) {
            return "0h00";
        }
        int totalMinutes = 0;
        for (OrderDetail detail : orderDetails) {
            totalMinutes += calculateMinutes(detail.getStartTime(), detail.getEndTime());
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