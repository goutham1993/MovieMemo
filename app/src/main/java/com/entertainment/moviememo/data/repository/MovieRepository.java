package com.entertainment.moviememo.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.entertainment.moviememo.data.database.AppDatabase;
import com.entertainment.moviememo.data.dao.MovieDao;
import com.entertainment.moviememo.data.entities.WatchedEntry;
import com.entertainment.moviememo.data.entities.WatchlistItem;
import com.entertainment.moviememo.data.entities.Genre;
import com.entertainment.moviememo.data.entities.MonthCount;
import com.entertainment.moviememo.data.entities.KeyCount;
import com.entertainment.moviememo.data.entities.KeySum;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MovieRepository {
    
    private MovieDao movieDao;
    private ExecutorService executor;
    
    public MovieRepository(Context context) {
        AppDatabase database = AppDatabase.getDatabase(context);
        this.movieDao = database.movieDao();
        this.executor = Executors.newFixedThreadPool(4);
    }
    
    // Watched Entries
    public void insertWatched(WatchedEntry entry) {
        executor.execute(() -> movieDao.insertWatched(entry));
    }
    
    public void updateWatched(WatchedEntry entry) {
        executor.execute(() -> movieDao.updateWatched(entry));
    }
    
    public void deleteWatched(WatchedEntry entry) {
        executor.execute(() -> movieDao.deleteWatched(entry));
    }
    
    public LiveData<List<WatchedEntry>> getAllWatched() {
        return movieDao.listWatched();
    }
    
    public LiveData<List<WatchedEntry>> searchWatched(String query) {
        return movieDao.searchWatched("%" + query + "%");
    }
    
    public LiveData<List<WatchedEntry>> getWatchedByLocation(String locationType) {
        return movieDao.getWatchedByLocation(locationType);
    }
    
    // Sorting methods
    public LiveData<List<WatchedEntry>> getWatchedByDateDesc() {
        return movieDao.listWatchedByDateDesc();
    }
    
    public LiveData<List<WatchedEntry>> getWatchedByDateAsc() {
        return movieDao.listWatchedByDateAsc();
    }
    
    public LiveData<List<WatchedEntry>> getWatchedByRatingDesc() {
        return movieDao.listWatchedByRatingDesc();
    }
    
    public LiveData<List<WatchedEntry>> getWatchedByRatingAsc() {
        return movieDao.listWatchedByRatingAsc();
    }
    
    public LiveData<List<WatchedEntry>> getWatchedBySpendDesc() {
        return movieDao.listWatchedBySpendDesc();
    }
    
    public LiveData<List<WatchedEntry>> getWatchedBySpendAsc() {
        return movieDao.listWatchedBySpendAsc();
    }
    
    public LiveData<Integer> getWatchedCount() {
        return movieDao.countWatched();
    }
    
    public LiveData<Double> getAverageRating() {
        return movieDao.avgRating();
    }
    
    public LiveData<Integer> getTotalSpendCents() {
        return movieDao.totalSpendCents();
    }
    
    public LiveData<Integer> getTotalDurationMinutes() {
        return movieDao.totalDurationMinutes();
    }
    
    public LiveData<List<MonthCount>> getMoviesPerMonth() {
        return movieDao.moviesPerMonth();
    }
    
    public LiveData<List<KeyCount>> getTopTimeOfDay() {
        return movieDao.topTimeOfDay();
    }
    
    public LiveData<List<KeyCount>> getTopGenres() {
        return movieDao.topGenres();
    }
    
    public LiveData<List<KeySum>> getSpendByLocation() {
        return movieDao.spendByLocation();
    }
    
    // New statistics methods
    public LiveData<List<KeyCount>> getMoviesByLocation() {
        return movieDao.moviesByLocation();
    }
    
    public LiveData<List<KeyCount>> getMoviesByLanguage() {
        return movieDao.moviesByLanguage();
    }
    
    public LiveData<List<KeyCount>> getMoviesByCompanion() {
        return movieDao.moviesByCompanion();
    }
    
    public LiveData<Integer> getThisMonthCount() {
        return movieDao.thisMonthCount();
    }
    
    public LiveData<Integer> getThisMonthSpendCents() {
        return movieDao.thisMonthSpendCents();
    }
    
    public LiveData<Integer> getAvgSpendCents() {
        return movieDao.avgSpendCents();
    }
    
    public LiveData<Integer> getThisMonthAvgSpendCents() {
        return movieDao.thisMonthAvgSpendCents();
    }
    
    public LiveData<Integer> getWeekdayCount() {
        return movieDao.weekdayCount();
    }
    
    public LiveData<Integer> getWeekendCount() {
        return movieDao.weekendCount();
    }
    
    // Watchlist Items
    public void insertWatchlist(WatchlistItem item) {
        executor.execute(() -> movieDao.insertWatchlist(item));
    }
    
    public void updateWatchlist(WatchlistItem item) {
        executor.execute(() -> movieDao.updateWatchlist(item));
    }
    
    public void deleteWatchlist(WatchlistItem item) {
        executor.execute(() -> movieDao.deleteWatchlist(item));
    }
    
    public LiveData<List<WatchlistItem>> getAllWatchlist() {
        return movieDao.listWatchlist();
    }
    
    public LiveData<List<WatchlistItem>> searchWatchlist(String query) {
        return movieDao.searchWatchlist("%" + query + "%");
    }
    
    // Genres
    public void addGenre(Genre genre) {
        executor.execute(() -> movieDao.addGenre(genre));
    }
    
    public LiveData<List<Genre>> getAllGenres() {
        return movieDao.listGenres();
    }
    
    public LiveData<Integer> getGenreCount() {
        return movieDao.countGenres();
    }

    public void clearAllWatched() {
        executor.execute(() -> movieDao.clearAllWatched());
    }

    public void clearAllWatchlist() {
        executor.execute(() -> movieDao.clearAllWatchlist());
    }
}
