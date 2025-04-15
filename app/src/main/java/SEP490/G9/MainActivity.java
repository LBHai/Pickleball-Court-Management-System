package SEP490.G9;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import Fragment.AccountFragment;
import Fragment.CourtsFragment;
import Fragment.MapFragment;
import Fragment.CourtServiceFragment;
import Session.SessionManager;

public class MainActivity extends AppCompatActivity {

    AccountFragment accountFragment;
    CourtsFragment courtsFragment;
    MapFragment mapFragment;
    CourtServiceFragment courtServiceFragment;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo SessionManager
        sessionManager = new SessionManager(this);

        // Reset cờ hasShownGuestDialog khi ứng dụng khởi động (tùy chọn)
        // Nếu bạn muốn dialog hiển thị lại mỗi khi ứng dụng khởi động lại, để dòng này
        // Nếu không, hãy xóa dòng dưới đây để dialog chỉ hiển thị một lần duy nhất cho đến khi ứng dụng bị gỡ cài đặt
        sessionManager.setHasShownGuestDialog(false);

        accountFragment = new AccountFragment();
        courtsFragment = new CourtsFragment();
        mapFragment = new MapFragment();
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
                } else if (itemId == R.id.nav_map) {
                    loadFragment(mapFragment);
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

        // Kiểm tra trạng thái đăng nhập và hiển thị dialog cho guest
        if (!isUserLoggedIn()) {
            if (!sessionManager.hasShownGuestDialog()) {
                showGuestDialog();
                sessionManager.setHasShownGuestDialog(true);
            }
        }

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
                case "map":
                    navigationView.setSelectedItemId(R.id.nav_map);
                    break;
                case "prominent":
                    navigationView.setSelectedItemId(R.id.nav_court_service);
                    break;
                default:
                    navigationView.setSelectedItemId(R.id.nav_map);
                    break;
            }
        } else {
            if (savedInstanceState == null) {
                navigationView.setSelectedItemId(R.id.nav_map);
                loadFragment(mapFragment);
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

    private void showGuestDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Tạo tài khoản để dễ dàng quản lý và lưu trữ lịch đặt của bạn.");

        builder.setPositiveButton("Đăng nhập", (dialog, which) -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        builder.setNegativeButton("Đăng ký", (dialog, which) -> {
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
            finish();
        });

        builder.setNeutralButton("OK", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(this, android.R.color.black));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(this, android.R.color.black));
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
                .setTextColor(ContextCompat.getColor(this, android.R.color.white));
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
                .setBackgroundColor(ContextCompat.getColor(this, R.color.green));
    }
}