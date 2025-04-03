package Fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import Api.ApiService;
import Api.RetrofitClient;
import Model.MyInfo;
import Model.MyInfoResponse;
import Model.Orders;
import SEP490.G9.ChangePassword;
import SEP490.G9.DetailBookingActivity;
import SEP490.G9.EditInformationActivity;
import SEP490.G9.LoginActivity;
import SEP490.G9.NotificationActivity;
import SEP490.G9.R;
import SEP490.G9.SignUpActivity;
import Session.SessionManager;
import Adapter.OrderAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountFragment extends Fragment {

    private TextView tvUserName, tvPhoneNumber;
    private TextView tvTabBooked, tvTabInfoMember, tvMemberInfo;
    private ImageButton btnNoti, btnOptions; // Thêm biến cho nút options

    private String id, username, email, firstName, lastName, userRank, gender, dob;
    private SessionManager sessionManager;

    private RecyclerView recyclerOrder;
    private OrderAdapter orderAdapter;
    private List<Orders> orderList;
    private boolean student;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sessionManager = new SessionManager(getContext());
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        setHasOptionsMenu(true);

        tvUserName = view.findViewById(R.id.userName);
        tvPhoneNumber = view.findViewById(R.id.phoneNumber);
        btnNoti = view.findViewById(R.id.notification);
        btnOptions = view.findViewById(R.id.options); // Ánh xạ nút options

        tvTabBooked = view.findViewById(R.id.tvTabBooked);
        tvTabInfoMember = view.findViewById(R.id.tvTabInfoMember);
        tvMemberInfo = view.findViewById(R.id.tvMemberInfo);

        recyclerOrder = view.findViewById(R.id.listOrderHistory);
        recyclerOrder.setLayoutManager(new LinearLayoutManager(getContext()));
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(orderList, getContext());
        recyclerOrder.setAdapter(orderAdapter);

        // Ẩn nút options nếu người dùng chưa đăng nhập
        if (sessionManager.getToken() == null || sessionManager.getToken().isEmpty()) {
            btnOptions.setVisibility(View.GONE);
        } else {
            btnOptions.setVisibility(View.VISIBLE);
        }

        btnNoti.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NotificationActivity.class);
            startActivity(intent);
        });

        btnOptions.setOnClickListener(this::showPopupMenu);

        showBookedTab();
        tvTabBooked.setOnClickListener(v -> showBookedTab());
        tvTabInfoMember.setOnClickListener(v -> showMemberInfoTab());

        // Kiểm tra trạng thái đăng nhập
        if (sessionManager.getToken() != null && !sessionManager.getToken().isEmpty()) {
            // Người dùng đã đăng nhập: load thông tin user và đơn đặt của user
            getMyInfo();
        } else {
            // Guest: hiển thị dialog khuyến khích tạo tài khoản (chỉ hiển thị một lần)
            if (!sessionManager.hasShownGuestDialog()) {
                showGuestDialog();
                sessionManager.setHasShownGuestDialog(true);
            }
            // Load danh sách đơn đặt của guest
            getAllOrderListForGuest();
        }
        return view;
    }

    private void getAllOrderListForGuest() {
        List<String> guestPhones = sessionManager.getGuestPhones();
        if (guestPhones == null || guestPhones.isEmpty()) {
            Toast.makeText(getContext(), "Chưa có số điện thoại được sử dụng để đặt lịch.", Toast.LENGTH_SHORT).show();
            return;
        }
        orderList.clear();
        final int[] callsRemaining = {guestPhones.size()};
        for (String phone : guestPhones) {
            ApiService apiService = RetrofitClient.getApiService(getContext());
            apiService.getOrders(phone).enqueue(new Callback<List<Orders>>() {
                @Override
                public void onResponse(Call<List<Orders>> call, Response<List<Orders>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        orderList.addAll(response.body());
                    }
                    callsRemaining[0]--;
                    if (callsRemaining[0] == 0) {
                        orderAdapter.notifyDataSetChanged();
                        recyclerOrder.setVisibility(View.VISIBLE);
                    }
                }
                @Override
                public void onFailure(Call<List<Orders>> call, Throwable t) {
                    callsRemaining[0]--;
                    if (callsRemaining[0] == 0) {
                        orderAdapter.notifyDataSetChanged();
                        recyclerOrder.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    private void showGuestDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Tạo tài khoản để dễ dàng quản lý và lưu trữ lịch đặt của bạn.");

        builder.setPositiveButton("Đăng nhập", (dialog, which) -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        });

        builder.setNegativeButton("Đăng ký", (dialog, which) -> {
            Intent intent = new Intent(getActivity(), SignUpActivity.class);
            startActivity(intent);
            getActivity().finish();
        });

        builder.setNeutralButton("OK", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
                .setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
                .setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green));
    }

    private void showBookedTab() {
        tvTabBooked.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
        tvTabInfoMember.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
        recyclerOrder.setVisibility(View.VISIBLE);
        tvMemberInfo.setVisibility(View.GONE);
    }

    private void showMemberInfoTab() {
        tvTabBooked.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
        tvTabInfoMember.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
        recyclerOrder.setVisibility(View.GONE);
        tvMemberInfo.setVisibility(View.VISIBLE);
    }

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
                            MyInfo info = myInfoResponse.getResult();
                            id = info.getId();
                            Log.d("AccountFragment", "User logged in with ID: " + id);

                            username = info.getUsername();
                            firstName = info.getFirstName();
                            lastName = info.getLastName();
                            email = info.getEmail();
                            userRank = info.getUserRank();
                            gender = info.getGender();
                            dob = info.getDob();
                            student = info.isStudent();
                            tvUserName.setText(firstName + " " + lastName);
                            tvPhoneNumber.setText(info.getPhoneNumber());

                            getOrderList(id);
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

    private void getOrderList(String userId) {
        ApiService apiService = RetrofitClient.getApiService(getContext());
        apiService.getOrders(userId).enqueue(new Callback<List<Orders>>() {
            @Override
            public void onResponse(Call<List<Orders>> call, Response<List<Orders>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Orders> orders = response.body();
                    orderList.clear();
                    orderList.addAll(orders);
                    orderAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Không có đơn đặt sân nào", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Orders>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPopupMenu(View anchorView) {
        PopupMenu popupMenu = new PopupMenu(getContext(), anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu_in_option, popupMenu.getMenu());
        forceShowPopupMenuIcons(popupMenu);
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_edit) {
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
                intent.putExtra("student", student);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.menu_changepassword) {
                Intent intent = new Intent(getActivity(), ChangePassword.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.menu_transalate) {
                showLanguageDialog();
                return true;
            } else if (itemId == R.id.menu_logout) {
                logoutUser();
                return true;
            } else if (itemId == R.id.menu_version) {
                return true;
            } else if (itemId == R.id.menu_deleteaccount) {
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
        String userId = sessionManager.getUserId();
        Log.d("AccountFragment", "User logged out with ID: " + userId);
        sessionManager.clearSession();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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

        Intent refresh = getActivity().getIntent();
        getActivity().finish();
        startActivity(refresh);
    }
}