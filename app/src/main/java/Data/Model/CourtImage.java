package Data.Model;

public class CourtImage {
    private String id;
    private String imageUrl;
    private boolean mapImage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isMapImage() {
        return mapImage;
    }

    public void setMapImage(boolean mapImage) {
        this.mapImage = mapImage;
    }
}
