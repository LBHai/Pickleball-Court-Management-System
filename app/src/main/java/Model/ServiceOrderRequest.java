package Model;

import java.util.List;

public class ServiceOrderRequest {
    private String courtId;
    private String courtName;
    private String address;
    private String userId;
    private double paymentAmount;
    private List<ServiceDetail> serviceDetails;

    // Getters và Setters
    public String getCourtId() { return courtId; }
    public void setCourtId(String courtId) { this.courtId = courtId; }
    public String getCourtName() { return courtName; }
    public void setCourtName(String courtName) { this.courtName = courtName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public double getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(double paymentAmount) { this.paymentAmount = paymentAmount; }
    public List<ServiceDetail> getServiceDetails() { return serviceDetails; }
    public void setServiceDetails(List<ServiceDetail> serviceDetails) { this.serviceDetails = serviceDetails; }
}