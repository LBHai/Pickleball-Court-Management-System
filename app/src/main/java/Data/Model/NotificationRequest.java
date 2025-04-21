package Data.Model;

import com.google.gson.annotations.SerializedName;

public class NotificationRequest {
    private String key; // userId hoặc phoneNumber

    private String token; // fcm-token từ Firebase


    public NotificationRequest(String key, String token) {
        this.key = key;
        this.token = token;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
