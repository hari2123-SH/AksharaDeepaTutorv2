package com.aksharadeep.tutor.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aksharadeep.tutor.models.Chapter;

import java.util.List;

@Dao
public interface ChapterDao {
    @Insert
    void insertAll(Chapter... chapters);

    @Query("SELECT * FROM chapters WHERE subjectId = :subjectId ORDER BY id ASC")
    LiveData<List<Chapter>> getChaptersBySubject(int subjectId);

    @Query("SELECT * FROM chapters WHERE subjectId = :subjectId ORDER BY id ASC")
    List<Chapter> getChaptersBySubjectSync(int subjectId);

    @Query("SELECT * FROM chapters ORDER BY id ASC")
    List<Chapter> getAllChaptersSync();

    @Query("SELECT * FROM chapters WHERE id = :chapterId")
    Chapter getChapterById(int chapterId);

    @Query("SELECT COUNT(*) FROM chapters WHERE subjectId = :subjectId")
    int getTotalChapters(int subjectId);

    @Query("SELECT COUNT(*) FROM chapters WHERE subjectId = :subjectId AND isCompleted = 1")
    int getCompletedChapters(int subjectId);

    @Query("SELECT AVG(quizScore) FROM chapters WHERE subjectId = :subjectId AND quizAttempts > 0")
    float getAverageScore(int subjectId);

    @Query("SELECT COUNT(*) FROM chapters")
    int getTotalCount();

    @Update
    void update(Chapter chapter);

    @Query("UPDATE chapters SET isCompleted = :completed, completedDate = :date WHERE id = :chapterId")
    void updateCompletion(int chapterId, boolean completed, long date);

    @Query("UPDATE chapters SET quizScore = :score, quizAttempts = quizAttempts + 1 WHERE id = :chapterId")
    void updateQuizScore(int chapterId, int score);
}
