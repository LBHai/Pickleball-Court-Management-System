package SEP490.G9;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.gson.Gson;

import Model.User;

public class MainActivity extends AppCompatActivity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            User user = new User();
            Gson gson = new Gson();
            String strJson = gson.toJson(user);

    // Ánh xạ nút Sign up
    TextView tvSignUp = findViewById(R.id.Signup);

    // Xử lý sự kiện khi bấm nút Sign up
        tvSignUp.setOnClickListener(v -> {
        Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
        startActivity(intent);
    });
    }
}
