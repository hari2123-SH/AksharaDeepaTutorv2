package com.aksharadeep.tutor.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.aksharadeep.tutor.models.Question;

import java.util.List;

@Dao
public interface QuestionDao {
    @Insert
    void insertAll(Question... questions);

    @Query("SELECT * FROM questions WHERE chapterId = :chapterId ORDER BY RANDOM() LIMIT 5")
    List<Question> getQuestionsForQuiz(int chapterId);

    @Query("SELECT COUNT(*) FROM questions")
    int getTotalCount();

    @Query("SELECT COUNT(*) FROM questions WHERE chapterId = :chapterId")
    int getCountForChapter(int chapterId);
}
