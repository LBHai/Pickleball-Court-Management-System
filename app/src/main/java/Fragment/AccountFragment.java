package Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import Api.ApiService;
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
        // Inflate layout cho Fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        setHasOptionsMenu(true); // Bật hiển thị menu trong Fragment

        // Ánh xạ các view hiển thị thông tin người dùng
        tvUserName = view.findViewById(R.id.userName);
        tvPhoneNumber = view.findViewById(R.id.phoneNumber);

        // Khởi tạo SessionManager để lấy token đã lưu
        sessionManager = new SessionManager(getContext());

        // Gọi API lấy thông tin người dùng (getMyInfo)
        getMyInfo();

        // Cấu hình nút Options trong header
        ImageButton optionsButton = view.findViewById(R.id.options);
        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });
        return view;
    }

    /**
     * Gọi API lấy thông tin người dùng dựa trên token lưu trong session.
     * Ở đây ta kiểm tra HTTP status code thông qua response.isSuccessful()
     */
    private void getMyInfo() {
        String token = sessionManager.getToken();
        if (token != null && !token.isEmpty()) {
            // Tạo header với định dạng "Bearer {token}"
            String authHeader = "Bearer " + token;
            ApiService.apiService.getMyInfo(authHeader).enqueue(new Callback<MyInfoResponse>() {
                @Override
                public void onResponse(Call<MyInfoResponse> call, Response<MyInfoResponse> response) {
                    if (response.isSuccessful()) { // Kiểm tra HTTP status code là 200 OK
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
                        Toast.makeText(getContext(), "Lấy thông tin thất bại: HTTP " + response.code() + " - " + response.message(), Toast.LENGTH_SHORT).show();
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
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.menu_edit) {
                    // Xử lý "Chỉnh sửa thông tin"
                    return true;
                } else if (id == R.id.menu_changepassword) {
                    // Xử lý "Thay đổi mật khẩu"
                    return true;
                } else if (id == R.id.menu_transalate) {
                    // Xử lý "Ngôn ngữ"
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
            }
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
        // Xóa token khỏi session
        sessionManager.clearSession();

        // Chuyển về màn hình đăng nhập
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Xóa toàn bộ stack
        startActivity(intent);
        getActivity().finish(); // Đóng AccountFragment
    }

}
