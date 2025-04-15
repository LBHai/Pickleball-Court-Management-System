package Adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import SEP490.G9.GalleryFragment;
import SEP490.G9.InfoFragment;
import SEP490.G9.ServiceFragment;

public class CourtDetailPagerAdapter extends FragmentStateAdapter {

    private static final int TAB_COUNT = 3;
    private String clubId; // ID sân được truyền từ CourtDetailFragment

    public CourtDetailPagerAdapter(@NonNull Fragment fragment, String clubId) {
        super(fragment);
        this.clubId = clubId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                // Tạo InfoFragment và truyền clubId qua Bundle nếu cần
                InfoFragment infoFragment = new InfoFragment();
                Bundle bundle = new Bundle();
                bundle.putString("club_id", clubId);
                infoFragment.setArguments(bundle);
                return infoFragment;
            case 1:
                // Tạo CourtServiceFragment và truyền clubId qua Bundle
                ServiceFragment serviceFragment = new ServiceFragment();
                Bundle bundle3 = new Bundle();
                bundle3.putString("club_id", clubId);
                serviceFragment.setArguments(bundle3);
                return serviceFragment;
            case 2:
                // Tạo GalleryFragment và truyền clubId qua Bundle
                GalleryFragment galleryFragment = new GalleryFragment();
                Bundle bundle2 = new Bundle();
                bundle2.putString("club_id", clubId);
                galleryFragment.setArguments(bundle2);
                return galleryFragment;
            default:
                return new InfoFragment();
        }
    }

    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }
}
