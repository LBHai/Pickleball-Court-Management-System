package Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import Model.Courts;
import SEP490.G9.BookingRegularTableActivity;
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
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .into(holder.imgClubLogo);
        } else {
            holder.imgClubLogo.setImageResource(R.drawable.logo);
        }

        // Xử lý sự kiện nhấn vào item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCourtClick(court);
            }
        });

        // Xử lý nút Book hiển thị Dialog
        holder.btnBook.setOnClickListener(v -> {
            String clubId = court.getId(); // Giả định Courts có phương thức getId()
            showBookingDialog(context, clubId);
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

    private void showBookingDialog(Context context, String clubId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_booking, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();

        // Lấy các thành phần trong dialog
        ImageView imgClose = dialogView.findViewById(R.id.imgClose);
        CardView cardHangNgay = dialogView.findViewById(R.id.cardHangNgay);
        CardView cardCoDinh = dialogView.findViewById(R.id.cardCoDinh);
        ImageButton btnBook = dialogView.findViewById(R.id.btnBook);
        ImageButton btnBookRegular = dialogView.findViewById(R.id.btnBookRegular);

        imgClose.setOnClickListener(v -> dialog.dismiss());

        // Listener cho BookingTableActivity
        View.OnClickListener bookingTableClick = v -> {
            dialog.dismiss();
            Intent intent = new Intent(context, BookingTableActivity.class);
            intent.putExtra("club_id", clubId);
            intent.putExtra("booking_type", "truc_quan");
            context.startActivity(intent);
        };

        // Listener cho BookingRegularTableActivity
        View.OnClickListener bookingRegularClick = v -> {
            dialog.dismiss();
            Intent intent = new Intent(context, BookingRegularTableActivity.class);
            intent.putExtra("club_id", clubId);
            intent.putExtra("booking_type", "xe_ve");
            context.startActivity(intent);
        };

        // Áp dụng listener cho card và nút tương ứng
        cardHangNgay.setOnClickListener(bookingTableClick);
        btnBook.setOnClickListener(bookingTableClick);

        cardCoDinh.setOnClickListener(bookingRegularClick);
        btnBookRegular.setOnClickListener(bookingRegularClick);
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
