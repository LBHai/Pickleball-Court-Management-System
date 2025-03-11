package SEP490.G9;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

public class DetailPriceCourtActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private String clubId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_price_court);

        clubId = getIntent().getStringExtra("club_id");

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(DetailPriceCourtActivity.this, BookingTableActivity.class);
            intent.putExtra("club_id", clubId); 
            startActivity(intent);
            finish();
        });
    }
}
