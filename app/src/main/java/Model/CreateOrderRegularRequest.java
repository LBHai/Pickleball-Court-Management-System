package Model;

import java.util.List;
import java.util.Map;

public class CreateOrderRegularRequest {
    private String courtId;
    private String courtName;
    private String address;
    private String userId;
    private String customerName;
    private String phoneNumber;
    private String note;
    private String paymentStatus;
    private String orderType;
    private String startDate;
    private String endDate;
    private String startTime;
    private String endTime;
    private String selectedDays;
    private List<String> selectedCourtSlots;
    private Map<String, String> flexibleCourtSlotFixes;

    // Getters and Setters
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
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getSelectedDays() { return selectedDays; }
    public void setSelectedDays(String selectedDays) { this.selectedDays = selectedDays; }
    public List<String> getSelectedCourtSlots() { return selectedCourtSlots; }
    public void setSelectedCourtSlots(List<String> selectedCourtSlots) { this.selectedCourtSlots = selectedCourtSlots; }
    public Map<String, String> getFlexibleCourtSlotFixes() { return flexibleCourtSlotFixes; }
    public void setFlexibleCourtSlotFixes(Map<String, String> flexibleCourtSlotFixes) { this.flexibleCourtSlotFixes = flexibleCourtSlotFixes; }
}