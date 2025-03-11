package SEP490.G9;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
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

public class EditInformationActivity extends AppCompatActivity {

    // Các view trên layout (loại bỏ etUsername)
    private EditText etEmail, etFirstName, etLastName, etPhoneNumber, etUserRank;
    private Spinner spGender;
    private CheckBox cbStudent;
    private Button btnSave;
    private NumberPicker npDay, npMonth, npYear;

    // Biến lưu trữ username (không hiển thị)
    private String username;
    // Biến lưu trữ id người dùng (được truyền từ AccountFragment)
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_information);

        // Ánh xạ các view từ XML
        etEmail       = findViewById(R.id.etEmail);
        etFirstName   = findViewById(R.id.etFirstName);
        etLastName    = findViewById(R.id.etLastName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etUserRank    = findViewById(R.id.etUserRank);
        spGender      = findViewById(R.id.spGender);
        npDay         = findViewById(R.id.npDay);
        npMonth       = findViewById(R.id.npMonth);
        npYear        = findViewById(R.id.npYear);
        cbStudent     = findViewById(R.id.cbStudent);
        btnSave       = findViewById(R.id.btnSave);

        // Thiết lập NumberPicker cho ngày, tháng, năm
        npDay.setMinValue(1);
        npDay.setMaxValue(31);
        npMonth.setMinValue(1);
        npMonth.setMaxValue(12);
        npYear.setMinValue(1900);
        npYear.setMaxValue(2100);

        // Cấu hình Spinner với adapter (giả sử mảng "gender_array" trong resources chứa: {"MALE", "FEMALE"})
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGender.setAdapter(adapter);

        // Nhận dữ liệu được truyền qua Intent
        Intent intent = getIntent();
        if (intent != null) {
            // Lấy id và username từ intent
            id = intent.getStringExtra("id");
            Log.d("EditInformationActivity", "Id: " + id);
            username = intent.getStringExtra("username"); // Lưu username vào biến riêng
            Log.d("EditInformationActivity", "Username: " + username);
            etEmail.setText(intent.getStringExtra("email"));
            etFirstName.setText(intent.getStringExtra("firstName"));
            etLastName.setText(intent.getStringExtra("lastName"));
            etPhoneNumber.setText(intent.getStringExtra("phoneNumber"));
            etUserRank.setText(intent.getStringExtra("userRank"));

            // Xử lý dữ liệu giới tính nhận được từ intent:
            String genderFromIntent = intent.getStringExtra("gender");
            if (genderFromIntent != null) {
                if (genderFromIntent.equalsIgnoreCase("MALE")) {
                    spGender.setSelection(0);
                } else if (genderFromIntent.equalsIgnoreCase("FEMALE")) {
                    spGender.setSelection(1);
                } else {
                    spGender.setSelection(0); // Mặc định
                }
            }

            // Xử lý ngày sinh với định dạng "YYYY-MM-DD"
            String dob = intent.getStringExtra("dob");
            if (dob != null && dob.contains("-")) {
                String[] dobParts = dob.split("-");
                if (dobParts.length == 3) {
                    npYear.setValue(Integer.parseInt(dobParts[0]));
                    npMonth.setValue(Integer.parseInt(dobParts[1]));
                    npDay.setValue(Integer.parseInt(dobParts[2]));
                }
            }

            // Nếu có dữ liệu cho checkbox student (truyền boolean dưới dạng String "true"/"false")
            String studentStr = intent.getStringExtra("student");
            if (studentStr != null) {
                cbStudent.setChecked(Boolean.parseBoolean(studentStr));
            }
        }

        // Xử lý nút SAVE để gọi API update
        btnSave.setOnClickListener(v -> saveUserInfo());
    }

    /**
     * Lấy dữ liệu từ các view, đóng gói vào đối tượng UpdateMyInfor và gọi API cập nhật thông tin.
     */
    private void saveUserInfo() {
        // Lấy dữ liệu từ UI (không lấy từ etUsername vì username đã có trong biến riêng)
        String email       = etEmail.getText().toString().trim();
        String firstName   = etFirstName.getText().toString().trim();
        String lastName    = etLastName.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String userRank    = etUserRank.getText().toString().trim();

        // Lấy giá trị giới tính từ spinner
        String gender = spGender.getSelectedItem().toString().toUpperCase();


        boolean student = cbStudent.isChecked();

        // Tạo chuỗi ngày sinh theo định dạng "YYYY-MM-DD"
        String dob = npYear.getValue() + "-"
                + String.format("%02d", npMonth.getValue()) + "-"
                + String.format("%02d", npDay.getValue());

        // Tạo đối tượng UpdateMyInfor và đóng gói dữ liệu
        UpdateMyInfor updateUser = new UpdateMyInfor();
        updateUser.setId(id);
        updateUser.setUsername(username);  // Sử dụng biến username đã lưu
        updateUser.setEmail(email);
        updateUser.setFirstName(firstName);
        updateUser.setLastName(lastName);
        updateUser.setDob(dob);
        updateUser.setPhoneNumber(phoneNumber);
        updateUser.setUserRank(null);
        updateUser.setGender(gender);
        updateUser.setStudent(student);

        // Lấy token từ SessionManager
        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn, vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gọi API update thông tin người dùng
        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.updateMyInfo("Bearer " + token, updateUser).enqueue(new Callback<UpdateMyInfor>() {
            @Override
            public void onResponse(Call<UpdateMyInfor> call, Response<UpdateMyInfor> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(EditInformationActivity.this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Toast.makeText(EditInformationActivity.this, "Vui lòng chọn giới tính ", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<UpdateMyInfor> call, Throwable t) {
                Toast.makeText(EditInformationActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
