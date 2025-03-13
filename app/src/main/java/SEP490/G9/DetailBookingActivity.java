package SEP490.G9;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DetailBookingActivity extends AppCompatActivity {

    private TextView tvTabBookingInfo, tvTabServiceDetail;
    private View lineBookingInfo, lineServiceDetail;
    private LinearLayout layoutBookingInfo, layoutServiceDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_booking);

        tvTabBookingInfo = findViewById(R.id.tvTabBookingInfo);
        tvTabServiceDetail = findViewById(R.id.tvTabServiceDetail);
        lineBookingInfo = findViewById(R.id.lineBookingInfo);
        lineServiceDetail = findViewById(R.id.lineServiceDetail);

    }
}
