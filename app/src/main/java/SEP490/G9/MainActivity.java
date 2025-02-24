package SEP490.G9;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import Fragment.AccountFragment;
import Fragment.ClubsFragment;
import Fragment.MapFragment;
import Fragment.ProminentFragment;

public class MainActivity extends AppCompatActivity {

    // Fragment
    AccountFragment accountFragment;
    ClubsFragment clubsFragment;
    MapFragment mapFragment;
    ProminentFragment prominentFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Khởi tạo các Fragment
        accountFragment = new AccountFragment();
        clubsFragment = new ClubsFragment();
        mapFragment = new MapFragment();
        prominentFragment = new ProminentFragment();

        // Khởi tạo navigationView sau setContentView
        BottomNavigationView navigationView = findViewById(R.id.bottom_nav);

        // Thiết lập listener cho BottomNavigationView
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_map) {
                    loadFragment(mapFragment);
                    return true;
                } else if (itemId == R.id.nav_clubs) {
                    loadFragment(clubsFragment);
                    return true;
                } else if (itemId == R.id.nav_prominent) {
                    loadFragment(prominentFragment);
                    return true;
                } else if (itemId == R.id.nav_account) {
                    loadFragment(accountFragment);
                    return true;
                }
                return false;
            }
        });

        // Mặc định hiển thị HomeFragment khi khởi động
        if (savedInstanceState == null) {
            navigationView.setSelectedItemId(R.id.nav_map); // Đặt item mặc định được chọn
            loadFragment(mapFragment);
        }
    }

    // Phương thức để load Fragment
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}