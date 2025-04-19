package SEP490.G9;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import Fragment.AccountFragment;
import Fragment.CourtsFragment;
import Fragment.CourtServiceFragment;
import Session.SessionManager;

public class MainActivity extends AppCompatActivity {

    AccountFragment accountFragment;
    CourtsFragment courtsFragment;
    CourtServiceFragment courtServiceFragment;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo SessionManager
        sessionManager = new SessionManager(this);

        accountFragment = new AccountFragment();
        courtsFragment = new CourtsFragment();
        courtServiceFragment = new CourtServiceFragment();

        BottomNavigationView navigationView = findViewById(R.id.bottom_nav);

        // Thiết lập listener cho BottomNavigationView
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_account) {
                    loadFragment(accountFragment);
                    return true;
                } else if (itemId == R.id.nav_courts) {
                    loadFragment(courtsFragment);
                    return true;
                } else if (itemId == R.id.nav_court_service) {
                    loadFragment(courtServiceFragment);
                    return true;
                }
                return false;
            }
        });

        // Kiểm tra xem có intent để hiển thị fragment cụ thể không
        String showFragment = getIntent().getStringExtra("showFragment");
        if (showFragment != null) {
            switch (showFragment) {
                case "courts":
                    navigationView.setSelectedItemId(R.id.nav_courts);
                    break;
                case "account":
                    if (!isUserLoggedIn()) {
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        return;
                    }
                    navigationView.setSelectedItemId(R.id.nav_account);
                    break;
                case "prominent":
                    navigationView.setSelectedItemId(R.id.nav_court_service);
                    break;
                default:
                    navigationView.setSelectedItemId(R.id.nav_courts);
                    break;
            }
        } else {
            // Mặc định, mở CourtsFragment khi khởi động app
            if (savedInstanceState == null) {
                navigationView.setSelectedItemId(R.id.nav_courts);
                loadFragment(courtsFragment);
            }
        }
    }

    private boolean isUserLoggedIn() {
        String token = sessionManager.getToken();
        return token != null && !token.isEmpty();
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}
