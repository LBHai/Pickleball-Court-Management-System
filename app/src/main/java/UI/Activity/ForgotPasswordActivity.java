package UI.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import Data.Network.ApiService;
import Data.Network.RetrofitClient;
import Data.Model.ForgetPasswordRequest;
import Data.Model.ForgetPasswordResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import SEP490.G9.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9]{4,}$";
    private static final String PHONE_PATTERN = "^(\\+84|0)(3|5|7|8|9)[0-9]{8}$";

    private TextInputLayout tilRecoveryKey;
    private TextInputEditText edtRecoveryKey;
    private MaterialButton btnResetPassword;
    private TextView btnBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);

        tilRecoveryKey = findViewById(R.id.tilRecoveryKey);
        edtRecoveryKey = findViewById(R.id.edtRecoveryKey);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);

        btnResetPassword.setOnClickListener(v -> resetPassword());
        btnBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void resetPassword() {
        String key = edtRecoveryKey.getText().toString().trim();

        // 1. Kiểm tra độ dài chung
        if (key.length() <= 2) {
            tilRecoveryKey.setError("Please enter at least 3 characters");
            return;
        }

        // 2. Xác định loại input và validate
        if (Patterns.EMAIL_ADDRESS.matcher(key).matches()) {
            // Email
            if (!validateEmailInput(key)) return;
            callForgetPassword(key);

        } else if (key.startsWith("+84") || key.startsWith("0")) {
            // Số điện thoại (bao gồm cả trường hợp chưa đúng định dạng)
            if (!validatePhoneInput(key)) return;
            callForgetPassword(key);

        } else {
            // Username
            if (!key.matches(USERNAME_PATTERN)) {
                tilRecoveryKey.setError("Username must be at least 4 characters and not contain special characters");
                return;
            }
            tilRecoveryKey.setError(null);
            callForgetPassword(key);
        }
    }

    private boolean validateEmailInput(String email) {
        if (!email.endsWith("@gmail.com.vn") && !email.endsWith("edu.vn")) {
            tilRecoveryKey.setError("Email must have the extension @gmail.com.vn or edu.vn");
            return false;
        }
        tilRecoveryKey.setError(null);
        return true;
    }

    private boolean validatePhoneInput(String pn) {
        if (!pn.matches(PHONE_PATTERN)) {
            tilRecoveryKey.setError("Phone number is not in correct format");
            return false;
        }
        tilRecoveryKey.setError(null);
        return true;
    }

    private void callForgetPassword(String recoveryKey) {
        showLoading(true);
        ForgetPasswordRequest body = new ForgetPasswordRequest(recoveryKey);
        ApiService api = RetrofitClient.getApiService(this);
        Call<Void> call = api.forgetPassword(body);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    showSuccessDialog();
                } else {
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
            startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
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