package Model;

import java.util.List;

public class CreateOrderRequest {
    private String courtId;
    private String courtName;
    private String address;
    private String bookingDate;
    private String customerName;
    private String userId; // có thể null
    private String phoneNumber;
    private int totalAmount;
    private String discountCode; // có thể null
    private String note; // có thể null
    private int discountAmount;
    private int paymentAmount;
    private String paymentStatus;
    private String depositAmount;
    private String signature;
    private String orderType;
    private List<OrderDetail> orderDetails;

    public CreateOrderRequest() {
    }

    public String getCourtId() {
        return courtId;
    }

    public void setCourtId(String courtId) {
        this.courtId = courtId;
    }

    public String getCourtName() {
        return courtName;
    }

    public void setCourtName(String courtName) {
        this.courtName = courtName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(int totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getDiscountCode() {
        return discountCode;
    }

    public void setDiscountCode(String discountCode) {
        this.discountCode = discountCode;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(int discountAmount) {
        this.discountAmount = discountAmount;
    }

    public int getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(int paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(String depositAmount) {
        this.depositAmount = depositAmount;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public List<OrderDetail> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }

    public CreateOrderRequest(String courtId, String courtName, String address, String bookingDate, String customerName, String userId, String phoneNumber, int totalAmount, String discountCode, String note, int discountAmount, int paymentAmount, String paymentStatus, String depositAmount, String signature, String orderType, List<OrderDetail> orderDetails) {
        this.courtId = courtId;
        this.courtName = courtName;
        this.address = address;
        this.bookingDate = bookingDate;
        this.customerName = customerName;
        this.userId = userId;
        this.phoneNumber = phoneNumber;
        this.totalAmount = totalAmount;
        this.discountCode = discountCode;
        this.note = note;
        this.discountAmount = discountAmount;
        this.paymentAmount = paymentAmount;
        this.paymentStatus = paymentStatus;
        this.depositAmount = depositAmount;
        this.signature = signature;
        this.orderType = orderType;
        this.orderDetails = orderDetails;
    }
}