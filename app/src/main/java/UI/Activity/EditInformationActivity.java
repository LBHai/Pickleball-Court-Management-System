package UI.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import Data.Network.ApiService;
import Data.Network.RetrofitClient;
import Data.Model.MyInfo;
import Data.Model.MyInfoResponse;
import Data.Model.UpdateMyInfor;
import SEP490.G9.R;
import Data.Session.SessionManager;
import UI.Component.BadWordsLoader;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditInformationActivity extends AppCompatActivity {

    // UI elements matching the XML
    private EditText etEmail, etFirstName, etLastName, etPhoneNumber;
    private RadioGroup rgGender;
    private Spinner spDay, spMonth, spYear;
    private SwitchMaterial swStudent;
    private MaterialButton btnSave, btnCancel;
    private FloatingActionButton btnEditAvatar;
    private ShapeableImageView imgAvatar;
    private ImageButton btnBack;

    private String username, id;
    private ArrayAdapter<String> dayAdapter, monthAdapter, yearAdapter;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_PERMISSION = 100;

    private Uri selectedImageUri = null;
    private Uri photoUri; // URI for camera-captured image
    private SessionManager sessionManager;

    private static final String NAME_PATTERN = "^[\\p{L} ]{2,}$";
    private static final String PHONE_PATTERN = "^(\\+84|0)(3|5|7|8|9)[0-9]{8}$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_information);

        sessionManager = new SessionManager(this);

        // Initialize UI elements with IDs from XML
        etEmail = findViewById(R.id.etEmail);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        setupUnicodeValidation(etFirstName);
        setupUnicodeValidation(etLastName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        rgGender = findViewById(R.id.rgGender);
        spDay = findViewById(R.id.spDay);
        spMonth = findViewById(R.id.spMonth);
        spYear = findViewById(R.id.spYear);
        swStudent = findViewById(R.id.swStudent);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnEditAvatar = findViewById(R.id.btnEditAvatar);
        imgAvatar = findViewById(R.id.imgAvatar);
        btnBack = findViewById(R.id.btnBack);

        // Set click listeners
        btnBack.setOnClickListener(v -> finish());
        btnEditAvatar.setOnClickListener(v -> showImagePickerDialog());
        btnCancel.setOnClickListener(v -> finish());

        setupSpinners();
        setupValidationListeners();
        loadIntentData();
        loadUserInfo();
        btnSave.setOnClickListener(v -> {
            if (validateInputs()) {
                saveUserInfo();
            }
        });

        checkAndRequestPermissions();
    }

    private void setupSpinners() {
        // Day spinner (1-31)
        List<String> dayList = new ArrayList<>();
        for (int d = 1; d <= 31; d++) {
            dayList.add(String.valueOf(d));
        }
        dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dayList);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDay.setAdapter(dayAdapter);

        // Month spinner (1-12)
        List<String> monthList = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            monthList.add(String.valueOf(m));
        }
        monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, monthList);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMonth.setAdapter(monthAdapter);

        // Year spinner (1900-2100)
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

            // Set gender RadioGroup based on intent data
            String genderFromIntent = intent.getStringExtra("gender");
            if (genderFromIntent != null) {
                if (genderFromIntent.equalsIgnoreCase("MALE")) {
                    rgGender.check(R.id.rbMale);
                } else if (genderFromIntent.equalsIgnoreCase("FEMALE")) {
                    rgGender.check(R.id.rbFemale);
                }
            } else {
                rgGender.clearCheck();
            }

            // Set date of birth spinners
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

            // Set student switch
            boolean isStudent = intent.getBooleanExtra("student", false);
            swStudent.setChecked(isStudent);
        }
    }

    private void saveUserInfo() {
        String email = etEmail.getText().toString().trim();
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();

        // Get selected gender from RadioGroup
        int selectedId = rgGender.getCheckedRadioButtonId();
        String gender = null;
        if (selectedId == R.id.rbMale) {
            gender = "MALE";
        } else if (selectedId == R.id.rbFemale) {
            gender = "FEMALE";
        }

        // Construct date of birth
        String day = spDay.getSelectedItem().toString();
        String month = spMonth.getSelectedItem().toString();
        String year = spYear.getSelectedItem().toString();
        String dob = year + "-" + String.format("%02d", Integer.parseInt(month)) + "-" + String.format("%02d", Integer.parseInt(day));

        // Create update object
        UpdateMyInfor updateMyInfor = new UpdateMyInfor();
        updateMyInfor.setId(id);
        updateMyInfor.setUsername(username);
        updateMyInfor.setEmail(email);
        updateMyInfor.setFirstName(firstName);
        updateMyInfor.setLastName(lastName);
        updateMyInfor.setDob(dob);
        updateMyInfor.setPhoneNumber(phoneNumber);
        updateMyInfor.setGender(gender);

        String token = sessionManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, getString(R.string.session_expired), Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getApiService(this);
        apiService.updateMyInfo("Bearer " + token, updateMyInfor).enqueue(new Callback<UpdateMyInfor>() {
            @Override
            public void onResponse(Call<UpdateMyInfor> call, Response<UpdateMyInfor> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(EditInformationActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : getString(R.string.no_error_info);
                        Toast.makeText(EditInformationActivity.this, getString(R.string.update_failed) + ": " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Toast.makeText(EditInformationActivity.this, getString(R.string.update_failed), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<UpdateMyInfor> call, Throwable t) {
                Toast.makeText(EditInformationActivity.this, getString(R.string.connection_error) + ": " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkAndRequestPermissions() {
        String[] permissions = { Manifest.permission.CAMERA };
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(perm);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), REQUEST_PERMISSION);
        } else {
            btnEditAvatar.setEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            boolean allPermissionsGranted = true;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    Toast.makeText(this, getString(R.string.permission_required, permissions[i]), Toast.LENGTH_LONG).show();
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                        showPermissionSettingsDialog();
                    }
                }
            }
            btnEditAvatar.setEnabled(allPermissionsGranted);
        }
    }

    private void showPermissionSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.need_permission_title))
                .setMessage(getString(R.string.need_permission_message))
                .setPositiveButton(getString(R.string.go_to_settings), (dialog, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.select_photo));

        PackageManager packageManager = getPackageManager();
        boolean hasCamera = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);

        List<String> optionsList = new ArrayList<>();
        optionsList.add(getString(R.string.select_from_library));
        if (hasCamera) {
            optionsList.add(getString(R.string.camera));
        }

        String[] options = optionsList.toArray(new String[0]);
        builder.setItems(options, (dialog, which) -> {
            if (options[which].equals(getString(R.string.select_from_library))) {
                Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickIntent, REQUEST_IMAGE_PICK);
            } else if (options[which].equals(getString(R.string.camera))) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        Toast.makeText(this, getString(R.string.error_creating_image_file), Toast.LENGTH_SHORT).show();
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
        return File.createTempFile(imageFileName, ".jpg", storageDir);
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
                    Toast.makeText(this, getString(R.string.photo_from_gallery_error), Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                if (photoUri != null) {
                    selectedImageUri = photoUri;
                    imgAvatar.setImageURI(selectedImageUri);
                    uploadAvatar(selectedImageUri);
                } else {
                    Toast.makeText(this, getString(R.string.photo_large_size_error), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void uploadAvatar(Uri imageUri) {
        String mimeType = getContentResolver().getType(imageUri);
        if (mimeType == null || !mimeType.startsWith("image/")) {
            Toast.makeText(this, getString(R.string.select_image_error), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Toast.makeText(this, getString(R.string.image_read_error), Toast.LENGTH_SHORT).show();
                return;
            }

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            byte[] imageBytes = compressImage(bitmap);
            if (imageBytes == null) {
                Toast.makeText(this, getString(R.string.image_compress_error), Toast.LENGTH_LONG).show();
                return;
            }

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);
            MultipartBody.Part avatarPart = MultipartBody.Part.createFormData("file", "avatar.jpg", requestFile);
            RequestBody oldPathBody = RequestBody.create(MediaType.parse("text/plain"), "");

            String token = sessionManager.getToken();
            if (token == null || token.isEmpty()) {
                Toast.makeText(this, getString(R.string.session_expired), Toast.LENGTH_SHORT).show();
                return;
            }

            ApiService apiService = RetrofitClient.getApiService(this);
            Call<String> call = apiService.uploadAvatar("Bearer " + token, avatarPart, oldPathBody);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(EditInformationActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : getString(R.string.no_error_info);
                            Toast.makeText(EditInformationActivity.this, getString(R.string.update_failed) + ": " + errorBody, Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            Toast.makeText(EditInformationActivity.this, getString(R.string.update_failed), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(EditInformationActivity.this, getString(R.string.connection_error) + ": " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.upload_image_error), Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] compressImage(Bitmap bitmap) {
        final int MAX_SIZE = 250 * 1024; // 250KB
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int quality = 100;
        Bitmap currentBitmap = bitmap;

        while (quality > 0) {
            byteArrayOutputStream.reset();
            currentBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            if (imageBytes.length <= MAX_SIZE) {
                return imageBytes;
            }
            quality -= 10;
        }

        int width = currentBitmap.getWidth();
        int height = currentBitmap.getHeight();
        float scale = 0.9f;

        while (scale > 0.1f) {
            int newWidth = (int) (width * scale);
            int newHeight = (int) (height * scale);
            if (newWidth < 10 || newHeight < 10) break;

            currentBitmap = Bitmap.createScaledBitmap(currentBitmap, newWidth, newHeight, true);
            quality = 80;

            while (quality > 0) {
                byteArrayOutputStream.reset();
                currentBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();
                if (imageBytes.length <= MAX_SIZE) {
                    return imageBytes;
                }
                quality -= 10;
            }
            scale -= 0.1f;
        }
        return null;
    }

    private void loadUserInfo() {
        ApiService apiService = RetrofitClient.getApiService(this);
        String token = sessionManager.getToken();

        apiService.getMyInfo("Bearer " + token).enqueue(new Callback<MyInfoResponse>() {
            @Override
            public void onResponse(Call<MyInfoResponse> call, Response<MyInfoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MyInfo info = response.body().getResult();

                    // Xử lý avatar
                    String avatarUrl = info.getAvatarUrl();
                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        RequestOptions options = new RequestOptions()
                                .placeholder(R.drawable.avatar)
                                .error(R.drawable.avatar)
                                .diskCacheStrategy(DiskCacheStrategy.ALL);

                        Glide.with(EditInformationActivity.this)
                                .load(avatarUrl)
                                .apply(options)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(imgAvatar);
                    }

                    // Cập nhật switch student
                    updateStudentSwitch(info);

                    // Hiển thị thông tin gender
                    updateGenderDisplay(info.getGender());
                }
            }

            @Override
            public void onFailure(Call<MyInfoResponse> call, Throwable t) {
                imgAvatar.setImageResource(R.drawable.avatar);
                Toast.makeText(EditInformationActivity.this, getString(R.string.load_user_info_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupUnicodeValidation(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không cần xử lý
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cho phép tất cả ký tự Unicode, ngoại trừ control characters
                String input = s.toString();
                for (int i = 0; i < input.length(); i++) {
                    if (Character.isISOControl(input.charAt(i))) {
                        editText.setError(getString(R.string.no_control_characters_allowed));
                        return;
                    }
                }
                editText.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Không cần xử lý
            }
        });
    }

    private void updateStudentSwitch(MyInfo userInfo) {
        // Kiểm tra vai trò của người dùng
        boolean isStudent = false;
        if (userInfo != null && userInfo.getRoles() != null) {
            for (MyInfo.Role role : userInfo.getRoles()) {
                if (role.getName() != null && "STUDENT".equalsIgnoreCase(role.getName())) {
                    isStudent = true;
                    break;
                }
            }
        }

        // Cập nhật trạng thái của SwitchMaterial dựa trên vai trò
        swStudent.setChecked(isStudent);

        // Giữ nguyên logic ngăn người dùng thay đổi trạng thái
        swStudent.setEnabled(true); // Bật để hiển thị màu sắc bình thường
        swStudent.setClickable(false); // Không cho click để thay đổi
        swStudent.setFocusable(false); // Không cho focus
    }

    private void updateGenderDisplay(String gender) {
        if (gender == null) {
            rgGender.clearCheck();
            return;
        }

        switch (gender.toUpperCase()) {
            case "MALE":
                rgGender.check(R.id.rbMale);
                break;
            case "FEMALE":
                rgGender.check(R.id.rbFemale);
                break;
            default:
                rgGender.clearCheck();
                break;
        }
    }

    private void setupValidationListeners() {
        etFirstName.setOnFocusChangeListener((v, hasFocus) -> { if (!hasFocus) validateFirstName(); });
        etLastName.setOnFocusChangeListener((v, hasFocus) -> { if (!hasFocus) validateLastName(); });
        etPhoneNumber.setOnFocusChangeListener((v, hasFocus) -> { if (!hasFocus) validatePhoneNumber(); });
        spDay.setOnFocusChangeListener((v, hasFocus) -> { /* spinner focus change not common */ });
    }

    private boolean validateInputs() {
        return validateFirstName()
                & validateLastName()
                & validatePhoneNumber()
                & validateDOB();
    }

    private boolean validateFirstName() {
        String n = etFirstName.getText().toString().trim();
        if (n.isEmpty()) {
            etFirstName.setError(getString(R.string.error_first_name_empty));
            return false;
        }
        Set<String> badWords = new HashSet<>();
        badWords.addAll(BadWordsLoader.loadEnglishBadWords(this));
        badWords.addAll(BadWordsLoader.loadVietnameseBadWords(this));
        String lower = n.toLowerCase();
        for (String bad : badWords) {
            if (lower.contains(bad)) {
                etFirstName.setError(getString(R.string.error_first_name_sensitive_content));
                return false;
            }
        }
        if (!Pattern.matches(NAME_PATTERN, n)) {
            etFirstName.setError(getString(R.string.error_first_name_invalid));
            return false;
        }
        etFirstName.setError(null);
        return true;
    }

    private boolean validateLastName() {
        String n = etLastName.getText().toString().trim();
        if (n.isEmpty()) {
            etLastName.setError(getString(R.string.error_last_name_empty));
            return false;
        }
        Set<String> badWords = new HashSet<>();
        badWords.addAll(BadWordsLoader.loadEnglishBadWords(this));
        badWords.addAll(BadWordsLoader.loadVietnameseBadWords(this));
        String lower = n.toLowerCase();
        for (String bad : badWords) {
            if (lower.contains(bad)) {
                etLastName.setError(getString(R.string.error_last_name_sensitive_content));
                return false;
            }
        }
        if (!Pattern.matches(NAME_PATTERN, n)) {
            etLastName.setError(getString(R.string.error_last_name_invalid));
            return false;
        }
        etLastName.setError(null);
        return true;
    }

    private boolean validatePhoneNumber() {
        String pn = etPhoneNumber.getText().toString().trim();
        if (pn.isEmpty()) {
            etPhoneNumber.setError(getString(R.string.error_phone_number_empty));
            return false;
        }
        if (!Pattern.matches(PHONE_PATTERN, pn)) {
            etPhoneNumber.setError(getString(R.string.error_phone_number_invalid));
            return false;
        }
        etPhoneNumber.setError(null);
        return true;
    }

    private boolean validateDOB() {
        String d = spDay.getSelectedItem().toString();
        String m = spMonth.getSelectedItem().toString();
        String y = spYear.getSelectedItem().toString();
        try {
            Calendar c = Calendar.getInstance();
            c.setLenient(false);
            c.set(Integer.parseInt(y), Integer.parseInt(m) - 1, Integer.parseInt(d));
            c.getTime();
            Calendar t = Calendar.getInstance();
            int age = t.get(Calendar.YEAR) - Integer.parseInt(y);
            if (t.get(Calendar.DAY_OF_YEAR) < c.get(Calendar.DAY_OF_YEAR)) age--;
            if (age < 10) {
                Toast.makeText(this, getString(R.string.error_age_under_10), Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        } catch (Exception ex) {
            Toast.makeText(this, getString(R.string.error_invalid_dob), Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}