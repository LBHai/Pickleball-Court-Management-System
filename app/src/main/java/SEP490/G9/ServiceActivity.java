package SEP490.G9;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import Adapter.ServiceAdapter;
import Api.ApiService;
import Api.RetrofitClient;
import Model.Courts;
import Model.Service;
import Model.ServiceDetail;
import Session.SessionManager;
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

        if (getArguments() != null) {
            courtId = getArguments().getString("courtId", "1");
            Log.d("courtId", courtId);
        } else {
            courtId = "1";
        }

        sessionManager = new SessionManager(getContext());
        orderedServiceDetails = new ArrayList<>();

        recyclerViewServices = view.findViewById(R.id.recyclerViewServices);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyServices = view.findViewById(R.id.tvEmptyServices);
        tvTotalItems = view.findViewById(R.id.tvItemCount);
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        btnOrderServices = view.findViewById(R.id.btnOrderServices);

        recyclerViewServices.setLayoutManager(new LinearLayoutManager(getContext()));
        setupRecyclerView();

        loadServices();

        btnOrderServices.setOnClickListener(v -> handleOrderServices());

        return view;
    }

    private void setupRecyclerView() {
        serviceAdapter = new ServiceAdapter(
                getContext(),
                serviceList,
                new ServiceAdapter.OnServiceActionListener() {
                    @Override
                    public void onQuantityChanged(Service service, int newQuantity) {
                        updateTotals();
                    }
                }
        );
        recyclerViewServices.setAdapter(serviceAdapter);
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
                    serviceAdapter.notifyDataSetChanged();
                    if (serviceList.isEmpty()) {
                        showEmptyView(true);
                    } else {
                        showEmptyView(false);
                    }
                } else {
                    showEmptyView(true);
                    Toast.makeText(getContext(), "Không thể tải dữ liệu dịch vụ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Service>> call, Throwable t) {
                showLoading(false);
                showEmptyView(true);
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
            int selectedQuantity = entry.getValue();

            if (selectedQuantity > 0) {
                Service service = findServiceById(serviceId);
                if (service != null) {
                    ServiceDetail detail = new ServiceDetail();
                    detail.setCourtServiceId(serviceId);
                    detail.setCourtServiceName(service.getName());
                    detail.setQuantity(selectedQuantity);
                    detail.setPrice(service.getPrice());
                    orderedServiceDetails.add(detail);
                }
            }
        }

        if (orderedServiceDetails.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng chọn ít nhất một dịch vụ", Toast.LENGTH_SHORT).show();
            return;
        }

        String totalPriceText = tvTotalPrice.getText().toString().replace("đ", "").replace(",", "").trim();
        double paymentAmount = Double.parseDouble(totalPriceText);

        ApiService apiService = RetrofitClient.getApiService(getContext());
        Call<Courts> courtCall = apiService.getCourtById(courtId);
        courtCall.enqueue(new Callback<Courts>() {
            @Override
            public void onResponse(Call<Courts> call, Response<Courts> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Courts court = response.body();
                    String courtName = court.getName();
                    String address = court.getAddress();

                    Intent intent = new Intent(getActivity(), ConfirmActivity.class);
                    intent.putExtra("orderType", "Đơn dịch vụ");
                    intent.putExtra("courtId", courtId);
                    intent.putExtra("courtName", courtName);
                    intent.putExtra("address", address);
                    intent.putExtra("paymentAmount", paymentAmount);
                    String serviceDetailsJson = new Gson().toJson(orderedServiceDetails);
                    Log.d("ServiceActivity", "serviceDetailsJson: " + serviceDetailsJson);
                    intent.putExtra("serviceDetailsJson", serviceDetailsJson);
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "Không thể lấy thông tin sân", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Courts> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Service findServiceById(String id) {
        for (Service service : serviceList) {
            if (service.getId().equals(id)) {
                return service;
            }
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