package com.aksharadeep.tutor.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "questions",
        foreignKeys = @ForeignKey(entity = Chapter.class,
                parentColumns = "id",
                childColumns = "chapterId",
                onDelete = ForeignKey.CASCADE))
public class Question {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int chapterId;
    public String questionText;
    public String optionA;
    public String optionB;
    public String optionC;
    public String optionD;
    public String correctAnswer; // "A", "B", "C", or "D"
    public String explanation;

    public Question(int chapterId, String questionText,
                    String optionA, String optionB,
                    String optionC, String optionD,
                    String correctAnswer, String explanation) {
        this.chapterId = chapterId;
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
    }

    public String getOptionByKey(String key) {
        switch (key) {
            case "A": return optionA;
            case "B": return optionB;
            case "C": return optionC;
            case "D": return optionD;
            default: return "";
        }
    }
}
