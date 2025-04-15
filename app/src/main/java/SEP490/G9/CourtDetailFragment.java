package SEP490.G9;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import Adapter.CourtDetailPagerAdapter;
import SEP490.G9.BookingRegularTableActivity;
import SEP490.G9.BookingTableActivity;
import SEP490.G9.R;

public class CourtDetailFragment extends Fragment {

    private TextView tvClubName, tvAddress;
    private Button btnBooking;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ImageView imgClubCover;
    private String clubId;
    private String phoneNumber; // Số điện thoại cần truyền qua

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_court_detail_fragment, container, false);

        // Ánh xạ các view
        tvClubName = view.findViewById(R.id.tvClubName);
        tvAddress = view.findViewById(R.id.tvAddress);
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        btnBooking = view.findViewById(R.id.btnBooking);
        imgClubCover = view.findViewById(R.id.imgClubCover);

        // Lấy dữ liệu từ Bundle
        Bundle args = getArguments();
        String backgroundUrl = "";

        if (args != null) {
            String clubName = args.getString("club_name", "Tên câu lạc bộ");
            clubId = args.getString("club_id", "");
            backgroundUrl = args.getString("backgroundUrl", "");
            String address = args.getString("address", "");
            // Lấy số điện thoại từ key "tvPhone"
            phoneNumber = args.getString("tvPhone", "");
            tvClubName.setText(clubName);
            tvAddress.setText(address);
        } else {
            clubId = "";
            phoneNumber = "";
        }

        // Hiển thị hình ảnh cover
        if (backgroundUrl != null && !backgroundUrl.isEmpty()) {
            Glide.with(this)
                    .load(backgroundUrl)
                    .placeholder(R.drawable.pickleball)
                    .error(R.drawable.warning)
                    .into(imgClubCover);
        } else {
            imgClubCover.setImageResource(R.drawable.pickleball);
        }

        // Cấu hình ViewPager và TabLayout
        CourtDetailPagerAdapter adapter = new CourtDetailPagerAdapter(this, clubId);
        viewPager.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Thông tin");
                    break;
                case 1:
                    tab.setText("Dịch vụ");
                    break;
                case 2:
                    tab.setText("Hình ảnh");
                    break;
            }
        }).attach();

        // Khi click nút Booking, gọi dialog và truyền số điện thoại
        btnBooking.setOnClickListener(v -> {
            String phone = phoneNumber != null ? phoneNumber : "";
            showBookingDialog(requireContext(), clubId, phone);
        });

        return view;
    }

    private void showBookingDialog(Context context, String clubId, String tvPhone) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_booking, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();

        // Ánh xạ các thành phần trong dialog
        ImageView imgClose = dialogView.findViewById(R.id.imgClose);
        CardView cardHangNgay = dialogView.findViewById(R.id.cardHangNgay);
        CardView cardCoDinh = dialogView.findViewById(R.id.cardCoDinh);
        ImageButton btnBook = dialogView.findViewById(R.id.btnBook);
        ImageButton btnBookRegular = dialogView.findViewById(R.id.btnBookRegular);

        imgClose.setOnClickListener(v -> dialog.dismiss());

        // Listener cho BookingTableActivity
        View.OnClickListener bookingTableClick = v -> {
            dialog.dismiss();
            Intent intent = new Intent(context, BookingTableActivity.class);
            intent.putExtra("club_id", clubId);
            intent.putExtra("booking_type", "truc_quan");
            intent.putExtra("tvPhone", tvPhone);
            context.startActivity(intent);
        };

        // Listener cho BookingRegularTableActivity
        View.OnClickListener bookingRegularClick = v -> {
            dialog.dismiss();
            Intent intent = new Intent(context, BookingRegularTableActivity.class);
            intent.putExtra("club_id", clubId);
            intent.putExtra("booking_type", "xe_ve");
            intent.putExtra("tvPhone", tvPhone);
            context.startActivity(intent);
        };

        cardHangNgay.setOnClickListener(bookingTableClick);
        btnBook.setOnClickListener(bookingTableClick);
        cardCoDinh.setOnClickListener(bookingRegularClick);
        btnBookRegular.setOnClickListener(bookingRegularClick);
    }
}
