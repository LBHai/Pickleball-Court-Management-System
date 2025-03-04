package SEP490.G9;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import Fragment.AccountFragment;
import Fragment.CourtsFragment;
import Fragment.MapFragment;
import Fragment.ProminentFragment;
import Session.SessionManager;

public class MainActivity extends AppCompatActivity {

    // Fragment
    AccountFragment accountFragment;
    CourtsFragment courtsFragment;
    MapFragment mapFragment;
    ProminentFragment prominentFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Khởi tạo các Fragment
        accountFragment = new AccountFragment();
        courtsFragment = new CourtsFragment();
        mapFragment = new MapFragment();
        prominentFragment = new ProminentFragment();

        // Khởi tạo navigationView sau setContentView
        BottomNavigationView navigationView = findViewById(R.id.bottom_nav);

        // Thiết lập listener cho BottomNavigationView
        // Thiết lập listener cho BottomNavigationView
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_account) {
                    if (!isUserLoggedIn()) { // Kiểm tra nếu chưa đăng nhập
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        return false; // Không load fragment nếu chưa đăng nhập
                    }
                    loadFragment(accountFragment);
                    return true;
                } else if (itemId == R.id.nav_map) {
                    loadFragment(mapFragment);
                    return true;
                } else if (itemId == R.id.nav_courts) {
                    loadFragment(courtsFragment);
                    return true;
                } else if (itemId == R.id.nav_prominent) {
                    loadFragment(prominentFragment);
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
    // Phương thức kiểm tra đăng nhập
    private boolean isUserLoggedIn() {
        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getToken();
        return token != null && !token.isEmpty();
    }
    // Phương thức để load Fragment
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}