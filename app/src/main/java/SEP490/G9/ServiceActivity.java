package SEP490.G9;

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
import SEP490.G9.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import Model.Service;
import Api.ApiService;
import Api.RetrofitClient;
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
    private String courtId;

    public ServiceActivity() {
        // Required empty public constructor
    }

    // Thêm courtId vào newInstance
    public static ServiceActivity newInstance(String courtId) {
        ServiceActivity fragment = new ServiceActivity();
        Bundle args = new Bundle();
        args.putString("courtId", courtId);

        fragment.setArguments(args);
        return fragment;
    }

    // Để tương thích với code cũ không có tham số
    public static ServiceActivity newInstance() {
        return newInstance("1"); // Giá trị mặc định nếu không có courtId
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate layout activity_service.xml
        View view = inflater.inflate(R.layout.activity_service, container, false);

        // Lấy courtId từ arguments
        if (getArguments() != null) {
            courtId = getArguments().getString("courtId", "1");
            Log.d("courtId",courtId);
        } else {
            courtId = "1"; // Giá trị mặc định
        }

        // Khởi tạo views
        recyclerViewServices = view.findViewById(R.id.recyclerViewServices);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyServices = view.findViewById(R.id.tvEmptyServices);
        tvTotalItems = view.findViewById(R.id.tvItemCount);  // Đảm bảo ID này tồn tại trong layout
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice); // Đảm bảo ID này tồn tại trong layout

        // Cài đặt RecyclerView
        recyclerViewServices.setLayoutManager(new LinearLayoutManager(getContext()));
        setupRecyclerView();

        // Load dữ liệu
        loadServices();

        return view;
    }

    private void setupRecyclerView() {
        // Khởi tạo ServiceAdapter với đủ 3 tham số
        serviceAdapter = new ServiceAdapter(
                getContext(),  // Context
                serviceList,   // Danh sách dịch vụ
                new ServiceAdapter.OnServiceActionListener() {  // Listener để cập nhật khi người dùng thay đổi số lượng
                    @Override
                    public void onQuantityChanged(Service service, int newQuantity) {
                        // Cập nhật tổng số lượng và tổng tiền
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

                    // Kiểm tra nếu danh sách trống
                    if (serviceList.isEmpty()) {
                        showEmptyView(true);
                    } else {
                        showEmptyView(false);
                    }
                } else {
                    // Xử lý khi không có dữ liệu hoặc lỗi API
                    showEmptyView(true);
                    Toast.makeText(getContext(), "Không thể tải dữ liệu dịch vụ", Toast.LENGTH_SHORT).show();
                }
                Log.d("ServiceActivity", "Số lượng dịch vụ nhận về: " + response.body().size());

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
        // Lấy tổng số lượng đã đặt từ adapter
        int totalItems = serviceAdapter.getTotalOrderQuantity();

        // Lấy tổng giá tiền từ adapter
        double totalPrice = serviceAdapter.getTotalPrice();

        // Cập nhật UI
        if (tvTotalItems != null) {
            tvTotalItems.setText(totalItems + " mục");
        }

        if (tvTotalPrice != null) {
            tvTotalPrice.setText(String.format(Locale.getDefault(), "%,.0fđ", totalPrice));
        }
    }



    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }

        if (recyclerViewServices != null) {
            recyclerViewServices.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        }
    }

    private void showEmptyView(boolean isEmpty) {
        if (tvEmptyServices != null) {
            tvEmptyServices.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }

        if (recyclerViewServices != null) {
            recyclerViewServices.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }
}