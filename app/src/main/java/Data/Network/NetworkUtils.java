package Data.Network;

import android.content.Context;
import android.widget.Toast;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Lớp tiện ích giúp gói gọn việc gọi API và bắt lỗi parse JSON,
 * hiển thị Toast tiếng Việt dễ hiểu cho người dùng.
 */
public class NetworkUtils {

    // Interface callback để trả kết quả về cho Activity/Fragment
    public interface ApiCallback<T> {
        void onSuccess(T data);
        void onError(String errorMessage);
    }

    // Hàm tiện ích: gọi API, bắt lỗi parse JSON, hiển thị Toast
    public static <T> void callApi(Call<T> call, Context context, ApiCallback<T> callback) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        // Thành công, có body -> trả kết quả
                        callback.onSuccess(response.body());
                    } else {
                        // Không thành công hoặc body null
                        String errorBody = "";
                        if (response.errorBody() != null) {
                            try {
                                errorBody = response.errorBody().string();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        String errorMsg = "Lỗi: " + response.code();
                        if (!errorBody.isEmpty()) {
                            errorMsg += " - " + errorBody;
                        }
                        // Hiển thị toast để người dùng biết
                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
                        // Gọi callback onError
                        callback.onError(errorMsg);
                    }
                } catch (JsonSyntaxException e) {
                    // Thường gặp khi JSON trả về không hợp lệ -> “End of input...”
                    e.printStackTrace();
                } catch (Exception e) {
                    // Bắt các lỗi khác (ví dụ NullPointer)
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
            }
        });
    }
}