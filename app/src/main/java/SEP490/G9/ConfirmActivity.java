package SEP490.G9;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import Model.ConfirmOrder;

public class ConfirmActivity extends AppCompatActivity {
    private TextView tvSummary;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
        tvSummary = findViewById(R.id.tvSummary);
        String date = getIntent().getStringExtra("selectedDate");
        String confirmOrdersJson = getIntent().getStringExtra("confirmOrdersJson");
        Gson gson = new Gson();
        Type listType = new TypeToken<List<ConfirmOrder>>(){}.getType();
        List<ConfirmOrder> confirmOrders = gson.fromJson(confirmOrdersJson, listType);
        StringBuilder sb = new StringBuilder();
        sb.append("Ngày đặt: ").append(date).append("\n\n");
        sb.append("Thông tin đặt:\n");
        for (ConfirmOrder order : confirmOrders) {
            sb.append("Sân: ").append(order.getCourtSlotName()).append("\n")
                    .append("Thời gian: ").append(order.getStartTime())
                    .append(" - ").append(order.getEndTime()).append("\n")
                    .append("Giá hàng ngày: ").append(order.getDailyPrice()).append("\n\n");
        }
        tvSummary.setText(sb.toString());
    }
}
