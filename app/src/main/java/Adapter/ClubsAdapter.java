package Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import Model.Clubs;
import SEP490.G9.R;

public class ClubsAdapter extends RecyclerView.Adapter<ClubsAdapter.ClubsViewHolder> {

    private List<Clubs> mListClubs;

    public ClubsAdapter(List<Clubs> mListClubs) {
        this.mListClubs = mListClubs;
    }


    @NonNull
    @Override
    public ClubsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_clubs,parent,false);
        return new ClubsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClubsViewHolder holder, int position) {
        Clubs clubs = mListClubs.get(position);
        if(clubs == null ){
            return;
        }
        holder.tvId.setText(String.valueOf(clubs.getId()));
        holder.tvId.setText(String.valueOf(clubs.getAddress()));
    }

    @Override
    public int getItemCount() {
        if(mListClubs != null){
            mListClubs.size();
        }
        return 0;
    }

    public static class ClubsViewHolder extends RecyclerView.ViewHolder {
        private TextView tvId, tvTitle;

        public ClubsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvId);
            tvTitle = itemView.findViewById(R.id.tvTitle);
        }
    }


}
