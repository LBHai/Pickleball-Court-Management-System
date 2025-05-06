package UI.Activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.regex.Pattern;

import SEP490.G9.R;
import Data.Network.ApiService;
import Data.Network.RetrofitClient;
import Data.Model.ChangePasswordRequest;
import Data.Session.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    private static final String PASSWORD_PATTERN ="^(?=.{6,}$)(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]+$";;

    private EditText etOldPassword, etNewPassword, etConfirmNewPassword;
    private ImageView ivToggleOldPassword, ivToggleNewPassword, ivToggleConfirmNewPassword;
    private ImageButton btnBack;
    private Button btnSave;
    private boolean isOldPasswordVisible = false;
    private boolean isNewPasswordVisible = false;
    private boolean isConfirmNewPasswordVisible = false;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        sessionManager = new SessionManager(this);

        // EditTexts
        etOldPassword = findViewById(R.id.etOldPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword);

        // Toggle icons
        ivToggleOldPassword = findViewById(R.id.ivToggleOldPassword);
        ivToggleNewPassword = findViewById(R.id.ivToggleNewPassword);
        ivToggleConfirmNewPassword = findViewById(R.id.ivToggleConfirmNewPassword);

        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSave);

        btnBack.setOnClickListener(v -> finish());

        ivToggleOldPassword.setOnClickListener(v -> toggleOldPasswordVisibility());
        ivToggleNewPassword.setOnClickListener(v -> toggleNewPasswordVisibility());
        ivToggleConfirmNewPassword.setOnClickListener(v -> toggleConfirmNewPasswordVisibility());

        btnSave.setOnClickListener(v -> changePassword());
    }

    private void toggleOldPasswordVisibility() {
        Typeface tf = etOldPassword.getTypeface();
        if (isOldPasswordVisible) {
            etOldPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivToggleOldPassword.setImageResource(android.R.drawable.ic_menu_view);
        } else {
            etOldPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ivToggleOldPassword.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        }
        etOldPassword.setTypeface(tf);
        etOldPassword.setSelection(etOldPassword.getText().length());
        isOldPasswordVisible = !isOldPasswordVisible;
    }

    private void toggleNewPasswordVisibility() {
        Typeface tf = etNewPassword.getTypeface();
        if (isNewPasswordVisible) {
            etNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivToggleNewPassword.setImageResource(android.R.drawable.ic_menu_view);
        } else {
            etNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ivToggleNewPassword.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        }
        etNewPassword.setTypeface(tf);
        etNewPassword.setSelection(etNewPassword.getText().length());
        isNewPasswordVisible = !isNewPasswordVisible;
    }

    private void toggleConfirmNewPasswordVisibility() {
        Typeface tf = etConfirmNewPassword.getTypeface();
        if (isConfirmNewPasswordVisible) {
            etConfirmNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivToggleConfirmNewPassword.setImageResource(android.R.drawable.ic_menu_view);
        } else {
            etConfirmNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ivToggleConfirmNewPassword.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        }
        etConfirmNewPassword.setTypeface(tf);
        etConfirmNewPassword.setSelection(etConfirmNewPassword.getText().length());
        isConfirmNewPasswordVisible = !isConfirmNewPasswordVisible;
    }

    private void changePassword() {
        // Validate fields
        if (!validateOldPassword() || !validateNewPassword() || !validateConfirmNewPassword()) {
            return;
        }

        String oldPassword = etOldPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();

        String token = sessionManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn, vui lòng đăng nhập lại!", Toast.LENGTH_LONG).show();
            sessionManager.clearSession();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        ChangePasswordRequest request = new ChangePasswordRequest(oldPassword, newPassword);
        ApiService api = RetrofitClient.getApiService(this);
        api.changePassword("Bearer " + token, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ChangePasswordActivity.this, "Password changed successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    try {
                        String body = response.errorBody() != null ? response.errorBody().string() : "";
                        JsonObject err = new Gson().fromJson(body, JsonObject.class);
                        String code = err.has("errorCode") ? err.get("errorCode").getAsString() : "";

                        if ("FAIL_PASS".equals(code)) {
                            Toast.makeText(ChangePasswordActivity.this,
                                    "Old password is incorrect, please try again!", Toast.LENGTH_LONG).show();
                            etOldPassword.setText("");
                            etOldPassword.requestFocus();
                        } else if (err.has("code") && err.get("code").getAsInt() == 401) {
                            Toast.makeText(ChangePasswordActivity.this,
                                    "Token không hợp lệ, vui lòng đăng nhập lại!", Toast.LENGTH_LONG).show();
                            sessionManager.clearSession();
                            Intent i = new Intent(ChangePasswordActivity.this, LoginActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            finish();
                        } else {
                            String msg = err.has("message") ? err.get("message").getAsString() : "Đã có lỗi";
                            Toast.makeText(ChangePasswordActivity.this,
                                    "Thay đổi mật khẩu thất bại: " + msg, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(ChangePasswordActivity.this,
                                "Lỗi xử lý phản hồi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ChangePasswordActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateOldPassword() {
        String old = etOldPassword.getText().toString().trim();
        if (old.isEmpty()) {
            etOldPassword.setError("Please enter old password");
            etOldPassword.requestFocus();
            return false;
        }
        etOldPassword.setError(null);
        return true;
    }

    private boolean validateNewPassword() {
        String p = etNewPassword.getText().toString().trim();
        if (p.isEmpty()) {
            etNewPassword.setError("Please enter new password");
            etNewPassword.requestFocus();
            return false;
        }
        if (!Pattern.matches(PASSWORD_PATTERN, p)) {
            etNewPassword.setError("Password must be at least 6 characters, contain at least 1 letter and 1 number!");
            etNewPassword.requestFocus();
            return false;
        }
        etNewPassword.setError(null);
        return true;
    }

    private boolean validateConfirmNewPassword() {
        String confirm = etConfirmNewPassword.getText().toString().trim();
        String newPass = etNewPassword.getText().toString().trim();
        if (confirm.isEmpty()) {
            etConfirmNewPassword.setError("Please re-enter new password");
            etConfirmNewPassword.requestFocus();
            return false;
        }
        if (!confirm.equals(newPass)) {
            etConfirmNewPassword.setError("Passwords do not match");
            etConfirmNewPassword.requestFocus();
            return false;
        }
        etConfirmNewPassword.setError(null);
        return true;
    }
}
