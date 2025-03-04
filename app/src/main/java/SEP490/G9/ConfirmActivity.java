package SEP490.G9;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import Api.ApiService;
import Model.ConfirmOrder;
import Model.Courts;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmActivity extends AppCompatActivity {

    private TextView tvHeader, tvStadiumName, tvAddress, tvDate, tvGameType, tvTotalPrice;
    private LinearLayout layoutConfirmOrders;
    private EditText etName, etPhone, etNote;
    private Button btnConfirm;

    private String clubId;
    private String selectedDate;
    private String confirmOrdersJson;
    private List<ConfirmOrder> confirmOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        // Ánh xạ View
        tvHeader = findViewById(R.id.tvHeader);
        tvStadiumName = findViewById(R.id.tvStadiumName);
        tvAddress = findViewById(R.id.tvAddress);
        tvDate = findViewById(R.id.tvDate);
        tvGameType = findViewById(R.id.tvGameType);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);

        layoutConfirmOrders = findViewById(R.id.layoutConfirmOrders);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etNote = findViewById(R.id.etNote);
        btnConfirm = findViewById(R.id.btnConfirm);

        // Lấy dữ liệu từ Intent
        clubId = getIntent().getStringExtra("club_id");
        selectedDate = getIntent().getStringExtra("selectedDate");
        confirmOrdersJson = getIntent().getStringExtra("confirmOrdersJson");

        // Parse JSON -> danh sách slot đã chọn
        confirmOrders = new Gson().fromJson(confirmOrdersJson, new TypeToken<List<ConfirmOrder>>(){}.getType());

        // Set ngày
        tvDate.setText("Ngày: " + selectedDate);

        // Giả sử môn chơi (game type) bạn đang fix cứng "Pickleball, Sân 5 người"
        // hoặc nếu cần thì tuỳ chỉnh hiển thị
        tvGameType.setText("Pickleball, Sân 5 người");

        // Gọi API lấy thông tin sân (nếu cần hiển thị địa chỉ, tên sân)
        if (clubId != null && !clubId.isEmpty()) {
            fetchCourtDetails(clubId);
        }

        // Hiển thị danh sách slot + tính tổng
        buildConfirmOrdersUI();

        // Xử lý khi nhấn xác nhận
        btnConfirm.setOnClickListener(v -> {
            // Tên, sđt, ghi chú người đặt
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String note = etNote.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(ConfirmActivity.this, "Vui lòng nhập đủ Tên và Số điện thoại", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Ở đây bạn có thể gọi API tạo đơn đặt sân, hoặc chuyển sang cổng thanh toán...
            Toast.makeText(ConfirmActivity.this,
                    "Đã nhận thông tin đặt sân.\nTên: " + name + "\nSĐT: " + phone + "\nGhi chú: " + note,
                    Toast.LENGTH_LONG).show();
        });
    }

    private void fetchCourtDetails(String clubId) {
        ApiService.apiService.getCourtById(clubId).enqueue(new Callback<Courts>() {
            @Override
            public void onResponse(Call<Courts> call, Response<Courts> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Courts court = response.body();
                    // Hiển thị tên sân, địa chỉ
                    tvStadiumName.setText(court.getName());
                    tvAddress.setText("Địa chỉ: " + court.getAddress());
                } else {
                    Toast.makeText(ConfirmActivity.this, "Lỗi khi lấy thông tin sân", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Courts> call, Throwable t) {
                Toast.makeText(ConfirmActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buildConfirmOrdersUI() {
        if (confirmOrders == null || confirmOrders.isEmpty()) return;

        double totalPrice = 0;
        for (ConfirmOrder order : confirmOrders) {
            // Inflate item_confirm_order.xml
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_confirm_order, layoutConfirmOrders, false);

            TextView tvCourtSlotName = itemView.findViewById(R.id.tvCourtSlotName);
            TextView tvTime = itemView.findViewById(R.id.tvTime);
            TextView tvPrice = itemView.findViewById(R.id.tvPrice);

            tvCourtSlotName.setText("Sân: " + order.getCourtSlotName());
            tvTime.setText("Thời gian: " + order.getStartTime() + " - " + order.getEndTime());
            tvPrice.setText("Giá: " + order.getDailyPrice() + " đ");

            totalPrice += order.getDailyPrice();

            layoutConfirmOrders.addView(itemView);
        }
        tvTotalPrice.setText("Tổng giá: " + (int)totalPrice + " đ");
    }
}
