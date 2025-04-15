package Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import Holder.ServiceViewHolder;
import Model.Service;
import SEP490.G9.R;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceViewHolder> {

    private final Context context;
    private List<Service> serviceList;
    private final OnServiceActionListener listener;
    private final Map<String, Integer> orderQuantities;

    public interface OnServiceActionListener {
        void onQuantityChanged(Service service, int newQuantity);
    }

    public ServiceAdapter(Context context, List<Service> serviceList, OnServiceActionListener listener) {
        this.context = context;
        this.serviceList = serviceList;
        this.listener = listener;
        this.orderQuantities = new HashMap<>();

        // Khởi tạo số lượng ban đầu là 0 cho tất cả dịch vụ
        initializeOrderQuantities(serviceList);
    }

    private void initializeOrderQuantities(List<Service> services) {
        if (services != null) {
            for (Service service : services) {
                if (service.getId() != null) {
                    orderQuantities.put(service.getId(), 0);
                }
            }
        }
    }

    public void updateList(List<Service> newList) {
        this.serviceList = newList;
        orderQuantities.clear(); // Xóa số lượng đặt hàng cũ

        // Khởi tạo lại số lượng ban đầu là 0 cho tất cả dịch vụ mới
        initializeOrderQuantities(newList);

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Service service = serviceList.get(position);
        String serviceId = service.getId();

        if (serviceId == null) {
            serviceId = "service_" + position; // Dự phòng nếu serviceId null
        }

        // Lưu serviceId vào tag của ViewHolder để sử dụng trong sự kiện click
        holder.itemView.setTag(serviceId);

        // Hiển thị thông tin dịch vụ
        holder.tvName.setText(service.getName());
        holder.tvPrice.setText(String.format(Locale.getDefault(), "%,.0fđ", service.getPrice()));

        // Hiển thị mô tả nếu có
        if (service.getDescription() != null && !service.getDescription().isEmpty()) {
            holder.tvDescription.setVisibility(View.VISIBLE);
            holder.tvDescription.setText(service.getDescription());
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        // Hiển thị số lượng tồn kho từ quantity
        holder.tvStockQuantity.setText("Còn: " + service.getQuantity());

        // Cập nhật hiển thị số lượng đặt hàng
        updateQuantityDisplay(holder, serviceId, service);

        // Tải hình ảnh sử dụng Glide
        if (service.getImageUrl() != null && !service.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(service.getImageUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.sting)
                            .error(R.drawable.sting))
                    .into(holder.ivDrink);
        } else {
            holder.ivDrink.setImageResource(R.drawable.sting);
        }

        // Thiết lập sự kiện cho nút giảm
        final String finalServiceId = serviceId;

        // Xóa listener cũ để tránh trùng lặp khi RecyclerView tái sử dụng ViewHolder
        holder.btnDecrease.setOnClickListener(null);
        holder.btnIncrease.setOnClickListener(null);

        holder.btnDecrease.setOnClickListener(v -> {
            decreaseQuantity(holder, finalServiceId, service);
        });

        // Thiết lập sự kiện cho nút tăng
        holder.btnIncrease.setOnClickListener(v -> {
            increaseQuantity(holder, finalServiceId, service);
        });
    }

    private void updateQuantityDisplay(ServiceViewHolder holder, String serviceId, Service service) {
        // Lấy số lượng hiện tại từ Map
        int currentQuantity = orderQuantities.getOrDefault(serviceId, 0);

        // Cập nhật hiển thị
        holder.tvQuantity.setText(String.valueOf(currentQuantity));

        // Cập nhật trạng thái nút giảm
        holder.btnDecrease.setEnabled(currentQuantity > 0);
        holder.btnDecrease.setAlpha(currentQuantity > 0 ? 1.0f : 0.5f);

        // Cập nhật trạng thái nút tăng dựa trên số lượng tồn kho
        boolean canIncrease = service.getQuantity() > currentQuantity;
        holder.btnIncrease.setEnabled(canIncrease);
        holder.btnIncrease.setAlpha(canIncrease ? 1.0f : 0.5f);
    }

    private void decreaseQuantity(ServiceViewHolder holder, String serviceId, Service service) {
        // Lấy số lượng hiện tại từ Map
        int currentQuantity = orderQuantities.getOrDefault(serviceId, 0);

        if (currentQuantity > 0) {
            int newQuantity = currentQuantity - 1;
            orderQuantities.put(serviceId, newQuantity);

            // Cập nhật giao diện
            updateQuantityDisplay(holder, serviceId, service);

            // Thông báo cho listener
            if (listener != null) {
                listener.onQuantityChanged(service, newQuantity);
            }
        }
    }

    private void increaseQuantity(ServiceViewHolder holder, String serviceId, Service service) {
        // Lấy số lượng hiện tại từ Map
        int currentQuantity = orderQuantities.getOrDefault(serviceId, 0);

        // Kiểm tra số lượng tồn kho
        if (currentQuantity < service.getQuantity()) {
            int newQuantity = currentQuantity + 1;
            orderQuantities.put(serviceId, newQuantity);

            // Cập nhật giao diện
            updateQuantityDisplay(holder, serviceId, service);

            // Thông báo cho listener
            if (listener != null) {
                listener.onQuantityChanged(service, newQuantity);
            }
        }
    }

    @Override
    public int getItemCount() {
        return serviceList != null ? serviceList.size() : 0;
    }

    // Phương thức để lấy tổng số lượng đã đặt
    public int getTotalOrderQuantity() {
        int total = 0;
        for (Integer quantity : orderQuantities.values()) {
            total += quantity;
        }
        return total;
    }

    // Phương thức để lấy Map số lượng đặt hàng
    public Map<String, Integer> getOrderQuantities() {
        return new HashMap<>(orderQuantities); // Trả về bản sao để tránh thay đổi trực tiếp
    }

    // Phương thức để lấy tổng giá tiền
    public double getTotalPrice() {
        double total = 0;
        for (int i = 0; i < serviceList.size(); i++) {
            Service service = serviceList.get(i);
            String serviceId = service.getId();
            if (serviceId != null && orderQuantities.containsKey(serviceId)) {
                int quantity = orderQuantities.get(serviceId);
                total += service.getPrice() * quantity;
            }
        }
        return total;
    }
}
