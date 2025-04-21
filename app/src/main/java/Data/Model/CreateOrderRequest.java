package Data.Model;

import java.util.List;

public class CreateOrderRequest {
    private String courtId;
    private String userId; // có thể null
    private String customerName;
    private String phoneNumber;
    private int totalAmount;
    private String discountCode; // có thể null
    private String note; // có thể null
    private int discountAmount;
    private int paymentAmount;
    private String paymentStatus;
    private int depositAmount; // Đổi sang int
    private String signature;
    private String orderType;
    private List<OrderDetailGroup> orderDetails; // Đổi sang List<OrderDetailGroup>

    public CreateOrderRequest() {
    }

    // Getters & Setters
    public String getCourtId() { return courtId; }
    public void setCourtId(String courtId) { this.courtId = courtId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public int getTotalAmount() { return totalAmount; }
    public void setTotalAmount(int totalAmount) { this.totalAmount = totalAmount; }
    public String getDiscountCode() { return discountCode; }
    public void setDiscountCode(String discountCode) { this.discountCode = discountCode; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public int getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(int discountAmount) { this.discountAmount = discountAmount; }
    public int getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(int paymentAmount) { this.paymentAmount = paymentAmount; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public int getDepositAmount() { return depositAmount; }
    public void setDepositAmount(int depositAmount) { this.depositAmount = depositAmount; }
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }
    public List<OrderDetailGroup> getOrderDetails() { return orderDetails; }
    public void setOrderDetails(List<OrderDetailGroup> orderDetails) { this.orderDetails = orderDetails; }
}