package UI.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
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
import java.util.Set;
import java.util.regex.Pattern;

import Data.Network.ApiService;
import Data.Network.RetrofitClient;
import Data.Model.CUser;
import Data.Model.GetToken;
import Data.Model.StudentRegistrationRequest;
import SEP490.G9.R;
import UI.Component.BadWordsLoader;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

    private TextInputLayout tilUsername, tilPassword, tilFirstName, tilLastName, tilPhoneNumber, tilEmail;
    private TextInputEditText edtUsername, edtPassword, edtFirstName, edtLastName, edtPhoneNumber, edtEmail;
    private Spinner spinnerDay, spinnerMonth, spinnerYear;
    private Button btnSignup;
    private RadioGroup rgGender; // Thêm RadioGroup cho giới tính
    private ArrayAdapter<String> dayAdapter, monthAdapter, yearAdapter;

    // Regex patterns
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9]{4,}$";
    private static final String PASSWORD_PATTERN ="^(?=.{6,}$)(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]+$";
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
        rgGender = findViewById(R.id.rgGender); // Khởi tạo RadioGroup

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
                & validateLastName() & validateDOB() & validatePhoneNumber() & validateEmail() & validateGender();
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

    private boolean validateFirstName() {
        String n = edtFirstName.getText().toString().trim();
        if (n.isEmpty()) {
            tilFirstName.setError(getString(R.string.error_enter_first_name));
            return false;
        }
        Set<String> badWords = BadWordsLoader.loadEnglishBadWords(this);
        badWords.addAll(BadWordsLoader.loadVietnameseBadWords(this));
        if (badWords.stream().anyMatch(n.toLowerCase()::contains)) {
            tilFirstName.setError(getString(R.string.error_first_name_sensitive_content));
            return false;
        }
        if (!Pattern.matches(NAME_PATTERN, n)) {
            tilFirstName.setError(getString(R.string.error_invalid_first_name));
            return false;
        }
        tilFirstName.setError(null);
        return true;
    }

    private boolean validateLastName() {
        String n = edtLastName.getText().toString().trim();
        if (n.isEmpty()) {
            tilLastName.setError(getString(R.string.error_enter_last_name));
            return false;
        }
        Set<String> badWords = BadWordsLoader.loadEnglishBadWords(this);
        badWords.addAll(BadWordsLoader.loadVietnameseBadWords(this));
        if (badWords.stream().anyMatch(n.toLowerCase()::contains)) {
            tilLastName.setError(getString(R.string.error_last_name_sensitive_content));
            return false;
        }
        if (!Pattern.matches(NAME_PATTERN, n)) {
            tilLastName.setError(getString(R.string.error_invalid_last_name));
            return false;
        }
        tilLastName.setError(null);
        return true;
    }

    private boolean validatePhoneNumber() {
        String pn = edtPhoneNumber.getText().toString().trim();
        if (pn.isEmpty()) {
            tilPhoneNumber.setError(getString(R.string.error_enter_phone_number));
            return false;
        }
        if (!pn.matches("^(\\+84|0)(3|5|7|8|9)[0-9]{8}$")) {
            tilPhoneNumber.setError(getString(R.string.error_invalid_phone_number));
            return false;
        }
        tilPhoneNumber.setError(null);
        return true;
    }

    private boolean validateEmail() {
        String e = edtEmail.getText().toString().trim();
        if (e.isEmpty()) {
            tilEmail.setError(getString(R.string.error_enter_email));
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
            tilEmail.setError(getString(R.string.error_invalid_email_format));
            return false;
        }
        if (!e.endsWith("edu.vn") && !e.endsWith("@gmail.com")) {
            tilEmail.setError(getString(R.string.error_invalid_email_domain));
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
                Toast.makeText(this, getString(R.string.error_underage), Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        } catch (Exception ex) {
            Toast.makeText(this, getString(R.string.error_invalid_dob), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private boolean validateGender() {
        if (rgGender.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, getString(R.string.error_select_gender), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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

        // Get gender from RadioGroup
        int selectedId = rgGender.getCheckedRadioButtonId();
        String gender = "";
        if (selectedId == R.id.rbMale) {
            gender = "MALE";
        } else if (selectedId == R.id.rbFemale) {
            gender = "FEMALE";
        }

        ApiService apiService = RetrofitClient.getApiService(this);

        if (e.endsWith("edu.vn")) {
            // Student registration
            StudentRegistrationRequest req = new StudentRegistrationRequest(u, p, fn, ln, dob, e, pn, gender);
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
                        handleRegistrationError(resp);
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(SignUpActivity.this,
                            "Cannot connect to server: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Normal registration
            CUser user = new CUser(u, p, fn, ln, dob, e, pn, gender);
            Call<GetToken> call = apiService.registerUser(user);
            call.enqueue(new Callback<GetToken>() {
                @Override
                public void onResponse(Call<GetToken> call, Response<GetToken> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(SignUpActivity.this,
                                "Registration successful!",
                                Toast.LENGTH_SHORT).show();
                        navigateToLogin();
                    } else {
                        handleRegistrationError(response);
                    }
                }

                @Override
                public void onFailure(Call<GetToken> call, Throwable t) {
                    Toast.makeText(SignUpActivity.this,
                            "Registration failed: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showEduEmailVerificationDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(getString(R.string.dialog_confirm_student_email_title));
        b.setMessage(getString(R.string.dialog_confirm_student_email_message));
        b.setPositiveButton(getString(R.string.dialog_confirm_student_email_close_button), (d, w) -> navigateToLogin());
        b.create().show();
    }

    private void navigateToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void handleRegistrationError(Response<?> response) {
        if (response.code() == 409) {
            try {
                if (response.errorBody() != null) {
                    String errorBodyString = response.errorBody().string();

                    // Check for username duplication (based on the error pattern)
                    if (errorBodyString.contains("Duplicate entry") && errorBodyString.contains("username")) {
                        Toast.makeText(this, getString(R.string.username_exists_error), Toast.LENGTH_LONG).show();
                        tilUsername.setError(getString(R.string.username_exists_error));
                    }
                    // Check for email duplication
                    else if (errorBodyString.contains("Duplicate entry") && errorBodyString.contains("email")) {
                        Toast.makeText(this, getString(R.string.email_exists_error), Toast.LENGTH_LONG).show();
                        tilEmail.setError(getString(R.string.email_exists_error));
                    }
                    // Check for phone number duplication
                    else if (errorBodyString.contains("Duplicate entry") && errorBodyString.contains("phone_number")) {
                        Toast.makeText(this, getString(R.string.phone_exists_error), Toast.LENGTH_LONG).show();
                        tilPhoneNumber.setError(getString(R.string.phone_exists_error));
                    }
                    // Generic duplication error
                    else {
                        Toast.makeText(this, getString(R.string.generic_duplicate_error), Toast.LENGTH_LONG).show();
                    }
                }
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.registration_error), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else if (response.code() == 400) {
            Toast.makeText(this, getString(R.string.registration_info_exists_error), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, String.format(getString(R.string.registration_failed_error), response.code()), Toast.LENGTH_SHORT).show();
        }
    }

}