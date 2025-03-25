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
import Api.NetworkUtils;
import Api.RetrofitClient;
import Model.CUser;
import Model.GetToken;
import Model.User;
import retrofit2.Call;

public class SignUpActivity extends AppCompatActivity {
    private TextInputEditText edtUsername, edtPassword, edtFirstName, edtLastName, edtDOB, edtPhoneNumber, edtEmail;
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
        edtEmail = findViewById(R.id.edtEmail);
        btnSignup = findViewById(R.id.btnSignup);
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });
    }

    private void signup() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String firstName = edtFirstName.getText().toString().trim();
        String lastName = edtLastName.getText().toString().trim();
        String dob = edtDOB.getText().toString().trim();
        String phoneNumber = edtPhoneNumber.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty() || firstName.isEmpty() ||
                lastName.isEmpty() || dob.isEmpty() || email.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        CUser newUser = new CUser(username, password, firstName, lastName, dob, email, phoneNumber);

        ApiService apiService = RetrofitClient.getApiService(SignUpActivity.this);
        Call<GetToken> call = apiService.registerUser(newUser);
        // Sử dụng NetworkUtils để gọi API, bắt lỗi và hiển thị thông báo cho người dùng
        NetworkUtils.callApi(call, SignUpActivity.this, new NetworkUtils.ApiCallback<GetToken>() {
            @Override
            public void onSuccess(GetToken data) {
                Toast.makeText(SignUpActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(SignUpActivity.this, "Đăng ký thất bại: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
