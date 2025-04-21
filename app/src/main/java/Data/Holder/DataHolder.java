package Data.Holder;

import java.util.ArrayList;

public class DataHolder {
    private static DataHolder instance;
    private ArrayList<Integer> slotPrices;
    private String totalTime;

    private DataHolder() {
        slotPrices = new ArrayList<>();
    }

    public static synchronized DataHolder getInstance() {
        if (instance == null) {
            instance = new DataHolder();
        }
        return instance;
    }

    public void setSlotPrices(ArrayList<Integer> slotPrices) {
        this.slotPrices = slotPrices;
    }

    public ArrayList<Integer> getSlotPrices() {
        return slotPrices;
    }

    public void setTotalTime(String totalTime) {
        this.totalTime = totalTime;
    }

    public String getTotalTime() {
        return totalTime;
    }
}