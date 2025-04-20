package Activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import SEP490.G9.R;

public class ImageZoomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_zoom);

        ImageView ivZoomedImage = findViewById(R.id.iv_zoomed_image);

        // Lấy URL ảnh từ Intent
        String imageUrl = getIntent().getStringExtra("image_url");

        // Tải ảnh vào ImageView bằng Glide
        Glide.with(this).load(imageUrl).into(ivZoomedImage);
    }
}