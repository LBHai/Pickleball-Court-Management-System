package UI.Adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import UI.Component.DisplayGallery;
import UI.Component.DisplayInforCourt;
import UI.Component.DisplayService;

public class CourtDetailAdapter extends FragmentStateAdapter {

    private static final int TAB_COUNT = 3;
    private String clubId; // ID sân được truyền từ CourtDetailFragment

    public CourtDetailAdapter(@NonNull Fragment fragment, String clubId) {
        super(fragment);
        this.clubId = clubId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                // Tạo InfoFragment và truyền clubId qua Bundle nếu cần
                DisplayInforCourt displayInforCourt = new DisplayInforCourt();
                Bundle bundle = new Bundle();
                bundle.putString("club_id", clubId);
                displayInforCourt.setArguments(bundle);
                return displayInforCourt;
            case 1:
                DisplayService displayService = DisplayService.newInstance(clubId);
                return displayService;

            case 2:
                // Tạo DisplayGallery và truyền clubId qua Bundle
                DisplayGallery displayGallery = new DisplayGallery();
                Bundle bundle2 = new Bundle();
                bundle2.putString("club_id", clubId);
                displayGallery.setArguments(bundle2);
                return displayGallery;
            default:
                return new DisplayInforCourt();
        }
    }

    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }
}
