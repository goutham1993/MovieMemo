package com.entertainment.moviememo.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.entertainment.moviememo.data.entities.WatchedEntry;
import com.entertainment.moviememo.data.entities.MonthCount;
import com.entertainment.moviememo.data.entities.KeyCount;
import com.entertainment.moviememo.data.entities.KeySum;
import com.entertainment.moviememo.data.entities.Genre;
import com.entertainment.moviememo.data.repository.MovieRepository;

import java.util.List;

public class WatchedViewModel extends AndroidViewModel {
    
    private MovieRepository repository;
    private LiveData<List<WatchedEntry>> allWatched;
    private LiveData<Integer> watchedCount;
    private LiveData<Double> averageRating;
    private LiveData<Integer> totalSpendCents;
    private LiveData<List<MonthCount>> moviesPerMonth;
    private LiveData<List<KeyCount>> topTimeOfDay;
    private LiveData<List<KeyCount>> topGenres;
    private LiveData<List<KeySum>> spendByLocation;
    
    public WatchedViewModel(@NonNull Application application) {
        super(application);
        repository = new MovieRepository(application);
        allWatched = repository.getAllWatched();
        watchedCount = repository.getWatchedCount();
        averageRating = repository.getAverageRating();
        totalSpendCents = repository.getTotalSpendCents();
        moviesPerMonth = repository.getMoviesPerMonth();
        topTimeOfDay = repository.getTopTimeOfDay();
        topGenres = repository.getTopGenres();
        spendByLocation = repository.getSpendByLocation();
    }
    
    public LiveData<List<WatchedEntry>> getAllWatched() {
        return allWatched;
    }
    
    public LiveData<Integer> getWatchedCount() {
        return watchedCount;
    }
    
    public LiveData<Double> getAverageRating() {
        return averageRating;
    }
    
    public LiveData<Integer> getTotalSpendCents() {
        return totalSpendCents;
    }
    
    public LiveData<List<MonthCount>> getMoviesPerMonth() {
        return moviesPerMonth;
    }
    
    public LiveData<List<KeyCount>> getTopTimeOfDay() {
        return topTimeOfDay;
    }
    
    public LiveData<List<KeyCount>> getTopGenres() {
        return topGenres;
    }
    
    public LiveData<List<KeySum>> getSpendByLocation() {
        return spendByLocation;
    }
    
    public LiveData<List<WatchedEntry>> searchWatched(String query) {
        return repository.searchWatched(query);
    }
    
    public void insertWatched(WatchedEntry entry) {
        repository.insertWatched(entry);
    }
    
    public void updateWatched(WatchedEntry entry) {
        repository.updateWatched(entry);
    }
    
    public void deleteWatched(WatchedEntry entry) {
        repository.deleteWatched(entry);
    }
    
    public LiveData<List<Genre>> getAllGenres() {
        return repository.getAllGenres();
    }

    public void clearAllWatched() {
        repository.clearAllWatched();
    }
}
