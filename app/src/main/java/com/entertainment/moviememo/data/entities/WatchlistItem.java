package com.entertainment.moviememo.data.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "watchlist_items",
        indices = {@Index(value = {"title"}, unique = false)})
public class WatchlistItem implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    @NonNull
    public String title;
    
    public String notes;
    
    public Integer priority; // 1..3
    
    public Long createdAt;
    
    public Long targetDate;
    
    public String language; // Language code (en, te, hi, etc.)
    
    public String whereToWatch; // WhereToWatch enum name (THEATER, OTT_STREAMING, OTHER)
    
    public Long releaseDate; // Release date in milliseconds (for Theater only)
    
    public WatchlistItem(@NonNull String title) {
        this.title = title;
        this.createdAt = System.currentTimeMillis();
        this.priority = 2; // Default to medium priority
    }
}
