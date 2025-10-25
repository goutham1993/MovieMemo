package com.entertainment.moviememo.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.entertainment.moviememo.data.entities.MonthCount;
import com.entertainment.moviememo.data.entities.KeyCount;
import com.entertainment.moviememo.data.entities.KeySum;
import com.entertainment.moviememo.data.repository.MovieRepository;

import java.util.List;

public class StatsViewModel extends AndroidViewModel {
    
    private MovieRepository repository;
    private LiveData<Integer> watchedCount;
    private LiveData<Double> averageRating;
    private LiveData<Integer> totalSpendCents;
    private LiveData<Integer> totalDurationMinutes;
    private LiveData<List<MonthCount>> moviesPerMonth;
    private LiveData<List<KeyCount>> topTimeOfDay;
    private LiveData<List<KeyCount>> topGenres;
    private LiveData<List<KeySum>> spendByLocation;
    private LiveData<List<KeyCount>> moviesByLocation;
    private LiveData<List<KeyCount>> moviesByLanguage;
    private LiveData<List<KeyCount>> moviesByCompanion;
    private LiveData<Integer> thisMonthCount;
    private LiveData<Integer> thisMonthSpendCents;
    private LiveData<Integer> avgSpendCents;
    private LiveData<Integer> thisMonthAvgSpendCents;
    
    public StatsViewModel(@NonNull Application application) {
        super(application);
        repository = new MovieRepository(application);
        watchedCount = repository.getWatchedCount();
        averageRating = repository.getAverageRating();
        totalSpendCents = repository.getTotalSpendCents();
        totalDurationMinutes = repository.getTotalDurationMinutes();
        moviesPerMonth = repository.getMoviesPerMonth();
        topTimeOfDay = repository.getTopTimeOfDay();
        topGenres = repository.getTopGenres();
        spendByLocation = repository.getSpendByLocation();
        moviesByLocation = repository.getMoviesByLocation();
        moviesByLanguage = repository.getMoviesByLanguage();
        moviesByCompanion = repository.getMoviesByCompanion();
        thisMonthCount = repository.getThisMonthCount();
        thisMonthSpendCents = repository.getThisMonthSpendCents();
        avgSpendCents = repository.getAvgSpendCents();
        thisMonthAvgSpendCents = repository.getThisMonthAvgSpendCents();
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
    
    public LiveData<Integer> getTotalDurationMinutes() {
        return totalDurationMinutes;
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
    
    public LiveData<List<KeyCount>> getMoviesByLocation() {
        return moviesByLocation;
    }
    
    public LiveData<List<KeyCount>> getMoviesByLanguage() {
        return moviesByLanguage;
    }
    
    public LiveData<List<KeyCount>> getMoviesByCompanion() {
        return moviesByCompanion;
    }
    
    public LiveData<Integer> getThisMonthCount() {
        return thisMonthCount;
    }
    
    public LiveData<Integer> getThisMonthSpendCents() {
        return thisMonthSpendCents;
    }
    
    public LiveData<Integer> getAvgSpendCents() {
        return avgSpendCents;
    }
    
    public LiveData<Integer> getThisMonthAvgSpendCents() {
        return thisMonthAvgSpendCents;
    }
}
