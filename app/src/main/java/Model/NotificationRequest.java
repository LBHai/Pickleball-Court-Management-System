package Model;

import com.google.gson.annotations.SerializedName;

public class NotificationRequest {
    @SerializedName("key")
    private String key; // userId hoặc phoneNumber

    @SerializedName("token")
    private String token; // fcm-token từ Firebase

    public NotificationRequest(String key, String token) {
        this.key = key;
        this.token = token;
    }

    public String getKey() {
        return key;
    }

    public String getToken() {
        return token;
    }
}
