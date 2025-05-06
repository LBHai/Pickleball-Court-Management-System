package UI.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import UI.Component.PermissionRequestDialog;
import UI.Fragment.AccountFragment;
import UI.Fragment.CourtsFragment;
import UI.Fragment.CourtServiceFragment;
import SEP490.G9.R;
import Data.Session.SessionManager;

public class MainActivity extends AppCompatActivity implements PermissionRequestDialog.PermissionCallback {

    AccountFragment accountFragment;
    CourtsFragment courtsFragment;
    CourtServiceFragment courtServiceFragment;
    private SessionManager sessionManager;

    private static final long DEBOUNCE_DELAY = 250; // milliseconds
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pendingFragmentLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Kiểm tra và yêu cầu quyền trước khi khởi tạo các thành phần khác
        PermissionRequestDialog.checkAndRequestPermissions(this);

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
                Fragment selectedFragment = null;
                if (itemId == R.id.nav_account) {
                    selectedFragment = accountFragment;
                } else if (itemId == R.id.nav_courts) {
                    selectedFragment = courtsFragment;
                } else if (itemId == R.id.nav_court_service) {
                    selectedFragment = courtServiceFragment;
                }

                if (selectedFragment != null) {
                    loadFragmentWithDebounce(selectedFragment);
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
                loadFragmentWithDebounce(courtsFragment);
            }
        }
    }

    private boolean isUserLoggedIn() {
        String token = sessionManager.getToken();
        return token != null && !token.isEmpty();
    }

    private void loadFragmentWithDebounce(final Fragment fragment) {
        if (pendingFragmentLoad != null) {
            handler.removeCallbacks(pendingFragmentLoad);
        }
        pendingFragmentLoad = new Runnable() {
            @Override
            public void run() {
                loadFragment(fragment);
                pendingFragmentLoad = null;
            }
        };
        handler.postDelayed(pendingFragmentLoad, DEBOUNCE_DELAY);
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onAllPermissionsGranted() {
        Toast.makeText(this, getString(R.string.all_permissions_granted), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionsDenied(List<String> deniedPermissions) {
        StringBuilder message = new StringBuilder();
        for (String permission : deniedPermissions) {
            message.append("- ").append(getPermissionName(permission)).append("\n");
        }

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.permissions_denied_title))
                .setMessage(message.toString() + "\n" + getString(R.string.permissions_denied_message_suffix))
                .setPositiveButton(getString(R.string.button_agree), null)
                .show();
    }

    private String getPermissionName(String permission) {
        switch (permission) {
            case Manifest.permission.CAMERA:
                return getString(R.string.denied_camera);
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                return getString(R.string.denied_read_storage);
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return getString(R.string.denied_write_storage);
            case Manifest.permission.ACCESS_FINE_LOCATION:
                return getString(R.string.denied_fine_location);
            case Manifest.permission.ACCESS_COARSE_LOCATION:
                return getString(R.string.denied_coarse_location);
            default:
                return permission.substring(permission.lastIndexOf(".") + 1);
        }
    }

}