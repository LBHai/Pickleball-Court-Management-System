package Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import Holder.CourtViewHolder;
import Model.Courts;
import SEP490.G9.BookingRegularTableActivity;
import SEP490.G9.BookingTableActivity;
import SEP490.G9.R;

public class CourtsAdapter extends RecyclerView.Adapter<CourtViewHolder> {

    private Context context;
    private List<Courts> courtsList;
    private OnCourtClickListener listener;

    // Interface callback truyền toàn bộ đối tượng Courts (bao gồm phone)
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

        // Hiển thị thông tin câu lạc bộ
        holder.getTvClubName().setText(court.getName());
        holder.getTvAddress().setText("Địa chỉ: " + court.getAddress());
        holder.getTvOpenTime().setText(court.getOpenTime());
        holder.getTvPhone().setText(court.getPhone());

        // Tải ảnh logo từ URL bằng Glide
        String logoUrl = court.getLogoUrl();
        if (logoUrl != null && !logoUrl.isEmpty()) {
            Glide.with(context)
                    .load(logoUrl)
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .into(holder.getImgClubLogo());
        } else {
            holder.getImgClubLogo().setImageResource(R.drawable.logo);
        }

        // Khi click vào item: gọi callback và truyền đối tượng court (bao gồm số điện thoại)
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                Log.d("CourtAdapter", "Item clicked, phone: " + court.getPhone());
                listener.onCourtClick(court);
            }
        });

        // Xử lý nút Book trong item: hiển thị dialog với club id và số điện thoại
        Button btnBook = holder.itemView.findViewById(R.id.btnBook);
        if (btnBook != null) {
            btnBook.setOnClickListener(v -> {
                String clubId = court.getId();
                // Nếu phone null thì gán chuỗi rỗng
                String phone = court.getPhone() != null ? court.getPhone() : "";
                Log.d("CourtAdapter", "Book button clicked, phone: " + phone);
                showBookingDialog(context, clubId, phone);
            });
        }
    }

    @Override
    public int getItemCount() {
        return courtsList.size();
    }

    public void updateList(List<Courts> newList) {
        this.courtsList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    // Hàm hiển thị dialog đặt sân kèm số điện thoại
    private void showBookingDialog(Context context, String clubId, String phone) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_booking, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();

        // Ánh xạ các thành phần trong dialog
        ImageView imgClose = dialogView.findViewById(R.id.imgClose);
        CardView cardHangNgay = dialogView.findViewById(R.id.cardHangNgay);
        CardView cardCoDinh = dialogView.findViewById(R.id.cardCoDinh);
        ImageButton btnBook = dialogView.findViewById(R.id.btnBook);
        ImageButton btnBookRegular = dialogView.findViewById(R.id.btnBookRegular);

        imgClose.setOnClickListener(v -> dialog.dismiss());

        // Listener cho BookingTableActivity (đặt sân trực quan)
        View.OnClickListener bookingTableClick = v -> {
            dialog.dismiss();
            Intent intent = new Intent(context, BookingTableActivity.class);
            intent.putExtra("club_id", clubId);
            intent.putExtra("booking_type", "truc_quan");
            intent.putExtra("tvPhone", phone);
            context.startActivity(intent);
        };

        // Listener cho BookingRegularTableActivity (đặt sân xe về)
        View.OnClickListener bookingRegularClick = v -> {
            dialog.dismiss();
            Intent intent = new Intent(context, BookingRegularTableActivity.class);
            intent.putExtra("club_id", clubId);
            intent.putExtra("booking_type", "xe_ve");
            intent.putExtra("tvPhone", phone);
            context.startActivity(intent);
        };

        cardHangNgay.setOnClickListener(bookingTableClick);
        btnBook.setOnClickListener(bookingTableClick);
        cardCoDinh.setOnClickListener(bookingRegularClick);
        btnBookRegular.setOnClickListener(bookingRegularClick);
    }
}
