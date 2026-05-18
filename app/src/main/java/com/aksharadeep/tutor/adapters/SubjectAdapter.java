package com.aksharadeep.tutor.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.aksharadeep.tutor.R;
import com.aksharadeep.tutor.activities.QuizActivity;
import com.aksharadeep.tutor.database.AppDatabase;
import com.aksharadeep.tutor.firebase.FirebaseManager;
import com.aksharadeep.tutor.models.Chapter;
import com.aksharadeep.tutor.models.Subject;
import com.aksharadeep.tutor.utils.StreakManager;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

    private final Context context;
    private final List<Subject> subjects;
    private final Map<Subject, List<Chapter>> dataMap;
    private final AppDatabase db;
    private final Map<Integer, Boolean> expandedState = new LinkedHashMap<>();

    public SubjectAdapter(Context context, Map<Subject, List<Chapter>> dataMap, AppDatabase db) {
        this.context = context;
        this.dataMap = dataMap;
        this.db = db;
        this.subjects = new ArrayList<>(dataMap.keySet());
    }

    @NonNull @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_subject, parent, false);
        return new SubjectViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        Subject subject = subjects.get(position);
        List<Chapter> chapters = dataMap.get(subject);

        holder.tvSubjectName.setText(subject.name);
        try { holder.headerLayout.setBackgroundColor(Color.parseColor(subject.colorCode)); }
        catch (Exception e) { holder.headerLayout.setBackgroundColor(Color.parseColor("#1A237E")); }

        int total = chapters != null ? chapters.size() : 0;
        int done = 0;
        if (chapters != null) for (Chapter ch : chapters) if (ch.isCompleted) done++;
        int percent = total > 0 ? done * 100 / total : 0;

        holder.tvProgress.setText(done + "/" + total + " chapters (" + percent + "%)");
        holder.progressBar.setProgress(percent);

        boolean expanded = expandedState.getOrDefault(subject.id, false);
        holder.chaptersContainer.setVisibility(expanded ? View.VISIBLE : View.GONE);
        holder.tvExpandIcon.setText(expanded ? "▲" : "▼");

        holder.headerLayout.setOnClickListener(v -> {
            boolean newState = !expandedState.getOrDefault(subject.id, false);
            expandedState.put(subject.id, newState);
            notifyItemChanged(position);
        });

        holder.chaptersContainer.removeAllViews();
        if (chapters != null) {
            for (Chapter chapter : chapters) {
                View cv = LayoutInflater.from(context).inflate(R.layout.item_chapter,
                        holder.chaptersContainer, false);

                TextView tvName   = cv.findViewById(R.id.tv_chapter_name);
                CheckBox cbDone   = cv.findViewById(R.id.cb_completed);
                Button btnQuiz    = cv.findViewById(R.id.btn_quiz);
                TextView tvStrength = cv.findViewById(R.id.tv_strength);

                tvName.setText(chapter.name);
                cbDone.setChecked(chapter.isCompleted);

                if (chapter.quizAttempts > 0) {
                    tvStrength.setVisibility(View.VISIBLE);
                    String lbl = chapter.getStrengthLabel();
                    tvStrength.setText(lbl + " • " + chapter.quizScore + "%");
                    switch (lbl) {
                        case "Strong":     tvStrength.setTextColor(Color.parseColor("#1B5E20")); break;
                        case "Good":       tvStrength.setTextColor(Color.parseColor("#F57F17")); break;
                        case "Gap Area":   tvStrength.setTextColor(Color.parseColor("#B71C1C")); break;
                        default:           tvStrength.setTextColor(Color.parseColor("#E65100")); break;
                    }
                } else { tvStrength.setVisibility(View.GONE); }

                cbDone.setOnCheckedChangeListener((btn, checked) -> {
                    chapter.isCompleted = checked;
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        db.chapterDao().updateCompletion(chapter.id, checked, System.currentTimeMillis());
                        // Record streak activity + Firebase analytics
                        if (checked) {
                            StreakManager.recordStudyActivity(context);
                            FirebaseManager.getInstance().logChapterCompleted(chapter.name, subject.name);
                        }
                    });
                    notifyItemChanged(position);
                    Toast.makeText(context, checked ? "✅ Marked complete!" : "Marked incomplete",
                            Toast.LENGTH_SHORT).show();
                });

                btnQuiz.setOnClickListener(v -> {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        int count = db.questionDao().getCountForChapter(chapter.id);
                        ((android.app.Activity) context).runOnUiThread(() -> {
                            if (count == 0) {
                                Toast.makeText(context, "No questions for this chapter yet.",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Intent intent = new Intent(context, QuizActivity.class);
                            intent.putExtra(QuizActivity.EXTRA_CHAPTER_ID, chapter.id);
                            intent.putExtra(QuizActivity.EXTRA_CHAPTER_NAME, chapter.name);
                            intent.putExtra(QuizActivity.EXTRA_SUBJECT_ID, subject.id);
                            intent.putExtra(QuizActivity.EXTRA_SUBJECT_NAME, subject.name);
                            context.startActivity(intent);
                        });
                    });
                });

                holder.chaptersContainer.addView(cv);
            }
        }
    }

    @Override public int getItemCount() { return subjects.size(); }

    public static class SubjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubjectName, tvProgress, tvExpandIcon;
        LinearLayout headerLayout, chaptersContainer;
        ProgressBar progressBar;
        public SubjectViewHolder(@NonNull View v) {
            super(v);
            tvSubjectName    = v.findViewById(R.id.tv_subject_name);
            tvProgress       = v.findViewById(R.id.tv_subject_progress);
            tvExpandIcon     = v.findViewById(R.id.tv_expand_icon);
            headerLayout     = v.findViewById(R.id.layout_subject_header);
            chaptersContainer= v.findViewById(R.id.layout_chapters_container);
            progressBar      = v.findViewById(R.id.progress_subject);
        }
    }
}
