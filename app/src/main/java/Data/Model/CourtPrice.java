package Data.Model;

import java.util.List;

public class CourtPrice {
    private String courtId;
    private List<TimeSlot> weekdayTimeSlots;
    private List<TimeSlot> weekendTimeSlots;

    public CourtPrice() {}

    // Getters and Setters
    public String getCourtId() {
        return courtId;
    }
    public void setCourtId(String courtId) {
        this.courtId = courtId;
    }
    public List<TimeSlot> getWeekdayTimeSlots() {
        return weekdayTimeSlots;
    }
    public void setWeekdayTimeSlots(List<TimeSlot> weekdayTimeSlots) {
        this.weekdayTimeSlots = weekdayTimeSlots;
    }
    public List<TimeSlot> getWeekendTimeSlots() {
        return weekendTimeSlots;
    }
    public void setWeekendTimeSlots(List<TimeSlot> weekendTimeSlots) {
        this.weekendTimeSlots = weekendTimeSlots;
    }
}
