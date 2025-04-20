package SEP490.G9;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import Activity.ImageZoomInOutActivity;
import Api.ApiService;
import Api.RetrofitClient;
import Api.NetworkUtils;
import Model.CourtImage;
import retrofit2.Call;

public class GalleryFragment extends Fragment {

    private TextView tvCourtLayoutTitle, tvTitle;
    private RecyclerView rvCourtLayout, rvGallery; // Thêm RecyclerView cho sơ đồ sân
    private GalleryAdapter layoutAdapter, galleryAdapter; // Hai adapter cho hai RecyclerView
    private List<CourtImage> courtLayouts = new ArrayList<>(); // Danh sách sơ đồ sân
    private List<CourtImage> courtImages = new ArrayList<>(); // Danh sách hình ảnh

    // ID sân được truyền qua Bundle (sử dụng key "club_id")
    private String clubId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        // Nhận clubId từ Bundle
        if (getArguments() != null) {
            clubId = getArguments().getString("club_id", "");
        }

        if (clubId.isEmpty()) {
            Toast.makeText(getContext(), "Không có thông tin sân", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Tham chiếu các View
        tvCourtLayoutTitle = view.findViewById(R.id.tv_court_layout_title);
        rvCourtLayout = view.findViewById(R.id.rv_court_layout); // Ánh xạ RecyclerView sơ đồ sân
        tvTitle = view.findViewById(R.id.tv_title);
        rvGallery = view.findViewById(R.id.rv_gallery);

        tvCourtLayoutTitle.setText("Sơ đồ sân");
        tvTitle.setText("Hình ảnh");

        // Thiết lập GridLayoutManager cho cả hai RecyclerView
        rvCourtLayout.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvGallery.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Lấy dữ liệu từ API
        fetchCourtLayoutsFromApi(getContext()); // Lấy sơ đồ sân
        fetchCourtImagesFromApi(getContext()); // Lấy hình ảnh

        return view;
    }

    private void fetchCourtLayoutsFromApi(Context context) {
        ApiService apiService = RetrofitClient.getApiService(context);
        Call<List<CourtImage>> call = apiService.getCourtImages(clubId, true); // isLayout = true để lấy sơ đồ sân

        NetworkUtils.callApi(call, context, new NetworkUtils.ApiCallback<List<CourtImage>>() {
            @Override
            public void onSuccess(List<CourtImage> data) {
                if (data != null && !data.isEmpty()) {
                    courtLayouts = data;
                    layoutAdapter = new GalleryAdapter(context, courtLayouts);
                    rvCourtLayout.setAdapter(layoutAdapter);
                } else {
                    Toast.makeText(context, "Sân chưa có sơ đồ", Toast.LENGTH_SHORT).show();
                    tvCourtLayoutTitle.setVisibility(View.GONE);
                    rvCourtLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                tvCourtLayoutTitle.setVisibility(View.GONE);
                rvCourtLayout.setVisibility(View.GONE);
            }
        });
    }

    private void fetchCourtImagesFromApi(Context context) {
        ApiService apiService = RetrofitClient.getApiService(context);
        Call<List<CourtImage>> call = apiService.getCourtImages(clubId, false); // isLayout = false để lấy hình ảnh

        NetworkUtils.callApi(call, context, new NetworkUtils.ApiCallback<List<CourtImage>>() {
            @Override
            public void onSuccess(List<CourtImage> data) {
                if (data != null && !data.isEmpty()) {
                    courtImages = data;
                    galleryAdapter = new GalleryAdapter(context, courtImages);
                    rvGallery.setAdapter(galleryAdapter);
                } else {
                    Toast.makeText(context, "Sân chưa có hình ảnh", Toast.LENGTH_SHORT).show();
                    tvTitle.setVisibility(View.GONE);
                    rvGallery.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                tvTitle.setVisibility(View.GONE);
                rvGallery.setVisibility(View.GONE);
            }
        });
    }

    private static class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
        private final List<CourtImage> courtImages;
        private final Context context;

        GalleryAdapter(Context context, List<CourtImage> courtImages) {
            this.context = context;
            this.courtImages = courtImages;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_image, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CourtImage courtImage = courtImages.get(position);
            Glide.with(holder.itemView.getContext())
                    .load(courtImage.getImageUrl())
                    .into(holder.imageView);

            holder.imageView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ImageZoomInOutActivity.class);
                intent.putExtra(ImageZoomInOutActivity.EXTRA_URL, courtImage.getImageUrl());
                context.startActivity(intent);
            });
        }

            @Override
        public int getItemCount() {
            return courtImages.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            ViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.iv_image);
            }
        }
    }
}