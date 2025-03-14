package SEP490.G9;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import Api.ApiService;
import Api.RetrofitClient;
import Model.UpdateMyInfor;
import Session.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class EditInformationActivity extends AppCompatActivity {

    private EditText etEmail, etFirstName, etLastName, etPhoneNumber, etUserRank;
    private Spinner spGender, spDay, spMonth, spYear;
    private CheckBox cbStudent;
    private Button btnSave;

    private String username, id;

    // Adapter cho Spinner ngày, tháng, năm
    private ArrayAdapter<String> dayAdapter, monthAdapter, yearAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_information);

        // Ánh xạ view từ XML
        etEmail       = findViewById(R.id.etEmail);
        etFirstName   = findViewById(R.id.etFirstName);
        etLastName    = findViewById(R.id.etLastName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etUserRank    = findViewById(R.id.etUserRank);
        spGender      = findViewById(R.id.spGender);
        spDay         = findViewById(R.id.spDay);
        spMonth       = findViewById(R.id.spMonth);
        spYear        = findViewById(R.id.spYear);
        cbStudent     = findViewById(R.id.cbStudent);
        btnSave       = findViewById(R.id.btnSave);

        setupSpinners();
        loadIntentData();

        btnSave.setOnClickListener(v -> saveUserInfo());
    }

    // Thiết lập dữ liệu cho các Spinner: Gender, Day, Month, Year
    private void setupSpinners() {
        // Spinner giới tính: sử dụng resource đã cập nhật gồm "Select gender", "Male", "Female"
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.gender_array,
                android.R.layout.simple_spinner_item
        );
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGender.setAdapter(genderAdapter);

        // Spinner ngày (1..31)
        List<String> dayList = new ArrayList<>();
        for (int d = 1; d <= 31; d++) {
            dayList.add(String.valueOf(d));
        }
        dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dayList);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDay.setAdapter(dayAdapter);

        // Spinner tháng (1..12)
        List<String> monthList = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            monthList.add(String.valueOf(m));
        }
        monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, monthList);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMonth.setAdapter(monthAdapter);

        // Spinner năm (1900..2100)
        List<String> yearList = new ArrayList<>();
        for (int y = 1900; y <= 2100; y++) {
            yearList.add(String.valueOf(y));
        }
        yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, yearList);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spYear.setAdapter(yearAdapter);
    }

    // Load dữ liệu từ Intent (truyền từ Activity khác) và hiển thị
    private void loadIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            id = intent.getStringExtra("id");
            username = intent.getStringExtra("username");
            Log.d("EditInfo", "ID: " + id + ", username: " + username);

            etEmail.setText(intent.getStringExtra("email"));
            etFirstName.setText(intent.getStringExtra("firstName"));
            etLastName.setText(intent.getStringExtra("lastName"));
            etPhoneNumber.setText(intent.getStringExtra("phoneNumber"));
            etUserRank.setText(intent.getStringExtra("userRank"));

            // Thiết lập giới tính: nếu gender null, hiển thị "Select gender"
            String genderFromIntent = intent.getStringExtra("gender"); // expected "MALE"/"FEMALE" hoặc null
            ArrayAdapter<CharSequence> genderAdapter = (ArrayAdapter<CharSequence>) spGender.getAdapter();
            if (genderFromIntent == null) {
                spGender.setSelection(0);  // "Select gender"
            } else {
                if (genderFromIntent.equalsIgnoreCase("MALE")) {
                    for (int i = 0; i < genderAdapter.getCount(); i++) {
                        if (genderAdapter.getItem(i).toString().equalsIgnoreCase("Male")) {
                            spGender.setSelection(i);
                            break;
                        }
                    }
                } else if (genderFromIntent.equalsIgnoreCase("FEMALE")) {
                    for (int i = 0; i < genderAdapter.getCount(); i++) {
                        if (genderAdapter.getItem(i).toString().equalsIgnoreCase("Female")) {
                            spGender.setSelection(i);
                            break;
                        }
                    }
                } else {
                    spGender.setSelection(0);
                }
            }

            // Thiết lập ngày sinh theo định dạng "YYYY-MM-DD"
            String dob = intent.getStringExtra("dob");
            if (dob != null && dob.contains("-")) {
                String[] dobParts = dob.split("-");
                if (dobParts.length == 3) {
                    String yearStr  = dobParts[0];
                    String monthStr = String.valueOf(Integer.parseInt(dobParts[1]));
                    String dayStr   = String.valueOf(Integer.parseInt(dobParts[2]));

                    int yearPos = yearAdapter.getPosition(yearStr);
                    if (yearPos >= 0) spYear.setSelection(yearPos);

                    int monthPos = monthAdapter.getPosition(monthStr);
                    if (monthPos >= 0) spMonth.setSelection(monthPos);

                    int dayPos = dayAdapter.getPosition(dayStr);
                    if (dayPos >= 0) spDay.setSelection(dayPos);
                }
            }

            // Checkbox Student
            String studentStr = intent.getStringExtra("student");
            if (studentStr != null) {
                cbStudent.setChecked(Boolean.parseBoolean(studentStr));
            }
        }
    }

    // Gọi API cập nhật thông tin người dùng
    private void saveUserInfo() {
        // Lấy dữ liệu từ giao diện
        String email       = etEmail.getText().toString().trim();
        String firstName   = etFirstName.getText().toString().trim();
        String lastName    = etLastName.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String userRank    = etUserRank.getText().toString().trim();

        // Lấy giá trị gender từ Spinner
        String spinnerGender = spGender.getSelectedItem().toString();
        String gender;
        if (spinnerGender.equalsIgnoreCase("Select gender")) {
            gender = null;
        } else if (spinnerGender.equalsIgnoreCase("Male")) {
            gender = "MALE";
        } else if (spinnerGender.equalsIgnoreCase("Female")) {
            gender = "FEMALE";
        } else {
            gender = null;
        }

        boolean student = cbStudent.isChecked();

        // Lấy dữ liệu ngày, tháng, năm và định dạng DOB theo "YYYY-MM-DD"
        String day   = spDay.getSelectedItem().toString();
        String month = spMonth.getSelectedItem().toString();
        String year  = spYear.getSelectedItem().toString();
        String dob   = year + "-" + String.format("%02d", Integer.parseInt(month))
                + "-" + String.format("%02d", Integer.parseInt(day));

        // Tạo đối tượng UpdateMyInfor với các dữ liệu lấy được
        UpdateMyInfor updateUser = new UpdateMyInfor();
        updateUser.setId(id);
        updateUser.setUsername(username);
        updateUser.setEmail(email);
        updateUser.setFirstName(firstName);
        updateUser.setLastName(lastName);
        updateUser.setDob(dob);
        updateUser.setPhoneNumber(phoneNumber);
        updateUser.setUserRank(userRank.isEmpty() ? null : userRank);
        updateUser.setGender(gender);  // "MALE"/"FEMALE" hoặc null nếu chưa chọn
        updateUser.setStudent(student);

        // Lấy token từ SessionManager
        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn, vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gọi API cập nhật thông tin người dùng
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.updateMyInfo("Bearer " + token, updateUser).enqueue(new Callback<UpdateMyInfor>() {
            @Override
            public void onResponse(Call<UpdateMyInfor> call, Response<UpdateMyInfor> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(EditInformationActivity.this,
                            "Cập nhật thông tin thành công!",
                            Toast.LENGTH_SHORT).show();
                    // Thực hiện các hành động khác nếu cần (ví dụ: kết thúc Activity)
                } else {
                    try {
                        String errorBody = (response.errorBody() != null)
                                ? response.errorBody().string()
                                : "Không có thông tin lỗi";
                        Toast.makeText(EditInformationActivity.this,
                                "Cập nhật thất bại: " + errorBody,
                                Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(EditInformationActivity.this,
                                "Cập nhật thất bại!",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<UpdateMyInfor> call, Throwable t) {
                Toast.makeText(EditInformationActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
