package Fragment;

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

import Adapter.CourtsAdapter;
import Api.ApiService;
import Api.RetrofitClient;
import Model.Courts;
import SEP490.G9.CourtDetailFragment;
import SEP490.G9.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourtsFragment extends Fragment {

    private RecyclerView rcvClubs;
    private EditText edtSearch;
    private CourtsAdapter courtsAdapter;
    private List<Courts> courtsList = new ArrayList<>(); // Danh sách gốc để lọc

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_courts, container, false);

        rcvClubs = view.findViewById(R.id.rcvClubs);
        //edtSearch = view.findViewById(R.id.edtSearch);

        rcvClubs.setLayoutManager(new LinearLayoutManager(getContext()));

        callApiGetCourts();

//        edtSearch.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                filterClubs(s.toString());
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) { }
//        });

        return view;
    }

    private void callApiGetCourts() {
        ApiService apiService = RetrofitClient.getApiService(getContext());
        apiService.getCourts().enqueue(new Callback<List<Courts>>() {
            @Override
            public void onResponse(Call<List<Courts>> call, Response<List<Courts>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    courtsList = response.body();
                    setupRecyclerView(courtsList);
                } else {
                    Log.e("API_ERROR", "Response body null hoặc lỗi: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<List<Courts>> call, Throwable t) {
                Log.e("API_ERROR", "Lỗi gọi API: " + t.getMessage());
                Toast.makeText(getContext(), "Không thể kết nối đến máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView(List<Courts> list) {
        courtsAdapter = new CourtsAdapter(getContext(), list, club -> {
            Fragment clubDetailFragment = new CourtDetailFragment();
            Bundle bundle = new Bundle();
            bundle.putString("club_id", club.getId());
            bundle.putString("club_name", club.getName());
            bundle.putString("backgroundUrl", club.getBackgroundUrl() != null ? club.getBackgroundUrl() : "");
            bundle.putString("address", club.getAddress());
            bundle.putString("tvPhone", club.getPhone());
            clubDetailFragment.setArguments(bundle);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, clubDetailFragment)
                    .addToBackStack(null)
                    .commit();
        });

        rcvClubs.setAdapter(courtsAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rcvClubs.getContext(), DividerItemDecoration.VERTICAL);
        rcvClubs.addItemDecoration(dividerItemDecoration);
    }

    private void filterClubs(String query) {
        List<Courts> filteredList = new ArrayList<>();
        for (Courts club : courtsList) {
            if (club.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(club);
            }
        }
        courtsAdapter.updateList(filteredList);
    }
}