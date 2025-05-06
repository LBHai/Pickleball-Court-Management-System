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
            tilRecoveryKey.setError(getString(R.string.error_username_format));
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
                tilRecoveryKey.setError(getString(R.string.error_username_format));
                return;
            }
            tilRecoveryKey.setError(null);
            callForgetPassword(key);
        }
    }

    private boolean validateEmailInput(String email) {
        if (!email.endsWith("@gmail.com") && !email.endsWith("edu.vn")) {
            tilRecoveryKey.setError(getString(R.string.error_email_extension));
            return false;
        }
        tilRecoveryKey.setError(null);
        return true;
    }

    private boolean validatePhoneInput(String pn) {
        if (!pn.matches(PHONE_PATTERN)) {
            tilRecoveryKey.setError(getString(R.string.error_phone_format));
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
                showErrorDialog(getString(R.string.connection_error), getString(R.string.connection_error));
            }
        });
    }

    private void handleErrorResponse(Response<Void> response) {
        try {
            String errJson = response.errorBody().string();
            ForgetPasswordResponse err = new Gson()
                    .fromJson(errJson, ForgetPasswordResponse.class);

            if (err.getCode() == 1005) {
                showErrorDialog(getString(R.string.user_not_found), err.getMessage());
            } else {
                showErrorDialog(getString(R.string.general_error), err.getMessage());
            }
        } catch (Exception e) {
            showErrorDialog(getString(R.string.general_error), getString(R.string.server_response_error));
        }
    }

    private void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.reset_password_success_title));
        builder.setMessage(getString(R.string.reset_password_success_message));
        builder.setPositiveButton(getString(R.string.go_to_login), (dialog, which) -> {
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
        builder.setPositiveButton(getString(R.string.ok), null);
        builder.show();
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            btnResetPassword.setEnabled(false);
            btnResetPassword.setText(getString(R.string.sending));
        } else {
            btnResetPassword.setEnabled(true);
            btnResetPassword.setText(getString(R.string.reset_password_button));
        }
    }

}