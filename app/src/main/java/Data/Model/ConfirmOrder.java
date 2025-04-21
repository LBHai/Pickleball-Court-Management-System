package Data.Model;

public class ConfirmOrder {
    private String courtSlotId;
    private String courtSlotName;
    private String startTime;
    private String endTime;
    private double dailyPrice;
    private String dayBooking;
    public String getCourtSlotId() {
        return courtSlotId;
    }
    public void setCourtSlotId(String courtSlotId) {
        this.courtSlotId = courtSlotId;
    }

    // Các getter, setter khác
    public String getCourtSlotName() {
        return courtSlotName;
    }
    public void setCourtSlotName(String courtSlotName) {
        this.courtSlotName = courtSlotName;
    }
    public String getStartTime() {
        return startTime;
    }
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    public String getEndTime() {
        return endTime;
    }
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
    public double getDailyPrice() {
        return dailyPrice;
    }
    public void setDailyPrice(double dailyPrice) {
        this.dailyPrice = dailyPrice;
    }
    public String getDayBooking() {
        return dayBooking;
    }

    public void setDayBooking(String dayBooking) {
        this.dayBooking = dayBooking;
    }
}
