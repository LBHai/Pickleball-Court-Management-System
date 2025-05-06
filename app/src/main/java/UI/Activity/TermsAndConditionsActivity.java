package UI.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import SEP490.G9.R;

public class TermsAndConditionsActivity extends AppCompatActivity {
    private CheckBox checkBoxAgree;
    private Button buttonContinue;
    private TextView textViewTerms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_conditions);

        // Khởi tạo các thành phần UI
        textViewTerms = findViewById(R.id.textViewTerms);
        checkBoxAgree = findViewById(R.id.checkBoxAgree);
        buttonContinue = findViewById(R.id.buttonContinue);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Kiểm tra nếu được gọi từ AccountFragment
        boolean fromAccountFragment = getIntent().getBooleanExtra("fromAccountFragment", false);
        if (fromAccountFragment) {
            // Ẩn checkbox và nút tiếp tục
            checkBoxAgree.setVisibility(View.GONE);
            buttonContinue.setVisibility(View.GONE);

            // Điều chỉnh layout để cardViewTerms chiếm toàn bộ không gian
            androidx.cardview.widget.CardView cardViewTerms = findViewById(R.id.cardViewTerms);
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) cardViewTerms.getLayoutParams();
            params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID; // Đặt bottom của cardViewTerms gắn với parent
            cardViewTerms.setLayoutParams(params);
        }

        // Thiết lập nội dung điều khoản với định dạng
        setFormattedTermsContent();

        // Thiết lập sự kiện click cho nút Tiếp tục
        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBoxAgree.isChecked()) {
                    // Lưu trạng thái đã đồng ý điều khoản
                    SharedPreferences preferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
                    preferences.edit().putBoolean("terms_accepted", true).apply();

                    // Chuyển đến MainActivity
                    Intent intent = new Intent(TermsAndConditionsActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Hiển thị thông báo nếu chưa đồng ý
                    Toast.makeText(TermsAndConditionsActivity.this,
                            getString(R.string.must_agree_to_terms),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Thiết lập nội dung điều khoản với định dạng đẹp
     */
    private void setFormattedTermsContent() {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        // Phần 1: ĐIỀU KHOẢN DỊCH VỤ
        appendSectionTitle(builder, getString(R.string.terms_section_1_title));
        appendSubsectionTitle(builder, getString(R.string.terms_section_1_1_title));
        appendBulletPoint(builder, getString(R.string.terms_section_1_1_bullet_1));
        appendBulletPoint(builder, getString(R.string.terms_section_1_1_bullet_2));
        appendBulletPoint(builder, getString(R.string.terms_section_1_1_bullet_3));

        appendSubsectionTitle(builder, getString(R.string.terms_section_1_2_title));
        appendParagraph(builder, getString(R.string.terms_section_1_2_paragraph));

        appendSubsectionTitle(builder, getString(R.string.terms_section_1_3_title));
        appendBulletPoint(builder, getString(R.string.terms_section_1_3_bullet_1));
        appendBulletPoint(builder, getString(R.string.terms_section_1_3_bullet_2));
        appendBulletPoint(builder, getString(R.string.terms_section_1_3_bullet_3));

        appendSubsectionTitle(builder, getString(R.string.terms_section_1_4_title));
        appendNumberedPoint(builder, 1, getString(R.string.terms_section_1_4_point_1));
        appendNumberedPoint(builder, 2, getString(R.string.terms_section_1_4_point_2));
        appendNumberedPoint(builder, 3, getString(R.string.terms_section_1_4_point_3));
        appendNumberedPoint(builder, 4, getString(R.string.terms_section_1_4_point_4));
        appendNumberedPoint(builder, 5, getString(R.string.terms_section_1_4_point_5));

        appendSubsectionTitle(builder, getString(R.string.terms_section_1_5_title));
        appendBulletPoint(builder, getString(R.string.terms_section_1_5_bullet_1));
        appendBulletPoint(builder, getString(R.string.terms_section_1_5_bullet_2));
        appendBulletPoint(builder, getString(R.string.terms_section_1_5_bullet_3));

        appendSubsectionTitle(builder, getString(R.string.terms_section_1_6_title));
        appendBulletPoint(builder, getString(R.string.terms_section_1_6_bullet_1));
        appendBulletPoint(builder, getString(R.string.terms_section_1_6_bullet_2));
        appendBulletPoint(builder, getString(R.string.terms_section_1_6_bullet_3));
        appendBulletPoint(builder, getString(R.string.terms_section_1_6_bullet_4));

        appendSubsectionTitle(builder, getString(R.string.terms_section_1_7_title));
        appendBulletPoint(builder, getString(R.string.terms_section_1_7_bullet_1));
        appendBulletPoint(builder, getString(R.string.terms_section_1_7_bullet_2));
        appendBulletPoint(builder, getString(R.string.terms_section_1_7_bullet_3));
        appendBulletPoint(builder, getString(R.string.terms_section_1_7_bullet_4));
        appendBulletPoint(builder, getString(R.string.terms_section_1_7_bullet_5));
        appendBulletPoint(builder, getString(R.string.terms_section_1_7_bullet_6));

        appendSubsectionTitle(builder, getString(R.string.terms_section_1_8_title));
        appendBulletPoint(builder, getString(R.string.terms_section_1_8_bullet_1));
        appendBulletPoint(builder, getString(R.string.terms_section_1_8_bullet_2));
        appendBulletPoint(builder, getString(R.string.terms_section_1_8_bullet_3));
        appendBulletPoint(builder, getString(R.string.terms_section_1_8_bullet_4));
        appendBulletPoint(builder, getString(R.string.terms_section_1_8_bullet_5));
        appendBulletPoint(builder, getString(R.string.terms_section_1_8_bullet_6));
        appendBulletPoint(builder, getString(R.string.terms_section_1_8_bullet_7));
        appendBulletPoint(builder, getString(R.string.terms_section_1_8_bullet_8));

        appendSubsectionTitle(builder, getString(R.string.terms_section_1_9_title));
        appendBulletPoint(builder, getString(R.string.terms_section_1_9_bullet_1));
        appendBulletPoint(builder, getString(R.string.terms_section_1_9_bullet_2));
        appendBulletPoint(builder, getString(R.string.terms_section_1_9_bullet_3));

        appendSubsectionTitle(builder, getString(R.string.terms_section_1_10_title));
        appendBulletPoint(builder, getString(R.string.terms_section_1_10_bullet_1));
        appendBulletPoint(builder, getString(R.string.terms_section_1_10_bullet_2));
        appendBulletPoint(builder, getString(R.string.terms_section_1_10_bullet_3));

        appendSubsectionTitle(builder, getString(R.string.terms_section_1_11_title));
        appendBulletPoint(builder, getString(R.string.terms_section_1_11_bullet_1));
        appendBulletPoint(builder, getString(R.string.terms_section_1_11_bullet_2));
        appendBulletPoint(builder, getString(R.string.terms_section_1_11_bullet_3));

        appendSubsectionTitle(builder, getString(R.string.terms_section_1_12_title));
        appendBulletPoint(builder, getString(R.string.terms_section_1_12_bullet_1));
        appendBulletPoint(builder, getString(R.string.terms_section_1_12_bullet_2));

        // Phần 2: CHÍNH SÁCH BẢO MẬT
        appendSectionTitle(builder, getString(R.string.terms_section_2_title));
        appendSubsectionTitle(builder, getString(R.string.terms_section_2_1_title));
        appendBulletPoint(builder, getString(R.string.terms_section_2_1_bullet_1));
        appendBulletPoint(builder, getString(R.string.terms_section_2_1_bullet_2));
        appendBulletPoint(builder, getString(R.string.terms_section_2_1_bullet_3));

        appendSubsectionTitle(builder, getString(R.string.terms_section_2_2_title));
        appendBulletPoint(builder, getString(R.string.terms_section_2_2_bullet_1));
        appendBulletPoint(builder, getString(R.string.terms_section_2_2_bullet_2));
        appendBulletPoint(builder, getString(R.string.terms_section_2_2_bullet_3));

        appendSubsectionTitle(builder, getString(R.string.terms_section_2_3_title));
        appendBulletPoint(builder, getString(R.string.terms_section_2_3_bullet_1));
        appendBulletPoint(builder, getString(R.string.terms_section_2_3_bullet_2));

        appendSubsectionTitle(builder, getString(R.string.terms_section_2_4_title));
        appendBulletPoint(builder, getString(R.string.terms_section_2_4_bullet_1));
        appendBulletPoint(builder, getString(R.string.terms_section_2_4_bullet_2));

        appendSubsectionTitle(builder, getString(R.string.terms_section_2_5_title));
        appendBulletPoint(builder, getString(R.string.terms_section_2_5_bullet_1));
        appendBulletPoint(builder, getString(R.string.terms_section_2_5_bullet_2));
        appendBulletPoint(builder, getString(R.string.terms_section_2_5_bullet_3));

        appendSubsectionTitle(builder, getString(R.string.terms_section_2_6_title));
        appendBulletPoint(builder, getString(R.string.terms_section_2_6_bullet_1));
        appendBulletPoint(builder, getString(R.string.terms_section_2_6_bullet_2));
        appendBulletPoint(builder, getString(R.string.terms_section_2_6_bullet_3));

        appendSubsectionTitle(builder, getString(R.string.terms_section_2_7_title));
        appendBulletPoint(builder, getString(R.string.terms_section_2_7_bullet_1));
        appendBulletPoint(builder, getString(R.string.terms_section_2_7_bullet_2));
        appendBulletPoint(builder, getString(R.string.terms_section_2_7_bullet_3));

        appendSubsectionTitle(builder, getString(R.string.terms_section_2_8_title));
        appendBulletPoint(builder, getString(R.string.terms_section_2_8_bullet_1));
        appendBulletPoint(builder, getString(R.string.terms_section_2_8_bullet_2));

        // Hiển thị nội dung đã định dạng
        textViewTerms.setText(builder);
    }

    /**
     * Thêm tiêu đề phần chính với định dạng lớn và đậm
     */
    private void appendSectionTitle(SpannableStringBuilder builder, String title) {
        SpannableString spannableString = new SpannableString(title + "\n\n");
        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new RelativeSizeSpan(1.3f),
                0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(
                        ContextCompat.getColor(this, R.color.colorPrimary)),
                0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
    }

    /**
     * Thêm tiêu đề phần phụ với định dạng đậm
     */
    private void appendSubsectionTitle(SpannableStringBuilder builder, String title) {
        SpannableString spannableString = new SpannableString(title + "\n\n");
        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new RelativeSizeSpan(1.1f),
                0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
    }

    /**
     * Thêm đoạn văn bản thường
     */
    private void appendParagraph(SpannableStringBuilder builder, String text) {
        builder.append(text + "\n\n");
    }

    /**
     * Thêm điểm đánh dấu (bullet point)
     */
    private void appendBulletPoint(SpannableStringBuilder builder, String text) {
        builder.append("• " + text + "\n");
    }

    /**
     * Thêm điểm đánh số
     */
    private void appendNumberedPoint(SpannableStringBuilder builder, int number, String text) {
        builder.append(number + ". " + text + "\n");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}