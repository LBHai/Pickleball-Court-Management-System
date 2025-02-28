package Model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CourtSlot {
    @SerializedName("courtSlotId")
    private String courtSlotId;

    @SerializedName("courtSlotName")
    private String courtSlotName;

    @SerializedName("bookingSlots")
    private List<BookingSlot> bookingSlots;

    // Getter, setter...
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

