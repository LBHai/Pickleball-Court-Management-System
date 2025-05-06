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
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.util.regex.Pattern;

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

    private static final String TAG = "LoginActivity";
    // Regex patterns
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9]{4,}$";
    private static final String PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$";

    private TextInputLayout tilUsername, tilPassword;
    private TextInputEditText edtUsername, edtPassword;
    private Button btnLogin;
    private TextView tvForgotPassword;
    private SessionManager sessionManager;
    private String userId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);

        // Init views
        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Click listeners
        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        });

        btnLogin.setOnClickListener(v -> login());

        TextView tvSignUp = findViewById(R.id.btnSignup);
        tvSignUp.setOnClickListener(v -> startActivity(new Intent(this, SignUpActivity.class)));

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
        // Validate inputs
        if (!validateUsername() || !validatePassword()) {
            return;
        }

        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

//        Log.d(TAG, "Attempting to login with username: " + username);
        User user = new User(username, password);
        ApiService apiService = RetrofitClient.getApiService(this);
        Call<GetToken> call = apiService.getToken(user);

        NetworkUtils.callApi(call, this, new NetworkUtils.ApiCallback<GetToken>() {
            @Override
            public void onSuccess(GetToken data) {
                if (data != null && data.getResult() != null && data.getResult().isAuthenticated()) {
                    String token = data.getResult().getToken();
                    if (token != null && !token.isEmpty()) {
                        //Log.d(TAG, "Login successful, token received");
                        sessionManager.saveToken(token);
                        Toast.makeText(LoginActivity.this,
                                data.getMessage() != null ? data.getMessage() : getString(R.string.invalid_token),
                                Toast.LENGTH_SHORT).show();
                        fetchUserInfo(token);
                    } else {
                        Toast.makeText(LoginActivity.this,
                                data != null && data.getMessage() != null ? data.getMessage() : getString(R.string.auth_failed),
                                Toast.LENGTH_SHORT).show();

                    }
                } else {
                    Toast.makeText(LoginActivity.this,
                            data != null && data.getMessage() != null ? data.getMessage() : "Authentication failed",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
//                Log.e(TAG, "Login API error: " + errorMessage);
                String message = extractMessageFromError(errorMessage);
                Toast.makeText(LoginActivity.this,
                        message != null ? message : "An error occurred",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserInfo(String token) {
        String authHeader = "Bearer " + token;
        ApiService apiService = RetrofitClient.getApiService(this);
        NetworkUtils.callApi(apiService.getMyInfo(authHeader), this, new NetworkUtils.ApiCallback<MyInfoResponse>() {
            @Override
            public void onSuccess(MyInfoResponse r) {
                if (r != null && r.getResult() != null) {
                    userId = r.getResult().getId();
                    sessionManager.saveUserId(userId);
                    //Log.d(TAG, "User info retrieved, userId: " + userId);
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    String errorMsg = r != null && r.getMessage() != null ? r.getMessage() : "Failed to retrieve user info";
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String e) {
                String message = extractMessageFromError(e);
                Toast.makeText(LoginActivity.this,
                        message != null ? message : "Error retrieving user info",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateUsername() {
        String u = edtUsername.getText().toString().trim();
        if (u.isEmpty()) {
            tilUsername.setError(getString(R.string.error_enter_username));
            return false;
        }
        if (!Pattern.matches(USERNAME_PATTERN, u)) {
            tilUsername.setError(getString(R.string.error_invalid_username));
            return false;
        }
        tilUsername.setError(null);
        return true;
    }

    private boolean validatePassword() {
        String p = edtPassword.getText().toString().trim();
        if (p.isEmpty()) {
            tilPassword.setError(getString(R.string.error_enter_password));
            return false;
        }
        if (!Pattern.matches(PASSWORD_PATTERN, p)) {
            tilPassword.setError(getString(R.string.error_invalid_password));
            return false;
        }
        tilPassword.setError(null);
        return true;
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
            //Log.e(TAG, "Error parsing error message: " + e.getMessage());
        }
        return null;
    }
}
