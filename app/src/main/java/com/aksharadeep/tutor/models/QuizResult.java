package com.aksharadeep.tutor.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "quiz_results")
public class QuizResult {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int chapterId;
    public int subjectId;
    public int score;
    public int totalQuestions;
    public long timestamp;

    public QuizResult(int chapterId, int subjectId, int score, int totalQuestions) {
        this.chapterId = chapterId;
        this.subjectId = subjectId;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.timestamp = System.currentTimeMillis();
    }

    public int getPercentage() {
        if (totalQuestions == 0) return 0;
        return (score * 100) / totalQuestions;
    }
}
