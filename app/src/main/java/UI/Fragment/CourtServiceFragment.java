package UI.Fragment;

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

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import UI.Adapter.CourtsServiceAdapter;
import Data.Network.ApiService;
import Data.Network.RetrofitClient;
import Data.Model.Courts;
import SEP490.G9.R;
import UI.Activity.ServiceActivity;

public class CourtServiceFragment extends Fragment {

    private RecyclerView rcvClubs;
    private EditText edtSearch;
    private CourtsServiceAdapter courtsServiceAdapter;
    // originalCourtsList giữ dữ liệu gốc
    private List<Courts> originalCourtsList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_court_service, container, false);

        rcvClubs = view.findViewById(R.id.rcvClubs);
        edtSearch = view.findViewById(R.id.edtSearch);
        rcvClubs.setLayoutManager(new LinearLayoutManager(getContext()));
        callApiGetCourts();

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { /* Không cần thao tác gì */ }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterClubs(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) { /* Không cần thao tác gì */ }
        });
        return view;
    }

    private void callApiGetCourts() {
        ApiService apiService = RetrofitClient.getApiService(getContext());
        apiService.getCourts().enqueue(new retrofit2.Callback<List<Courts>>() {
            @Override
            public void onResponse(retrofit2.Call<List<Courts>> call, retrofit2.Response<List<Courts>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    originalCourtsList = response.body();
                    setupRecyclerView(originalCourtsList);
                } else {
                    Log.e("API_ERROR", "Response body null hoặc lỗi: " + response.errorBody());
                    Toast.makeText(getContext(), "Lỗi API: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(retrofit2.Call<List<Courts>> call, Throwable t) {
                Log.e("API_ERROR", "Lỗi gọi API: " + t.getMessage());
                Toast.makeText(getContext(), "Không thể kết nối đến máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Trong CourtServiceFragment.java, phương thức setupRecyclerView():
    private void setupRecyclerView(List<Courts> list) {
        // Khởi tạo adapter và cài đặt listener cho nút btnService
        courtsServiceAdapter = new CourtsServiceAdapter(getContext(), list, new CourtsServiceAdapter.OnCourtClickListener() {
            @Override
            public void onCourtClick(Courts court) {
                // Khi bấm vào btnService, chuyển sang fragment ServiceActivity
                // Truyền courtId từ đối tượng court vào newInstance()
                ServiceActivity serviceFragment = ServiceActivity.newInstance(court.getId());
                // Sử dụng FragmentTransaction để thay thế Fragment hiện tại
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, serviceFragment) // R.id.fragment_container là container của fragment trong layout của Activity
                        .addToBackStack(null)
                        .commit();
            }
        });
        rcvClubs.setAdapter(courtsServiceAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rcvClubs.getContext(), DividerItemDecoration.VERTICAL);
        rcvClubs.addItemDecoration(dividerItemDecoration);
    }


    private String removeDiacritics(String str) {
        if (str == null) return "";
        String normalized = Normalizer.normalize(str, Normalizer.Form.NFD);
        return Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
                .matcher(normalized)
                .replaceAll("")
                .toLowerCase(Locale.getDefault())
                .trim();
    }

    private void filterClubs(String query) {
        List<Courts> filteredList = new ArrayList<>();
        String normalizedQuery = removeDiacritics(query);
        for (Courts club : originalCourtsList) {
            String normalizedName = removeDiacritics(club.getName());
            if (normalizedName.contains(normalizedQuery)) {
                filteredList.add(club);
            }
        }
        courtsServiceAdapter.updateList(filteredList);
    }
}
