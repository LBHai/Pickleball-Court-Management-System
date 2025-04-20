package Model;

import java.util.List;

public class ServiceOrderRequest {
    private String courtId;
    private String userId;
    private double paymentAmount;
    private String customerName;
    private String phoneNumber;
    private String note;
    private List<ServiceDetail> serviceDetails;

    public String getCourtId() { return courtId; }
    public void setCourtId(String courtId) { this.courtId = courtId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public double getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(double paymentAmount) { this.paymentAmount = paymentAmount; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public List<ServiceDetail> getServiceDetails() { return serviceDetails; }
    public void setServiceDetails(List<ServiceDetail> serviceDetails) { this.serviceDetails = serviceDetails; }
}