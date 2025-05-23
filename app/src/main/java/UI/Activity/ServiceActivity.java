package UI.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import UI.Adapter.ServiceAdapter;
import Data.Network.ApiService;
import Data.Network.RetrofitClient;
import Data.Model.Courts;
import Data.Model.Service;
import Data.Model.ServiceDetail;
import SEP490.G9.R;
import Data.Session.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceActivity extends Fragment {
    private RecyclerView recyclerViewServices;
    private ServiceAdapter serviceAdapter;
    private List<Service> serviceList = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView tvEmptyServices;
    private TextView tvTotalItems;
    private TextView tvTotalPrice;
    private MaterialButton btnOrderServices;
    private EditText etSearch;
    private ImageView imgClearSearch;
    private TabLayout tabLayout;

    private String courtId;
    private SessionManager sessionManager;
    private List<ServiceDetail> orderedServiceDetails;

    public ServiceActivity() {
        // Required empty public constructor
    }

    public static ServiceActivity newInstance(String courtId) {
        ServiceActivity fragment = new ServiceActivity();
        Bundle args = new Bundle();
        args.putString("courtId", courtId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_service, container, false);

        // Lấy courtId từ arguments
        if (getArguments() != null) {
            courtId = getArguments().getString("courtId", "1");
            Log.d("ServiceActivity", "courtId=" + courtId);
        } else {
            courtId = "1";
        }

        // Khởi tạo session, list order
        sessionManager = new SessionManager(getContext());
        orderedServiceDetails = new ArrayList<>();

        // Find view
        recyclerViewServices = view.findViewById(R.id.recyclerViewServices);
        progressBar         = view.findViewById(R.id.progressBar);
        tvEmptyServices     = view.findViewById(R.id.tvEmptyServices);
        tvTotalItems        = view.findViewById(R.id.tvItemCount);
        tvTotalPrice        = view.findViewById(R.id.tvTotalPrice);
        btnOrderServices    = view.findViewById(R.id.btnOrderServices);
        etSearch            = view.findViewById(R.id.etSearch);
        imgClearSearch      = view.findViewById(R.id.imgClearSearch);
        tabLayout           = view.findViewById(R.id.tabLayout);

        // Setup RecyclerView, Search và TabLayout
        recyclerViewServices.setLayoutManager(new LinearLayoutManager(getContext()));
        setupRecyclerView();
        setupSearch();
        setupTabLayout();

        // Load data
        loadServices();

        // Button order
        btnOrderServices.setOnClickListener(v -> handleOrderServices());

        return view;
    }

    private void setupRecyclerView() {
        serviceAdapter = new ServiceAdapter(
                getContext(),
                serviceList,
                (service, newQty) -> updateTotals()
        );
        recyclerViewServices.setAdapter(serviceAdapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c1, int c2) {}
            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                serviceAdapter.getFilter().filter(s);
                imgClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
            @Override public void afterTextChanged(Editable e) {}
        });

        imgClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            serviceAdapter.getFilter().filter("");
        });
    }

    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterServicesByTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                filterServicesByTab(tab.getPosition());
            }
        });
    }

    private void filterServicesByTab(int position) {
        List<Service> filteredList = new ArrayList<>();
        String category = getCategoryFromTabPosition(position);

        if (category.equals("All")) {
            filteredList.addAll(serviceList);
        } else {
            for (Service service : serviceList) {
                if (service.getCategory() != null && service.getCategory().equalsIgnoreCase(category)) {
                    filteredList.add(service);
                }
            }
        }

        serviceAdapter.updateList(filteredList);
        serviceAdapter.getFilter().filter(etSearch.getText());
        showEmptyView(filteredList.isEmpty());
    }

    private String getCategoryFromTabPosition(int position) {
        switch (position) {
            case 0: return "All";       // Hiển thị tất cả dịch vụ
            case 1: return "Đồ uống";   // Lọc các dịch vụ có category là "Đồ uống"
            case 2: return "Đồ ăn";     // Lọc các dịch vụ có category là "Đồ ăn"
            case 3: return "Khác";      // Lọc các dịch vụ có category là "Khác"
            default: return "All";
        }
    }

    private void loadServices() {
        showLoading(true);
        ApiService apiService = RetrofitClient.getApiService(getContext());
        apiService.getServices(courtId).enqueue(new Callback<List<Service>>() {
            @Override
            public void onResponse(Call<List<Service>> call, Response<List<Service>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    serviceList.clear();
                    serviceList.addAll(response.body());
                    filterServicesByTab(0); // Hiển thị tab "All" mặc định
                } else {
                    showEmptyView(true);
                    Toast.makeText(getContext(), getString(R.string.error_loading_service), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Service>> call, Throwable t) {
                showLoading(false);
                showEmptyView(true);
                Toast.makeText(getContext(), getString(R.string.connection_error)+ t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTotals() {
        int totalItems = serviceAdapter.getTotalOrderQuantity();
        double totalPrice = serviceAdapter.getTotalPrice();
        tvTotalItems.setText(totalItems + " mục");
        tvTotalPrice.setText(String.format(Locale.getDefault(), "%,.0fđ", totalPrice));
    }

    private void handleOrderServices() {
        Map<String, Integer> orderQuantities = serviceAdapter.getOrderQuantities();
        orderedServiceDetails.clear();

        for (Map.Entry<String, Integer> entry : orderQuantities.entrySet()) {
            String serviceId = entry.getKey();
            int qty = entry.getValue();
            if (qty > 0) {
                Service svc = findServiceById(serviceId);
                if (svc != null) {
                    ServiceDetail detail = new ServiceDetail();
                    detail.setCourtServiceId(serviceId);
                    detail.setQuantity(qty);
                    detail.setPrice(svc.getPrice());
                    orderedServiceDetails.add(detail);
                }
            }
        }

        if (orderedServiceDetails.isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.select_service), Toast.LENGTH_SHORT).show();
            return;
        }

        String priceText = tvTotalPrice.getText().toString()
                .replace("đ","")
                .replace(",","")
                .trim();
        double paymentAmount = Double.parseDouble(priceText);

        RetrofitClient.getApiService(getContext())
                .getCourtById(courtId)
                .enqueue(new Callback<Courts>() {
                    @Override
                    public void onResponse(Call<Courts> call, Response<Courts> resp) {
                        if (resp.isSuccessful() && resp.body() != null) {
                            Courts court = resp.body();
                            Intent intent = new Intent(getActivity(), ConfirmActivity.class);
                            intent.putExtra("orderType", "Đơn dịch vụ");
                            intent.putExtra("courtId", courtId);
                            intent.putExtra("courtName", court.getName());
                            intent.putExtra("address", court.getAddress());
                            intent.putExtra("paymentAmount", paymentAmount);
                            intent.putExtra("serviceDetailsJson", new Gson().toJson(orderedServiceDetails));
                            intent.putExtra("serviceListJson", new Gson().toJson(serviceList));
                            startActivity(intent);
                        } else {
                            Toast.makeText(getContext(), getString(R.string.error_getting_field_info), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Courts> call, Throwable t) {
                        Toast.makeText(getContext(), getString(R.string.network_error)+ t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Service findServiceById(String id) {
        for (Service s : serviceList) {
            if (s.getId().equals(id)) return s;
        }
        return null;
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerViewServices.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void showEmptyView(boolean isEmpty) {
        tvEmptyServices.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerViewServices.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}