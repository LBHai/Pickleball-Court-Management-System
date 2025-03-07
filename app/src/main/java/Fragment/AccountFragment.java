package Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import Model.MyInfoResponse;
import SEP490.G9.LoginActivity;
import SEP490.G9.R;
import Session.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountFragment extends Fragment {

    private TextView tvUserName, tvPhoneNumber;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Kiểm tra nếu chưa đăng nhập, chuyển về LoginActivity
        sessionManager = new SessionManager(getContext());
        if (sessionManager.getToken() == null || sessionManager.getToken().isEmpty()) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish(); // Đóng MainActivity nếu cần
            return null;
        }

        View view = inflater.inflate(R.layout.fragment_account, container, false);
        setHasOptionsMenu(true);

        tvUserName = view.findViewById(R.id.userName);
        tvPhoneNumber = view.findViewById(R.id.phoneNumber);

        // Gọi API lấy thông tin người dùng
        getMyInfo();

        ImageButton optionsButton = view.findViewById(R.id.options);
        optionsButton.setOnClickListener(v -> showPopupMenu(v));

        return view;
    }

    /**
     * Gọi API lấy thông tin người dùng dựa trên token lưu trong session.
     * Sử dụng RetrofitClient để lấy ApiService.
     */
    private void getMyInfo() {
        String token = sessionManager.getToken();
        if (token != null && !token.isEmpty()) {
            String authHeader = "Bearer " + token;
            // Lấy đối tượng ApiService qua RetrofitClient
            ApiService apiService = RetrofitClient.getApiService(getContext());
            apiService.getMyInfo(authHeader).enqueue(new Callback<MyInfoResponse>() {
                @Override
                public void onResponse(Call<MyInfoResponse> call, Response<MyInfoResponse> response) {
                    if (response.isSuccessful()) { // HTTP 200 OK
                        MyInfoResponse myInfoResponse = response.body();
                        if (myInfoResponse != null && myInfoResponse.getResult() != null) {
                            String fullName = myInfoResponse.getResult().getFirstName() + " " +
                                    myInfoResponse.getResult().getLastName();
                            tvUserName.setText(fullName);
                            tvPhoneNumber.setText(myInfoResponse.getResult().getPhoneNumber());
                        } else {
                            Toast.makeText(getContext(), "Dữ liệu trả về không hợp lệ", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Lấy thông tin thất bại, vui lòng đăng nhập lại!!!", Toast.LENGTH_SHORT).show();
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
     * Hiển thị PopupMenu cho các tùy chọn trong AccountFragment.
     */
    private void showPopupMenu(View anchorView) {
        PopupMenu popupMenu = new PopupMenu(getContext(), anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu_in_option, popupMenu.getMenu());
        forceShowPopupMenuIcons(popupMenu);
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_edit) {
                // Xử lý "Chỉnh sửa thông tin"
                return true;
            } else if (id == R.id.menu_changepassword) {
                // Xử lý "Thay đổi mật khẩu"
                return true;
            } else if (id == R.id.menu_transalate) {
                showLanguageDialog();
                return true;
            } else if (id == R.id.menu_logout) {
                logoutUser();
                return true;
            } else if (id == R.id.menu_version) {
                // Xử lý "Version"
                return true;
            } else if (id == R.id.menu_deleteaccount) {
                // Xử lý "Xóa tài khoản"
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

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

    private void logoutUser() {
        sessionManager.clearSession();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Xóa toàn bộ stack
        startActivity(intent);
        getActivity().finish();
    }

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
