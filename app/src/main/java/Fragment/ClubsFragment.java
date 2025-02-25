package Fragment;

import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import Adapter.ClubsAdapter;
import Api.ApiService;
import Model.Clubs;
import SEP490.G9.ClubDetailFragment;
import SEP490.G9.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClubsFragment extends Fragment {

    private RecyclerView rcvClubs;
    private EditText edtSearch;
    private ClubsAdapter clubsAdapter;
    private List<Clubs> clubsList = new ArrayList<>(); // Danh sách gốc để lọc



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clubs, container, false);

        // Ánh xạ view
        rcvClubs = view.findViewById(R.id.rcvClubs);
        edtSearch = view.findViewById(R.id.edtSearch);

        rcvClubs.setLayoutManager(new LinearLayoutManager(getContext()));

        // Gọi API lấy danh sách CLB
        callApiGetClubs();

        // Lắng nghe sự kiện nhập vào ô tìm kiếm
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterClubs(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void callApiGetClubs() {
        ApiService.apiService.getlistClubs().enqueue(new Callback<List<Clubs>>() {
            @Override
            public void onResponse(Call<List<Clubs>> call, Response<List<Clubs>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    clubsList = response.body();
                    setupRecyclerView(clubsList);
                } else {
                    Log.e("API_ERROR", "Response body null hoặc lỗi: " + response.errorBody());
                    Toast.makeText(getContext(), "Lỗi API: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Clubs>> call, Throwable t) {
                Log.e("API_ERROR", "Lỗi gọi API: " + t.getMessage());
                Toast.makeText(getContext(), "Không thể kết nối đến máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setupRecyclerView(List<Clubs> list) {
        clubsAdapter = new ClubsAdapter(getContext(), list, club -> {
            // Mở ClubDetailFragment
            Fragment clubDetailFragment = new ClubDetailFragment();
            Bundle bundle = new Bundle();
            bundle.putString("club_id", club.getId());
            clubDetailFragment.setArguments(bundle);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, clubDetailFragment)
                    .addToBackStack(null)
                    .commit();
        });

        rcvClubs.setAdapter(clubsAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rcvClubs.getContext(), DividerItemDecoration.VERTICAL);
        rcvClubs.addItemDecoration(dividerItemDecoration);
    }




    private void filterClubs(String query) {
        List<Clubs> filteredList = new ArrayList<>();
        for (Clubs club : clubsList) {
            if (club.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(club);
            }
        }
        clubsAdapter.updateList(filteredList);
    }
}
