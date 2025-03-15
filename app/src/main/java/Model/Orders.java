package Model;

import java.util.List;

public class Orders {
    private String id;

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

    public Orders() {
    }

    public Orders(String id, String courtName, String address, String bookingDate, String userId, String customerName, String phoneNumber, String note, String orderType, String orderStatus, String paymentStatus, String discountCode, int totalAmount, int discountAmount, int paymentAmount, int amountPaid, int amountRefund, String paymentTimeout, List<OrderDetail> orderDetails, String qrcode) {
        this.id = id;
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
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getDiscountCode() {
        return discountCode;
    }

    public void setDiscountCode(String discountCode) {
        this.discountCode = discountCode;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(int totalAmount) {
        this.totalAmount = totalAmount;
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

    public int getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(int amountPaid) {
        this.amountPaid = amountPaid;
    }

    public int getAmountRefund() {
        return amountRefund;
    }

    public void setAmountRefund(int amountRefund) {
        this.amountRefund = amountRefund;
    }

    public String getPaymentTimeout() {
        return paymentTimeout;
    }

    public void setPaymentTimeout(String paymentTimeout) {
        this.paymentTimeout = paymentTimeout;
    }

    public List<OrderDetail> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }

    public String getQrcode() {
        return qrcode;
    }

    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }
}