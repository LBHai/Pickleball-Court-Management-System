package SEP490.G9;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import Adapter.CourtDetailPagerAdapter;

public class CourtDetailFragment extends Fragment {

    private TextView tvClubName, tvAddress;
    private Button btnBooking;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ImageView imgClubCover;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_court_detail_fragment, container, false);

        // Ánh xạ các view
        tvClubName = view.findViewById(R.id.tvClubName);
        tvAddress  = view.findViewById(R.id.tvAddress);
        tabLayout  = view.findViewById(R.id.tabLayout);
        viewPager  = view.findViewById(R.id.viewPager);
        btnBooking = view.findViewById(R.id.btnBooking);
        imgClubCover = view.findViewById(R.id.imgClubCover);

        // Lấy dữ liệu từ Bundle
        Bundle args = getArguments();
        final String clubId;
        String backgroundUrl = getArguments().getString("backgroundUrl", "");
        Log.d("CourtDetailFragment", "Received backgroundUrl: " + backgroundUrl);
        args.putString("backgroundUrl", backgroundUrl);
        if (args != null) {
            String clubName = args.getString("club_name", "Tên câu lạc bộ");
            clubId = args.getString("club_id", "");
            backgroundUrl = args.getString("backgroundUrl", "");
            String address = args.getString("address", "");
            tvClubName.setText(clubName);
            tvAddress.setText(address);

            // Log để kiểm tra giá trị
            Log.d("CourtDetailFragment", "Received backgroundUrl: " + backgroundUrl);
        } else {
            clubId = "";
        }

        // Hiển thị hình ảnh vào imgClubCover
        if (backgroundUrl != null && !backgroundUrl.isEmpty()) {
            Glide.with(this)
                    .load(backgroundUrl)
                    .placeholder(R.drawable.pickleball) // Hình ảnh trong khi tải
                    .error(R.drawable.warning)          // Hình ảnh nếu lỗi
                    .into(imgClubCover);
        } else {
            imgClubCover.setImageResource(R.drawable.pickleball); // Hình ảnh mặc định
            Log.d("CourtDetailFragment", "backgroundUrl is empty or null");
        }

        // Cấu hình ViewPager và TabLayout
        CourtDetailPagerAdapter adapter = new CourtDetailPagerAdapter(this, clubId);
        viewPager.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Thông tin"); break;
                case 1: tab.setText("Dịch vụ"); break;
                case 2: tab.setText("Hình ảnh"); break;
            }
        }).attach();

        // Xử lý sự kiện nút Booking
        btnBooking.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), BookingTableActivity.class);
            intent.putExtra("club_id", clubId);
            startActivity(intent);
        });

        return view;
    }
}