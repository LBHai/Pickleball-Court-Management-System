package SEP490.G9;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import Adapter.CourtDetailPagerAdapter;

public class CourtDetailFragment extends Fragment {

    private TextView tvClubName, tvAddress;
    private Button btnBooking;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflate layout
        View view = inflater.inflate(R.layout.activity_court_detail_fragment, container, false);

        // Tham chiếu các view
        tvClubName = view.findViewById(R.id.tvClubName);
        tvAddress  = view.findViewById(R.id.tvAddress);
        tabLayout  = view.findViewById(R.id.tabLayout);
        viewPager  = view.findViewById(R.id.viewPager);
        btnBooking = view.findViewById(R.id.btnBooking);

        // Trong CourtDetailFragment.java, thêm lấy clubId từ Bundle
        Bundle args = getArguments();
        final String clubId;
        if (args != null) {
            String clubName = args.getString("club_name", "Tên câu lạc bộ");
            tvClubName.setText(clubName);
            clubId = args.getString("club_id", "");
        } else {
            clubId = "";
        }


        // Sau đó, khi khởi tạo adapter, truyền cả clubId:
        CourtDetailPagerAdapter adapter = new CourtDetailPagerAdapter(this, clubId);
        viewPager.setAdapter(adapter);


        // Dùng TabLayoutMediator để gắn TabLayout với ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Thông tin");
                    break;
                case 1:
                    tab.setText("Hình ảnh");
                    break;
            }
        }).attach();

        // Xử lý nút Đặt sân
        btnBooking.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), BookingTableActivity.class);
            intent.putExtra("club_id", clubId);
            startActivity(intent);
        });

        return view;
    }
}
