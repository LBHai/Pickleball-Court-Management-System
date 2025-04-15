package Holder;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import SEP490.G9.R;

public class CourtViewHolder extends RecyclerView.ViewHolder {
    private ViewPager2 viewPager;
    private LinearLayout layoutIndicators;
    private ImageView imgClubLogo, btnMap;
    private TextView tvClubName, tvAddress, tvOpenTime, tvPhone;
    private Button btnService;
    private CardView btnShare;

    // Handler và Runnable dùng để tự động chuyển slide
    private Handler sliderHandler;
    private Runnable sliderRunnable;

    public CourtViewHolder(@NonNull View itemView) {
        super(itemView);
        viewPager = itemView.findViewById(R.id.viewPagerCourtImages);
        layoutIndicators = itemView.findViewById(R.id.layoutIndicators);
        imgClubLogo = itemView.findViewById(R.id.imgClubLogo);
        btnMap = itemView.findViewById(R.id.btnMap);
        tvClubName = itemView.findViewById(R.id.tvClubName);
        tvAddress = itemView.findViewById(R.id.tvAddress);
        tvOpenTime = itemView.findViewById(R.id.tvOpenTime);
        tvPhone = itemView.findViewById(R.id.tvPhone);
        btnService = itemView.findViewById(R.id.btnService);
        btnShare = itemView.findViewById(R.id.btnShare);

        // Khởi tạo Handler dùng Looper của thread chính
        sliderHandler = new Handler(Looper.getMainLooper());
    }

    // Các getter dùng cho adapter
    public ViewPager2 getViewPager() {
        return viewPager;
    }

    public LinearLayout getLayoutIndicators() {
        return layoutIndicators;
    }

    public ImageView getImgClubLogo() {
        return imgClubLogo;
    }

    public ImageView getBtnMap() {
        return btnMap;
    }

    public TextView getTvClubName() {
        return tvClubName;
    }

    public TextView getTvAddress() {
        return tvAddress;
    }

    public TextView getTvOpenTime() {
        return tvOpenTime;
    }

    public TextView getTvPhone() {
        return tvPhone;
    }

    public Button getBtnService() {
        return btnService;
    }

    public CardView getBtnShare() {
        return btnShare;
    }

    // Lưu lại handler và runnable để quản lý auto-slide
    public void setSliderHandler(Handler handler, Runnable runnable) {
        this.sliderHandler = handler;
        this.sliderRunnable = runnable;
    }

    // Dừng auto-slide khi item bị recycle
    public void stopAutoSlide() {
        if (sliderHandler != null && sliderRunnable != null) {
            sliderHandler.removeCallbacks(sliderRunnable);

        }
    }
}
