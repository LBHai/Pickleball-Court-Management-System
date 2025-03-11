package Fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

import Api.ApiService;
import Api.RetrofitClient;
import Model.MyInfo;
import Model.MyInfoResponse;
import SEP490.G9.EditInformationActivity;
import SEP490.G9.LoginActivity;
import SEP490.G9.R;
import Session.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountFragment extends Fragment {

    // Các view hiển thị trên AccountFragment
    private TextView tvUserName, tvPhoneNumber;
    // Các biến lưu trữ dữ liệu người dùng lấy từ API
    private String id, username, email, firstName, lastName, userRank, gender, dob;

    private SessionManager sessionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Kiểm tra trạng thái đăng nhập
        sessionManager = new SessionManager(getContext());
        if (sessionManager.getToken() == null || sessionManager.getToken().isEmpty()) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
            return null;
        }

        // Inflate layout cho Fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        // Bắt buộc sử dụng options menu nếu cần
        setHasOptionsMenu(true);

        // Ánh xạ các view từ XML
        tvUserName = view.findViewById(R.id.userName);
        tvPhoneNumber = view.findViewById(R.id.phoneNumber);

        // Gọi API để lấy thông tin người dùng
        getMyInfo();

        // Xử lý nút Options (PopupMenu) có trong layout với id: options
        ImageButton optionsButton = view.findViewById(R.id.options);
        optionsButton.setOnClickListener(v -> showPopupMenu(v));

        return view;
    }

    /**
     * Gọi API lấy thông tin người dùng và lưu dữ liệu vào các biến toàn cục.
     */
    private void getMyInfo() {
        String token = sessionManager.getToken();
        if (token != null && !token.isEmpty()) {
            String authHeader = "Bearer " + token;
            ApiService apiService = RetrofitClient.getApiService(getContext());
            apiService.getMyInfo(authHeader).enqueue(new Callback<MyInfoResponse>() {
                @Override
                public void onResponse(Call<MyInfoResponse> call, Response<MyInfoResponse> response) {
                    if (response.isSuccessful()) {
                        MyInfoResponse myInfoResponse = response.body();
                        if (myInfoResponse != null && myInfoResponse.getResult() != null) {
                            // Lưu dữ liệu lấy được từ API vào các biến
                            id = myInfoResponse.getResult().getId();
                            username = myInfoResponse.getResult().getUsername();
                            firstName = myInfoResponse.getResult().getFirstName();
                            lastName = myInfoResponse.getResult().getLastName();
                            email = myInfoResponse.getResult().getEmail();
                            userRank = myInfoResponse.getResult().getUserRank();
                            gender = myInfoResponse.getResult().getGender();
                            dob = myInfoResponse.getResult().getDob();

                            // Cập nhật giao diện (chỉ hiển thị tên và số điện thoại)
                            tvUserName.setText(firstName + " " + lastName);
                            tvPhoneNumber.setText(myInfoResponse.getResult().getPhoneNumber());
                        } else {
                            Toast.makeText(getContext(), "Dữ liệu trả về không hợp lệ", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Lấy thông tin thất bại, vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<MyInfoResponse> call, Throwable t) {
                    Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "Token không tồn tại, vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Hiển thị PopupMenu và xử lý sự kiện khi chọn từng mục.
     */
    private void showPopupMenu(View anchorView) {
        PopupMenu popupMenu = new PopupMenu(getContext(), anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu_in_option, popupMenu.getMenu());
        forceShowPopupMenuIcons(popupMenu);
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_edit) {
                // Khi chọn "Edit", tạo Intent và truyền dữ liệu qua extras
                Intent intent = new Intent(getActivity(), EditInformationActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("username", username);
                intent.putExtra("email", email);
                intent.putExtra("firstName", firstName);
                intent.putExtra("lastName", lastName);
                intent.putExtra("phoneNumber", tvPhoneNumber.getText().toString());
                intent.putExtra("userRank", userRank);
                intent.putExtra("gender", gender);
                intent.putExtra("dob", dob);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.menu_changepassword) {
                // Xử lý thay đổi mật khẩu
                return true;
            } else if (itemId == R.id.menu_transalate) {
                showLanguageDialog();
                return true;
            } else if (itemId == R.id.menu_logout) {
                logoutUser();
                return true;
            } else if (itemId == R.id.menu_version) {
                // Xử lý version
                return true;
            } else if (itemId == R.id.menu_deleteaccount) {
                // Xử lý xóa tài khoản
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    /**
     * Bắt buộc hiển thị icon trong PopupMenu.
     */
    private void forceShowPopupMenuIcons(PopupMenu popupMenu) {
        try {
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Xử lý đăng xuất người dùng.
     */
    private void logoutUser() {
        sessionManager.clearSession();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    /**
     * Hiển thị dialog chọn ngôn ngữ.
     */
    private void showLanguageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.choose_language));
        String[] languages = {"English", "Tiếng Việt"};
        int[] icons = {R.drawable.usa_flag, R.drawable.vn_flag};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.select_dialog_item, languages) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.select_dialog_item, parent, false);
                }
                TextView textView = convertView.findViewById(android.R.id.text1);
                textView.setText(languages[position]);
                textView.setTextSize(18);
                Drawable drawable = ContextCompat.getDrawable(getContext(), icons[position]);
                if (drawable != null) {
                    drawable.setBounds(0, 0, 80, 80);
                    textView.setCompoundDrawables(drawable, null, null, null);
                    textView.setCompoundDrawablePadding(20);
                }
                return convertView;
            }
        };

        builder.setAdapter(adapter, (dialog, which) -> {
            String languageCode = (which == 0) ? "en" : "vi";
            changeLanguage(languageCode);
        });
        builder.show();
    }

    /**
     * Đổi ngôn ngữ và khởi động lại Activity.
     */
    private void changeLanguage(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
        // Khởi động lại Activity hiện tại
        Intent refresh = getActivity().getIntent();
        getActivity().finish();
        startActivity(refresh);
    }
}
