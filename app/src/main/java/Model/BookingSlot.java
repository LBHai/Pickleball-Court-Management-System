package Model;

public class BookingSlot {
    private String startTime;
    private String endTime;
    private double regularPrice;
    private double dailyPrice;
    private double studentPrice;
    private String status;

    // Thêm thuộc tính selected
    private boolean selected;

    // Getter, setter cho các trường đã có
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
    public double getRegularPrice() {
        return regularPrice;
    }
    public void setRegularPrice(double regularPrice) {
        this.regularPrice = regularPrice;
    }
    public double getDailyPrice() {
        return dailyPrice;
    }
    public void setDailyPrice(double dailyPrice) {
        this.dailyPrice = dailyPrice;
    }
    public double getStudentPrice() {
        return studentPrice;
    }
    public void setStudentPrice(double studentPrice) {
        this.studentPrice = studentPrice;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    // Thêm getter và setter cho thuộc tính selected
    public boolean isSelected() {
        return selected;
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
