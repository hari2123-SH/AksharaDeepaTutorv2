package com.aksharadeep.tutor.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.aksharadeep.tutor.R;
import com.aksharadeep.tutor.database.AppDatabase;
import com.aksharadeep.tutor.models.Chapter;
import com.aksharadeep.tutor.models.Subject;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class StrengthMapActivity extends AppCompatActivity {

    private AppDatabase db;
    private RadarChart radarChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strength_map);

        db = AppDatabase.getDatabase(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Strength Map");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        radarChart = findViewById(R.id.radar_chart);
        setupChart();
        loadStrengthData();
    }

    private void setupChart() {
        radarChart.getDescription().setEnabled(false);
        radarChart.setWebLineWidth(1.5f);
        radarChart.setWebColor(Color.parseColor("#CCCCCC"));
        radarChart.setWebLineWidthInner(1f);
        radarChart.setWebColorInner(Color.parseColor("#DDDDDD"));
        radarChart.setWebAlpha(100);
        radarChart.animateXY(1400, 1400);

        radarChart.getYAxis().setAxisMinimum(0f);
        radarChart.getYAxis().setAxisMaximum(100f);
        radarChart.getYAxis().setDrawLabels(false);

        Legend legend = radarChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setTextSize(13f);
        legend.setTextColor(Color.parseColor("#212121"));
    }

    private void loadStrengthData() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Subject> subjects = db.subjectDao().getAllSubjectsSync();

            List<RadarEntry> entries = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            List<String> summaries = new ArrayList<>();

            for (Subject s : subjects) {
                List<Chapter> chapters = db.chapterDao().getChaptersBySubjectSync(s.id);
                float totalScore = 0;
                int attempted = 0;

                for (Chapter c : chapters) {
                    if (c.quizAttempts > 0) {
                        totalScore += c.quizScore;
                        attempted++;
                    }
                }

                float avg = attempted > 0 ? totalScore / attempted : 0f;
                entries.add(new RadarEntry(avg));
                labels.add(s.name);

                String label = avg >= 70 ? "✅ Strong" : avg >= 40 ? "⚠️ Developing" : "❌ Gap Area";
                summaries.add(s.name + ": " + (int) avg + "% — " + label);
            }

            RadarDataSet dataSet = new RadarDataSet(entries, "Subject Mastery");
            dataSet.setColor(Color.parseColor("#1A237E"));
            dataSet.setFillColor(Color.parseColor("#3949AB"));
            dataSet.setDrawFilled(true);
            dataSet.setFillAlpha(100);
            dataSet.setLineWidth(2f);
            dataSet.setDrawHighlightCircleEnabled(true);
            dataSet.setHighlightCircleFillColor(Color.parseColor("#FF6F00"));
            dataSet.setValueTextSize(12f);
            dataSet.setValueTextColor(Color.parseColor("#1A237E"));

            RadarData data = new RadarData(dataSet);

            runOnUiThread(() -> {
                XAxis xAxis = radarChart.getXAxis();
                xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                xAxis.setTextSize(13f);
                xAxis.setTextColor(Color.parseColor("#212121"));

                radarChart.setData(data);
                radarChart.invalidate();

                // Show summary text
                TextView tvSummary = findViewById(R.id.tv_strength_summary);
                if (tvSummary != null) {
                    StringBuilder sb = new StringBuilder();
                    for (String s : summaries) sb.append(s).append("\n\n");
                    tvSummary.setText(sb.toString().trim());
                }

                TextView tvHint = findViewById(R.id.tv_chart_hint);
                if (tvHint != null) {
                    tvHint.setText("Complete quizzes to see your Strength Map grow! Each quiz updates your score in real-time.");
                }
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
