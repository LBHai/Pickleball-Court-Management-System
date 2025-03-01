package Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import Api.ApiService;
import SEP490.G9.LoginActivity;
import SEP490.G9.R;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AccountFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate layout cho Fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        setHasOptionsMenu(true); // Bật hiển thị menu trong Fragment
        ImageButton optionsButton = view.findViewById(R.id.options);
        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });
        return view;
    }
    private void showPopupMenu(View anchorView) {
        PopupMenu popupMenu = new PopupMenu(getContext(), anchorView);
        // Inflate file menu cho popup
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
        // Hiển thị PopupMenu
        popupMenu.show();
    }
    private void forceShowPopupMenuIcons(PopupMenu popupMenu) {
        try {
            // Lấy các trường (Field) của PopupMenu
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());

                    // Tìm phương thức setForceShowIcon(boolean)
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}