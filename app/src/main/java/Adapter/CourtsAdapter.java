package Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import Model.Courts;
import SEP490.G9.BookingTableActivity;
import SEP490.G9.R;

public class CourtsAdapter extends RecyclerView.Adapter<CourtsAdapter.CourtViewHolder> {

    private Context context;
    private List<Courts> courtsList;
    private OnCourtClickListener listener;

    public interface OnCourtClickListener {
        void onCourtClick(Courts court);
    }

    public CourtsAdapter(Context context, List<Courts> courtsList, OnCourtClickListener listener) {
        this.context = context;
        this.courtsList = courtsList != null ? courtsList : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public CourtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_court, parent, false);
        return new CourtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourtViewHolder holder, int position) {
        Courts court = courtsList.get(position);

        // Hiển thị thông tin CLB
        holder.tvClubName.setText(court.getName());
        holder.tvAddress.setText(court.getAddress());
        holder.tvOpenTime.setText(court.getOpenTime());
        holder.tvPhone.setText(court.getPhone());

        // Tải ảnh logo từ URL bằng Glide
        String logoUrl = court.getLogoUrl(); // Giả định Courts có thuộc tính logoUrl
        if (logoUrl != null && !logoUrl.isEmpty()) {
            Glide.with(context)
                    .load(logoUrl)
                    .placeholder(R.drawable.logo) // Ảnh placeholder khi đang tải
                    .error(R.drawable.logo) // Ảnh hiển thị nếu lỗi
                    .into(holder.imgClubLogo);
        } else {
            holder.imgClubLogo.setImageResource(R.drawable.logo); // Ảnh mặc định nếu không có URL
        }

        // Xử lý sự kiện nhấn vào item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCourtClick(court);
            }
        });

        // Xử lý nút Book
        holder.btnBook.setOnClickListener(v -> {
            // Giả định Courts có phương thức getId() trả về club_id
            String clubId = court.getId(); // Thay đổi tùy theo tên phương thức thực tế trong model Courts
            Intent intent = new Intent(context, BookingTableActivity.class);
            intent.putExtra("club_id", clubId);
            context.startActivity(intent); // Sử dụng context để khởi động Activity
        });
    }

    @Override
    public int getItemCount() {
        return courtsList.size();
    }

    public void updateList(List<Courts> newList) {
        this.courtsList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class CourtViewHolder extends RecyclerView.ViewHolder {
        ImageView imgClubLogo, imgHeart, btnMap, imgDongho, imgPhone;
        TextView tvClubName, tvAddress, tvOpenTime, tvPhone;
        Button btnBook;

        CourtViewHolder(@NonNull View itemView) {
            super(itemView);
            imgClubLogo = itemView.findViewById(R.id.imgClubLogo);
            imgHeart = itemView.findViewById(R.id.imgHeart);
            btnMap = itemView.findViewById(R.id.btnMap);
            imgDongho = itemView.findViewById(R.id.imgDongho);
            imgPhone = itemView.findViewById(R.id.imgPhone);
            tvClubName = itemView.findViewById(R.id.tvClubName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvOpenTime = itemView.findViewById(R.id.tvOpenTime);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            btnBook = itemView.findViewById(R.id.btnBook);
        }
    }
}