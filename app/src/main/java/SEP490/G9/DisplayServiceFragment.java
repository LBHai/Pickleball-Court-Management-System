package SEP490.G9;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Api.ApiService;
import Api.RetrofitClient;
import Model.Service;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DisplayServiceFragment extends Fragment {
    private static final String ARG_COURT_ID = "courtId";
    private TableLayout tableServices;

    // Factory method để tạo instance với courtId
    public static DisplayServiceFragment newInstance(String courtId) {
        DisplayServiceFragment fragment = new DisplayServiceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COURT_ID, courtId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // 1) Inflate đúng XML bạn cung cấp
        View view = inflater.inflate(
                R.layout.fragment_display_service, container, false);

        tableServices = view.findViewById(R.id.tableServices);

        // 2) Lấy courtId từ arguments
        String courtId = null;
        if (getArguments() != null) {
            courtId = getArguments().getString(ARG_COURT_ID);
        }

        // 3) Validate và gọi API
        if (courtId == null || courtId.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Không có courtId, không thể tải dữ liệu dịch vụ",
                    Toast.LENGTH_SHORT).show();
        } else {
            fetchServices(courtId);
        }

        return view;
    }

    private void fetchServices(String courtId) {
        ApiService api = RetrofitClient.getApiService(requireContext());
        api.getServices(courtId)
                .enqueue(new Callback<List<Service>>() {
                    @Override
                    public void onResponse(Call<List<Service>> call, Response<List<Service>> res) {
                        if (res.isSuccessful() && res.body() != null) {
                            // Xóa các hàng cũ trừ header
                            while (tableServices.getChildCount() > 1) {
                                tableServices.removeViewAt(1);
                            }

                            // Nhóm các dịch vụ theo category
                            Map<String, List<Service>> byCat = groupServicesByCategory(res.body());

                            // Kiểm tra nếu byCat rỗng
                            if (byCat.isEmpty()) {
                                Toast.makeText(requireContext(),
                                        "Không có dịch vụ nào cho sân này",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // Lấy category đầu tiên nếu byCat không rỗng
                            String firstCat = byCat.keySet().iterator().next();
                            TextView tvCatHeader = getView().findViewById(R.id.tvCategoryHeader);
                            tvCatHeader.setText(firstCat.toUpperCase());

                            // Thêm từng service của category đầu tiên
                            List<Service> list = byCat.get(firstCat);
                            for (Service s : list) {
                                tableServices.addView(createServiceRow(s));
                            }
                        } else {
                            Toast.makeText(requireContext(),
                                    "Không thể tải dữ liệu dịch vụ",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Service>> call, Throwable t) {
                        Toast.makeText(requireContext(),
                                "Lỗi kết nối: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Map<String,List<Service>> groupServicesByCategory(List<Service> services) {
        Map<String,List<Service>> map = new HashMap<>();
        for (Service s: services) {
            String cat = s.getCategory();
            if (cat==null || cat.isEmpty()) cat = "Khác";
            if (!map.containsKey(cat)) map.put(cat,new ArrayList<>());
            map.get(cat).add(s);
        }
        return map;
    }

    private void displayServicesByCategory(Map<String,List<Service>> byCat) {
        for (Map.Entry<String,List<Service>> e : byCat.entrySet()) {
            // 1) category header row
            tableServices.addView(
                    createCategoryRow(e.getKey())
            );
            // 2) từng service row
            for (Service s: e.getValue()) {
                tableServices.addView(
                        createServiceRow(s)
                );
            }
        }
    }

    private TableRow createCategoryRow(String category) {
        TableRow row = new TableRow(requireContext());
        row.setBackgroundColor(
                getResources().getColor(R.color.dark_green));
        row.setPadding(8,12,8,12);

        TextView tv = new TextView(requireContext());
        TableRow.LayoutParams p = new TableRow.LayoutParams();
        p.span = 2;
        p.width = TableRow.LayoutParams.MATCH_PARENT;
        tv.setLayoutParams(p);

        tv.setText(category.toUpperCase());
        tv.setTextColor(Color.WHITE);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(16);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(16,8,16,8);

        row.addView(tv);
        return row;
    }

    private TableRow createServiceRow(Service s) {
        TableRow row = new TableRow(requireContext());
        row.setBackgroundResource(R.drawable.cell_border);
        row.setPadding(8,8,8,8);

        TextView name = new TextView(requireContext());
        name.setLayoutParams(
                new TableRow.LayoutParams(0,
                        TableRow.LayoutParams.WRAP_CONTENT, 2f));
        name.setText(s.getName());
        name.setTextColor(
                getResources().getColor(R.color.dark_green));
        name.setPadding(16,8,8,8);

        TextView price = new TextView(requireContext());
        price.setLayoutParams(
                new TableRow.LayoutParams(0,
                        TableRow.LayoutParams.WRAP_CONTENT, 1f));
        price.setText(formatPrice(s.getPrice())
                +"/"+ (s.getUnit()!=null
                ? s.getUnit() : "sản phẩm"));
        price.setGravity(Gravity.RIGHT);
        price.setPadding(8,8,16,8);

        row.addView(name);
        row.addView(price);
        return row;
    }

    private String formatPrice(double price) {
        return String.format("%,.0fđ", price);
    }
}
