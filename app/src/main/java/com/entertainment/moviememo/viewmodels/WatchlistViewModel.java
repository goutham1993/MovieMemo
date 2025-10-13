package com.entertainment.moviememo.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.entertainment.moviememo.data.entities.WatchlistItem;
import com.entertainment.moviememo.data.repository.MovieRepository;

import java.util.List;

public class WatchlistViewModel extends AndroidViewModel {
    
    private MovieRepository repository;
    private LiveData<List<WatchlistItem>> allWatchlist;
    
    public WatchlistViewModel(@NonNull Application application) {
        super(application);
        repository = new MovieRepository(application);
        allWatchlist = repository.getAllWatchlist();
    }
    
    public LiveData<List<WatchlistItem>> getAllWatchlist() {
        return allWatchlist;
    }
    
    public LiveData<List<WatchlistItem>> searchWatchlist(String query) {
        return repository.searchWatchlist(query);
    }
    
    public void insertWatchlist(WatchlistItem item) {
        repository.insertWatchlist(item);
    }
    
    public void updateWatchlist(WatchlistItem item) {
        repository.updateWatchlist(item);
    }
    
    public void deleteWatchlist(WatchlistItem item) {
        repository.deleteWatchlist(item);
    }

    public void clearAllWatchlist() {
        repository.clearAllWatchlist();
    }
}
