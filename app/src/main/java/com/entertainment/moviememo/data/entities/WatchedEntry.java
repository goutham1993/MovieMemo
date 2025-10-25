package com.entertainment.moviememo.data.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "watched_entries",
        indices = {@Index(value = {"watchedDate"}), @Index(value = {"title"})})
public class WatchedEntry implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    @NonNull
    public String title;
    
    public Integer rating; // 0-10
    
    @NonNull
    public String watchedDate; // ISO yyyy-MM-dd for easy queries
    
    @NonNull
    public String locationType; // enum name
    
    public String locationNotes;
    
    public String companions; // "Alice,Bob"
    
    public Integer spendCents;
    
    public Integer durationMin;
    
    @NonNull
    public String timeOfDay; // enum name
    
    public String genre;
    
    public String notes;
    
    public String posterUri;
    
    public String language; // Language code (en, te, hi, etc.)
    
    public String theaterName; // Theater name when location is THEATER
    
    public String city; // City when location is THEATER
    
    public WatchedEntry(@NonNull String title, @NonNull String watchedDate, 
                       @NonNull String locationType, @NonNull String timeOfDay) {
        this.title = title;
        this.watchedDate = watchedDate;
        this.locationType = locationType;
        this.timeOfDay = timeOfDay;
    }
}
