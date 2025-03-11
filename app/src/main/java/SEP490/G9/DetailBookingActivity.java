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
        //layoutBookingInfo = findViewById(R.id.layoutBookingInfo);
        //layoutServiceDetail = findViewById(R.id.layoutServiceDetail);

        showBookingInfo();

        tvTabBookingInfo.setOnClickListener(v -> showBookingInfo());
        tvTabServiceDetail.setOnClickListener(v -> showServiceDetail());
    }

    private void showBookingInfo() {
        layoutBookingInfo.setVisibility(View.VISIBLE);
        layoutServiceDetail.setVisibility(View.GONE);
        lineBookingInfo.setVisibility(View.VISIBLE);
        lineServiceDetail.setVisibility(View.INVISIBLE);
        tvTabBookingInfo.setTextColor(getResources().getColor(android.R.color.white));
        tvTabServiceDetail.setTextColor(getResources().getColor(android.R.color.darker_gray));
    }

    private void showServiceDetail() {
        layoutBookingInfo.setVisibility(View.GONE);
        layoutServiceDetail.setVisibility(View.VISIBLE);
        lineBookingInfo.setVisibility(View.INVISIBLE);
        lineServiceDetail.setVisibility(View.VISIBLE);
        tvTabBookingInfo.setTextColor(getResources().getColor(android.R.color.darker_gray));
        tvTabServiceDetail.setTextColor(getResources().getColor(android.R.color.white));
    }
}
