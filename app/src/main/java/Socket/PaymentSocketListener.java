package Socket;

import android.util.Log;
import org.json.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class PaymentSocketListener extends WebSocketListener {
    private static final int NORMAL_CLOSURE_STATUS = 1000;
    private String key;
    private PaymentStatusCallback callback;

    // key ở đây chính là id từ CreateOrderResponse
    public PaymentSocketListener(String key, PaymentStatusCallback callback) {
        this.key = key;
        this.callback = callback;
    }

    public void connect() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("ws://203.145.46.242:8081/identity/ws/notifications?key=" + key)
                .build();
        client.newWebSocket(request, this);
        client.dispatcher().executorService().shutdown();
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        Log.d("PaymentSocket", "Connected with key: " + key);
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        Log.d("PaymentSocket", "Received: " + text);
        try {
            JSONObject json = new JSONObject(text);
            int resCode = json.getInt("resCode");
            String orderId = json.getString("orderId");
            if (resCode == 200) {
                // Khi nhận được thanh toán thành công, gọi callback
                callback.onPaymentSuccess(orderId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        // Không xử lý trường hợp binary nếu không cần
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        webSocket.close(NORMAL_CLOSURE_STATUS, null);
        Log.d("PaymentSocket", "Closing: " + code + "/" + reason);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        Log.e("PaymentSocket", "Error: " + t.getMessage());
    }

    public interface PaymentStatusCallback {
        void onPaymentSuccess(String orderId);
    }
}
