package UI.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import SEP490.G9.R;

public class PaymentFailedActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private Button btnHistory,btnReturn;
    private String orderId, totalTime, selectedDate, orderStatus, courtId, orderType, serviceDetailsJson;
    private int totalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_failed);

        btnBack = findViewById(R.id.btnBack);
        btnHistory = findViewById(R.id.btnHistory);
        btnReturn = findViewById(R.id.btnReturn);
        // Lấy dữ liệu từ Intent
        orderId = getIntent().getStringExtra("orderId");
        totalTime = getIntent().getStringExtra("totalTime");
        selectedDate = getIntent().getStringExtra("selectedDate");
        totalPrice = getIntent().getIntExtra("totalPrice", 0);
        orderStatus = getIntent().getStringExtra("orderStatus");
        courtId = getIntent().getStringExtra("courtId");
        orderType = getIntent().getStringExtra("orderType");
        serviceDetailsJson = getIntent().getStringExtra("serviceDetailsJson");
        Log.d("PaymentFailedActivity", "Nhận từ QRCodeActivity:");
        Log.d("PaymentFailedActivity", "orderId: " + orderId);
        Log.d("PaymentFailedActivity", "totalTime: " + totalTime);
        Log.d("PaymentFailedActivity", "selectedDate: " + selectedDate);
        Log.d("PaymentFailedActivity", "totalPrice: " + totalPrice);
        Log.d("PaymentFailedActivity", "orderStatus: " + orderStatus);
        Log.d("PaymentFailedActivity", "courtId: " + courtId);
        Log.d("PaymentFailedActivity", "orderType: " + orderType);
        Log.d("PaymentFailedActivity", "serviceDetailsJson: " + serviceDetailsJson);
        Log.d("PaymentFailedActivity", "customerName: " + getIntent().getStringExtra("customerName"));
        Log.d("PaymentFailedActivity", "phoneNumber: " + getIntent().getStringExtra("phoneNumber"));
        Log.d("PaymentFailedActivity", "note: " + getIntent().getStringExtra("note"));
        if (totalTime == null) {
            //Log.e("PaymentFailedActivity", "totalTime là null");
            totalTime = "0h00";
        }
        //Log.d("PaymentFailedActivity", "serviceDetailsJson: " + serviceDetailsJson);

        btnBack.setOnClickListener(v -> goBackToMainActivity());
        btnReturn.setOnClickListener(v -> goBackToMainActivity());
        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(PaymentFailedActivity.this, DetailBookingActivity.class);
            intent.putExtra("orderId", orderId);
            intent.putExtra("totalTime", totalTime);
            intent.putExtra("selectedDate", selectedDate);
            intent.putExtra("totalPrice", totalPrice);
            intent.putExtra("orderStatus", orderStatus);
            intent.putExtra("courtId", courtId);
            intent.putExtra("orderType", orderType);
            intent.putExtra("serviceDetailsJson", serviceDetailsJson);
            intent.putExtra("customerName", getIntent().getStringExtra("customerName"));
            intent.putExtra("phoneNumber", getIntent().getStringExtra("phoneNumber"));
            intent.putExtra("note", getIntent().getStringExtra("note"));
            intent.putExtra("fromPaymentFlow", true);
            Log.d("PaymentFailedActivity", "Truyền sang BookingDetailActivity:");
            Log.d("PaymentFailedActivity", "orderId: " + orderId);
            Log.d("PaymentFailedActivity", "totalTime: " + totalTime);
            Log.d("PaymentFailedActivity", "selectedDate: " + selectedDate);
            Log.d("PaymentFailedActivity", "totalPrice: " + totalPrice);
            Log.d("PaymentFailedActivity", "orderStatus: " + orderStatus);
            Log.d("PaymentFailedActivity", "courtId: " + courtId);
            Log.d("PaymentFailedActivity", "orderType: " + orderType);
            Log.d("PaymentFailedActivity", "serviceDetailsJson: " + serviceDetailsJson);
            Log.d("PaymentFailedActivity", "customerName: " + getIntent().getStringExtra("customerName"));
            Log.d("PaymentFailedActivity", "phoneNumber: " + getIntent().getStringExtra("phoneNumber"));
            Log.d("PaymentFailedActivity", "note: " + getIntent().getStringExtra("note"));
            startActivity(intent);
            finish();
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