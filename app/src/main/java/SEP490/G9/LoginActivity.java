package SEP490.G9;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import Api.ApiService;
import Api.NetworkUtils;
import Api.RetrofitClient;
import Model.GetToken;
import Model.User;
import Session.SessionManager;
import retrofit2.Call;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edtUsername, edtPassword;
    private Button btnLogin;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Xử lý nút Login
        btnLogin.setOnClickListener(v -> login());

        // Xử lý nút Sign Up
        TextView tvSignUp = findViewById(R.id.btnSignup);
        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // Cho phép bấm Enter trên bàn phím để login
        edtPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
                btnLogin.performClick();
                return true;
            }
            return false;
        });
    }

    private void login() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = new User(username, password);
        ApiService apiService = RetrofitClient.getApiService(LoginActivity.this);
        Call<GetToken> call = apiService.getToken(user);

        // Dùng NetworkUtils để bắt lỗi parse JSON & hiển thị Toast
        NetworkUtils.callApi(call, this, new NetworkUtils.ApiCallback<GetToken>() {
            @Override
            public void onSuccess(GetToken data) {
                try {
                    if (data != null && data.getResult() != null) {
                        if (data.getResult().isAuthenticated()) {
                            String token = data.getResult().getToken();
                            if (token != null && !token.isEmpty()) {
                                sessionManager.saveToken(token);
                                Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Token không hợp lệ", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Dữ liệu trả về không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(LoginActivity.this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onError(String errorMessage) {
                // errorMessage đã được Toast, bạn có thể log thêm hoặc xử lý tuỳ ý
            }
        });
    }
}
