package com.entertainment.moviememo.data.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "genres")
public class Genre {
    @PrimaryKey
    @NonNull
    public String name;
    
    public Long createdAt;
    
    public Genre(@NonNull String name) {
        this.name = name;
        this.createdAt = System.currentTimeMillis();
    }
}
