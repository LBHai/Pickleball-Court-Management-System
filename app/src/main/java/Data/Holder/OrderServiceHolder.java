package Data.Holder;

import java.util.HashMap;
import java.util.Map;

public class OrderServiceHolder {
    private static OrderServiceHolder instance;
    private Map<String, String> serviceDetailsMap = new HashMap<>();
    private Map<String, String> serviceListMap = new HashMap<>();

    private OrderServiceHolder() {}

    public static synchronized OrderServiceHolder getInstance() {
        if (instance == null) {
            instance = new OrderServiceHolder();
        }
        return instance;
    }

    public void addOrderDetail(String orderId, String serviceDetailsJson, String serviceListJson) {
        serviceDetailsMap.put(orderId, serviceDetailsJson);
        serviceListMap.put(orderId, serviceListJson);
    }

    public String getServiceDetailsJson(String orderId) {
        return serviceDetailsMap.get(orderId);
    }

    public String getServiceListJson(String orderId) {
        return serviceListMap.get(orderId);
    }
}