package com.aksharadeep.tutor.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.aksharadeep.tutor.models.QuizResult;

import java.util.List;

@Dao
public interface QuizResultDao {
    @Insert
    void insert(QuizResult result);

    @Query("SELECT * FROM quiz_results WHERE chapterId = :chapterId ORDER BY timestamp DESC")
    List<QuizResult> getResultsForChapter(int chapterId);

    @Query("SELECT AVG(score * 100.0 / totalQuestions) FROM quiz_results WHERE subjectId = :subjectId")
    float getSubjectAverage(int subjectId);
}
