package SEP490.G9;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import Api.ApiService;
import Api.NetworkUtils;
import Api.RetrofitClient;
import Model.UpdateMyInfor;
import Session.SessionManager;
import retrofit2.Call;

public class EditInformationActivity extends AppCompatActivity {

    // View tham chiếu từ layout
    private ShapeableImageView imgAvatar;
    private TextInputEditText etEmail, etPhone, etFullName, etPhoneNumber, etUserRank;
    private Spinner spGender, spDay, spMonth, spYear;
    private CheckBox cbStudent;
    private Button btnSave;

    // Biến lưu trữ username, id
    private String username;
    private String id;

    // Adapter cho Spinner ngày/tháng/năm
    private ArrayAdapter<String> dayAdapter, monthAdapter, yearAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_information);

        // Ánh xạ view
        imgAvatar     = findViewById(R.id.imgAvatar);
        etEmail       = findViewById(R.id.etEmail);
        etPhone       = findViewById(R.id.etPhone);        // Layout "tilPhone"
        etFullName    = findViewById(R.id.etFullName);       // Layout "tilFullName"
        etPhoneNumber = findViewById(R.id.etPhoneNumber);    // Layout "tilPhoneNumber"
        etUserRank    = findViewById(R.id.etUserRank);       // Layout "tilUserRank"
        spGender      = findViewById(R.id.spGender);
        spDay         = findViewById(R.id.spDay);
        spMonth       = findViewById(R.id.spMonth);
        spYear        = findViewById(R.id.spYear);
        cbStudent     = findViewById(R.id.cbStudent);
        btnSave       = findViewById(R.id.btnSave);

        // Thiết lập Spinner Gender từ resources (ví dụ: gender_array = {"MALE","FEMALE","OTHER"})
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.gender_array,
                android.R.layout.simple_spinner_item
        );
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGender.setAdapter(genderAdapter);

        // Thiết lập Spinner cho Day (1..31)
        List<String> dayList = new ArrayList<>();
        for (int d = 1; d <= 31; d++) {
            dayList.add(String.valueOf(d));
        }
        dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dayList);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDay.setAdapter(dayAdapter);

        // Thiết lập Spinner cho Month (1..12)
        List<String> monthList = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            monthList.add(String.valueOf(m));
        }
        monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, monthList);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMonth.setAdapter(monthAdapter);

        // Thiết lập Spinner cho Year (1900..2100)
        List<String> yearList = new ArrayList<>();
        for (int y = 1900; y <= 2100; y++) {
            yearList.add(String.valueOf(y));
        }
        yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, yearList);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spYear.setAdapter(yearAdapter);

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        if (intent != null) {
            // Lấy id, username
            id = intent.getStringExtra("id");
            username = intent.getStringExtra("username");
            Log.d("EditInfo", "ID: " + id + ", username: " + username);

            // Set các trường
            etEmail.setText(intent.getStringExtra("email"));
            etPhone.setText(intent.getStringExtra("firstName"));      // Map field "firstName" => etPhone
            etFullName.setText(intent.getStringExtra("lastName"));      // Map field "lastName"  => etFullName
            etPhoneNumber.setText(intent.getStringExtra("phoneNumber"));
            etUserRank.setText(intent.getStringExtra("userRank"));

            // Giới tính
            String genderFromIntent = intent.getStringExtra("gender");
            if (genderFromIntent != null) {
                for (int i = 0; i < genderAdapter.getCount(); i++) {
                    if (genderAdapter.getItem(i).toString().equalsIgnoreCase(genderFromIntent)) {
                        spGender.setSelection(i);
                        break;
                    }
                }
            }

            // Ngày sinh: "YYYY-MM-DD"
            String dob = intent.getStringExtra("dob");
            if (dob != null && dob.contains("-")) {
                String[] dobParts = dob.split("-");
                if (dobParts.length == 3) {
                    String yearStr  = dobParts[0];
                    String monthStr = dobParts[1];
                    String dayStr   = dobParts[2];

                    int yearPos = yearAdapter.getPosition(yearStr);
                    if (yearPos >= 0) {
                        spYear.setSelection(yearPos);
                    }

                    int monthPos = monthAdapter.getPosition(String.valueOf(Integer.parseInt(monthStr)));
                    if (monthPos >= 0) {
                        spMonth.setSelection(monthPos);
                    }

                    int dayPos = dayAdapter.getPosition(String.valueOf(Integer.parseInt(dayStr)));
                    if (dayPos >= 0) {
                        spDay.setSelection(dayPos);
                    }
                }
            }

            // Checkbox Student
            String studentStr = intent.getStringExtra("student");
            if (studentStr != null) {
                cbStudent.setChecked(Boolean.parseBoolean(studentStr));
            }
        }

        // Sự kiện bấm nút Save: sử dụng NetworkUtils để gọi API updateMyInfo
        btnSave.setOnClickListener(v -> saveUserInfo());
    }

    private void saveUserInfo() {
        // Lấy dữ liệu từ UI
        String email       = etEmail.getText().toString().trim();
        String firstName   = etPhone.getText().toString().trim();      // Map field firstName => etPhone
        String lastName    = etFullName.getText().toString().trim();     // Map field lastName  => etFullName
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String userRank    = etUserRank.getText().toString().trim();

        String gender      = spGender.getSelectedItem().toString().toUpperCase();
        boolean student    = cbStudent.isChecked();

        // Lấy day/month/year từ Spinner
        String day   = spDay.getSelectedItem().toString();
        String month = spMonth.getSelectedItem().toString();
        String year  = spYear.getSelectedItem().toString();

        // Tạo chuỗi dob dạng "YYYY-MM-DD"
        String dob = year + "-" + String.format("%02d", Integer.parseInt(month)) + "-" + String.format("%02d", Integer.parseInt(day));

        // Tạo đối tượng UpdateMyInfor
        UpdateMyInfor updateUser = new UpdateMyInfor();
        updateUser.setId(id);
        updateUser.setUsername(username);
        updateUser.setEmail(email);
        updateUser.setFirstName(firstName);
        updateUser.setLastName(lastName);
        updateUser.setDob(dob);
        updateUser.setPhoneNumber(phoneNumber);
        updateUser.setUserRank(userRank);
        updateUser.setGender(gender);
        updateUser.setStudent(student);

        // Lấy token từ SessionManager
        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn, vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gọi API update thông qua NetworkUtils
        ApiService apiService = RetrofitClient.getApiService(this);
        Call<UpdateMyInfor> call = apiService.updateMyInfo("Bearer " + token, updateUser);
        NetworkUtils.callApi(call, EditInformationActivity.this, new NetworkUtils.ApiCallback<UpdateMyInfor>() {
            @Override
            public void onSuccess(UpdateMyInfor updateResponse) {
                Toast.makeText(EditInformationActivity.this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(String errorMessage) {
                Toast.makeText(EditInformationActivity.this, "Cập nhật thông tin thất bại: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
