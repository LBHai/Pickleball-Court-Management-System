package Model;

import java.util.List;

public class ServiceOrderRequest {
    private String courtId;
    private String courtName;
    private String address;
    private String userId;
    private double paymentAmount;
    private String customerName;
    private String phoneNumber;
    private String note;
    private List<ServiceDetail> serviceDetails;

    public ServiceOrderRequest() {
    }

    public ServiceOrderRequest(String courtId, String courtName, String address, String userId, double paymentAmount, String customerName, String phoneNumber, String note, List<ServiceDetail> serviceDetails) {
        this.courtId = courtId;
        this.courtName = courtName;
        this.address = address;
        this.userId = userId;
        this.paymentAmount = paymentAmount;
        this.customerName = customerName;
        this.phoneNumber = phoneNumber;
        this.note = note;
        this.serviceDetails = serviceDetails;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(double paymentAmount) {
        this.paymentAmount = paymentAmount;
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

    public List<ServiceDetail> getServiceDetails() {
        return serviceDetails;
    }

    public void setServiceDetails(List<ServiceDetail> serviceDetails) {
        this.serviceDetails = serviceDetails;
    }
}