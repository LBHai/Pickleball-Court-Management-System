package SEP490.G9;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import Api.ApiService;
import Model.GetToken;
import Model.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {
    private TextInputEditText edtUsername, edtPassword,edtFirstName,edtLastName,edtDOB,edtPhoneNumber;
    private Button btnSignup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Tìm TextView Login và gắn sự kiện click
        TextView loginTextView = findViewById(R.id.btnLogin);
        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Quay lại màn hình Login
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Kết thúc SignUpActivity để tránh quay lại khi nhấn back
            }
        });

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtFirstName = findViewById(R.id.edtFirstName);
        edtLastName = findViewById(R.id.edtLastName);
        edtDOB = findViewById(R.id.edtDOB);
        edtPhoneNumber = findViewById(R.id.edtPhoneNumber);

//        btnSignup.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                signup();
//            }
//
//        });
//        private void signup() {
//            String username = edtUsername.getText().toString().trim();
//            String password = edtPassword.getText().toString().trim();
//
//            if (username.isEmpty() || password.isEmpty()) {
//                Toast.makeText(SignUpActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            User user = new User(username, password);
//            ApiService.apiService.getToken(user).enqueue(new Callback<GetToken>() {
//                @Override
//                public void onResponse(Call<GetToken> call, Response<GetToken> response) {
//                    if (response.isSuccessful() && response.code() == 200) {
//                        GetToken getToken = response.body();
//                        if (getToken != null && getToken.getResult().isAuthenticated()) {
//                            String token = getToken.getResult().getToken();
//                            Toast.makeText(SignUpActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
//                            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
//                            intent.putExtra("TOKEN", token);
//                            startActivity(intent);
//                            finish();
//                        } else {
//                            Toast.makeText(SignUpActivity.this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
//                        }
//                    } else {
//                        Toast.makeText(SignUpActivity.this, "Đăng nhập thất bại: " + response.message(), Toast.LENGTH_SHORT).show();
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<GetToken> call, Throwable t) {
//                    Toast.makeText(SignUpActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
    }
}
