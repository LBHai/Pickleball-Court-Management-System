package UI.Activity;

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

import org.json.JSONObject;

import Data.Network.ApiService;
import Data.Network.NetworkUtils;
import Data.Network.RetrofitClient;
import Data.Model.GetToken;
import Data.Model.MyInfoResponse;
import Data.Model.User;
import SEP490.G9.R;
import Data.Session.SessionManager;
import retrofit2.Call;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edtUsername, edtPassword;
    private Button btnLogin;
    private SessionManager sessionManager;
    private String userId = null;
    private TextView tvForgotPassword;
    private static final String TAG = "LoginActivity";

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

        btnLogin.setOnClickListener(v -> login());

        TextView tvSignUp = findViewById(R.id.btnSignup);
        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        edtPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
                btnLogin.performClick();
                return true;
            }
            return false;
        });

        ImageView btnHome = findViewById(R.id.btnHome);
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void login() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Log.d(TAG, "Login failed: username or password is empty");
            Toast.makeText(LoginActivity.this, "Username or password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Attempting to login with username: " + username);
        User user = new User(username, password);
        ApiService apiService = RetrofitClient.getApiService(LoginActivity.this);
        Call<GetToken> call = apiService.getToken(user);

        NetworkUtils.callApi(call, this, new NetworkUtils.ApiCallback<GetToken>() {
            @Override
            public void onSuccess(GetToken data) {
                if (data != null && data.getResult() != null) {
                    if (data.getResult().isAuthenticated()) {
                        String token = data.getResult().getToken();
                        if (token != null && !token.isEmpty()) {
                            Log.d(TAG, "Login successful, token received");
                            sessionManager.saveToken(token);
                            // Hiển thị message từ API
                            Toast.makeText(LoginActivity.this, data.getMessage(), Toast.LENGTH_SHORT).show();
                            ApiService apiService = RetrofitClient.getApiService(LoginActivity.this);
                            String authHeader = "Bearer " + token;
                            NetworkUtils.callApi(apiService.getMyInfo(authHeader), LoginActivity.this, new NetworkUtils.ApiCallback<MyInfoResponse>() {
                                @Override
                                public void onSuccess(MyInfoResponse r) {
                                    if (r != null && r.getResult() != null) {
                                        userId = r.getResult().getId();
                                        sessionManager.saveUserId(userId);
                                        Log.d(TAG, "User info retrieved, userId: " + userId);
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // Giả sử API trả về message khi lỗi
                                        String errorMsg = r != null && r.getMessage() != null ? r.getMessage() : "Failed to retrieve user info";
                                        Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onError(String e) {
                                    String message = extractMessageFromError(e);
                                    Toast.makeText(LoginActivity.this, message != null ? message : "Error retrieving user info", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(LoginActivity.this, data.getMessage() != null ? data.getMessage() : "Invalid token", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Hiển thị message từ API khi không authenticated
                        Toast.makeText(LoginActivity.this, data.getMessage() != null ? data.getMessage() : "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, data.getMessage() != null ? data.getMessage() : "Invalid response data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Login API error: " + errorMessage);
                String message = extractMessageFromError(errorMessage);
                Toast.makeText(LoginActivity.this, message != null ? message : "An error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String extractMessageFromError(String errorMessage) {
        try {
            int jsonStart = errorMessage.indexOf("{");
            int jsonEnd = errorMessage.lastIndexOf("}");
            if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
                String jsonString = errorMessage.substring(jsonStart, jsonEnd + 1);
                JSONObject jsonObject = new JSONObject(jsonString);
                return jsonObject.getString("message");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing error message: " + e.getMessage());
        }
        return null;
    }
}