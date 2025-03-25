package SEP490.G9;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;


public class InfoFragment extends Fragment {
    private static final String ARG_CLUB_NAME = "club_name";

    public static InfoFragment newInstance(String clubName) {
        InfoFragment fragment = new InfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CLUB_NAME, clubName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);
        TextView tvInfo = view.findViewById(R.id.tv_info);

        if (getArguments() != null) {
            String clubName = getArguments().getString(ARG_CLUB_NAME);
            tvInfo.setText("Thông tin chi tiết về: " + clubName);
        }

        return view;
    }
}