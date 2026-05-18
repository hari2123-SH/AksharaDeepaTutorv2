package com.aksharadeep.tutor.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.aksharadeep.tutor.R;
import com.aksharadeep.tutor.database.AppDatabase;
import com.aksharadeep.tutor.firebase.FirebaseManager;
import com.aksharadeep.tutor.models.Chapter;
import com.aksharadeep.tutor.utils.StreakManager;
import com.google.firebase.auth.FirebaseUser;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private AppDatabase db;
    private FirebaseManager fb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = AppDatabase.getDatabase(this);
        fb = FirebaseManager.getInstance();
        fb.initAnalytics(this);
        fb.logScreenView("Main Dashboard");

        setupGreeting();
        setupCards();
        setupStreak();
        updateOverallProgress();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateOverallProgress();
        setupStreak();
    }

    private void setupGreeting() {
        FirebaseUser user = fb.getCurrentUser();
        TextView tvGreeting = findViewById(R.id.tv_greeting);
        TextView tvClass = findViewById(R.id.tv_student_class);

        if (user != null) {
            fb.getUserProfile(new FirebaseManager.OnProfileLoadedListener() {
                public void onLoaded(java.util.Map<String, Object> data) {
                    String name = (String) data.getOrDefault("name", "Student");
                    String usn  = (String) data.getOrDefault("usn", "Class 10");
                    runOnUiThread(() -> {
                        if (tvGreeting != null) tvGreeting.setText("Hello, " + name + "! 👋");
                        if (tvClass != null) tvClass.setText(usn);
                    });
                }
                public void onFailure(String e) {
                    runOnUiThread(() -> {
                        if (tvGreeting != null) tvGreeting.setText("Hello, Student! 👋");
                    });
                }
            });
        }

        TextView tvLogout = findViewById(R.id.tv_logout);
        if (tvLogout != null) tvLogout.setOnClickListener(v -> confirmLogout());
    }

    private void setupStreak() {
        int streak = StreakManager.getCurrentStreak(this);
        TextView tvStreak = findViewById(R.id.tv_streak);
        if (tvStreak != null) {
            tvStreak.setText(StreakManager.getStreakEmoji(streak) + " " + streak + " Day Streak");
        }
        TextView tvStreakMsg = findViewById(R.id.tv_streak_message);
        if (tvStreakMsg != null) {
            tvStreakMsg.setText(StreakManager.getMotivationalMessage(streak));
        }
    }

    private void setupCards() {
        CardView cardSyllabus   = findViewById(R.id.card_syllabus);
        CardView cardStrength   = findViewById(R.id.card_strength);
        CardView cardLeader     = findViewById(R.id.card_leaderboard);
        CardView cardAnalytics  = findViewById(R.id.card_analytics);

        if (cardSyllabus  != null) cardSyllabus.setOnClickListener(v ->
            startActivity(new Intent(this, SyllabusActivity.class)));
        if (cardStrength  != null) cardStrength.setOnClickListener(v ->
            startActivity(new Intent(this, StrengthMapActivity.class)));
        if (cardLeader    != null) cardLeader.setOnClickListener(v ->
            startActivity(new Intent(this, LeaderboardActivity.class)));
        if (cardAnalytics != null) cardAnalytics.setOnClickListener(v ->
            startActivity(new Intent(this, AnalyticsDashboardActivity.class)));
    }

    private void updateOverallProgress() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Chapter> all = db.chapterDao().getAllChaptersSync();
            int total = all.size(), done = 0;
            float totalScore = 0; int attempted = 0;
            for (Chapter c : all) {
                if (c.isCompleted) done++;
                if (c.quizAttempts > 0) { totalScore += c.quizScore; attempted++; }
            }
            int percent = total > 0 ? (done * 100) / total : 0;
            float avg = attempted > 0 ? totalScore / attempted : 0;
            int finalDone = done; int finalTotal = total;

            // Sync to Firebase
            fb.syncProgressToCloud(done, total, avg, attempted);

            runOnUiThread(() -> {
                ProgressBar pb = findViewById(R.id.progress_overall);
                TextView tvPct = findViewById(R.id.tv_progress_text);
                TextView tvDone = findViewById(R.id.tv_chapters_done);
                if (pb != null) pb.setProgress(percent);
                if (tvPct != null) tvPct.setText(percent + "% Complete");
                if (tvDone != null) tvDone.setText(finalDone + " / " + finalTotal + " chapters done");
            });
        });
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout", (d, w) -> { fb.logout(); startActivity(new Intent(this, LoginActivity.class)); finish(); })
            .setNegativeButton("Cancel", null).show();
    }
}
