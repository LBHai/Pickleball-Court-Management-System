package UI.Activity;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import SEP490.G9.R;

public class PaymentSuccessActivity extends AppCompatActivity {

    private TextView tvTitle, tvSubTitle;
    private Button btnXemLichDatChiTiet, btnQuayVe;
    private ImageButton btnBack;
    private String orderId, totalTime, selectedDate, orderStatus, courtId, orderType, serviceDetailsJson;    private int totalPrice;
    private ArrayList<Integer> slotPrices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_success);

        // Ánh xạ view
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        tvSubTitle = findViewById(R.id.tvSubTitle);
        btnXemLichDatChiTiet = findViewById(R.id.btnXemLichDatChiTiet);
        btnQuayVe = findViewById(R.id.btnQuayVe);

        orderId = getIntent().getStringExtra("orderId");
        totalTime = getIntent().getStringExtra("totalTime");
        selectedDate = getIntent().getStringExtra("selectedDate");
        totalPrice = getIntent().getIntExtra("totalPrice", 0);
        orderStatus = getIntent().getStringExtra("orderStatus");
        courtId = getIntent().getStringExtra("courtId");
        orderType = getIntent().getStringExtra("orderType");
        serviceDetailsJson = getIntent().getStringExtra("serviceDetailsJson");
        slotPrices = getIntent().getIntegerArrayListExtra("slotPrices");
        Log.d("PaymentSuccessActivity", "serviceDetailsJson: " + serviceDetailsJson);
        // In log danh sách giá nếu slotPrices không null
        if (slotPrices != null) {
            for (Integer price : slotPrices) {
                //Log.d("QRCodeActivity", "Slot price: " + price);
            }
        } else {
            //Log.d("QRCodeActivity", "Không có dữ liệu slotPrices được truyền qua Intent");
        }

        // Log giá trị orderId nhận được
        if (orderId != null && !orderId.isEmpty()) {
            //Log.d("Paymentsuscess", "orderId nhan dc: " + orderId);
        } else {
            //Log.e("Paymentsuscess", "orderId is null or empty!");
        }

        // Xử lý nút Back: quay về MainActivity
        btnBack.setOnClickListener(v -> {
            Intent mainIntent = new Intent(PaymentSuccessActivity.this, MainActivity.class);
            startActivity(mainIntent);
            finish();
        });

        // Cập nhật thông tin thông báo cho người dùng
        tvTitle.setText(getString(R.string.booking_success_title));
        tvSubTitle.setText(getString(R.string.booking_success_subtitle));

        // Xử lý nút Xem lịch đặt chi tiết
        btnXemLichDatChiTiet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent detailIntent = new Intent(PaymentSuccessActivity.this, DetailBookingActivity.class);
                detailIntent.putExtra("orderId", orderId);
                detailIntent.putExtra("totalTime", totalTime);
                detailIntent.putExtra("selectedDate", selectedDate);
                detailIntent.putExtra("totalPrice", totalPrice);
                detailIntent.putExtra("courtId", courtId);
                detailIntent.putExtra("orderType", orderType);
                detailIntent.putExtra("slotPrices", slotPrices); // Truyền slotPrices
                detailIntent.putExtra("serviceDetailsJson", getIntent().getStringExtra("serviceDetailsJson"));
                detailIntent.putIntegerArrayListExtra("slotPrices", getIntent().getIntegerArrayListExtra("slotPrices"));
                detailIntent.putExtra("customerName", getIntent().getStringExtra("customerName"));  // Truyền tiếp
                detailIntent.putExtra("phoneNumber", getIntent().getStringExtra("phoneNumber"));    // Truyền tiếp
                detailIntent.putExtra("note", getIntent().getStringExtra("note"));                  // Truyền tiếp
                detailIntent.putExtra("fromPaymentFlow", true);
                startActivity(detailIntent);
                finish();
            }
        });

        btnBack.setOnClickListener(v -> goBackToMainActivity());

        // Xử lý nút Quay về: quay trở lại MainActivity
        btnQuayVe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(PaymentSuccessActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goBackToMainActivity();
    }

    private void goBackToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}