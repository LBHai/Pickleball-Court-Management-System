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

import Api.ApiService;
import Api.RetrofitClient;
import Api.NetworkUtils;
import Model.CourtImage;
import retrofit2.Call;

public class GalleryFragment extends Fragment {

    private TextView tvTitle;
    private RecyclerView recyclerView;
    private GalleryAdapter adapter;
    private List<CourtImage> courtImages = new ArrayList<>();

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
        tvTitle = view.findViewById(R.id.tv_title);
        recyclerView = view.findViewById(R.id.rv_gallery);

        tvTitle.setText("Hình ảnh");

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        fetchCourtImagesFromApi(getContext());

        return view;
    }

    private void fetchCourtImagesFromApi(Context context) {
        ApiService apiService = RetrofitClient.getApiService(context);
        Call<List<CourtImage>> call = apiService.getCourtImages(clubId, false);

        NetworkUtils.callApi(call, context, new NetworkUtils.ApiCallback<List<CourtImage>>() {
            @Override
            public void onSuccess(List<CourtImage> data) {
                if (data != null && !data.isEmpty()) {
                    courtImages = data;
                    adapter = new GalleryAdapter(context, courtImages); // Truyền context
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(context, "Sân chưa có hình ảnh", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
        private final List<CourtImage> courtImages;
        private final Context context;

        // Thêm tham số Context vào constructor
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

            // Thêm sự kiện nhấp chuột
            holder.imageView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ImageZoomActivity.class);
                intent.putExtra("image_url", courtImage.getImageUrl());
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
