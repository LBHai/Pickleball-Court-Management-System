package SEP490.G9;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;
import Api.ApiService;
import Api.NetworkUtils;
import Api.RetrofitClient;
import Model.Courts;
import Model.MyInfoResponse;
import Session.SessionManager;

public class DetailBookingActivity extends AppCompatActivity {

    // Tab header
    private TextView tvTabBookingInfo, tvTabServiceDetail;
    private View lineBookingInfo, lineServiceDetail;
    private ScrollView layoutBookingInfo, layoutServiceDetail;

    // Các view hiển thị thông tin đặt lịch (Card "Thông tin Đặt lịch")
    private TextView tvStadiumName, tvAddress, tvBookingDate, tvTotalPrice, tvTotalTime;
    private ImageButton btnBack;

    // Các trường dữ liệu được truyền từ các Activity
    private String orderId, selectedDate, confirmOrdersJson;
    private String totalPrice, totalTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_booking);

        // Ánh xạ các view cho tab header
        tvTabBookingInfo = findViewById(R.id.tvTabBookingInfo);
        tvTabServiceDetail = findViewById(R.id.tvTabServiceDetail);
        lineBookingInfo = findViewById(R.id.lineBookingInfo);
        lineServiceDetail = findViewById(R.id.lineServiceDetail);
        layoutBookingInfo = findViewById(R.id.layoutBookingInfo);
        layoutServiceDetail = findViewById(R.id.layoutServiceDetail);

        // Ánh xạ các view hiển thị thông tin đặt lịch
        tvStadiumName = findViewById(R.id.tvStadiumName);
        tvAddress = findViewById(R.id.tvAddress);
        tvBookingDate = findViewById(R.id.tvBookingDate);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        btnBack = findViewById(R.id.btnBack);

        // Nhận dữ liệu từ Intent
        orderId = getIntent().getStringExtra("orderId");
        Log.d("DetailBookingActivity", "orderId nhận được: " + orderId);
        selectedDate = getIntent().getStringExtra("selectedDate");
        confirmOrdersJson = getIntent().getStringExtra("confirmOrdersJson");
        totalPrice = getIntent().getStringExtra("totalPrice");
        totalTime = getIntent().getStringExtra("totalTime");

        // Cập nhật các thông tin (Ngày đặt, Tổng tiền, Tổng thời gian)
        if (selectedDate != null) {
            tvBookingDate.setText("Ngày đặt: " + selectedDate);
        }
        if (totalPrice != null) {
            tvTotalPrice.setText("Tổng tiền: " + totalPrice);
        }
        if (totalTime != null) {
            tvTotalTime.setText("Tổng thời gian: " + totalTime);
        }

        // Gọi API lấy thông tin sân dựa trên club_id
        if (orderId != null && !orderId.isEmpty()) {
            fetchCourtDetails(orderId);
        } else {
            Toast.makeText(this, "orderId không hợp lệ", Toast.LENGTH_SHORT).show();
        }

        // Cài đặt chuyển đổi giữa các tab
        tvTabBookingInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBookingInfoTab();
            }
        });
        tvTabServiceDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showServiceDetailTab();
            }
        });
        showBookingInfoTab();

        btnBack.setOnClickListener(v -> finish());

        fetchUserInfo();
    }

    private void fetchCourtDetails(String orderId) {
        ApiService apiService = RetrofitClient.getApiService(DetailBookingActivity.this);
        NetworkUtils.callApi(apiService.getCourtById(orderId), DetailBookingActivity.this, new NetworkUtils.ApiCallback<Courts>() {
            @Override
            public void onSuccess(Courts court) {
                if (court != null) {
                    tvStadiumName.setText("Tên sân: "+ court.getName());
                    tvAddress.setText("Địa chỉ: " + court.getAddress());
                } else {
                    Toast.makeText(DetailBookingActivity.this, "Không có thông tin sân", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onError(String errorMessage) {
                Toast.makeText(DetailBookingActivity.this, "Lỗi khi lấy thông tin sân: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showBookingInfoTab() {
        tvTabBookingInfo.setTextColor(getResources().getColor(android.R.color.white));
        tvTabServiceDetail.setTextColor(getResources().getColor(android.R.color.darker_gray));
        lineBookingInfo.setVisibility(View.VISIBLE);
        lineServiceDetail.setVisibility(View.INVISIBLE);
        layoutBookingInfo.setVisibility(View.VISIBLE);
        layoutServiceDetail.setVisibility(View.GONE);
    }

    private void showServiceDetailTab() {
        tvTabServiceDetail.setTextColor(getResources().getColor(android.R.color.white));
        tvTabBookingInfo.setTextColor(getResources().getColor(android.R.color.darker_gray));
        lineServiceDetail.setVisibility(View.VISIBLE);
        lineBookingInfo.setVisibility(View.INVISIBLE);
        layoutServiceDetail.setVisibility(View.VISIBLE);
        layoutBookingInfo.setVisibility(View.GONE);
    }

    private void fetchUserInfo() {
        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getToken();
        if (token != null && !token.isEmpty()) {
            String authHeader = "Bearer " + token;
            ApiService apiService = RetrofitClient.getApiService(DetailBookingActivity.this);
            NetworkUtils.callApi(apiService.getMyInfo(authHeader), DetailBookingActivity.this, new NetworkUtils.ApiCallback<MyInfoResponse>() {
                @Override
                public void onSuccess(MyInfoResponse myInfoResponse) {
                    if (myInfoResponse != null && myInfoResponse.getResult() != null) {
                        // Cập nhật thông tin người dùng nếu cần
                    }
                }
                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(DetailBookingActivity.this, "Lỗi khi lấy thông tin: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
