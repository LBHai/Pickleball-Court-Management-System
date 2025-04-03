package SEP490.G9;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import Api.ApiService;
import Api.RetrofitClient;
import Model.ChangePasswordRequest;
import Session.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePassword extends AppCompatActivity {

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

        // Khởi tạo SessionManager
        sessionManager = new SessionManager(this);

        // Ánh xạ view từ XML
        etOldPassword = findViewById(R.id.etOldPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword);
        ivToggleOldPassword = findViewById(R.id.ivToggleOldPassword);
        ivToggleNewPassword = findViewById(R.id.ivToggleNewPassword);
        ivToggleConfirmNewPassword = findViewById(R.id.ivToggleConfirmNewPassword);
        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSave);

        // Xử lý sự kiện nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // Xử lý sự kiện hiển thị/ẩn mật khẩu
        ivToggleOldPassword.setOnClickListener(v -> toggleOldPasswordVisibility());
        ivToggleNewPassword.setOnClickListener(v -> toggleNewPasswordVisibility());
        ivToggleConfirmNewPassword.setOnClickListener(v -> toggleConfirmNewPasswordVisibility());

        // Xử lý sự kiện nút "Lưu"
        btnSave.setOnClickListener(v -> {
            try {
                changePassword();
            } catch (IllegalArgumentException e) {
                // Xử lý ngoại lệ khi mật khẩu cũ không đúng hoặc các lỗi đầu vào khác
                Toast.makeText(ChangePassword.this, e.getMessage(), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                // Xử lý các ngoại lệ khác
                Toast.makeText(ChangePassword.this, "Đã xảy ra lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Hiển thị/ẩn mật khẩu cũ
    private void toggleOldPasswordVisibility() {
        Typeface currentTypeface = etOldPassword.getTypeface();
        if (isOldPasswordVisible) {
            etOldPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivToggleOldPassword.setImageResource(android.R.drawable.ic_menu_view);
        } else {
            etOldPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ivToggleOldPassword.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        }
        etOldPassword.setTypeface(currentTypeface);
        etOldPassword.setSelection(etOldPassword.getText().length());
        isOldPasswordVisible = !isOldPasswordVisible;
    }

    // Hiển thị/ẩn mật khẩu mới
    private void toggleNewPasswordVisibility() {
        Typeface currentTypeface = etNewPassword.getTypeface();
        if (isNewPasswordVisible) {
            etNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivToggleNewPassword.setImageResource(android.R.drawable.ic_menu_view);
        } else {
            etNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ivToggleNewPassword.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        }
        etNewPassword.setTypeface(currentTypeface);
        etNewPassword.setSelection(etNewPassword.getText().length());
        isNewPasswordVisible = !isNewPasswordVisible;
    }

    // Hiển thị/ẩn xác nhận mật khẩu mới
    private void toggleConfirmNewPasswordVisibility() {
        Typeface currentTypeface = etConfirmNewPassword.getTypeface();
        if (isConfirmNewPasswordVisible) {
            etConfirmNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ivToggleConfirmNewPassword.setImageResource(android.R.drawable.ic_menu_view);
        } else {
            etConfirmNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            ivToggleConfirmNewPassword.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        }
        etConfirmNewPassword.setTypeface(currentTypeface);
        etConfirmNewPassword.setSelection(etConfirmNewPassword.getText().length());
        isConfirmNewPasswordVisible = !isConfirmNewPasswordVisible;
    }

    // Xử lý thay đổi mật khẩu
    private void changePassword() throws IllegalArgumentException {
        String oldPassword = etOldPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmNewPassword = etConfirmNewPassword.getText().toString().trim();

        // Kiểm tra các trường nhập liệu
        if (oldPassword.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập mật khẩu cũ!");
        }

        if (newPassword.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập mật khẩu mới!");
        }

        if (confirmNewPassword.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập lại mật khẩu mới!");
        }

        // Kiểm tra mật khẩu mới và xác nhận mật khẩu mới có khớp không
        if (!newPassword.equals(confirmNewPassword)) {
            throw new IllegalArgumentException("Mật khẩu mới và xác nhận mật khẩu không khớp!");
        }

        // Kiểm tra độ dài mật khẩu mới (tối thiểu 6 ký tự)
        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự!");
        }

        // Lấy token từ SessionManager
        String token = sessionManager.getToken();
        if (token == null || token.isEmpty()) {
            // Nếu token không tồn tại, yêu cầu người dùng đăng nhập lại
            Toast.makeText(this, "Phiên đăng nhập hết hạn, vui lòng đăng nhập lại!", Toast.LENGTH_LONG).show();
            sessionManager.clearSession();
            Intent intent = new Intent(ChangePassword.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Tạo request để gửi lên API
        ChangePasswordRequest request = new ChangePasswordRequest(oldPassword, newPassword);

        // Gọi API thay đổi mật khẩu
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.changePassword("Bearer " + token, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ChangePassword.this, "Thay đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    try {
                        String errorBody = (response.errorBody() != null)
                                ? response.errorBody().string()
                                : "Không có thông tin lỗi";

                        Gson gson = new Gson();
                        JsonObject errorJson = gson.fromJson(errorBody, JsonObject.class);
                        int errorCode = errorJson.get("code").getAsInt();
                        String errorMessage = errorJson.get("message").getAsString();

                        if (errorCode == 1006 && errorMessage.equals("Unauthenticated")) {

                            throw new IllegalArgumentException("Mật khẩu cũ không đúng, xin vui lòng thử lại!");
                        } else if (errorCode == 401) {

                            Toast.makeText(ChangePassword.this, "Token không hợp lệ, vui lòng đăng nhập lại!", Toast.LENGTH_LONG).show();
                            sessionManager.clearSession();
                            Intent intent = new Intent(ChangePassword.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            throw new Exception("Thay đổi mật khẩu thất bại: " + errorMessage);
                        }
                    } catch (IllegalArgumentException e) {
                        // Chuyển ngoại lệ lên cho try-catch ở btnSave
                        throw new RuntimeException(e);
                    } catch (Exception e) {
                        // Chuyển các ngoại lệ khác lên cho try-catch ở btnSave
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Chuyển ngoại lệ lên cho try-catch ở btnSave
                throw new RuntimeException("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}