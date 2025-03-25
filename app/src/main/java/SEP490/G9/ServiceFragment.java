package SEP490.G9;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

public class ServiceFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service, container, false);
        TextView tvService = view.findViewById(R.id.tv_service);
        tvService.setText("Các dịch vụ nổi bật:\n- Thuê sân\n- Bán đồ thể thao\n- Đào tạo cầu lông");
        return view;
    }
}