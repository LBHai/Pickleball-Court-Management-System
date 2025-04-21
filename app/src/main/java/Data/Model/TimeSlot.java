package Data.Model;

public class TimeSlot {
    private String id;
    private String startTime;      // Ví dụ: "06:00:00"
    private String endTime;        // Ví dụ: "10:00:00"
    private double regularPrice;   // Ví dụ: 250000.00
    private double dailyPrice;     // Ví dụ: 30000.00
    private double studentPrice;   // Ví dụ: 20000.00

    public TimeSlot() {}

    // Getters and Setters
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
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
}
