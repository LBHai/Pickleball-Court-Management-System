package Data.Model;

import com.google.gson.annotations.SerializedName;

public class ForgetPasswordRequest {
    @SerializedName("key")
    private String key;
    public ForgetPasswordRequest(String key) { this.key = key; }
}