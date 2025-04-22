package UI.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

import Data.Network.ApiService;
import Data.Network.RetrofitClient;
import Data.Model.CUser;
import Data.Model.GetToken;
import Data.Model.StudentRegistrationRequest;
import SEP490.G9.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

    private TextInputLayout tilUsername, tilPassword, tilFirstName, tilLastName, tilPhoneNumber, tilEmail;
    private TextInputEditText edtUsername, edtPassword, edtFirstName, edtLastName, edtPhoneNumber, edtEmail;
    private Spinner spinnerDay, spinnerMonth, spinnerYear;
    private Button btnSignup;
    private ArrayAdapter<String> dayAdapter, monthAdapter, yearAdapter;

    // Regex patterns
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9]{4,}$";
    private static final String PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$";
    private static final String NAME_PATTERN = "^[\\p{L} ]{2,}$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Khởi tạo các view
        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        tilFirstName = findViewById(R.id.tilFirstName);
        tilLastName = findViewById(R.id.tilLastName);
        tilPhoneNumber = findViewById(R.id.tilPhoneNumber);
        tilEmail = findViewById(R.id.tilEmail);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtFirstName = findViewById(R.id.edtFirstName);
        edtLastName = findViewById(R.id.edtLastName);
        edtPhoneNumber = findViewById(R.id.edtPhoneNumber);
        edtEmail = findViewById(R.id.edtEmail);

        spinnerDay = findViewById(R.id.spinnerDay);
        spinnerMonth = findViewById(R.id.spinnerMonth);
        spinnerYear = findViewById(R.id.spinnerYear);
        btnSignup = findViewById(R.id.btnSignup);

        setupSpinners();
        setupValidationListeners();

        // Chuyển sang màn Login khi click
        TextView loginTextView = findViewById(R.id.btnLogin);
        loginTextView.setOnClickListener(v -> navigateToLogin());

        btnSignup.setOnClickListener(v -> {
            if (validateInputs()) signup();
        });
    }

    private void setupSpinners() {
        List<String> dayList = new ArrayList<>();
        for (int d = 1; d <= 31; d++) dayList.add(String.format("%02d", d));
        dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dayList);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDay.setAdapter(dayAdapter);

        List<String> monthList = new ArrayList<>();
        for (int m = 1; m <= 12; m++) monthList.add(String.format("%02d", m));
        monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, monthList);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        List<String> yearList = new ArrayList<>();
        for (int y = 1900; y <= 2100; y++) yearList.add(String.valueOf(y));
        yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, yearList);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        spinnerDay.setSelection(0);
        spinnerMonth.setSelection(0);
        spinnerYear.setSelection(yearAdapter.getPosition("2000"));
    }

    private void setupValidationListeners() {
        edtUsername.setOnFocusChangeListener((v, f) -> { if (!f) validateUsername(); });
        edtPassword.setOnFocusChangeListener((v, f) -> { if (!f) validatePassword(); });
        edtFirstName.setOnFocusChangeListener((v, f) -> { if (!f) validateFirstName(); });
        edtLastName.setOnFocusChangeListener((v, f) -> { if (!f) validateLastName(); });
        edtEmail.setOnFocusChangeListener((v, f) -> { if (!f) validateEmail(); });
    }

    private boolean validateInputs() {
        return validateUsername() & validatePassword() & validateFirstName()
                & validateLastName() & validateDOB() & validatePhoneNumber() & validateEmail();
    }

    private boolean validateUsername() {
        String u = edtUsername.getText().toString().trim();
        if (u.isEmpty())      { tilUsername.setError("Please enter username"); return false; }
        if (!Pattern.matches(USERNAME_PATTERN, u)) {
            tilUsername.setError("Login name must be at least 4 characters and cannot contain special characters");
            return false;
        }
        tilUsername.setError(null);
        return true;
    }

    private boolean validatePassword() {
        String p = edtPassword.getText().toString().trim();
        if (p.isEmpty())      { tilPassword.setError("Please enter password"); return false; }
        if (!Pattern.matches(PASSWORD_PATTERN, p)) {
            tilPassword.setError("Password must be at least 6 characters, contain at least 1 letter and 1 number");
            return false;
        }
        tilPassword.setError(null);
        return true;
    }

    private boolean validateFirstName() {
        String n = edtFirstName.getText().toString().trim();
        if (n.isEmpty())      { tilFirstName.setError("Please enter name"); return false; }
        if (!Pattern.matches(NAME_PATTERN, n)) {
            tilFirstName.setError("Invalid name (letters and spaces only, ≥2 characters)");
            return false;
        }
        tilFirstName.setError(null);
        return true;
    }

    private boolean validateLastName() {
        String n = edtLastName.getText().toString().trim();
        if (n.isEmpty())      { tilLastName.setError("Please enter your last name"); return false; }
        if (!Pattern.matches(NAME_PATTERN, n)) {
            tilLastName.setError("Invalid last name (letters and spaces only, ≥2 characters)");
            return false;
        }
        tilLastName.setError(null);
        return true;
    }

    private boolean validatePhoneNumber() {
        String pn = edtPhoneNumber.getText().toString().trim();
        if (pn.isEmpty())     { tilPhoneNumber.setError("Please enter phone number"); return false; }
        if (!pn.matches("^(\\+84|0)(3|5|7|8|9)[0-9]{8}$")) {
            tilPhoneNumber.setError("Invalid phone number");
            return false;
        }
        tilPhoneNumber.setError(null);
        return true;
    }

    private boolean validateEmail() {
        String e = edtEmail.getText().toString().trim();
        if (e.isEmpty())      { tilEmail.setError("Please enter email"); return false; }
        if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
            tilEmail.setError("Email is not in correct format.");
            return false;
        }
        if (!e.endsWith("edu.vn") && !e.endsWith("@gmail.com")) {
            tilEmail.setError("Please use email @gmail.com or edu.vn");
            return false;
        }
        tilEmail.setError(null);
        return true;
    }

    private boolean validateDOB() {
        String d = spinnerDay.getSelectedItem().toString();
        String m = spinnerMonth.getSelectedItem().toString();
        String y = spinnerYear.getSelectedItem().toString();
        try {
            Calendar c = Calendar.getInstance();
            c.setLenient(false);
            c.set(Integer.parseInt(y), Integer.parseInt(m) - 1, Integer.parseInt(d));
            c.getTime();
            Calendar t = Calendar.getInstance();
            int age = t.get(Calendar.YEAR) - Integer.parseInt(y);
            if (t.get(Calendar.DAY_OF_YEAR) < c.get(Calendar.DAY_OF_YEAR)) age--;
            if (age < 10) {
                Toast.makeText(this, "You must be 10 years or older", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        } catch (Exception ex) {
            Toast.makeText(this, "Invalid date of birth", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void signup() {
        String u = edtUsername.getText().toString().trim();
        String p = edtPassword.getText().toString().trim();
        String fn = edtFirstName.getText().toString().trim();
        String ln = edtLastName.getText().toString().trim();
        String d = spinnerDay.getSelectedItem().toString();
        String m = spinnerMonth.getSelectedItem().toString();
        String y = spinnerYear.getSelectedItem().toString();
        String dob = String.format("%s-%02d-%02d", y, Integer.parseInt(m), Integer.parseInt(d));
        String pn = edtPhoneNumber.getText().toString().trim();
        String e = edtEmail.getText().toString().trim();

        ApiService apiService = RetrofitClient.getApiService(this);

        if (e.endsWith("edu.vn")) {
            // Student registration flow
            StudentRegistrationRequest req = new StudentRegistrationRequest(
                    u, p, fn, ln, dob, e, pn
            );
            Call<Void> call = apiService.registerStudent(req);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> resp) {
                    if (resp.isSuccessful()) {
                        Toast.makeText(SignUpActivity.this,
                                "Registration successful! Please check your 'Spam' mail",
                                Toast.LENGTH_SHORT).show();
                        showEduEmailVerificationDialog();
                    } else {
                        // Xử lý phản hồi lỗi cho email edu.vn
                        handleRegistrationError(resp);
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(SignUpActivity.this,
                            "Không thể kết nối đến máy chủ: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Normal (Gmail) registration flow
            CUser user = new CUser(u, p, fn, ln, dob, e, pn);
            Call<GetToken> call = apiService.registerUser(user);

            call.enqueue(new Callback<GetToken>() {
                @Override
                public void onResponse(Call<GetToken> call, Response<GetToken> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(SignUpActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        navigateToLogin();
                    } else {
                        // Xử lý phản hồi lỗi cho email Gmail
                        handleRegistrationError(response);
                    }
                }

                @Override
                public void onFailure(Call<GetToken> call, Throwable t) {
                    Toast.makeText(SignUpActivity.this,
                            "Đăng ký thất bại: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showEduEmailVerificationDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Confirm student email");
        b.setMessage("Please check your inbox and 'Spam' mail to confirm your account.");
        b.setPositiveButton("Đóng", (d, w) -> navigateToLogin());
        b.create().show();
    }

    private void navigateToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
    private void handleRegistrationError(Response<?> response) {
        if (response.code() == 409) {  // HTTP 409 Conflict - Trùng lặp dữ liệu
            try {
                if (response.errorBody() != null) {
                    String errorBodyString = response.errorBody().string();

                    // Kiểm tra loại lỗi trùng lặp dựa trên thông báo lỗi từ server
                    if (errorBodyString.contains("email")) {
                        Toast.makeText(this, "Email already exists", Toast.LENGTH_LONG).show();
                        tilEmail.setError("Email already exists");
                    } else if (errorBodyString.contains("phone") || errorBodyString.contains("phoneNumber")) {
                        Toast.makeText(this, "Phone number already exists", Toast.LENGTH_LONG).show();
                        tilPhoneNumber.setError("Phone number already exists");
                    } else if (errorBodyString.contains("username")) {
                        Toast.makeText(this, "Username already exists", Toast.LENGTH_LONG).show();
                        tilUsername.setError("Username already exists");
                    } else {
                        // Nếu thông báo lỗi không xác định được loại lỗi
                        Toast.makeText(this, "Email, phone number or username already exists",
                                Toast.LENGTH_LONG).show();
                    }
                }
            } catch (Exception e) {
                Toast.makeText(this, "Có lỗi xảy ra khi đăng ký", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else if (response.code() == 400) {  // HTTP 400 Bad Request
            Toast.makeText(this, "Thông tin đăng ký không hợp lệ", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Đăng ký thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
        }
    }


}
