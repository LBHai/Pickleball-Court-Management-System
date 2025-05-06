package UI.Fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.app.AlertDialog;
import UI.Adapter.CourtsAdapter;
import Data.Network.ApiService;
import Data.Network.RetrofitClient;
import Data.Model.Courts;
import Data.Model.MyInfo;
import Data.Model.MyInfoResponse;
import UI.Activity.LoginActivity;
import UI.Activity.NotificationActivity;
import SEP490.G9.R;
import UI.Activity.SignUpActivity;
import Data.Session.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourtsFragment extends Fragment {

    // UI components
    private RecyclerView rcvClubs;
    private CourtsAdapter courtsAdapter;
    private List<Courts> courtsList = new ArrayList<>();
    private List<Courts> fullCourtsList = new ArrayList<>(); // Danh sách gốc của Courts
    private boolean isFavoriteFilterActive = false;

    // Các thành phần giao diện header
    private CardView cardUserAvatar;
    private ImageView imgUserAvatar;
    private CardView cardSearch;
    private CardView cardSearchCompact;
    private ImageView imgAvtarIcon;
    private FloatingActionButton fabScrollUp;
    private TextView tvUserName;
    private TextView tvDate;
    private ImageView imgFavorite,imgNotification,imgAvtarIconCompact;
    private SessionManager sessionManager;
    private CardView favoriteContainer;
    private ImageView imgSearch,imgFlag;
    private LinearLayout notificationContainer;

    // EditText cho thanh tìm kiếm khi header mở rộng
    private EditText etSearch;

    // Các biến animation
    private boolean isAnimating = false;
    private boolean isHeaderCollapsed = false;
    private float[] avatarStartPosition = new float[2];
    private float[] avatarEndPosition = new float[2];
    private float[] favoriteStartPosition = new float[2];
    private float[] favoriteEndPosition = new float[2];

    // Ngưỡng cuộn để kích hoạt animation
    private static final int SCROLL_THRESHOLD = 150;
    private Handler handler = new Handler();
    private Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            callApiGetCourts();
            handler.postDelayed(this, 500); // Làm mới mỗi 60 giây
        }
    };
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate layout fragment
        View view = inflater.inflate(R.layout.fragment_courts, container, false);

        // Lấy tham chiếu đến các thành phần giao diện từ layout XML
        rcvClubs = view.findViewById(R.id.rcvClubs);
        cardUserAvatar = view.findViewById(R.id.cardUserAvatar);
        imgUserAvatar = view.findViewById(R.id.imgUserAvatar);
        cardSearch = view.findViewById(R.id.cardSearch);
        cardSearchCompact = view.findViewById(R.id.cardSearchCompact);
        imgAvtarIcon = view.findViewById(R.id.imgAvtarIcon);
        fabScrollUp = view.findViewById(R.id.fabScrollUp);
        tvUserName = view.findViewById(R.id.tvUserName);

        sessionManager = new SessionManager(requireContext());
        sessionManager.setHasShownGuestDialog(false);

        tvUserName = view.findViewById(R.id.tvUserName);
        imgUserAvatar = view.findViewById(R.id.imgUserAvatar);
        imgAvtarIcon = view.findViewById(R.id.imgAvtarIcon);
        imgAvtarIconCompact = view.findViewById(R.id.imgAvtarIconCompact);
        imgFlag = view.findViewById(R.id.imgFlag);
        imgFlag.setOnClickListener(v -> showLanguageDialog());
        initializeLanguageFlag();

        getMyInfoAndShow();

        tvDate = view.findViewById(R.id.tvDate);
        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String currentDate = sdf.format(today);
        tvDate.setText(currentDate);


        imgFavorite = view.findViewById(R.id.imgFavorite);
        favoriteContainer = view.findViewById(R.id.favoriteContainer);
        imgSearch = view.findViewById(R.id.imgSearch);
        notificationContainer = view.findViewById(R.id.notificationContainer);
        notificationContainer.setVisibility(View.VISIBLE);
        imgFavorite = view.findViewById(R.id.imgFavorite);
        imgFavorite.setOnClickListener(v -> {
            isFavoriteFilterActive = !isFavoriteFilterActive;
            if (isFavoriteFilterActive) {
                showFavoriteCourts();
                imgFavorite.setImageResource(R.drawable.ic_heart_filled); // Đổi icon thành tim đỏ khi đang filter
            } else {
                courtsAdapter.updateList(fullCourtsList); // Hiện lại toàn bộ
                imgFavorite.setImageResource(R.drawable.ic_heart_outline); // Đổi lại icon thành tim trắng
            }

        });
        imgNotification = view.findViewById(R.id.imgNotification);
        imgNotification.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NotificationActivity.class);
            startActivity(intent);
        });
        // Lấy tham chiếu EditText trong cardSearch (trạng thái expanded)
        etSearch = view.findViewById(R.id.edtSearch);
        etSearch.setFilters(new InputFilter[] {
                (source, start, end, dest, dstart, dend) -> {
                    for (int i = start; i < end; i++) {
                        if (source.charAt(i) == '\n') {
                            return "";
                        }
                    }
                    return null;
                }
        });

        // Thiết lập TextWatcher để lọc danh sách Courts theo truy vấn người dùng nhập
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không cần xử lý gì trước khi thay đổi
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Lọc danh sách Courts theo nội dung trong ô tìm kiếm
                filterCourts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Không cần xử lý sau khi thay đổi
            }
        });


        // Sự kiện click cho cardSearchCompact để mở lại header tìm kiếm
        cardSearchCompact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Nếu header đang ở trạng thái collapse, mở header để hiển thị thanh tìm kiếm expanded
                if (isHeaderCollapsed) {
                    animateHeaderExpand();
                    // Focus vào EditText để cho phép nhập trực tiếp
                    etSearch.requestFocus();
                    // Hiển thị bàn phím mềm
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
                    }
                }
            }
        });

        // Thiết lập RecyclerView và thêm animation khi scroll
        setupRecyclerViewWithAnimation();

        // Gọi API để lấy danh sách Courts
        callApiGetCourts();

        // Sự kiện click cho Floating Action Button: cuộn lên đầu danh sách
        fabScrollUp.setOnClickListener(v -> {
            rcvClubs.smoothScrollToPosition(0);
            if (isHeaderCollapsed) {
                resetHeaderAnimation();
            }
        });

        // Tính toán lại vị trí cho các thành phần dùng trong animation
        view.post(() -> calculateAnimationPositions());
        if (!isUserLoggedIn()) {
            if (!sessionManager.hasShownGuestDialog()) {
                showGuestDialog();
                sessionManager.setHasShownGuestDialog(true);
            }
        }
        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        handler.post(refreshRunnable); // Bắt đầu polling
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(refreshRunnable); // Dừng polling
    }
    private String removeDiacritics(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private void filterCourts(String query) {
        List<Courts> filteredList = new ArrayList<>();
        String normalizedQuery = removeDiacritics(query.toLowerCase());

        for (Courts court : fullCourtsList) {
            String normalizedName = removeDiacritics(court.getName().toLowerCase());
            if (normalizedName.contains(normalizedQuery)) {
                filteredList.add(court);
            }
        }
        courtsAdapter.updateList(filteredList);
    }



    // Thiết lập RecyclerView cùng với listener cho sự kiện scroll để kích hoạt animation
    private void setupRecyclerViewWithAnimation() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rcvClubs.setLayoutManager(layoutManager);

        rcvClubs.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private int lastScrollY = 0;

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int scrollY = recyclerView.computeVerticalScrollOffset();

                if (!isAnimating) {
                    // Khi cuộn xuống vượt qua ngưỡng, tiến hành collapse header
                    if (scrollY > SCROLL_THRESHOLD && !isHeaderCollapsed) {
                        animateHeaderCollapse();
                    }
                    // Nếu cuộn lên trên và không vượt quá ngưỡng, mở lại header
                    else if (scrollY <= SCROLL_THRESHOLD && isHeaderCollapsed) {
                        animateHeaderExpand();
                    }
                }

                // Hiển thị/hide Floating Action Button dựa vào vị trí cuộn
                if (scrollY > SCROLL_THRESHOLD) {
                    if (fabScrollUp.getVisibility() != View.VISIBLE) {
                        fabScrollUp.show();
                    }
                } else {
                    if (fabScrollUp.getVisibility() == View.VISIBLE) {
                        fabScrollUp.hide();
                    }
                }

                // Đảm bảo luôn hiển thị notificationContainer
                notificationContainer.setVisibility(View.VISIBLE);
                notificationContainer.bringToFront();

                lastScrollY = scrollY;
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // Nếu cuộn lên đầu danh sách và header đang collapsed, reset lại animation header
                if (newState == RecyclerView.SCROLL_STATE_IDLE &&
                        recyclerView.computeVerticalScrollOffset() == 0 && isHeaderCollapsed) {
                    resetHeaderAnimation();
                }
            }
        });
    }

    // Animation collapse header (ẩn thông tin người dùng, di chuyển avatar và favorite, chuyển từ cardSearch sang cardSearchCompact)
    private void animateHeaderCollapse() {
        if (isAnimating || isHeaderCollapsed) return;
        isAnimating = true;

        calculateAnimationPositions();

        // Animation fade out cho thông tin người dùng
        ObjectAnimator fadeOutUserInfo = ObjectAnimator.ofFloat(tvUserName, "alpha", 1f, 0f);
        ObjectAnimator fadeOutDate = ObjectAnimator.ofFloat(tvDate, "alpha", 1f, 0f);

        // Tính toán vị trí di chuyển và scale cho avatar
        float translateAvatarX = avatarEndPosition[0] - avatarStartPosition[0];
        float translateAvatarY = avatarEndPosition[1] - avatarStartPosition[1];
        ObjectAnimator moveAvatarX = ObjectAnimator.ofFloat(cardUserAvatar, "translationX", 0f, translateAvatarX);
        ObjectAnimator moveAvatarY = ObjectAnimator.ofFloat(cardUserAvatar, "translationY", 0f, translateAvatarY);
        ObjectAnimator scaleAvatarX = ObjectAnimator.ofFloat(cardUserAvatar, "scaleX", 1f, 0.46f);
        ObjectAnimator scaleAvatarY = ObjectAnimator.ofFloat(cardUserAvatar, "scaleY", 1f, 0.46f);

        // Tính toán vị trí di chuyển và scale cho favorite icon
        float translateFavoriteX = favoriteEndPosition[0] - favoriteStartPosition[0];
        float translateFavoriteY = favoriteEndPosition[1] - favoriteStartPosition[1];
        ObjectAnimator moveFavoriteX = ObjectAnimator.ofFloat(favoriteContainer, "translationX", 0f, translateFavoriteX);
        ObjectAnimator moveFavoriteY = ObjectAnimator.ofFloat(favoriteContainer, "translationY", 0f, translateFavoriteY);
        ObjectAnimator scaleFavoriteX = ObjectAnimator.ofFloat(favoriteContainer, "scaleX", 1f, 0.87f);
        ObjectAnimator scaleFavoriteY = ObjectAnimator.ofFloat(favoriteContainer, "scaleY", 1f, 0.87f);

        // Animation fade cho chuyển đổi thanh tìm kiếm: ẩn cardSearch và hiện cardSearchCompact
        ObjectAnimator fadeOutSearchBar = ObjectAnimator.ofFloat(cardSearch, "alpha", 1f, 0f);
        ObjectAnimator fadeInCompactSearch = ObjectAnimator.ofFloat(cardSearchCompact, "alpha", 0f, 1f);

        // Tổng hợp các Animator vào một AnimatorSet
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                fadeOutUserInfo, fadeOutDate,
                moveAvatarX, moveAvatarY, scaleAvatarX, scaleAvatarY,
                moveFavoriteX, moveFavoriteY, scaleFavoriteX, scaleFavoriteY,
                fadeOutSearchBar, fadeInCompactSearch
        );
        animatorSet.setDuration(300);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                // Trước khi animation bắt đầu: hiển thị cardSearchCompact và đảm bảo notificationContainer nổi lên
                cardSearchCompact.setVisibility(View.VISIBLE);
                cardSearchCompact.setAlpha(0f);
                notificationContainer.setVisibility(View.VISIBLE);
                notificationContainer.bringToFront();
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // Sau khi animation kết thúc, ẩn các thành phần không cần thiết và chuyển trạng thái headerCollapsed
                cardUserAvatar.setVisibility(View.INVISIBLE);
                favoriteContainer.setVisibility(View.INVISIBLE);
                cardSearch.setVisibility(View.INVISIBLE);
                imgAvtarIcon.setVisibility(View.VISIBLE);
                imgSearch.setVisibility(View.VISIBLE);
                isAnimating = false;
                isHeaderCollapsed = true;
            }
        });
        animatorSet.start();
    }

    // Animation expand header (hiển thị lại thông tin, chuyển về trạng thái cardSearch và ẩn cardSearchCompact)
    private void animateHeaderExpand() {
        if (isAnimating || !isHeaderCollapsed) return;
        isAnimating = true;

        ObjectAnimator fadeInUserInfo = ObjectAnimator.ofFloat(tvUserName, "alpha", 0f, 1f);
        ObjectAnimator fadeInDate = ObjectAnimator.ofFloat(tvDate, "alpha", 0f, 1f);

        float translateAvatarX = avatarEndPosition[0] - avatarStartPosition[0];
        float translateAvatarY = avatarEndPosition[1] - avatarStartPosition[1];
        ObjectAnimator moveAvatarX = ObjectAnimator.ofFloat(cardUserAvatar, "translationX", translateAvatarX, 0f);
        ObjectAnimator moveAvatarY = ObjectAnimator.ofFloat(cardUserAvatar, "translationY", translateAvatarY, 0f);
        ObjectAnimator scaleAvatarX = ObjectAnimator.ofFloat(cardUserAvatar, "scaleX", 0.46f, 1f);
        ObjectAnimator scaleAvatarY = ObjectAnimator.ofFloat(cardUserAvatar, "scaleY", 0.46f, 1f);

        ObjectAnimator moveFavoriteX = ObjectAnimator.ofFloat(favoriteContainer, "translationX", favoriteEndPosition[0] - favoriteStartPosition[0], 0f);
        ObjectAnimator moveFavoriteY = ObjectAnimator.ofFloat(favoriteContainer, "translationY", favoriteEndPosition[1] - favoriteStartPosition[1], 0f);
        ObjectAnimator scaleFavoriteX = ObjectAnimator.ofFloat(favoriteContainer, "scaleX", 0.87f, 1f);
        ObjectAnimator scaleFavoriteY = ObjectAnimator.ofFloat(favoriteContainer, "scaleY", 0.87f, 1f);

        ObjectAnimator fadeInSearchBar = ObjectAnimator.ofFloat(cardSearch, "alpha", 0f, 1f);
        ObjectAnimator fadeOutCompactSearch = ObjectAnimator.ofFloat(cardSearchCompact, "alpha", 1f, 0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                fadeInUserInfo, fadeInDate,
                moveAvatarX, moveAvatarY, scaleAvatarX, scaleAvatarY,
                moveFavoriteX, moveFavoriteY, scaleFavoriteX, scaleFavoriteY,
                fadeInSearchBar, fadeOutCompactSearch
        );
        animatorSet.setDuration(300);
        animatorSet.setInterpolator(new DecelerateInterpolator());

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                cardUserAvatar.setVisibility(View.VISIBLE);
                favoriteContainer.setVisibility(View.VISIBLE);
                cardSearch.setVisibility(View.VISIBLE);
                cardSearch.setAlpha(0f);
                notificationContainer.setVisibility(View.VISIBLE);
                notificationContainer.bringToFront();
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                cardSearchCompact.setVisibility(View.GONE);
                isAnimating = false;
                isHeaderCollapsed = false;
            }
        });
        animatorSet.start();
    }

    // Reset animation của header về trạng thái ban đầu
    private void resetHeaderAnimation() {
        tvUserName.setAlpha(1f);
        tvDate.setAlpha(1f);
        cardSearch.setVisibility(View.VISIBLE);
        cardSearch.setAlpha(1f);
        cardSearchCompact.setVisibility(View.GONE);
        cardSearchCompact.setAlpha(0f);

        cardUserAvatar.setVisibility(View.VISIBLE);
        cardUserAvatar.setTranslationX(0f);
        cardUserAvatar.setTranslationY(0f);
        cardUserAvatar.setScaleX(1f);
        cardUserAvatar.setScaleY(1f);

        favoriteContainer.setVisibility(View.VISIBLE);
        favoriteContainer.setTranslationX(0f);
        favoriteContainer.setTranslationY(0f);
        favoriteContainer.setScaleX(1f);
        favoriteContainer.setScaleY(1f);

        notificationContainer.setVisibility(View.VISIBLE);
        notificationContainer.bringToFront();

        isHeaderCollapsed = false;
    }

    // Tính toán vị trí ban đầu và vị trí đến của avatar và favorite để phục vụ cho animation
    private void calculateAnimationPositions() {
        int[] avatarLocation = new int[2];
        cardUserAvatar.getLocationOnScreen(avatarLocation);
        avatarStartPosition[0] = avatarLocation[0];
        avatarStartPosition[1] = avatarLocation[1];

        int[] avatarIconLocation = new int[2];
        imgAvtarIcon.getLocationOnScreen(avatarIconLocation);
        avatarEndPosition[0] = avatarIconLocation[0];
        avatarEndPosition[1] = avatarIconLocation[1];

        int[] favoriteLocation = new int[2];
        favoriteContainer.getLocationOnScreen(favoriteLocation);
        favoriteStartPosition[0] = favoriteLocation[0];
        favoriteStartPosition[1] = favoriteLocation[1];

        int[] searchLocation = new int[2];
        imgSearch.getLocationOnScreen(searchLocation);
        favoriteEndPosition[0] = searchLocation[0];
        favoriteEndPosition[1] = searchLocation[1];
    }

    // Gọi API để lấy danh sách Courts, sau đó cập nhật vào RecyclerView
    private void callApiGetCourts() {
        ApiService apiService = RetrofitClient.getApiService(getContext());
        apiService.getCourts().enqueue(new Callback<List<Courts>>() {
            @Override
            public void onResponse(Call<List<Courts>> call, Response<List<Courts>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    fullCourtsList = response.body();
                    courtsList = new ArrayList<>(fullCourtsList);
                    if (courtsAdapter == null) {
                        setupRecyclerView(courtsList);
                    } else {
                        courtsAdapter.updateList(courtsList);
                    }
                } else {
                    Log.e("API_ERROR", "Response body null hoặc lỗi: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<List<Courts>> call, Throwable t) {
                Log.e("API_ERROR", "Lỗi gọi API: " + t.getMessage());
                Toast.makeText(getContext(), "Không thể kết nối đến máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Cài đặt RecyclerView với CourtsAdapter và DividerItemDecoration
    private void setupRecyclerView(List<Courts> list) {
        courtsAdapter = new CourtsAdapter(getContext(), list, club -> {
            // Khi chọn 1 court, mở chi tiết court
            Fragment clubDetailFragment = new CourtDetailFragment();
            Bundle bundle = new Bundle();
            bundle.putString("club_id", club.getId());
            bundle.putString("club_name", club.getName());
            bundle.putString("backgroundUrl", club.getBackgroundUrl() != null ? club.getBackgroundUrl() : "");
            bundle.putString("address", club.getAddress());
            bundle.putString("tvPhone", club.getPhone());
            clubDetailFragment.setArguments(bundle);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, clubDetailFragment)
                    .addToBackStack(null)
                    .commit();
        });

        rcvClubs.setAdapter(courtsAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rcvClubs.getContext(), DividerItemDecoration.VERTICAL);
        rcvClubs.addItemDecoration(dividerItemDecoration);
    }
    private void showFavoriteCourts() {
        SessionManager sessionManager = new SessionManager(requireContext());
        List<String> favoriteCourtIds = sessionManager.getFavoriteCourts();
        List<Courts> filteredList = new ArrayList<>();
        for (Courts court : fullCourtsList) {
            if (favoriteCourtIds.contains(court.getId())) {
                filteredList.add(court);
            }
        }
        courtsAdapter.updateList(filteredList);
    }
    private void getMyInfoAndShow() {
        String token = sessionManager.getToken();
        if (token == null || token.isEmpty()) {
            tvUserName.setText("Khách");
            imgUserAvatar.setImageResource(R.drawable.avatar);
            imgAvtarIcon.setImageResource(R.drawable.avatar);
            imgAvtarIconCompact.setImageResource(R.drawable.avatar);
            return;
        }

        String authHeader = "Bearer " + token;
        ApiService apiService = RetrofitClient.getApiService(getContext());
        apiService.getMyInfo(authHeader).enqueue(new retrofit2.Callback<MyInfoResponse>() {
            @Override
            public void onResponse(Call<MyInfoResponse> call, Response<MyInfoResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    MyInfo info = response.body().getResult();
                    String fullName = info.getFirstName() + " " + info.getLastName();
                    String avatarUrl = info.getAvatarUrl();

                    tvUserName.setText(fullName);

                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        Glide.with(requireContext())
                                .load(avatarUrl)
                                .placeholder(R.drawable.avatar)
                                .error(R.drawable.avatar)
                                .into(imgUserAvatar);

                        Glide.with(requireContext())
                                .load(avatarUrl)
                                .placeholder(R.drawable.avatar)
                                .error(R.drawable.avatar)
                                .into(imgAvtarIcon);

                        Glide.with(requireContext())
                                .load(avatarUrl)
                                .placeholder(R.drawable.avatar)
                                .error(R.drawable.avatar)
                                .into(imgAvtarIconCompact);
                    } else {
                        imgUserAvatar.setImageResource(R.drawable.avatar);
                        imgAvtarIcon.setImageResource(R.drawable.avatar);
                        imgAvtarIconCompact.setImageResource(R.drawable.avatar);
                    }
                } else {
                    tvUserName.setText("Không rõ tên");
                    imgUserAvatar.setImageResource(R.drawable.avatar);
                    imgAvtarIcon.setImageResource(R.drawable.avatar);
                    imgAvtarIconCompact.setImageResource(R.drawable.avatar);
                }
            }

            @Override
            public void onFailure(Call<MyInfoResponse> call, Throwable t) {
                tvUserName.setText("Lỗi kết nối");
                imgUserAvatar.setImageResource(R.drawable.avatar);
                imgAvtarIcon.setImageResource(R.drawable.avatar);
                imgAvtarIconCompact.setImageResource(R.drawable.avatar);
            }
        });
    }
    private void showGuestDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage("Tạo tài khoản để dễ dàng quản lý và lưu trữ lịch đặt của bạn.");

        builder.setPositiveButton("Đăng nhập", (dialog, which) -> {
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        builder.setNegativeButton("Đăng ký", (dialog, which) -> {
            Intent intent = new Intent(requireActivity(), SignUpActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        builder.setNeutralButton("OK", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
                .setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
                .setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green));
    }
    private boolean isUserLoggedIn() {
        String token = sessionManager.getToken();
        return token != null && !token.isEmpty();
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

        // Cập nhật hình ảnh cờ dựa trên ngôn ngữ đã chọn
        if ("en".equals(languageCode)) {
            imgFlag.setImageResource(R.drawable.usa_flag);
        } else {
            imgFlag.setImageResource(R.drawable.vn_flag);
        }

        Intent refresh = getActivity().getIntent();
        getActivity().finish();
        startActivity(refresh);
    }
    private void initializeLanguageFlag() {
        Locale currentLocale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            currentLocale = getResources().getConfiguration().getLocales().get(0);
        } else {
            currentLocale = getResources().getConfiguration().locale;
        }

        String languageCode = currentLocale.getLanguage();
        if ("en".equals(languageCode)) {
            imgFlag.setImageResource(R.drawable.usa_flag);
        } else {
            imgFlag.setImageResource(R.drawable.vn_flag);
        }
    }

}
