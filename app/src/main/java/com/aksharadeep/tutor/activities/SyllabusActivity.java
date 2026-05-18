package com.aksharadeep.tutor.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aksharadeep.tutor.R;
import com.aksharadeep.tutor.adapters.SubjectAdapter;
import com.aksharadeep.tutor.database.AppDatabase;
import com.aksharadeep.tutor.models.Chapter;
import com.aksharadeep.tutor.models.Subject;

import java.util.List;

public class SyllabusActivity extends AppCompatActivity {

    private AppDatabase db;
    private SubjectAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_syllabus);

        db = AppDatabase.getDatabase(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Mission Map – Syllabus");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.rv_subjects);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Subject> subjects = db.subjectDao().getAllSubjectsSync();
            java.util.Map<Subject, List<Chapter>> subjectChaptersMap = new java.util.LinkedHashMap<>();

            for (Subject s : subjects) {
                List<Chapter> chapters = db.chapterDao().getChaptersBySubjectSync(s.id);
                subjectChaptersMap.put(s, chapters);
            }

            runOnUiThread(() -> {
                adapter = new SubjectAdapter(this, subjectChaptersMap, db);
                recyclerView.setAdapter(adapter);
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
