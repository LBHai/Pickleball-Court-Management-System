package Model;

import java.util.List;
import java.util.Map;

public class CheckInvalidSlotsResponse {
    private Map<String, Object> invalidCourtSlots;
    private List<String> availableCourtSlots;

    public Map<String, Object> getInvalidCourtSlots() {
        return invalidCourtSlots;
    }

    public void setInvalidCourtSlots(Map<String, Object> invalidCourtSlots) {
        this.invalidCourtSlots = invalidCourtSlots;
    }

    public List<String> getAvailableCourtSlots() {
        return availableCourtSlots;
    }

    public void setAvailableCourtSlots(List<String> availableCourtSlots) {
        this.availableCourtSlots = availableCourtSlots;
    }
}