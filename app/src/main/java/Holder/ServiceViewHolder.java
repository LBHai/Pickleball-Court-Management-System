package Holder;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import SEP490.G9.R;

public class ServiceViewHolder extends RecyclerView.ViewHolder {
    public ImageView ivDrink;
    public TextView tvName;
    public TextView tvDescription;
    public TextView tvPrice;
    public TextView tvQuantity;
    public ImageButton btnDecrease;
    public ImageButton btnIncrease;
    public TextView tvStockQuantity;

    public ServiceViewHolder(@NonNull View itemView) {
        super(itemView);
        ivDrink = itemView.findViewById(R.id.ivDrink);
        tvName = itemView.findViewById(R.id.tvName);
        tvDescription = itemView.findViewById(R.id.tvDescription);
        tvPrice = itemView.findViewById(R.id.tvPrice);
        tvQuantity = itemView.findViewById(R.id.tvQuantity);
        btnDecrease = itemView.findViewById(R.id.btnDecrease);
        btnIncrease = itemView.findViewById(R.id.btnIncrease);
        tvStockQuantity = itemView.findViewById(R.id.tvStockQuantity);
    }
}
