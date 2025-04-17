package SEP490.G9;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PaymentFailedActivity extends AppCompatActivity {
    private TextView tvOrderId;
    private ImageButton btnBack;
    private Button btnHistory;
    private String orderId, totalTime, selectedDate, orderStatus, courtId, orderType;
    private int totalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_failed);

        btnBack = findViewById(R.id.btnBack);
        btnHistory = findViewById(R.id.btnHistory);

        orderId = getIntent().getStringExtra("orderId");
        totalTime = getIntent().getStringExtra("totalTime");
        selectedDate = getIntent().getStringExtra("selectedDate");
        totalPrice = getIntent().getIntExtra("totalPrice", 0);
        orderStatus = getIntent().getStringExtra("orderStatus");
        courtId = getIntent().getStringExtra("courtId");
        orderType = getIntent().getStringExtra("orderType");

        if (totalTime == null) {
            Log.e("PaymentFailedActivity", "totalTime là null, kiểm tra lại QRCodeActivity");
            totalTime = "0h00";
        }

        btnBack.setOnClickListener(v -> goBackToMainActivity());

        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(PaymentFailedActivity.this, DetailBookingActivity.class);
            intent.putExtra("orderId", orderId);
            intent.putExtra("totalTime", totalTime);
            intent.putExtra("selectedDate", selectedDate);
            intent.putExtra("totalPrice", totalPrice);
            intent.putExtra("orderStatus", orderStatus);
            intent.putExtra("courtId", courtId);
            intent.putExtra("orderType", orderType);
            intent.putExtra("serviceDetailsJson", getIntent().getStringExtra("serviceDetailsJson"));
            intent.putExtra("customerName", getIntent().getStringExtra("customerName"));  // Truyền tiếp
            intent.putExtra("phoneNumber", getIntent().getStringExtra("phoneNumber"));    // Truyền tiếp
            intent.putExtra("note", getIntent().getStringExtra("note"));                  // Truyền tiếp
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