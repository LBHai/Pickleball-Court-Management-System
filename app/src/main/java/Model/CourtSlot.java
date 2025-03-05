package Model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CourtSlot {
    private String courtSlotId;
    private String courtSlotName;
    private List<BookingSlot> bookingSlots;

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

    public List<BookingSlot> getBookingSlots() {
        return bookingSlots;
    }
    public void setBookingSlots(List<BookingSlot> bookingSlots) {
        this.bookingSlots = bookingSlots;
    }
}

