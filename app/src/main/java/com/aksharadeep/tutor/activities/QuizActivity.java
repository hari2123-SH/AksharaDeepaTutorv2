package com.aksharadeep.tutor.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.aksharadeep.tutor.R;
import com.aksharadeep.tutor.database.AppDatabase;
import com.aksharadeep.tutor.firebase.FirebaseManager;
import com.aksharadeep.tutor.models.Question;
import com.aksharadeep.tutor.models.QuizResult;
import com.aksharadeep.tutor.utils.GeminiHelper;
import com.aksharadeep.tutor.utils.StreakManager;
import java.util.List;

public class QuizActivity extends AppCompatActivity {
    public static final String EXTRA_CHAPTER_ID   = "chapter_id";
    public static final String EXTRA_CHAPTER_NAME = "chapter_name";
    public static final String EXTRA_SUBJECT_ID   = "subject_id";
    public static final String EXTRA_SUBJECT_NAME = "subject_name";

    private AppDatabase db;
    private FirebaseManager fb;
    private List<Question> questions;
    private int currentIndex = 0, score = 0;
    private String selectedAnswer = null;
    private boolean answered = false;
    private int chapterId, subjectId;
    private String chapterName;

    private TextView tvQuestionNum, tvQuestion, tvTimer, tvExplanation;
    private TextView[] optionViews;
    private Button btnNext, btnReview, btnHint;
    private ProgressBar progressBar;
    private LinearLayout layoutExplanation, layoutHint;
    private TextView tvHintText;
    private CountDownTimer countDownTimer;
    private static final int TIMER_SECONDS = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        chapterId   = getIntent().getIntExtra(EXTRA_CHAPTER_ID, -1);
        subjectId   = getIntent().getIntExtra(EXTRA_SUBJECT_ID, -1);
        chapterName = getIntent().getStringExtra(EXTRA_CHAPTER_NAME);
        db = AppDatabase.getDatabase(this);
        fb = FirebaseManager.getInstance();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quiz: " + chapterName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        initViews();
        loadQuestions();
    }

    private void initViews() {
        tvQuestionNum    = findViewById(R.id.tv_question_num);
        tvQuestion       = findViewById(R.id.tv_question);
        tvTimer          = findViewById(R.id.tv_timer);
        tvExplanation    = findViewById(R.id.tv_explanation);
        layoutExplanation= findViewById(R.id.layout_explanation);
        layoutHint       = findViewById(R.id.layout_ai_hint);
        tvHintText       = findViewById(R.id.tv_ai_hint);
        progressBar      = findViewById(R.id.progress_quiz);
        btnNext          = findViewById(R.id.btn_next);
        btnReview        = findViewById(R.id.btn_review);
        btnHint          = findViewById(R.id.btn_ai_hint);

        optionViews = new TextView[]{
            findViewById(R.id.option_a), findViewById(R.id.option_b),
            findViewById(R.id.option_c), findViewById(R.id.option_d)
        };
        String[] keys = {"A","B","C","D"};
        for (int i = 0; i < optionViews.length; i++) {
            final String key = keys[i];
            optionViews[i].setOnClickListener(v -> selectAnswer(key));
        }
        btnNext.setOnClickListener(v -> {
            if (!answered) { Toast.makeText(this,"Select an answer!",Toast.LENGTH_SHORT).show(); return; }
            moveToNext();
        });
        btnReview.setOnClickListener(v -> toggleExplanation());
        if (btnHint != null) btnHint.setOnClickListener(v -> getAIHint());
    }

    private void loadQuestions() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            questions = db.questionDao().getQuestionsForQuiz(chapterId);
            runOnUiThread(() -> {
                if (questions == null || questions.isEmpty()) {
                    Toast.makeText(this,"No questions available yet.",Toast.LENGTH_LONG).show();
                    finish(); return;
                }
                showQuestion();
            });
        });
    }

    private void showQuestion() {
        if (currentIndex >= questions.size()) { finishQuiz(); return; }
        answered = false; selectedAnswer = null;
        if (layoutExplanation != null) layoutExplanation.setVisibility(View.GONE);
        if (layoutHint != null) layoutHint.setVisibility(View.GONE);
        if (btnReview != null) btnReview.setVisibility(View.GONE);
        if (btnHint != null)   btnHint.setVisibility(View.VISIBLE);

        Question q = questions.get(currentIndex);
        tvQuestionNum.setText("Question " + (currentIndex+1) + " of " + questions.size());
        tvQuestion.setText(q.questionText);
        tvExplanation.setText("💡 " + q.explanation);

        String[] opts  = {q.optionA, q.optionB, q.optionC, q.optionD};
        String[] lbls  = {"A) ","B) ","C) ","D) "};
        for (int i = 0; i < optionViews.length; i++) {
            optionViews[i].setText(lbls[i] + opts[i]);
            optionViews[i].setBackgroundResource(R.drawable.quiz_option_bg);
            optionViews[i].setTextColor(Color.parseColor("#212121"));
            optionViews[i].setEnabled(true);
        }
        progressBar.setMax(questions.size());
        progressBar.setProgress(currentIndex + 1);
        btnNext.setText(currentIndex == questions.size()-1 ? "Submit Quiz" : "Next →");
        startTimer();
    }

    private void startTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        countDownTimer = new CountDownTimer(TIMER_SECONDS * 1000L, 1000) {
            public void onTick(long ms) {
                int s = (int)(ms/1000);
                tvTimer.setText("⏱ " + s + "s");
                tvTimer.setTextColor(s <= 10 ? Color.RED : Color.parseColor("#1A237E"));
            }
            public void onFinish() {
                tvTimer.setText("⏱ 0s");
                if (!answered) { answered=true; highlightAnswers(); if(btnReview!=null) btnReview.setVisibility(View.VISIBLE); Toast.makeText(QuizActivity.this,"Time's up! ⏰",Toast.LENGTH_SHORT).show(); }
            }
        }.start();
    }

    private void selectAnswer(String key) {
        if (answered) return;
        answered = true; selectedAnswer = key;
        if (key.equals(questions.get(currentIndex).correctAnswer)) score++;
        highlightAnswers();
        if (btnReview != null) btnReview.setVisibility(View.VISIBLE);
        if (btnHint   != null) btnHint.setVisibility(View.GONE);
        if (countDownTimer != null) countDownTimer.cancel();
    }

    private void highlightAnswers() {
        Question q = questions.get(currentIndex);
        String[] keys = {"A","B","C","D"};
        for (int i = 0; i < optionViews.length; i++) {
            optionViews[i].setEnabled(false);
            String k = keys[i];
            if (k.equals(q.correctAnswer)) { optionViews[i].setBackgroundResource(R.drawable.quiz_correct_bg); optionViews[i].setTextColor(Color.parseColor("#1B5E20")); }
            else if (k.equals(selectedAnswer)) { optionViews[i].setBackgroundResource(R.drawable.quiz_wrong_bg); optionViews[i].setTextColor(Color.parseColor("#B71C1C")); }
        }
    }

    private void toggleExplanation() {
        if (layoutExplanation == null) return;
        boolean vis = layoutExplanation.getVisibility() == View.VISIBLE;
        layoutExplanation.setVisibility(vis ? View.GONE : View.VISIBLE);
        btnReview.setText(vis ? "Review Answer" : "Hide Explanation");
    }

    private void getAIHint() {
        Question q = questions.get(currentIndex);
        if (layoutHint == null) return;
        layoutHint.setVisibility(View.VISIBLE);
        tvHintText.setText("🤖 Thinking...");
        if (btnHint != null) btnHint.setEnabled(false);

        GeminiHelper.getHintForQuestion(q.questionText,
            q.getOptionByKey(q.correctAnswer), q.explanation,
            new GeminiHelper.HintCallback() {
                public void onHint(String hint) {
                    tvHintText.setText("🤖 AI Hint: " + hint);
                    if (btnHint != null) btnHint.setEnabled(true);
                }
                public void onError(String error) {
                    tvHintText.setText("💡 Think about the key concept carefully. Read the question again!");
                    if (btnHint != null) btnHint.setEnabled(true);
                }
            });
    }

    private void moveToNext() { currentIndex++; if (currentIndex >= questions.size()) finishQuiz(); else showQuestion(); }

    private void finishQuiz() {
        if (countDownTimer != null) countDownTimer.cancel();
        int percent = (score * 100) / questions.size();

        // Record streak
        int streak = StreakManager.recordStudyActivity(this);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.quizResultDao().insert(new QuizResult(chapterId, subjectId, score, questions.size()));
            db.chapterDao().updateQuizScore(chapterId, percent);

            // Firebase: save result + update leaderboard
            fb.saveQuizResultToCloud(chapterName, score, questions.size(), subjectId);
            fb.logQuizCompleted(chapterName, score);

            // Update leaderboard score
            List<com.aksharadeep.tutor.models.Chapter> allChapters = db.chapterDao().getAllChaptersSync();
            int totalScore = 0;
            for (com.aksharadeep.tutor.models.Chapter c : allChapters) totalScore += c.quizScore;
            int finalStreak = streak;
            int finalTotal  = totalScore;

            fb.getUserProfile(new FirebaseManager.OnProfileLoadedListener() {
                public void onLoaded(java.util.Map<String,Object> data) {
                    String name = (String) data.getOrDefault("name","Student");
                    String usn  = (String) data.getOrDefault("usn","");
                    fb.updateLeaderboard(name, usn, finalTotal, finalStreak);
                }
                public void onFailure(String e) {}
            });
        });

        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(ResultActivity.EXTRA_SCORE, score);
        intent.putExtra(ResultActivity.EXTRA_TOTAL, questions.size());
        intent.putExtra(ResultActivity.EXTRA_CHAPTER_NAME, chapterName);
        intent.putExtra(ResultActivity.EXTRA_CHAPTER_ID, chapterId);
        intent.putExtra(ResultActivity.EXTRA_STREAK, streak);
        startActivity(intent);
        finish();
    }

    @Override protected void onDestroy() { super.onDestroy(); if (countDownTimer!=null) countDownTimer.cancel(); }
    @Override public boolean onSupportNavigateUp() { onBackPressed(); return true; }
}
