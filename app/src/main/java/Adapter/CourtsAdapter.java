package Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import Model.Courts;
import SEP490.G9.BookingTableActivity;
import SEP490.G9.R;

public class CourtsAdapter extends RecyclerView.Adapter<CourtsAdapter.ClubsViewHolder> {

    private Context context;
    private List<Courts> courtsList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Courts club);
    }

    public CourtsAdapter(Context context, List<Courts> courtsList, OnItemClickListener listener) {
        this.context = context;
        this.courtsList = courtsList;
        this.listener = listener;
    }

    public void updateList(List<Courts> newList) {
        courtsList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ClubsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_court, parent, false);
        return new ClubsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClubsViewHolder holder, int position) {
        Courts club = courtsList.get(position);

        // Binding dữ liệu
        holder.tvClubName.setText(club.getName());
        holder.tvAddress.setText("Địa chỉ: " + club.getAddress());
        holder.tvOpenTime.setText("Giờ mở cửa: " + club.getOpenTime());
        holder.tvPhone.setText("Liên hệ: " + club.getPhone());

        // Xử lý khi bấm vào item → mở màn hình chi tiết CLB
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(club);
            }
        });

        // Xử lý nút "Đặt Sân"
        holder.btnBook.setOnClickListener(v -> {
            Intent intent = new Intent(context, BookingTableActivity.class);
            intent.putExtra("club_id", club.getId());
            context.startActivity(intent);
        });

        // Xử lý nút "Xem Bản Đồ"
        holder.btnMap.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(club.getAddress()));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(mapIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return courtsList.size();
    }

    public static class ClubsViewHolder extends RecyclerView.ViewHolder {
        TextView tvClubName, tvAddress, tvOpenTime, tvPhone;
        Button btnBook, btnMap;

        public ClubsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClubName = itemView.findViewById(R.id.tvClubName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvOpenTime = itemView.findViewById(R.id.tvOpenTime);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            btnBook = itemView.findViewById(R.id.btnBook);
            btnMap = itemView.findViewById(R.id.btnMap);
        }
    }
}
