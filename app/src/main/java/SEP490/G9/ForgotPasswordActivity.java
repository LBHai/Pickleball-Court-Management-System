package SEP490.G9;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import org.json.JSONObject;

import Api.ApiService;
import Api.NetworkUtils;
import Api.RetrofitClient;
import Model.ForgetPasswordRequest;
import Model.ForgetPasswordResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputLayout tilRecoveryKey;
    private TextInputEditText edtRecoveryKey;
    private MaterialButton btnResetPassword;
    private TextView btnBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);

        // Initialize views
        tilRecoveryKey = findViewById(R.id.tilRecoveryKey);
        edtRecoveryKey = findViewById(R.id.edtRecoveryKey);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);

        // Set click listener for Reset Password button
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });

        // Set click listener for back to login
        btnBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void resetPassword() {
        String recoveryKey = edtRecoveryKey.getText().toString().trim();
        if (recoveryKey.isEmpty()) {
            tilRecoveryKey.setError("Vui lòng nhập username, email hoặc số điện thoại");
            return;
        }
        tilRecoveryKey.setError(null);
        showLoading(true);

        ForgetPasswordRequest body = new ForgetPasswordRequest(recoveryKey);
        ApiService api = RetrofitClient.getApiService(this);
        Call<Void> call = api.forgetPassword(body);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    // HTTP 200 – xem như thành công
                    showSuccessDialog();
                } else {
                    // HTTP lỗi – parse JSON {"code":1005,...}
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showLoading(false);
                showErrorDialog("Connection Error", "Vui lòng kiểm tra kết nối Internet");
            }
        });
    }

    private void handleErrorResponse(Response<Void> response) {
        try {
            String errJson = response.errorBody().string();
            ForgetPasswordResponse err = new Gson()
                    .fromJson(errJson, ForgetPasswordResponse.class);

            if (err.getCode() == 1005) {
                showErrorDialog("User Not Found", err.getMessage());
            } else {
                showErrorDialog("Error", err.getMessage());
            }
        } catch (Exception e) {
            showErrorDialog("Error", "Không thể đọc phản hồi từ server");
        }
    }



    private void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Password Reset Sent");
        builder.setMessage("Your new password has been sent to your email address. Please check your inbox.");
        builder.setPositiveButton("GO TO LOGIN", (dialog, which) -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void showErrorDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            btnResetPassword.setEnabled(false);
            btnResetPassword.setText("SENDING...");
        } else {
            btnResetPassword.setEnabled(true);
            btnResetPassword.setText("RESET PASSWORD");
        }
    }
}