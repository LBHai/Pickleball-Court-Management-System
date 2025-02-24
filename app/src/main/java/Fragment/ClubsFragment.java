package Fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

import Api.ApiService;
import Model.Clubs;
import SEP490.G9.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ClubsFragment extends Fragment {

    //RecycleView
    private RecyclerView rcvClubs;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_clubs, container, false);


        rcvClubs = view.findViewById(R.id.rcvClubs);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rcvClubs.setLayoutManager(linearLayoutManager);

        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);

        rcvClubs.addItemDecoration(itemDecoration);
        return view;

    }
//    private void callApiGetClubs{
//        ApiService.apiService.getlistClubs(1).enqueue(new Callback<List<Clubs>>() {
//            @Override
//            public void onResponse(Call<List<Clubs>> call, Response<List<Clubs>> response) {
//
//            }
//
//            @Override
//            public void onFailure(Call<List<Clubs>> call, Throwable throwable) {
//
//            }
//        });
//    }

}