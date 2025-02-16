//package SEP490.G9;
//
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//
//import Api.ApiService;
//import Model.GetToken;
//import Model.User;
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//public class LoginActivity extends AppCompatActivity {
//    private TextView tvcode;
//    private TextView tvtoken;
//    private TextView tvauthenticated;
//    private Button btnCallApi;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_login);
//
//        tvcode = findViewById(R.id.tv_code);
//        tvtoken = findViewById(R.id.tv_token);
//        tvauthenticated = findViewById(R.id.tv_authenticated);
//        btnCallApi = findViewById(R.id.btn_call_api);
//
//        btnCallApi.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                clickCallApi();
//            }
//        });
//    }
//
//    private void clickCallApi() {
//        User user = new User();
//        user.setUsername("hyperUser");
//        user.setPassword("Hyper@123");
//        ApiService.apiService.getToken(user).enqueue(new Callback<GetToken>() {
//            @Override
//            public void onResponse(Call<GetToken> call, Response<GetToken> response) {
//                Toast.makeText(LoginActivity.this,"Call API Success: ", Toast.LENGTH_SHORT).show();
//
//                GetToken gettoken = response.body();
//                if(gettoken != null && gettoken.getCode().equals("1000")){
//                    tvcode.setText(gettoken.getCode());
//                    tvtoken.setText(gettoken.getResult().getToken());
//                    tvauthenticated.setText(String.valueOf(gettoken.getResult().isAuthenticated()));
//                }
//            }
//            @Override
//            public void onFailure(Call<GetToken> call, Throwable throwable) {
//                Toast.makeText(LoginActivity.this,"Call API Error: ", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//}
package SEP490.G9;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import Model.User;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Ánh xạ nút Sign up
        TextView tvSignUp = findViewById(R.id.btnSignup);

        // Xử lý sự kiện khi bấm nút Sign up
        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }
}
