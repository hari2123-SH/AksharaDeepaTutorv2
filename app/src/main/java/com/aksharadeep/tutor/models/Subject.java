package com.aksharadeep.tutor.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "subjects")
public class Subject {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public String colorCode;
    public int iconResId;

    public Subject(String name, String colorCode) {
        this.name = name;
        this.colorCode = colorCode;
    }
}
