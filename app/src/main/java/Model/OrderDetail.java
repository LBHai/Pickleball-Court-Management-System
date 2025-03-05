package Model;

public class OrderDetail {
    private String courtSlotId;
    private String courtSlotName;
    private String startTime;
    private String endTime;
    private int price;

    // Getters & Setters
    public String getCourtSlotId() {
        return courtSlotId;
    }
    public void setCourtSlotId(String courtSlotId) {
        this.courtSlotId = courtSlotId;
    }
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
    public int getPrice() {
        return price;
    }
    public void setPrice(int price) {
        this.price = price;
    }
}
