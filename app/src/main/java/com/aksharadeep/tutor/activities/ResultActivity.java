package com.aksharadeep.tutor.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.aksharadeep.tutor.R;

public class ResultActivity extends AppCompatActivity {

    public static final String EXTRA_SCORE = "score";
    public static final String EXTRA_TOTAL = "total";
    public static final String EXTRA_CHAPTER_NAME = "chapter_name";
    public static final String EXTRA_CHAPTER_ID = "chapter_id";
    public static final String EXTRA_STREAK = "streak";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        int score = getIntent().getIntExtra(EXTRA_SCORE, 0);
        int total = getIntent().getIntExtra(EXTRA_TOTAL, 5);
        String chapterName = getIntent().getStringExtra(EXTRA_CHAPTER_NAME);

        int percent = (score * 100) / total;

        TextView tvScore = findViewById(R.id.tv_score);
        TextView tvScoreLabel = findViewById(R.id.tv_score_label);
        TextView tvFeedback = findViewById(R.id.tv_feedback);
        TextView tvChapterName = findViewById(R.id.tv_result_chapter);
        ProgressBar progressBar = findViewById(R.id.progress_score);
        Button btnGoHome = findViewById(R.id.btn_go_home);
        Button btnStrengthMap = findViewById(R.id.btn_view_strength);

        tvChapterName.setText(chapterName);
        tvScore.setText(score + " / " + total);
        tvScoreLabel.setText(percent + "%");
        progressBar.setProgress(percent);

        if (percent >= 80) {
            tvFeedback.setText("🌟 Excellent! You've mastered this chapter!");
            tvFeedback.setTextColor(Color.parseColor("#1B5E20"));
        } else if (percent >= 60) {
            tvFeedback.setText("👍 Good job! A little more practice will make you perfect.");
            tvFeedback.setTextColor(Color.parseColor("#F57F17"));
        } else if (percent >= 40) {
            tvFeedback.setText("📖 Keep going! Revise the chapter and try again.");
            tvFeedback.setTextColor(Color.parseColor("#E65100"));
        } else {
            tvFeedback.setText("⚠️ Gap Area detected! Please re-read this chapter carefully.");
            tvFeedback.setTextColor(Color.parseColor("#B71C1C"));
        }

        btnGoHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        btnStrengthMap.setOnClickListener(v -> {
            startActivity(new Intent(this, StrengthMapActivity.class));
        });
    }
}
