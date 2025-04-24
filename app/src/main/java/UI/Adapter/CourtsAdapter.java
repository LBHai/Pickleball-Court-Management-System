package UI.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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

import Data.Holder.CourtViewHolder;
import Data.Model.Courts;
import UI.Activity.BookingRegularTableActivity;
import UI.Activity.BookingTableActivity;
import Utils.DebouncedOnClickListener;
import SEP490.G9.R;
import Data.Session.SessionManager;

public class CourtsAdapter extends RecyclerView.Adapter<CourtViewHolder> {

    private Context context;
    private List<Courts> courtsList;
    private OnCourtClickListener listener;


    // Interface callback truyền đối tượng Courts khi click vào item
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

        holder.getTvClubName().setText(court.getName());
        holder.getTvAddress().setText("Địa chỉ: " + court.getAddress());
        holder.getTvOpenTime().setText(court.getOpenTime());
        holder.getTvPhone().setText(court.getPhone());

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
        String backgroundUrl = court.getBackgroundUrl();
        if (backgroundUrl != null && !backgroundUrl.isEmpty()) {
            Glide.with(context)
                    .load(backgroundUrl)
                    .placeholder(R.drawable.anh_pickleball)
                    .error(R.drawable.anh_pickleball)
                    .into(holder.getImgCourt());
        } else {
            // nếu không có URL, giữ ảnh mặc định hoặc đặt ảnh khác
            holder.getImgCourt().setImageResource(R.drawable.anh_pickleball);
        }

        // Callback khi click vào item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                Log.d("CourtAdapter", "Item clicked, phone: " + court.getPhone());
                listener.onCourtClick(court);
            }
        });

        // Xử lý nút Book trong item: hiển thị dialog đặt sân kèm số điện thoại
        Button btnBook = holder.itemView.findViewById(R.id.btnBook);
        btnBook.setOnClickListener(new DebouncedOnClickListener(100) { // 1000ms = 1 giây
            @Override
            public void onDebouncedClick(View v) {
                // Xử lý đặt lịch ở đây
                showBookingDialog(context, court.getId(), court.getPhone());
            }
        });


        if (btnBook != null) {

            btnBook.setOnClickListener(v -> {
                String clubId = court.getId();
                String phone = court.getPhone() != null ? court.getPhone() : "";
                Log.d("CourtAdapter", "Book button clicked, phone: " + phone);
                showBookingDialog(context, clubId, phone);
            });
        }
        SessionManager sessionManager = new SessionManager(context);
        boolean isFavorite = sessionManager.isCourtFavorite(court.getId());
        if (isFavorite) {
            holder.getImgHeart().setImageResource(R.drawable.ic_heart_filled); // Tim đỏ
        } else {
            holder.getImgHeart().setImageResource(R.drawable.ic_heart_outline); // Tim trắng
        }

        // Xử lý sự kiện click vào trái tim
        holder.getImgHeart().setOnClickListener(v -> {
            if (sessionManager.isCourtFavorite(court.getId())) {
                sessionManager.removeFavoriteCourt(court.getId());
                holder.getImgHeart().setImageResource(R.drawable.ic_heart_outline); // Tim trắng
            } else {
                sessionManager.addFavoriteCourt(court.getId());
                holder.getImgHeart().setImageResource(R.drawable.ic_heart_filled); // Tim đỏ
            }
            // Nếu muốn cập nhật lại toàn bộ danh sách (nếu có filter), gọi notifyDataSetChanged()
            // notifyDataSetChanged();
        });
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

    }

    @Override
    public int getItemCount() {
        return courtsList.size();
    }

    // Hàm cập nhật danh sách mới (sau khi tìm kiếm)
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
