package Data.Model;

public class ServiceDetailResponse {
    private String courtServiceId;
    private String courtServiceName;
    private int quantity;
    private double price;

    // Getters và Setters
    public String getCourtServiceId() { return courtServiceId; }
    public void setCourtServiceId(String courtServiceId) { this.courtServiceId = courtServiceId; }
    public String getCourtServiceName() { return courtServiceName; }
    public void setCourtServiceName(String courtServiceName) { this.courtServiceName = courtServiceName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}