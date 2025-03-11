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

    AccountFragment accountFragment;
    CourtsFragment courtsFragment;
    MapFragment mapFragment;
    ProminentFragment prominentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accountFragment = new AccountFragment();
        courtsFragment = new CourtsFragment();
        mapFragment = new MapFragment();
        prominentFragment = new ProminentFragment();

        BottomNavigationView navigationView = findViewById(R.id.bottom_nav);

        // Thiết lập listener cho BottomNavigationView
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_account) {
                    if (!isUserLoggedIn()) {
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        return false;
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
                    navigationView.setSelectedItemId(R.id.nav_prominent);
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
        SessionManager sessionManager = new SessionManager(this);
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
