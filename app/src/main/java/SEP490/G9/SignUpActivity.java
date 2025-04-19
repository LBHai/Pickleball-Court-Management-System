package SEP490.G9;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Pattern;

import Api.ApiService;
import Api.NetworkUtils;
import Api.RetrofitClient;
import Model.CUser;
import Model.GetToken;
import retrofit2.Call;

public class SignUpActivity extends AppCompatActivity {

    private TextInputLayout tilUsername, tilPassword, tilFirstName, tilLastName, tilDOB, tilPhoneNumber, tilEmail;
    private TextInputEditText edtUsername, edtPassword, edtFirstName, edtLastName, edtDOB, edtPhoneNumber, edtEmail;
    private Button btnSignup;
    private Calendar calendar;

    // Regex patterns
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9]{4,}$";
    private static final String PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$";
    private static final String NAME_PATTERN = "^[a-zA-Z]{2,}$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize calendar
        calendar = Calendar.getInstance();

        // Find TextInputLayouts for error messages
        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        tilFirstName = findViewById(R.id.tilFirstName);
        tilLastName = findViewById(R.id.tilLastName);
        tilDOB = findViewById(R.id.tilDOB);
        tilPhoneNumber = findViewById(R.id.tilPhoneNumber);
        tilEmail = findViewById(R.id.tilEmail);

        // Find EditTexts
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtFirstName = findViewById(R.id.edtFirstName);
        edtLastName = findViewById(R.id.edtLastName);
        edtDOB = findViewById(R.id.edtDOB);
        edtPhoneNumber = findViewById(R.id.edtPhoneNumber);
        edtEmail = findViewById(R.id.edtEmail);
        btnSignup = findViewById(R.id.btnSignup);

        // Setup DatePicker for DOB field
        setupDatePicker();

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

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    signup();
                }
            }
        });

        // Set up field validation listeners
        setupValidationListeners();
    }

    private void setupDatePicker() {
        // Make DOB field read-only
        edtDOB.setFocusable(false);
        edtDOB.setClickable(true);

        // Set up date picker dialog
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateLabel();
            }
        };

        // Show DatePickerDialog when clicking on the DOB field
        edtDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        SignUpActivity.this,
                        dateSetListener,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );
                // Set max date to today
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });

        // Setup calendar icon click
        tilDOB.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtDOB.performClick();
            }
        });
    }

    private void updateDateLabel() {
        // Format date as DD/MM/YYYY for display
        String displayFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(displayFormat, Locale.getDefault());
        edtDOB.setText(sdf.format(calendar.getTime()));
    }

    private void setupValidationListeners() {
        // Add text change listeners for real-time validation
        edtUsername.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateUsername();
            }
        });

        edtPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validatePassword();
            }
        });

        edtFirstName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateFirstName();
            }
        });

        edtLastName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateLastName();
            }
        });

        edtEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateEmail();
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (!validateUsername()) isValid = false;
        if (!validatePassword()) isValid = false;
        if (!validateFirstName()) isValid = false;
        if (!validateLastName()) isValid = false;
        if (!validateDOB()) isValid = false;
        if (!validatePhoneNumber()) isValid = false;
        if (!validateEmail()) isValid = false;

        return isValid;
    }

    private boolean validateUsername() {
        String username = edtUsername.getText().toString().trim();

        if (username.isEmpty()) {
            tilUsername.setError("Vui lòng nhập tên đăng nhập");
            return false;
        } else if (!Pattern.matches(USERNAME_PATTERN, username)) {
            tilUsername.setError("Tên đăng nhập cần ít nhất 4 ký tự và không chứa ký tự đặc biệt");
            return false;
        } else {
            tilUsername.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String password = edtPassword.getText().toString().trim();

        if (password.isEmpty()) {
            tilPassword.setError("Vui lòng nhập mật khẩu");
            return false;
        } else if (!Pattern.matches(PASSWORD_PATTERN, password)) {
            tilPassword.setError("Mật khẩu cần ít nhất 6 ký tự, chứa ít nhất 1 chữ cái và 1 chữ số");
            return false;
        } else {
            tilPassword.setError(null);
            return true;
        }
    }

    private boolean validateFirstName() {
        String firstName = edtFirstName.getText().toString().trim();

        if (firstName.isEmpty()) {
            tilFirstName.setError("Vui lòng nhập tên");
            return false;
        } else if (!Pattern.matches(NAME_PATTERN, firstName)) {
            tilFirstName.setError("Tên cần ít nhất 2 ký tự và không chứa số hoặc ký tự đặc biệt");
            return false;
        } else {
            tilFirstName.setError(null);
            return true;
        }
    }

    private boolean validateLastName() {
        String lastName = edtLastName.getText().toString().trim();

        if (lastName.isEmpty()) {
            tilLastName.setError("Vui lòng nhập họ");
            return false;
        } else if (!Pattern.matches(NAME_PATTERN, lastName)) {
            tilLastName.setError("Họ cần ít nhất 2 ký tự và không chứa số hoặc ký tự đặc biệt");
            return false;
        } else {
            tilLastName.setError(null);
            return true;
        }
    }

    private boolean validateDOB() {
        String dob = edtDOB.getText().toString().trim();

        if (dob.isEmpty()) {
            tilDOB.setError("Vui lòng chọn ngày sinh");
            return false;
        } else {
            tilDOB.setError(null);
            return true;
        }
    }

    private boolean validatePhoneNumber() {
        String phoneNumber = edtPhoneNumber.getText().toString().trim();

        if (phoneNumber.isEmpty()) {
            tilPhoneNumber.setError("Vui lòng nhập số điện thoại");
            return false;
        } else {
            tilPhoneNumber.setError(null);
            return true;
        }
    }

    private boolean validateEmail() {
        String email = edtEmail.getText().toString().trim();

        if (email.isEmpty()) {
            tilEmail.setError("Vui lòng nhập email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Vui lòng nhập đúng định dạng email");
            return false;
        } else {
            tilEmail.setError(null);
            return true;
        }
    }

    private String formatDateForApi(String displayDate) {
        try {
            // Convert from display format (DD/MM/YYYY) to API format (YYYY/MM/DD)
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
            return apiFormat.format(displayFormat.parse(displayDate));
        } catch (Exception e) {
            e.printStackTrace();
            return displayDate; // Return original if parsing fails
        }
    }

    private void signup() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String firstName = edtFirstName.getText().toString().trim();
        String lastName = edtLastName.getText().toString().trim();
        String displayDob = edtDOB.getText().toString().trim();
        String apiFormattedDob = formatDateForApi(displayDob); // Convert to YYYY/MM/DD
        String phoneNumber = edtPhoneNumber.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();

        // Check if email is edu.vn domain
        if (email.endsWith("@edu.vn")) {
            showEduEmailVerificationDialog();
            return;
        } else if (!email.endsWith("@gmail.com")) {
            tilEmail.setError("Vui lòng sử dụng email @gmail.com");
            return;
        }

        // Create user object and call API
        CUser newUser = new CUser(username, password, firstName, lastName, apiFormattedDob, email, phoneNumber);
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

    private void showEduEmailVerificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận email sinh viên");
        builder.setMessage("Vui lòng kiểm tra email để xác nhận tài khoản sinh viên của bạn");
        builder.setPositiveButton("Mở Gmail", (dialog, which) -> {
            // Open Gmail app
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setPackage("com.google.android.gm");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    // If Gmail app is not installed, open Gmail website
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://mail.google.com/"));
                    startActivity(intent);
                }
            } catch (Exception e) {
                Toast.makeText(this, "Không thể mở ứng dụng Gmail", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Đóng", null);
        builder.show();
    }
}