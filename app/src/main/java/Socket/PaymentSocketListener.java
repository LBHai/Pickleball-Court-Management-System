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
    private static final String TAG = "PaymentSocket";
    private static final int NORMAL_CLOSURE_STATUS = 1000;

    private String key;
    private boolean connected = false;
    private PaymentStatusCallback callback;

    private OkHttpClient client;
    private WebSocket webSocket;

    public PaymentSocketListener(String key, PaymentStatusCallback callback) {
        this.key = key;
        this.callback = callback;
    }

    public void connect() {
        if (connected) {
            Log.d(TAG, "Already connected, skip...");
            return;
        }
        Log.d(TAG, "Attempting to connect with key: " + key);
        client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("ws://203.145.46.242:8081/identity/ws/notifications?key=" + key)
                .build();
        webSocket = client.newWebSocket(request, this);
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        connected = true;
        Log.d(TAG, "onOpen => Connected with key: " + key);
        // Gửi thông điệp test ban đầu
        webSocket.send("{\"action\":\"test\",\"key\":\"" + key + "\"}");
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        Log.d(TAG, "onMessage => Received raw: " + text);
        try {
            JSONObject json = new JSONObject(text);
            String resCode = json.optString("resCode", "");
            String resDesc = json.optString("resDesc", "");
            Log.d(TAG, "resCode: " + resCode + ", resDesc: " + resDesc);

            // Kiểm tra thành công theo điều kiện: resCode = "200" và resDesc = "Payment successfully"
            if (resCode.equals("200") && resDesc.equals("Payment successfully")) {
                String orderId = json.optString("orderId", "");
                if (callback != null) {
                    callback.onPaymentSuccess(orderId);
                }
            } else if (resCode.equals("400")) {
                String errorMessage = "Thanh toán không thành công: " + resDesc;
                if (callback != null && callback instanceof ExtendedPaymentStatusCallback) {
                    ((ExtendedPaymentStatusCallback) callback).onPaymentFailure(errorMessage);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing message: " + e.getMessage());
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        Log.d(TAG, "onMessage => Received bytes: " + bytes.hex());
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        connected = false;
        webSocket.close(NORMAL_CLOSURE_STATUS, null);
        Log.d(TAG, "onClosing => code: " + code + ", reason: " + reason);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        connected = false;
        String errorMessage = (t != null ? t.toString() : "Unknown error");
        Log.e(TAG, "onFailure => " + errorMessage);
        if (callback != null && callback instanceof ExtendedPaymentStatusCallback) {
            ((ExtendedPaymentStatusCallback) callback).onPaymentFailure(errorMessage);
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public void close() {
        connected = false;
        if (webSocket != null) {
            webSocket.close(NORMAL_CLOSURE_STATUS, "Activity destroyed");
        }
        if (client != null) {
            client.dispatcher().executorService().shutdown();
        }
    }

    // Callback cơ bản cho thanh toán thành công
    public interface PaymentStatusCallback {
        void onPaymentSuccess(String orderId);
    }

    // Callback mở rộng để xử lý lỗi thanh toán
    public interface ExtendedPaymentStatusCallback extends PaymentStatusCallback {
        void onPaymentFailure(String error);
    }
}
