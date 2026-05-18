package com.aksharadeep.tutor.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.aksharadeep.tutor.R;
import com.aksharadeep.tutor.database.AppDatabase;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize DB in background
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase.getDatabase(getApplicationContext());
        });

        // Animate the title
        TextView tvTitle = findViewById(R.id.tv_splash_title);
        TextView tvSubtitle = findViewById(R.id.tv_splash_subtitle);

        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(1200);
        fadeIn.setFillAfter(true);
        tvTitle.startAnimation(fadeIn);

        AlphaAnimation fadeInSub = new AlphaAnimation(0f, 1f);
        fadeInSub.setDuration(1200);
        fadeInSub.setStartOffset(400);
        fadeInSub.setFillAfter(true);
        tvSubtitle.startAnimation(fadeInSub);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, 2500);
    }
}
