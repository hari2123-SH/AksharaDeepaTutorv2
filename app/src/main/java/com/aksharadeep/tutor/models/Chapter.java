package com.aksharadeep.tutor.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "chapters",
        foreignKeys = @ForeignKey(entity = Subject.class,
                parentColumns = "id",
                childColumns = "subjectId",
                onDelete = ForeignKey.CASCADE))
public class Chapter {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int subjectId;
    public String name;
    public boolean isCompleted;
    public int quizScore;       // 0-100 percentage
    public int quizAttempts;
    public long completedDate;

    public Chapter(int subjectId, String name) {
        this.subjectId = subjectId;
        this.name = name;
        this.isCompleted = false;
        this.quizScore = 0;
        this.quizAttempts = 0;
    }

    public int getStrengthLevel() {
        if (quizAttempts == 0) return 0;
        return quizScore;
    }

    public String getStrengthLabel() {
        int strength = getStrengthLevel();
        if (quizAttempts == 0) return "Not Attempted";
        if (strength >= 80) return "Strong";
        if (strength >= 60) return "Good";
        if (strength >= 40) return "Needs Work";
        return "Gap Area";
    }
}
