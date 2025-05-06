package UI.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import Data.Holder.ServiceViewHolder;
import Data.Model.Service;
import SEP490.G9.R;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceViewHolder>
        implements Filterable {

    private final Context context;
    private List<Service> originalList;    // danh sách gốc để filter
    private List<Service> filteredList;    // danh sách đang hiển thị
    private final OnServiceActionListener listener;
    private final Map<String, Integer> orderQuantities;

    public interface OnServiceActionListener {
        void onQuantityChanged(Service service, int newQuantity);
    }

    public ServiceAdapter(Context context, List<Service> serviceList, OnServiceActionListener listener) {
        this.context = context;
        this.originalList = new ArrayList<>(serviceList);
        this.filteredList = new ArrayList<>(serviceList);
        this.listener = listener;
        this.orderQuantities = new HashMap<>();
        initializeOrderQuantities(serviceList);
    }

    private void initializeOrderQuantities(List<Service> services) {
        orderQuantities.clear();
        if (services != null) {
            for (Service service : services) {
                if (service.getId() != null) {
                    orderQuantities.put(service.getId(), 0);
                }
            }
        }
    }

    /** Cập nhật toàn bộ danh sách (khi load lại từ server) */
    public void updateList(List<Service> newList) {
        this.originalList = new ArrayList<>(newList);
        this.filteredList = new ArrayList<>(newList);
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
        Service service = filteredList.get(position);
        String serviceId = service.getId() != null ? service.getId() : "service_" + position;

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

        holder.btnDecrease.setOnClickListener(v -> decreaseQuantity(holder, serviceId, service));
        holder.btnIncrease.setOnClickListener(v -> increaseQuantity(holder, serviceId, service));
    }

    @Override
    public int getItemCount() {
        return filteredList != null ? filteredList.size() : 0;
    }

    /** Filterable implementation **/
    @Override
    public Filter getFilter() {
        return serviceFilter;
    }

    private final Filter serviceFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Service> filtered = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filtered.addAll(originalList);
            } else {
                String pattern = constraint.toString().toLowerCase().trim();
                for (Service svc : originalList) {
                    if (svc.getName().toLowerCase().contains(pattern)) {
                        filtered.add(svc);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filtered;
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredList.clear();
            filteredList.addAll((List<Service>) results.values);
            notifyDataSetChanged();
            // cập nhật lại tổng khi filter
            listener.onQuantityChanged(null, 0);
        }
    };

    /** Các phương thức tính tổng số lượng, tổng giá **/
    public int getTotalOrderQuantity() {
        int total = 0;
        for (Integer qty : orderQuantities.values()) {
            total += qty;
        }
        return total;
    }

    public double getTotalPrice() {
        double sum = 0;
        for (Service svc : originalList) {
            String id = svc.getId();
            if (id != null && orderQuantities.containsKey(id)) {
                sum += orderQuantities.get(id) * svc.getPrice();
            }
        }
        return sum;
    }

    public Map<String, Integer> getOrderQuantities() {
        return new HashMap<>(orderQuantities);
    }

    /** Helper cập nhật UI nút + số lượng **/
    private void updateQuantityDisplay(ServiceViewHolder holder, String serviceId, Service service) {
        int current = orderQuantities.getOrDefault(serviceId, 0);
        holder.tvQuantity.setText(String.valueOf(current));
        holder.btnDecrease.setEnabled(current > 0);
        holder.btnDecrease.setAlpha(current > 0 ? 1f : 0.5f);
        boolean canInc = current < service.getQuantity();
        holder.btnIncrease.setEnabled(canInc);
        holder.btnIncrease.setAlpha(canInc ? 1f : 0.5f);
    }

    private void decreaseQuantity(ServiceViewHolder holder, String serviceId, Service service) {
        int curr = orderQuantities.getOrDefault(serviceId, 0);
        if (curr > 0) {
            orderQuantities.put(serviceId, curr - 1);
            updateQuantityDisplay(holder, serviceId, service);
            listener.onQuantityChanged(service, curr - 1);
        }
    }

    private void increaseQuantity(ServiceViewHolder holder, String serviceId, Service service) {
        int curr = orderQuantities.getOrDefault(serviceId, 0);
        if (curr < service.getQuantity()) {
            orderQuantities.put(serviceId, curr + 1);
            updateQuantityDisplay(holder, serviceId, service);
            listener.onQuantityChanged(service, curr + 1);
        }
    }
}
