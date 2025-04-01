package Model;

import java.util.List;

public class OrderDetailGroup {
    private String bookingDate;
    private List<OrderDetail> bookingSlots;

    // Getters & Setters
    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }
    public List<OrderDetail> getBookingSlots() { return bookingSlots; }
    public void setBookingSlots(List<OrderDetail> bookingSlots) { this.bookingSlots = bookingSlots; }
}