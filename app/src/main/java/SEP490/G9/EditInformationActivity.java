package SEP490.G9;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.imageview.ShapeableImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Api.ApiService;
import Api.RetrofitClient;
import Model.UpdateMyInfor;
import Session.SessionManager;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditInformationActivity extends AppCompatActivity {

    private EditText etEmail, etFirstName, etLastName, etPhoneNumber, etUserRank;
    private Spinner spGender, spDay, spMonth, spYear;
    private CheckBox cbStudent;
    private Button btnSave;
    private ImageButton btnEditAvatar;
    private ShapeableImageView imgAvatar;

    private String username, id;

    private ArrayAdapter<String> dayAdapter, monthAdapter, yearAdapter;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_PERMISSION = 100;

    private Uri selectedImageUri = null;
    private Uri photoUri; // Lưu URI của ảnh chụp từ camera

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_information);

        etEmail = findViewById(R.id.etEmail);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etUserRank = findViewById(R.id.etUserRank);
        spGender = findViewById(R.id.spGender);
        spDay = findViewById(R.id.spDay);
        spMonth = findViewById(R.id.spMonth);
        spYear = findViewById(R.id.spYear);
        cbStudent = findViewById(R.id.cbStudent);
        btnSave = findViewById(R.id.btnSave);
        btnEditAvatar = findViewById(R.id.btnEditAvatar);
        imgAvatar = findViewById(R.id.imgAvatar);

        setupSpinners();
        loadIntentData();

        btnEditAvatar.setOnClickListener(v -> showImagePickerDialog());
        btnSave.setOnClickListener(v -> saveUserInfo());

        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        String[] permissions = {
                Manifest.permission.CAMERA
        };
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(perm);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[0]), REQUEST_PERMISSION);
        } else {
            btnEditAvatar.setEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            boolean allPermissionsGranted = true;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    Toast.makeText(this, "Bạn cần cấp quyền " + permissions[i] + " để sử dụng tính năng này", Toast.LENGTH_LONG).show();
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                        showPermissionSettingsDialog();
                    }
                }
            }
            if (!allPermissionsGranted) {
                btnEditAvatar.setEnabled(false);
            } else {
                btnEditAvatar.setEnabled(true);
            }
        }
    }

    private void showPermissionSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Cần cấp quyền")
                .setMessage("Ứng dụng cần quyền truy cập camera để chụp ảnh. Vui lòng cấp quyền trong cài đặt.")
                .setPositiveButton("Đi tới cài đặt", (dialog, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn ảnh");

        PackageManager packageManager = getPackageManager();
        boolean hasCamera = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);

        List<String> optionsList = new ArrayList<>();
        optionsList.add("Chọn từ thư viện");
        if (hasCamera) {
            optionsList.add("Máy ảnh");
        }

        String[] options = optionsList.toArray(new String[0]);
        builder.setItems(options, (dialog, which) -> {
            if (options[which].equals("Chọn từ thư viện")) {
                Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickIntent, REQUEST_IMAGE_PICK);
            } else if (options[which].equals("Máy ảnh")) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        Toast.makeText(this, "Lỗi khi tạo file ảnh", Toast.LENGTH_SHORT).show();
                    }
                    if (photoFile != null) {
                        photoUri = FileProvider.getUriForFile(this, "SEP490.G9.fileprovider", photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        });
        builder.show();
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    imgAvatar.setImageURI(selectedImageUri);
                    uploadAvatar(selectedImageUri);
                } else {
                    Toast.makeText(this, "Không thể lấy ảnh từ thư viện", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                if (photoUri != null) {
                    selectedImageUri = photoUri;
                    imgAvatar.setImageURI(selectedImageUri);
                    uploadAvatar(selectedImageUri);
                } else {
                    Toast.makeText(this, "Không thể lấy ảnh từ camera", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.gender_array,
                android.R.layout.simple_spinner_item
        );
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGender.setAdapter(genderAdapter);

        List<String> dayList = new ArrayList<>();
        for (int d = 1; d <= 31; d++) {
            dayList.add(String.valueOf(d));
        }
        dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dayList);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDay.setAdapter(dayAdapter);

        List<String> monthList = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            monthList.add(String.valueOf(m));
        }
        monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, monthList);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMonth.setAdapter(monthAdapter);

        List<String> yearList = new ArrayList<>();
        for (int y = 1900; y <= 2100; y++) {
            yearList.add(String.valueOf(y));
        }
        yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, yearList);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spYear.setAdapter(yearAdapter);
    }

    private void loadIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            id = intent.getStringExtra("id");
            username = intent.getStringExtra("username");
            etEmail.setText(intent.getStringExtra("email"));
            etFirstName.setText(intent.getStringExtra("firstName"));
            etLastName.setText(intent.getStringExtra("lastName"));
            etPhoneNumber.setText(intent.getStringExtra("phoneNumber"));
            etUserRank.setText(intent.getStringExtra("userRank"));

            String genderFromIntent = intent.getStringExtra("gender");
            ArrayAdapter<CharSequence> genderAdapter = (ArrayAdapter<CharSequence>) spGender.getAdapter();
            if (genderFromIntent == null) {
                spGender.setSelection(0);
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

            String dob = intent.getStringExtra("dob");
            if (dob != null && dob.contains("-")) {
                String[] dobParts = dob.split("-");
                if (dobParts.length == 3) {
                    String yearStr = dobParts[0];
                    String monthStr = String.valueOf(Integer.parseInt(dobParts[1]));
                    String dayStr = String.valueOf(Integer.parseInt(dobParts[2]));

                    int yearPos = yearAdapter.getPosition(yearStr);
                    if (yearPos >= 0) spYear.setSelection(yearPos);

                    int monthPos = monthAdapter.getPosition(monthStr);
                    if (monthPos >= 0) spMonth.setSelection(monthPos);

                    int dayPos = dayAdapter.getPosition(dayStr);
                    if (dayPos >= 0) spDay.setSelection(dayPos);
                }
            }
            boolean isStudent = getIntent().getBooleanExtra("student", false);
            cbStudent.setChecked(isStudent);
        }
    }

    private void saveUserInfo() {
        String email = etEmail.getText().toString().trim();
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String userRank = etUserRank.getText().toString().trim();

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

        String day = spDay.getSelectedItem().toString();
        String month = spMonth.getSelectedItem().toString();
        String year = spYear.getSelectedItem().toString();
        String dob = year + "-" + String.format("%02d", Integer.parseInt(month))
                + "-" + String.format("%02d", Integer.parseInt(day));

        UpdateMyInfor updateMyInfor = new UpdateMyInfor();
        updateMyInfor.setId(id);
        updateMyInfor.setUsername(username);
        updateMyInfor.setEmail(email);
        updateMyInfor.setFirstName(firstName);
        updateMyInfor.setLastName(lastName);
        updateMyInfor.setDob(dob);
        updateMyInfor.setPhoneNumber(phoneNumber);
        updateMyInfor.setGender(gender);
        updateMyInfor.setStudent(student);

        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn, vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.updateMyInfo("Bearer " + token, updateMyInfor).enqueue(new Callback<UpdateMyInfor>() {
            @Override
            public void onResponse(Call<UpdateMyInfor> call, Response<UpdateMyInfor> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(EditInformationActivity.this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        String errorBody = (response.errorBody() != null)
                                ? response.errorBody().string() : "Không có thông tin lỗi";
                        Toast.makeText(EditInformationActivity.this, "Cập nhật thất bại: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(EditInformationActivity.this, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<UpdateMyInfor> call, Throwable t) {
                Toast.makeText(EditInformationActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadAvatar(Uri imageUri) {
        // Kiểm tra mimeType để đảm bảo là ảnh
        String mimeType = getContentResolver().getType(imageUri);
        if (mimeType == null || !mimeType.startsWith("image/")) {
            Toast.makeText(this, "Vui lòng chọn một file ảnh!", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("UploadAvatar", "File MIME type: " + mimeType);

        // Đọc dữ liệu từ Uri mà không cần chuyển thành File
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Toast.makeText(this, "Không thể đọc ảnh", Toast.LENGTH_SHORT).show();
                return;
            }

            // Đọc ảnh vào Bitmap để nén
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            // Nén ảnh để đảm bảo kích thước dưới 250KB
            byte[] imageBytes = compressImage(bitmap);
            if (imageBytes == null) {
                Toast.makeText(this, "Không thể nén ảnh xuống dưới 250KB. Vui lòng chọn ảnh khác!", Toast.LENGTH_LONG).show();
                return;
            }
            Log.d("UploadAvatar", "Compressed image size: " + imageBytes.length + " bytes");

            // Tạo RequestBody từ dữ liệu byte
            // Sau khi nén, ảnh sẽ ở định dạng JPEG
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);
            String fileName = "avatar.jpg"; // Sau khi nén, luôn là JPEG
            MultipartBody.Part avatarPart = MultipartBody.Part.createFormData("file", fileName, requestFile);

            // Tạo RequestBody cho oldPath
            RequestBody oldPathBody = RequestBody.create(MediaType.parse("text/plain"), "");

            SessionManager sessionManager = new SessionManager(this);
            String token = sessionManager.getToken();
            if (token == null || token.isEmpty()) {
                Toast.makeText(this, "Token không hợp lệ. Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d("UploadAvatar", "Token: " + token);

            ApiService apiService = RetrofitClient.getApiService(this);
            Call<String> call = apiService.uploadAvatar("Bearer " + token, avatarPart, oldPathBody);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            String serverMessage = response.body();
                            Toast.makeText(EditInformationActivity.this, serverMessage, Toast.LENGTH_SHORT).show();
                            Log.d("UploadAvatar", "Server response: " + serverMessage);
                        } else {
                            Toast.makeText(EditInformationActivity.this, "Upload thành công, nhưng body rỗng!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Phản hồi rỗng";
                            Log.e("UploadAvatar", "Error code: " + response.code() + ", error body: " + errorBody);
                            Toast.makeText(EditInformationActivity.this, "Upload thất bại: " + response.code() + " - " + errorBody, Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(EditInformationActivity.this, "Upload thất bại!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.e("UploadAvatar", "Lỗi kết nối: " + t.getMessage(), t);
                    Toast.makeText(EditInformationActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi đọc ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private byte[] compressImage(Bitmap bitmap) {
        final int MAX_SIZE = 250 * 1024; // 250KB = 250 * 1024 bytes
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int quality = 100; // Chất lượng ban đầu (0-100)
        Bitmap currentBitmap = bitmap;

        // Bước 1: Thử nén với chất lượng giảm dần
        while (quality > 0) {
            byteArrayOutputStream.reset(); // Xóa dữ liệu cũ
            currentBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            if (imageBytes.length <= MAX_SIZE) {
                return imageBytes; // Kích thước đạt yêu cầu
            }
            quality -= 10; // Giảm chất lượng 10% mỗi lần
        }

        // Bước 2: Nếu không thể đạt 250KB bằng cách giảm chất lượng, thu nhỏ kích thước ảnh
        int width = currentBitmap.getWidth();
        int height = currentBitmap.getHeight();
        float scale = 0.9f; // Bắt đầu giảm kích thước 10%

        while (scale > 0.1f) { // Giới hạn scale tối thiểu để tránh ảnh quá nhỏ
            int newWidth = (int) (width * scale);
            int newHeight = (int) (height * scale);
            if (newWidth < 10 || newHeight < 10) break; // Tránh kích thước quá nhỏ

            // Tạo Bitmap mới với kích thước nhỏ hơn
            currentBitmap = Bitmap.createScaledBitmap(currentBitmap, newWidth, newHeight, true);
            quality = 80; // Đặt lại chất lượng trung bình

            // Thử nén lại với kích thước mới
            while (quality > 0) {
                byteArrayOutputStream.reset();
                currentBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();
                if (imageBytes.length <= MAX_SIZE) {
                    return imageBytes; // Kích thước đạt yêu cầu
                }
                quality -= 10;
            }

            scale -= 0.1f; // Giảm kích thước thêm 10% mỗi lần
        }

        // Nếu vẫn không thể đạt 250KB, trả về null
        return null;
    }
}