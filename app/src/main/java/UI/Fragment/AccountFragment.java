package UI.Fragment;

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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.nex3z.notificationbadge.NotificationBadge;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Data.Network.ApiService;
import Data.Network.RetrofitClient;
import Data.Holder.OrderServiceHolder;
import Data.Model.MyInfo;
import Data.Model.MyInfoResponse;
import Data.Model.NotificationItem;
import Data.Model.NotificationResponse;
import Data.Model.Orders;
import UI.Activity.ChangePasswordActivity;
import UI.Activity.DetailBookingActivity;
import UI.Activity.EditInformationActivity;
import UI.Activity.LoginActivity;
import UI.Activity.NotificationActivity;
import SEP490.G9.R;
import UI.Activity.SignUpActivity;
import Data.Session.SessionManager;
import UI.Adapter.OrderAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountFragment extends Fragment {

    private TextView tvUserName, tvPhoneNumber;
    private TextView tvTabBooked, tvServiceInfo, tvServiceOrderInfo;
    private ImageButton btnNoti, btnOptions, btnFilter;
    private String id, username, email, firstName, lastName, userRank, gender, dob;
    private SessionManager sessionManager;
    private RecyclerView recyclerOrder;
    private OrderAdapter orderAdapter;
    private List<Orders> orderList;
    private List<Orders> filteredOrderList;
    private boolean student;
    private ImageView ivAvatar;
    private NotificationBadge badge;
    private List<NotificationItem> notificationList;
    private int unreadCount = 0;
    private LinearLayout authButtonsContainer;
    private Button btnLogin, btnRegister;
    private List<Orders> bookedOrders;  // Thêm danh sách cho "Lịch đã đặt"
    private List<Orders> serviceOrders; // Thêm danh sách cho "Dịch vụ đã đặt"
    private static final int REFRESH_INTERVAL = 2000;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sessionManager = new SessionManager(getContext());
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        setHasOptionsMenu(true);
        tvUserName = view.findViewById(R.id.userName);
        tvPhoneNumber = view.findViewById(R.id.phoneNumber);
        btnNoti = view.findViewById(R.id.notification);
        btnOptions = view.findViewById(R.id.options);
        btnFilter = view.findViewById(R.id.btn_filter);
        ivAvatar = view.findViewById(R.id.ivAvatar);
        tvTabBooked = view.findViewById(R.id.tvTabBooked);
        tvServiceInfo = view.findViewById(R.id.tvServiceInfo);
        tvServiceOrderInfo = view.findViewById(R.id.tvServiceOrderInfo);

        authButtonsContainer = view.findViewById(R.id.authButtonsContainer);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnRegister = view.findViewById(R.id.btnRegister);

        recyclerOrder = view.findViewById(R.id.listOrderHistory);
        recyclerOrder.setLayoutManager(new LinearLayoutManager(getContext()));
        orderList = new ArrayList<>();
        filteredOrderList = new ArrayList<>();

        bookedOrders = new ArrayList<>();  // Khởi tạo danh sách
        serviceOrders = new ArrayList<>();

        orderAdapter = new OrderAdapter(filteredOrderList, getContext());
        recyclerOrder.setAdapter(orderAdapter);
        badge = view.findViewById(R.id.badge);
        getNotifications();


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

        btnFilter.setOnClickListener(this::showFilterMenu);

        showBookedTab();
        tvTabBooked.setOnClickListener(v -> showBookedTab());
        tvServiceInfo.setOnClickListener(v -> showMemberInfoTab());

        if (sessionManager.getToken() != null && !sessionManager.getToken().isEmpty()) {
            getMyInfo();
        } else {
            if (!sessionManager.hasShownGuestDialog()) {
                showGuestDialog();
                sessionManager.setHasShownGuestDialog(true);
            }
            getAllOrderListForGuest();
        }
        orderAdapter.setOnItemClickListener(order -> {
            Intent intent = new Intent(getActivity(), DetailBookingActivity.class);
            intent.putExtra("orderId", order.getId());
            intent.putExtra("totalTime", order.getTotalTime());
            intent.putExtra("selectedDate", order.getCreatedAt().substring(0, 10));
            intent.putExtra("totalPrice", order.getTotalAmount());
            intent.putExtra("courtId", order.getCourtId());
            intent.putExtra("orderType", order.getOrderType());
            intent.putExtra("customerName", order.getCustomerName());
            intent.putExtra("phoneNumber", order.getPhoneNumber());
            intent.putExtra("note", order.getNote());
            String serviceDetailsJson = OrderServiceHolder.getInstance().getServiceDetailsJson(order.getId());
            String serviceListJson = OrderServiceHolder.getInstance().getServiceListJson(order.getId());
            if (serviceDetailsJson != null) {
                intent.putExtra("serviceDetailsJson", serviceDetailsJson);
            }
            if (serviceListJson != null) {
                intent.putExtra("serviceListJson", serviceListJson);
            }
            Log.d("AccountFragment", "Truyền customerName: " + order.getCustomerName() + ", phoneNumber: " + order.getPhoneNumber());
            startActivity(intent);

        });

        btnNoti.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NotificationActivity.class);

            // Kiểm tra xem orderList có dữ liệu hay không
            if (!orderList.isEmpty()) {
                Orders order = orderList.get(0); // Lấy đơn hàng đầu tiên (hoặc chọn theo logic của bạn)
                intent.putExtra("orderId", order.getId());
                intent.putExtra("totalTime", order.getTotalTime());
                intent.putExtra("selectedDate", order.getCreatedAt().substring(0, 10));
                intent.putExtra("totalPrice", order.getTotalAmount());
                intent.putExtra("courtId", order.getCourtId());
                intent.putExtra("orderType", order.getOrderType());
                intent.putExtra("customerName", order.getCustomerName());
                intent.putExtra("phoneNumber", order.getPhoneNumber());
                intent.putExtra("note", order.getNote());

                String serviceDetailsJson = OrderServiceHolder.getInstance().getServiceDetailsJson(order.getId());
                String serviceListJson = OrderServiceHolder.getInstance().getServiceListJson(order.getId());
                if (serviceDetailsJson != null) {
                    intent.putExtra("serviceDetailsJson", serviceDetailsJson);
                }
                if (serviceListJson != null) {
                    intent.putExtra("serviceListJson", serviceListJson);
                }

                Log.d("AccountFragment", "Truyền totalTime: " + order.getTotalTime());
            }

            startActivity(intent);
        });

        if (sessionManager.getToken() == null || sessionManager.getToken().isEmpty()) {
            btnOptions.setVisibility(View.GONE);
            authButtonsContainer.setVisibility(View.VISIBLE);

            btnLogin.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            });

            btnRegister.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), SignUpActivity.class);
                startActivity(intent);
            });
        } else {
            btnOptions.setVisibility(View.VISIBLE);
            authButtonsContainer.setVisibility(View.GONE);
        }

        return view;

    }
    @Override
    public void onResume() {
        super.onResume();
        if (sessionManager.getToken() != null && !sessionManager.getToken().isEmpty()) {
            getMyInfo(); // Gọi lại phương thức để tải thông tin người dùng
            getNotifications(); // Tải lại thông báo nếu cần
        } else {
            getAllOrderListForGuest();
        }
    }
    private void showDatePickerDialog() {
        // Create a MaterialDatePicker for selecting a date range
        MaterialDatePicker.Builder<androidx.core.util.Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Select Date Range");


        // Build and show the date picker
        MaterialDatePicker<androidx.core.util.Pair<Long, Long>> datePicker = builder.build();
        datePicker.addOnPositiveButtonClickListener(selection -> {
            Long startDate = selection.first;
            Long endDate = selection.second;

            // Format the dates as needed
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String startDateStr = dateFormat.format(new Date(startDate));
            String endDateStr = dateFormat.format(new Date(endDate));

            // Use the selected dates (e.g., filter data)
            filterOrdersByDateRange(startDateStr, endDateStr);
        });

        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }

    private void filterOrdersByDateRange(String startDate, String endDate) {
        filteredOrderList.clear();
        for (Orders order : orderList) {
            String orderDate = order.getCreatedAt().substring(0, 10);
            if (orderDate.compareTo(startDate) >= 0 && orderDate.compareTo(endDate) <= 0) {
                filteredOrderList.add(order);
            }
        }
        orderAdapter.notifyDataSetChanged();
        if (filteredOrderList.isEmpty()) {
            Toast.makeText(getContext(), "Không có đơn đặt nào trong khoảng thời gian đã chọn", Toast.LENGTH_SHORT).show();
        }
    }

    private void showFilterMenu(View anchorView) {
        PopupMenu popupMenu = new PopupMenu(getContext(), anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.filter_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.filter_by_newest) {
                filterOrdersByNewest();
                return true;
            } else if (id == R.id.filter_by_date) {
                showDatePickerDialog();
                return true;
            } else if (id == R.id.filter_by_status) {
                showStatusFilterDialog();
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void showStatusFilterDialog() {
        String[] statuses = {"Đang xử lý", "Đã hoàn thành", "Hủy đặt lịch", "Đặt lịch thành công", "Thay đổi lịch đặt thành công", "Hủy đặt lịch do quá giờ thanh toán","Đặt dịch vụ tại sân"
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Chọn trạng thái");
        builder.setSingleChoiceItems(statuses, -1, (dialog, which) -> {
            String selectedStatus = statuses[which];
            filterOrdersByStatus(selectedStatus);
            dialog.dismiss();
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void filterOrdersByStatus(String selectedStatus) {
        filteredOrderList.clear();
        for (Orders order : orderList) {
            if (order.getOrderStatus().equals(selectedStatus)) {
                filteredOrderList.add(order);
            }
        }
        orderAdapter.notifyDataSetChanged();
    }

    private void getAllOrderListForGuest() {
        String guestPhone = sessionManager.getGuestPhone();
        if (guestPhone == null || guestPhone.isEmpty()) {
            Toast.makeText(getContext(), "Chưa có số điện thoại được sử dụng để đặt lịch.", Toast.LENGTH_SHORT).show();
            return;
        }

        orderList.clear();
        filteredOrderList.clear();

        ApiService apiService = RetrofitClient.getApiService(getContext());
        apiService.getOrders(guestPhone).enqueue(new Callback<List<Orders>>() {
            @Override
            public void onResponse(Call<List<Orders>> call, Response<List<Orders>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    orderList.addAll(response.body());
                    filterOrdersByType();
                    showBookedTab();
                } else {
                    Toast.makeText(getContext(), "Không có dữ liệu đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Orders>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
        tvServiceInfo.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
        filteredOrderList.clear();
        filteredOrderList.addAll(bookedOrders); // Hiển thị đơn "Đơn cố định" và "Đơn ngày"
        orderAdapter.notifyDataSetChanged();
        recyclerOrder.setVisibility(View.VISIBLE);
        tvServiceOrderInfo.setVisibility(View.GONE);
    }

    private void showMemberInfoTab() {
        tvTabBooked.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
        tvServiceInfo.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
        filteredOrderList.clear();
        filteredOrderList.addAll(serviceOrders); // Hiển thị đơn "Đơn dịch vụ"
        orderAdapter.notifyDataSetChanged();
        recyclerOrder.setVisibility(View.VISIBLE);
        tvServiceOrderInfo.setVisibility(View.GONE);
    }

    private void getMyInfo() {
        String token = sessionManager.getToken();
        if (token != null && !token.isEmpty()) {
            String authHeader = "Bearer " + token;
            ApiService apiService = RetrofitClient.getApiService(getContext());
            apiService.getMyInfo(authHeader).enqueue(new Callback<MyInfoResponse>() {
                @Override
                public void onResponse(Call<MyInfoResponse> call, Response<MyInfoResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        MyInfoResponse myInfoResponse = response.body();
                        if (myInfoResponse.getResult() != null) {
                            MyInfo info = myInfoResponse.getResult();

                            id = info.getId();
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

                            String avatarUrl = info.getAvatarUrl();

                            Log.d("AccountFragment", "Avatar URL: " + avatarUrl);

                            if (requireContext() == null) {
                                Log.e("AccountFragment", "Context is null, cannot load avatar");
                                ivAvatar.setImageResource(R.drawable.avatar);
                                return;
                            }

                            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                Log.d("AccountFragment", "Loading avatar from URL: " + avatarUrl);
                                Glide.with(requireContext())
                                        .load(avatarUrl)
                                        .placeholder(R.drawable.avatar)
                                        .error(R.drawable.avatar)
                                        .listener(new com.bumptech.glide.request.RequestListener<Drawable>() {
                                            @Override
                                            public boolean onLoadFailed(@Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<Drawable> target, boolean isFirstResource) {
                                                Log.e("AccountFragment", "Failed to load avatar: " + (e != null ? e.getMessage() : "Unknown error"));
                                                Toast.makeText(getContext(), "Không thể tải ảnh đại diện. Vui lòng thử upload lại trong phần chỉnh sửa thông tin!", Toast.LENGTH_LONG).show();
                                                return false;
                                            }

                                            @Override
                                            public boolean onResourceReady(Drawable resource, Object model, com.bumptech.glide.request.target.Target<Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                                                Log.d("AccountFragment", "Avatar loaded successfully");
                                                return false;
                                            }
                                        })
                                        .into(ivAvatar);
                            } else {
                                Log.d("AccountFragment", "Avatar URL is empty, using default avatar");
                                ivAvatar.setImageResource(R.drawable.avatar);
                            }

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
                    filterOrdersByType(); // Lọc đơn hàng theo loại
                    showBookedTab(); // Mặc định hiển thị tab "Lịch đã đặt"
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
                Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.menu_transalate) {
                showLanguageDialog();
                return true;
            } else if (itemId == R.id.menu_logout) {
                logoutUser();
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

    private void getNotifications() {
        ApiService apiService = RetrofitClient.getApiService(getContext());

        // Lấy userId hoặc guestPhone từ session
        String userId = sessionManager.getUserId();

        if (userId != null && !userId.isEmpty()) {
            // Nếu là user đã đăng nhập, lấy thông báo theo userId
            apiService.getNotifications(userId)
                    .enqueue(new Callback<NotificationResponse>() {
                        @Override
                        public void onResponse(Call<NotificationResponse> call, Response<NotificationResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                NotificationResponse notiResponse = response.body();
                                notificationList = notiResponse.getNotifications();

                                // Nếu API trả về số lượng chưa đọc
                                int unreadCount = notiResponse.getUnreadCount();
                                badge.setNumber(unreadCount);

                                // Hoặc tính toán số lượng chưa đọc từ danh sách
                                // updateBadge();
                            }
                        }
                        @Override
                        public void onFailure(Call<NotificationResponse> call, Throwable t) {
                            Log.e("AccountFragment", "Lỗi lấy thông báo: " + t.getMessage());
                        }
                    });
        } else {
            // Nếu là guest, lấy thông báo theo số điện thoại
            String guestPhone = sessionManager.getGuestPhone();
            if (guestPhone != null && !guestPhone.isEmpty()) {
                apiService.getNotifications(guestPhone)
                        .enqueue(new Callback<NotificationResponse>() {
                            @Override
                            public void onResponse(Call<NotificationResponse> call, Response<NotificationResponse> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    NotificationResponse notiResponse = response.body();
                                    notificationList = notiResponse.getNotifications();

                                    // Nếu API trả về số lượng chưa đọc
                                    int unreadCount = notiResponse.getUnreadCount();
                                    badge.setNumber(unreadCount);

                                    // Hoặc tính toán số lượng chưa đọc từ danh sách
                                    // updateBadge();
                                }
                            }
                            @Override
                            public void onFailure(Call<NotificationResponse> call, Throwable t) {
                                Log.e("AccountFragment", "Lỗi lấy thông báo guest: " + t.getMessage());
                            }
                        });
            } else {
                // Không có thông tin để lấy thông báo
                badge.setNumber(0);
            }
        }
    }

    private void filterOrdersByType() {
        bookedOrders.clear();
        serviceOrders.clear();
        for (Orders order : orderList) {
            String orderType = order.getOrderType();
            if ("Đơn cố định".equals(orderType) || "Đơn ngày".equals(orderType)) {
                bookedOrders.add(order);
            } else if ("Đơn dịch vụ".equals(orderType)) {
                serviceOrders.add(order);
            }
        }
    }
    private void filterOrdersByNewest() {
        // Lấy toàn bộ đơn (orderList), sắp xếp từ mới nhất xuống cũ nhất
        filteredOrderList.clear();
        filteredOrderList.addAll(orderList);
        Collections.sort(filteredOrderList, new Comparator<Orders>() {
            @Override
            public int compare(Orders o1, Orders o2) {
                // Giả sử createdAt là chuỗi ISO (yyyy-MM-dd...),
                // so sánh lexicographically sẽ đúng thứ tự thời gian
                return o2.getCreatedAt().compareTo(o1.getCreatedAt());
            }
        });
        orderAdapter.notifyDataSetChanged();

        if (filteredOrderList.isEmpty()) {
            Toast.makeText(getContext(), "Không có đơn nào để hiển thị", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Đã lọc: Mới nhất", Toast.LENGTH_SHORT).show();
        }
    }
}