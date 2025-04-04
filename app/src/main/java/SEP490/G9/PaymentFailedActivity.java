package SEP490.G9;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class PaymentFailedActivity extends AppCompatActivity {
    private TextView tvOrderId;
    private ImageButton btnBack;
    private Button btnHistory,btnReturn;
    private String orderId, totalTime, selectedDate,orderStatus,courtId;
    private int totalPrice;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_failed);

        //tvOrderId = findViewById(R.id.tvOrderId);
        btnBack = findViewById(R.id.btnBack);
        btnHistory = findViewById(R.id.btnHistory);
        orderId = getIntent().getStringExtra("orderId");
        totalTime = getIntent().getStringExtra("totalTime");
        selectedDate = getIntent().getStringExtra("selectedDate");
        totalPrice = getIntent().getIntExtra("totalPrice", 0);
        orderStatus = getIntent().getStringExtra("orderStatus");
        courtId = getIntent().getStringExtra("courtId");

        btnBack.setOnClickListener(v -> goBackToMainActivity());

        // Khi nhấn nút History: chuyển sang DetailBookingActivity và truyền orderId (có thể truyền thêm các dữ liệu khác nếu cần)
        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(PaymentFailedActivity.this, DetailBookingActivity.class);
            intent.putExtra("orderId", orderId);
            intent.putExtra("totalTime", totalTime);
            intent.putExtra("selectedDate", selectedDate);
            intent.putExtra("totalPrice", totalPrice);
            intent.putExtra("orderStatus", orderStatus);
            intent.putExtra("courtId", courtId);
            Log.d("DetailBookingActivity", "courtId truyen di: " + courtId);

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
