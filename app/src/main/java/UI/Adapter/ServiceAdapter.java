package UI.Adapter;

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

import Data.Holder.ServiceViewHolder;
import Data.Model.Service;
import SEP490.G9.R;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceViewHolder> {

    private final Context context;
    private List<Service> serviceList;
    private final OnServiceActionListener listener;
    private final Map<String, Integer> orderQuantities; // Lưu số lượng dịch vụ người dùng chọn

    public interface OnServiceActionListener {
        void onQuantityChanged(Service service, int newQuantity);
    }

    public ServiceAdapter(Context context, List<Service> serviceList, OnServiceActionListener listener) {
        this.context = context;
        this.serviceList = serviceList;
        this.listener = listener;
        this.orderQuantities = new HashMap<>();
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
        orderQuantities.clear();
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
            serviceId = "service_" + position;
        }

        holder.itemView.setTag(serviceId);
        holder.tvName.setText(service.getName());
        holder.tvPrice.setText(String.format(Locale.getDefault(), "%,.0fđ", service.getPrice()));

        if (service.getDescription() != null && !service.getDescription().isEmpty()) {
            holder.tvDescription.setVisibility(View.VISIBLE);
            holder.tvDescription.setText(service.getDescription());
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        holder.tvStockQuantity.setText("Còn: " + service.getQuantity());
        updateQuantityDisplay(holder, serviceId, service);

        if (service.getImageUrl() != null && !service.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(service.getImageUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.revive)
                            .error(R.drawable.revive))
                    .into(holder.ivDrink);
        } else {
            holder.ivDrink.setImageResource(R.drawable.revive);
        }

        final String finalServiceId = serviceId;
        holder.btnDecrease.setOnClickListener(null);
        holder.btnIncrease.setOnClickListener(null);

        holder.btnDecrease.setOnClickListener(v -> decreaseQuantity(holder, finalServiceId, service));
        holder.btnIncrease.setOnClickListener(v -> increaseQuantity(holder, finalServiceId, service));
    }

    private void updateQuantityDisplay(ServiceViewHolder holder, String serviceId, Service service) {
        int currentQuantity = orderQuantities.getOrDefault(serviceId, 0);
        holder.tvQuantity.setText(String.valueOf(currentQuantity));
        holder.btnDecrease.setEnabled(currentQuantity > 0);
        holder.btnDecrease.setAlpha(currentQuantity > 0 ? 1.0f : 0.5f);
        boolean canIncrease = service.getQuantity() > currentQuantity;
        holder.btnIncrease.setEnabled(canIncrease);
        holder.btnIncrease.setAlpha(canIncrease ? 1.0f : 0.5f);
    }

    private void decreaseQuantity(ServiceViewHolder holder, String serviceId, Service service) {
        int currentQuantity = orderQuantities.getOrDefault(serviceId, 0);
        if (currentQuantity > 0) {
            int newQuantity = currentQuantity - 1;
            orderQuantities.put(serviceId, newQuantity);
            updateQuantityDisplay(holder, serviceId, service);
            if (listener != null) {
                listener.onQuantityChanged(service, newQuantity);
            }
        }
    }

    private void increaseQuantity(ServiceViewHolder holder, String serviceId, Service service) {
        int currentQuantity = orderQuantities.getOrDefault(serviceId, 0);
        if (currentQuantity < service.getQuantity()) {
            int newQuantity = currentQuantity + 1;
            orderQuantities.put(serviceId, newQuantity);
            updateQuantityDisplay(holder, serviceId, service);
            if (listener != null) {
                listener.onQuantityChanged(service, newQuantity);
            }
        }
    }

    @Override
    public int getItemCount() {
        return serviceList != null ? serviceList.size() : 0;
    }

    public int getTotalOrderQuantity() {
        int total = 0;
        for (Integer quantity : orderQuantities.values()) {
            total += quantity;
        }
        return total;
    }

    public Map<String, Integer> getOrderQuantities() {
        return new HashMap<>(orderQuantities);
    }

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