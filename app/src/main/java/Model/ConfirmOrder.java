package Model;

public class ConfirmOrder {
    private String courtSlotName;
    private String startTime;
    private String endTime;
    private double dailyPrice;

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
}
