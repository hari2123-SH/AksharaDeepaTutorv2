package com.aksharadeep.tutor.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.aksharadeep.tutor.R;
import com.aksharadeep.tutor.database.AppDatabase;
import com.aksharadeep.tutor.firebase.FirebaseManager;
import com.aksharadeep.tutor.models.Chapter;
import com.aksharadeep.tutor.models.Subject;
import com.aksharadeep.tutor.utils.GeminiHelper;
import com.aksharadeep.tutor.utils.StreakManager;
import java.util.List;

public class AnalyticsDashboardActivity extends AppCompatActivity {

    private AppDatabase db;
    private FirebaseManager fb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);
        db = AppDatabase.getDatabase(this);
        fb = FirebaseManager.getInstance();
        fb.logScreenView("Analytics Dashboard");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("📊 Analytics Dashboard");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        loadAnalytics();
    }

    private void loadAnalytics() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Subject> subjects   = db.subjectDao().getAllSubjectsSync();
            List<Chapter> allChapters = db.chapterDao().getAllChaptersSync();

            int totalChapters = allChapters.size();
            int doneChapters  = 0;
            int totalQuizzes  = 0;
            int gapAreas      = 0;
            float totalScore  = 0;
            int attempted     = 0;
            String weakestSubject = "";
            float weakestAvg = 101f;
            String weakestChapter = "";

            for (Chapter c : allChapters) {
                if (c.isCompleted) doneChapters++;
                if (c.quizAttempts > 0) {
                    totalQuizzes++;
                    totalScore += c.quizScore;
                    attempted++;
                    if (c.quizScore < 40) gapAreas++;
                    if (c.quizScore < weakestAvg) {
                        weakestAvg = c.quizScore;
                        weakestChapter = c.name;
                    }
                }
            }

            float overallAvg = attempted > 0 ? totalScore / attempted : 0;

            // Find weakest subject
            for (Subject s : subjects) {
                float avg = db.chapterDao().getAverageScore(s.id);
                if (avg > 0 && avg < weakestAvg) {
                    weakestSubject = s.name;
                }
            }

            int streak     = StreakManager.getCurrentStreak(this);
            int bestStreak = StreakManager.getBestStreak(this);

            // Build per-subject stats
            StringBuilder subjectStats = new StringBuilder();
            for (Subject s : subjects) {
                List<Chapter> sChapters = db.chapterDao().getChaptersBySubjectSync(s.id);
                int sDone = 0, sAttempted = 0; float sScore = 0;
                for (Chapter c : sChapters) {
                    if (c.isCompleted) sDone++;
                    if (c.quizAttempts > 0) { sScore += c.quizScore; sAttempted++; }
                }
                float sAvg = sAttempted > 0 ? sScore / sAttempted : 0;
                String status = sAvg >= 70 ? "✅ Strong" : sAvg >= 40 ? "⚠️ Developing" : sAttempted==0 ? "📖 Not Started" : "❌ Gap Area";
                subjectStats.append(s.name).append(": ").append(sDone).append("/").append(sChapters.size())
                    .append(" chapters | Avg: ").append((int)sAvg).append("% ").append(status).append("\n\n");
            }

            final int    fd = doneChapters, ft = totalChapters, fq = totalQuizzes, fg = gapAreas;
            final float  fa = overallAvg;
            final int    fs = streak, fbs = bestStreak;
            final String fwc = weakestChapter.isEmpty() ? "Complete a quiz first!" : weakestChapter;
            final String fws = weakestSubject.isEmpty() ? "N/A" : weakestSubject;
            final String fss = subjectStats.toString();

            runOnUiThread(() -> {
                // Overall stats
                setTV(R.id.tv_total_chapters,  fd + " / " + ft);
                setTV(R.id.tv_total_quizzes,   fq + " quizzes");
                setTV(R.id.tv_overall_avg,     (int)fa + "%");
                setTV(R.id.tv_gap_areas,       fg + " chapters");
                setTV(R.id.tv_current_streak,  fs + " days " + StreakManager.getStreakEmoji(fs));
                setTV(R.id.tv_best_streak,     fbs + " days");
                setTV(R.id.tv_weakest_chapter, fwc);
                setTV(R.id.tv_subject_breakdown, fss.trim());

                // Color overall avg
                TextView tvAvg = findViewById(R.id.tv_overall_avg);
                if (tvAvg != null) {
                    if (fa >= 70) tvAvg.setTextColor(Color.parseColor("#1B5E20"));
                    else if (fa >= 40) tvAvg.setTextColor(Color.parseColor("#F57F17"));
                    else tvAvg.setTextColor(Color.parseColor("#B71C1C"));
                }

                // Progress bar
                ProgressBar pb = findViewById(R.id.progress_analytics);
                if (pb != null) pb.setProgress(ft > 0 ? (fd*100)/ft : 0);

                // AI study tip
                if (!fwc.equals("Complete a quiz first!")) {
                    TextView tvTip = findViewById(R.id.tv_ai_tip);
                    if (tvTip != null) tvTip.setText("🤖 Getting AI study tip...");
                    GeminiHelper.getStudyTip(fws.isEmpty() ? "General" : fws, fwc,
                        new GeminiHelper.HintCallback() {
                            public void onHint(String tip) {
                                if (tvTip != null) tvTip.setText("🤖 AI Tip: " + tip);
                            }
                            public void onError(String e) {
                                if (tvTip != null) tvTip.setText("🤖 AI Tip: Focus on your weakest chapter — read it slowly and take notes!");
                            }
                        });
                }
            });
        });
    }

    private void setTV(int id, String text) {
        TextView tv = findViewById(id);
        if (tv != null) tv.setText(text);
    }

    @Override public boolean onSupportNavigateUp() { onBackPressed(); return true; }
}
