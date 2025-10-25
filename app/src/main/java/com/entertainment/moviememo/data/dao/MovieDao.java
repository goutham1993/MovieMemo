package com.entertainment.moviememo.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.entertainment.moviememo.data.entities.WatchedEntry;
import com.entertainment.moviememo.data.entities.WatchlistItem;
import com.entertainment.moviememo.data.entities.Genre;
import com.entertainment.moviememo.data.entities.MonthCount;
import com.entertainment.moviememo.data.entities.KeyCount;
import com.entertainment.moviememo.data.entities.KeySum;

import java.util.List;

@Dao
public interface MovieDao {
    
    // Watched Entries
    @Insert
    long insertWatched(WatchedEntry entry);
    
    @Update
    int updateWatched(WatchedEntry entry);
    
    @Delete
    int deleteWatched(WatchedEntry entry);
    
    @Query("SELECT * FROM watched_entries ORDER BY watchedDate DESC, id DESC")
    LiveData<List<WatchedEntry>> listWatched();
    
    @Query("SELECT * FROM watched_entries WHERE title LIKE :query OR notes LIKE :query ORDER BY watchedDate DESC")
    LiveData<List<WatchedEntry>> searchWatched(String query);
    
    @Query("SELECT COUNT(*) FROM watched_entries")
    LiveData<Integer> countWatched();
    
    @Query("SELECT AVG(rating) FROM watched_entries WHERE rating IS NOT NULL")
    LiveData<Double> avgRating();
    
    @Query("SELECT IFNULL(SUM(spendCents),0) FROM watched_entries")
    LiveData<Integer> totalSpendCents();
    
    @Query("SELECT IFNULL(SUM(durationMin),0) FROM watched_entries WHERE durationMin IS NOT NULL")
    LiveData<Integer> totalDurationMinutes();
    
    @Query("SELECT substr(watchedDate,1,7) AS ym, COUNT(*) AS cnt FROM watched_entries GROUP BY ym ORDER BY ym DESC")
    LiveData<List<MonthCount>> moviesPerMonth();
    
    @Query("SELECT timeOfDay AS category, COUNT(*) AS cnt FROM watched_entries GROUP BY timeOfDay ORDER BY cnt DESC")
    LiveData<List<KeyCount>> topTimeOfDay();
    
    @Query("SELECT genre AS category, COUNT(*) AS cnt FROM watched_entries WHERE genre IS NOT NULL GROUP BY genre ORDER BY cnt DESC")
    LiveData<List<KeyCount>> topGenres();
    
    @Query("SELECT locationType AS category, IFNULL(SUM(spendCents),0) AS total FROM watched_entries GROUP BY locationType")
    LiveData<List<KeySum>> spendByLocation();
    
    // New statistics queries
    @Query("SELECT locationType AS category, COUNT(*) AS cnt FROM watched_entries GROUP BY locationType ORDER BY cnt DESC")
    LiveData<List<KeyCount>> moviesByLocation();
    
    @Query("SELECT language AS category, COUNT(*) AS cnt FROM watched_entries WHERE language IS NOT NULL GROUP BY language ORDER BY cnt DESC")
    LiveData<List<KeyCount>> moviesByLanguage();
    
    @Query("SELECT companions AS category, COUNT(*) AS cnt FROM watched_entries WHERE companions IS NOT NULL AND companions != '' GROUP BY companions ORDER BY cnt DESC")
    LiveData<List<KeyCount>> moviesByCompanion();
    
    // Watchlist Items
    @Insert
    long insertWatchlist(WatchlistItem item);
    
    @Update
    int updateWatchlist(WatchlistItem item);
    
    @Delete
    int deleteWatchlist(WatchlistItem item);
    
    @Query("SELECT * FROM watchlist_items ORDER BY priority ASC, createdAt DESC")
    LiveData<List<WatchlistItem>> listWatchlist();
    
    @Query("SELECT * FROM watchlist_items WHERE title LIKE :query OR notes LIKE :query ORDER BY priority ASC, createdAt DESC")
    LiveData<List<WatchlistItem>> searchWatchlist(String query);
    
    // Genres
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addGenre(Genre genre);
    
    @Query("SELECT * FROM genres ORDER BY name ASC")
    LiveData<List<Genre>> listGenres();
    
    @Query("SELECT COUNT(*) FROM genres")
    LiveData<Integer> countGenres();

    @Query("DELETE FROM watched_entries")
    void clearAllWatched();

    @Query("DELETE FROM watchlist_items")
    void clearAllWatchlist();
}
