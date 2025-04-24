package UI.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import Data.Model.Service;
import SEP490.G9.R;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder> {

    private Context context;
    private List<Service> services;

    public ImageSliderAdapter(Context context, List<Service> services) {
        this.context = context;
        this.services = services;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout cho từng slide; đảm bảo file XML item_image_slider.xml có ImageView với id imageSlide
        View view = LayoutInflater.from(context).inflate(R.layout.item_image_slider, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Service service = services.get(position);
        if (service.getImageUrl() != null && !service.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(service.getImageUrl())
                    .placeholder(R.drawable.nuoc_uong_the_thao) // Hình mặc định khi load
                    .error(R.drawable.nuoc_uong_the_thao)       // Hình hiển thị khi có lỗi
                    .into(holder.getImageView());
        } else {
            // Nếu không có imageUrl, hiển thị hình mặc định
            holder.getImageView().setImageResource(R.drawable.nuoc_uong_the_thao);
        }
    }

    @Override
    public int getItemCount() {
        return services != null ? services.size() : 0;
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageSlide);
        }
        public ImageView getImageView() {
            return imageView;
        }
    }
}
