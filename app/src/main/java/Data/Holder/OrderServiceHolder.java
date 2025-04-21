package Data.Holder;

import java.util.HashMap;
import java.util.Map;

public class OrderServiceHolder {
    private static OrderServiceHolder instance;
    private Map<String, String> orderDetails = new HashMap<>(); // Lưu orderId và serviceDetailsJson

    private OrderServiceHolder() {}

    public static synchronized OrderServiceHolder getInstance() {
        if (instance == null) {
            instance = new OrderServiceHolder();
        }
        return instance;
    }

    public void addOrderDetail(String orderId, String serviceDetailsJson) {
        orderDetails.put(orderId, serviceDetailsJson);
    }

    public String getServiceDetailsJson(String orderId) {
        return orderDetails.get(orderId);
    }

    public void clearOrderDetails() {
        orderDetails.clear();
    }
}