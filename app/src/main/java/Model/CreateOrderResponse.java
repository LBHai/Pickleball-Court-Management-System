package Model;

import java.util.List;

public class CreateOrderResponse {
    private String id;
    private String courtId;
    private String courtName;
    private String address;
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
    private int depositAmount;
    private Integer amountPaid;
    private Integer amountRefund;
    private String paymentTimeout;
    private List<OrderDetailGroup> orderDetails; // Đổi sang List<OrderDetailGroup>
    private String qrcode;
    private String createdAt;
    private String serviceDetails;

    public CreateOrderResponse(String serviceDetails, String createdAt, String qrcode, List<OrderDetailGroup> orderDetails, String paymentTimeout, Integer amountRefund, Integer amountPaid, int depositAmount, int paymentAmount, int discountAmount, int totalAmount, String discountCode, String paymentStatus, String orderStatus, String orderType, String note, String phoneNumber, String customerName, String userId, String address, String courtName, String courtId, String id) {
        this.serviceDetails = serviceDetails;
        this.createdAt = createdAt;
        this.qrcode = qrcode;
        this.orderDetails = orderDetails;
        this.paymentTimeout = paymentTimeout;
        this.amountRefund = amountRefund;
        this.amountPaid = amountPaid;
        this.depositAmount = depositAmount;
        this.paymentAmount = paymentAmount;
        this.discountAmount = discountAmount;
        this.totalAmount = totalAmount;
        this.discountCode = discountCode;
        this.paymentStatus = paymentStatus;
        this.orderStatus = orderStatus;
        this.orderType = orderType;
        this.note = note;
        this.phoneNumber = phoneNumber;
        this.customerName = customerName;
        this.userId = userId;
        this.address = address;
        this.courtName = courtName;
        this.courtId = courtId;
        this.id = id;
    }

    public String getServiceDetails() {
        return serviceDetails;
    }

    public void setServiceDetails(String serviceDetails) {
        this.serviceDetails = serviceDetails;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCourtId() { return courtId; }
    public void setCourtId(String courtId) { this.courtId = courtId; }
    public String getCourtName() { return courtName; }
    public void setCourtName(String courtName) { this.courtName = courtName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
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
    public int getDepositAmount() { return depositAmount; }
    public void setDepositAmount(int depositAmount) { this.depositAmount = depositAmount; }
    public Integer getAmountPaid() { return amountPaid; }
    public void setAmountPaid(Integer amountPaid) { this.amountPaid = amountPaid; }
    public Integer getAmountRefund() { return amountRefund; }
    public void setAmountRefund(Integer amountRefund) { this.amountRefund = amountRefund; }
    public String getPaymentTimeout() { return paymentTimeout; }
    public void setPaymentTimeout(String paymentTimeout) { this.paymentTimeout = paymentTimeout; }
    public List<OrderDetailGroup> getOrderDetails() { return orderDetails; }
    public void setOrderDetails(List<OrderDetailGroup> orderDetails) { this.orderDetails = orderDetails; }
    public String getQrcode() { return qrcode; }
    public void setQrcode(String qrcode) { this.qrcode = qrcode; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}