package com.aksharadeep.tutor.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aksharadeep.tutor.models.Subject;

import java.util.List;

@Dao
public interface SubjectDao {
    @Insert
    void insertAll(Subject... subjects);

    @Query("SELECT * FROM subjects ORDER BY id ASC")
    LiveData<List<Subject>> getAllSubjects();

    @Query("SELECT * FROM subjects ORDER BY id ASC")
    List<Subject> getAllSubjectsSync();

    @Query("SELECT COUNT(*) FROM subjects")
    int getCount();

    @Update
    void update(Subject subject);
}
