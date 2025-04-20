package Activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import com.github.chrisbanes.photoview.PhotoView;

import SEP490.G9.R;

public class ImageZoomInOutActivity extends AppCompatActivity {
    public static final String EXTRA_URL = "image_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_zoom_in_out);

        PhotoView photoView = findViewById(R.id.photoView);
        String url = getIntent().getStringExtra(EXTRA_URL);

        if (url == null || url.isEmpty()) {
            Toast.makeText(this, "Không có URL hình ảnh", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load ảnh vào PhotoView
        Glide.with(this)
                .load(url)
                .into(photoView);

        // (Tuỳ chọn) Cấu hình mức Zoom
        photoView.setMaximumScale(4.0f);
        photoView.setMediumScale(2.0f);
        photoView.setMinimumScale(1.0f);
    }
}
