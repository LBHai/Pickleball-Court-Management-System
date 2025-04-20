package Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import Api.ApiService;
import Api.NetworkUtils;
import Api.RetrofitClient;
import Model.GetToken;
import Model.MyInfoResponse;
import Model.User;
import SEP490.G9.R;
import Session.SessionManager;
import retrofit2.Call;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edtUsername, edtPassword;
    private Button btnLogin;
    private SessionManager sessionManager;
    private String userId = null;
    private TextView tvForgotPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
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
        ImageView btnHome = findViewById(R.id.btnHome);

        // Thiết lập sự kiện click cho nút Home
        btnHome.setOnClickListener(v -> {
            // Chuyển về MainActivity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish(); // Đóng LoginActivity
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
                    // Inside the onSuccess of the token call
                    if (data != null && data.getResult() != null) {
                        if (data.getResult().isAuthenticated()) {
                            String token = data.getResult().getToken();
                            if (token != null && !token.isEmpty()) {
                                sessionManager.saveToken(token);
                                ApiService apiService = RetrofitClient.getApiService(LoginActivity.this);
                                String authHeader = "Bearer " + token;
                                // Use LoginActivity.this as the context here
                                NetworkUtils.callApi(apiService.getMyInfo(authHeader), LoginActivity.this, new NetworkUtils.ApiCallback<MyInfoResponse>() {
                                    @Override
                                    public void onSuccess(MyInfoResponse r) {
                                        if (r != null && r.getResult() != null) {
                                            userId = r.getResult().getId();
                                            sessionManager.saveUserId(userId);
                                            Log.d("userId2", userId);

                                            // Sau khi cập nhật userId, chuyển sang MainActivity
                                            Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                    @Override
                                    public void onError(String e) {
                                        Toast.makeText(LoginActivity.this, "Lấy thông tin thất bại!", Toast.LENGTH_SHORT).show();
                                    }
                                });


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
