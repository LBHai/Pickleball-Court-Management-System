package Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import SEP490.G9.R;


public class AccountFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate layout cho Fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        setHasOptionsMenu(true); // Bật hiển thị menu trong Fragment
        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_menu, menu); // Inflate menu từ file XML
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_edit_info) {
            // Handle "Edit your information"
            return true;
        } else if (itemId == R.id.menu_change_password) {
            // Handle "Change password"
            return true;
        } else if (itemId == R.id.menu_language) {
            // Handle "Language"
            return true;
        } else if (itemId == R.id.menu_logout) {
            // Handle "Logout"
            return true;
        } else if (itemId == R.id.menu_delete_account) {
            // Handle "Delete account"
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}