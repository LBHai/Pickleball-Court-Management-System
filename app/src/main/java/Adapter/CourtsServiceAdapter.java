// Trong CourtsServiceAdapter.java
package Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.lang.ref.WeakReference;
import java.util.List;

import Api.ApiService;
import Api.RetrofitClient;
import Holder.CourtViewHolder;
import Model.Courts;
import Model.Service;
import SEP490.G9.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourtsServiceAdapter extends RecyclerView.Adapter<CourtViewHolder> {

    private Context context;
    private List<Courts> courtsList;
    private OnCourtClickListener listener;

    public interface OnCourtClickListener {
        void onCourtClick(Courts court);
    }

    public CourtsServiceAdapter(Context context, List<Courts> courtsList, OnCourtClickListener listener) {
        this.context = context;
        this.courtsList = courtsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CourtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_court_service, parent, false);
        return new CourtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourtViewHolder holder, int position) {
        Courts court = courtsList.get(position);

        // Load logo court (nếu có)
        if (court.getLogoUrl() != null && !court.getLogoUrl().isEmpty()) {
            Glide.with(context)
                    .load(court.getLogoUrl())
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .into(holder.getImgClubLogo());
        }

        // Thiết lập các thông tin văn bản
        holder.getTvClubName().setText(court.getName());
        // Trong phương thức onBindViewHolder của CourtsServiceAdapter
        holder.getTvAddress().setText("Địa chỉ: " + court.getAddress());
        holder.getTvOpenTime().setText(court.getOpenTime());
        holder.getTvPhone().setText(court.getPhone());

        // Lấy danh sách Service của court và hiển thị theo dạng ImageSlider
        loadServiceImages(court.getId(), holder);

        // Ví dụ: click cho nút map hoặc book (tùy từng chức năng)
        holder.getBtnMap().setOnClickListener(v -> {
            // Lấy thông tin địa chỉ hoặc tọa độ của sân
            String address = court.getAddress();
            String uri = "geo:0,0?q=" + Uri.encode(address);

            Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");

            // Kiểm tra Google Maps đã cài chưa
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                // Nếu chưa có Google Maps, có thể mở trên trình duyệt
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(address)));
                context.startActivity(browserIntent);
            }
        });
        holder.getBtnService().setOnClickListener(v -> {
            if (listener != null) {
                listener.onCourtClick(court);
            }
        });
    }

    /**
     * Gọi API lấy danh sách Service của court sau đó cập nhật adapter cho ViewPager2
     */
    private void loadServiceImages(String courtId, final CourtViewHolder holder) {
        // Sử dụng WeakReference để tránh rò rỉ khi ViewHolder bị recycle
        final WeakReference<CourtViewHolder> holderRef = new WeakReference<>(holder);

        ApiService apiService = RetrofitClient.getApiService(context);
        apiService.getServices(courtId).enqueue(new Callback<List<Service>>() {
            @Override
            public void onResponse(Call<List<Service>> call, Response<List<Service>> response) {
                CourtViewHolder currentHolder = holderRef.get();
                if (currentHolder == null || currentHolder.getViewPager() == null) {
                    Log.d("CourtsServiceAdapter", "ViewHolder or ViewPager is null, skipping update");
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    List<Service> services = response.body();
                    if (!services.isEmpty()) {
                        setupImageSlider(currentHolder, services);
                    } else {
                        // Nếu không có service, ẩn slider
                        currentHolder.getViewPager().setVisibility(View.GONE);
                        currentHolder.getLayoutIndicators().setVisibility(View.GONE);
                    }
                } else {
                    Log.e("API_ERROR", "Lỗi API hoặc response null: " + response.errorBody());
                }
            }
            @Override
            public void onFailure(Call<List<Service>> call, Throwable t) {
                Log.e("API_ERROR", "Error calling API: " + t.getMessage());
            }
        });
    }

    /**
     * Thiết lập ImageSlider cho ViewPager2 và tự động chuyển slide
     */
    private void setupImageSlider(final CourtViewHolder holder, final List<Service> services) {
        if (services == null || services.isEmpty()) {
            holder.getViewPager().setVisibility(View.GONE);
            holder.getLayoutIndicators().setVisibility(View.GONE);
            return;
        }
        // Tạo adapter cho ViewPager2 dùng cho image slider
        ImageSliderAdapter sliderAdapter = new ImageSliderAdapter(context, services);
        holder.getViewPager().setAdapter(sliderAdapter);
        // Nếu bạn có layoutIndicators, bạn có thể cập nhật indicator dots tại đây
        // Ví dụ: cập nhật số lượng dot = sliderAdapter.getItemCount()

        // Thiết lập auto-slide với Handler và Runnable
        final Handler sliderHandler = new Handler(Looper.getMainLooper());
        final Runnable sliderRunnable = new Runnable() {
            @Override
            public void run() {
                if (holder.getViewPager() != null && sliderAdapter.getItemCount() > 0) {
                    int nextPosition = (holder.getViewPager().getCurrentItem() + 1) % sliderAdapter.getItemCount();
                    holder.getViewPager().setCurrentItem(nextPosition, true);
                    sliderHandler.postDelayed(this, 5000); // chuyển slide sau 5 giây
                }
            }
        };

        // Bắt đầu auto-slide sau 3 giây
        sliderHandler.postDelayed(sliderRunnable, 3000);

        // Lưu lại Handler và Runnable trong holder để có thể dừng auto-slide khi ViewHolder bị recycle
        holder.setSliderHandler(sliderHandler, sliderRunnable);
    }

    @Override
    public int getItemCount() {
        return courtsList != null ? courtsList.size() : 0;
    }

    // Phương thức cập nhật danh sách khi cần thiết
    public void updateList(List<Courts> filteredList) {
        this.courtsList = filteredList;
        notifyDataSetChanged();
    }

    @Override
    public void onViewRecycled(@NonNull CourtViewHolder holder) {
        super.onViewRecycled(holder);
        // Dừng auto-slide khi ViewHolder bị recycle để tránh rò rỉ bộ nhớ
        holder.stopAutoSlide();
    }
}
