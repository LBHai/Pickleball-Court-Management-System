package Data.Model;

public class ServiceDetail {
    private String courtServiceId;
    private int quantity;
    private double price;

    // Getters và Setters
    public String getCourtServiceId() { return courtServiceId; }
    public void setCourtServiceId(String courtServiceId) { this.courtServiceId = courtServiceId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}