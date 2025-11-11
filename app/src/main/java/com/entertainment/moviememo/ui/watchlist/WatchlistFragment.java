package com.entertainment.moviememo.ui.watchlist;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.entertainment.moviememo.databinding.FragmentWatchlistBinding;
import com.entertainment.moviememo.viewmodels.WatchlistViewModel;
import com.entertainment.moviememo.ui.adapters.WatchlistAdapter;
import com.entertainment.moviememo.data.enums.WhereToWatch;
import com.entertainment.moviememo.utils.NotificationHelper;

import java.util.ArrayList;
import java.util.List;

public class WatchlistFragment extends Fragment {

    private FragmentWatchlistBinding binding;
    private WatchlistViewModel viewModel;
    private WatchlistAdapter adapter;
    private String currentFilter = "ALL"; // ALL, THEATER, OTT_STREAMING

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWatchlistBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupViewModel();
        setupRecyclerView();
        setupSearch();
        setupFilters();
        observeData();
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(WatchlistViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new WatchlistAdapter();
        binding.recyclerViewWatchlist.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewWatchlist.setAdapter(adapter);
        
        // Improve touch handling
        binding.recyclerViewWatchlist.setNestedScrollingEnabled(false);
        binding.recyclerViewWatchlist.setHasFixedSize(true);
        
        // Set up click listeners
        adapter.setOnItemClickListener(new WatchlistAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(com.entertainment.moviememo.data.entities.WatchlistItem item) {
                // Edit watchlist item
                editWatchlistItem(item);
            }

            @Override
            public void onItemLongClick(com.entertainment.moviememo.data.entities.WatchlistItem item) {
                showDeleteConfirmation(item);
            }
        });
        
        // Swipe actions removed to prevent conflicts with tab swipes
        // Users can still delete items via long-click
    }

    private void setupSearch() {
        binding.layoutSearch.editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    // When search is cleared, respect the current filter
                    if (currentFilter.equals("ALL")) {
                        observeData();
                    } else {
                        filterByWhereToWatch(currentFilter);
                    }
                } else {
                    searchWatchlist(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilters() {
        // Set initial state - All button selected
        updateFilterButtons("ALL");
        
        binding.buttonFilterAll.setOnClickListener(v -> {
            currentFilter = "ALL";
            updateFilterButtons("ALL");
            observeData();
        });
        
        binding.buttonFilterTheater.setOnClickListener(v -> {
            currentFilter = "THEATER";
            updateFilterButtons("THEATER");
            filterByWhereToWatch(WhereToWatch.THEATER.name());
        });
        
        binding.buttonFilterOtt.setOnClickListener(v -> {
            currentFilter = "OTT_STREAMING";
            updateFilterButtons("OTT_STREAMING");
            filterByWhereToWatch(WhereToWatch.OTT_STREAMING.name());
        });
    }
    
    private void updateFilterButtons(String selectedFilter) {
        // Reset all buttons to unselected state
        binding.buttonFilterAll.setSelected(false);
        binding.buttonFilterTheater.setSelected(false);
        binding.buttonFilterOtt.setSelected(false);
        
        // Set selected button
        switch (selectedFilter) {
            case "ALL":
                binding.buttonFilterAll.setSelected(true);
                break;
            case "THEATER":
                binding.buttonFilterTheater.setSelected(true);
                break;
            case "OTT_STREAMING":
                binding.buttonFilterOtt.setSelected(true);
                break;
        }
    }
    
    private void filterByWhereToWatch(String whereToWatch) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerViewWatchlist.setVisibility(View.GONE);
        binding.textEmptyState.setVisibility(View.GONE);
        
        viewModel.getAllWatchlist().observe(getViewLifecycleOwner(), watchlistItems -> {
            binding.progressBar.setVisibility(View.GONE);
            
            if (watchlistItems != null) {
                List<com.entertainment.moviememo.data.entities.WatchlistItem> filteredItems = new ArrayList<>();
                for (com.entertainment.moviememo.data.entities.WatchlistItem item : watchlistItems) {
                    if (item.whereToWatch != null && item.whereToWatch.equals(whereToWatch)) {
                        filteredItems.add(item);
                    }
                }
                
                if (!filteredItems.isEmpty()) {
                    adapter.submitList(filteredItems);
                    binding.textEmptyState.setVisibility(View.GONE);
                    binding.recyclerViewWatchlist.setVisibility(View.VISIBLE);
                } else {
                    adapter.submitList(new ArrayList<>());
                    String filterName = whereToWatch.equals(WhereToWatch.THEATER.name()) ? "theater" : "OTT/Streaming";
                    binding.textEmptyState.setText("No movies in watchlist for " + filterName);
                    binding.textEmptyState.setVisibility(View.VISIBLE);
                    binding.recyclerViewWatchlist.setVisibility(View.GONE);
                }
            }
        });
    }

    private void searchWatchlist(String query) {
        viewModel.getAllWatchlist().observe(getViewLifecycleOwner(), watchlistItems -> {
            if (watchlistItems != null) {
                List<com.entertainment.moviememo.data.entities.WatchlistItem> filteredItems = new ArrayList<>();
                for (com.entertainment.moviememo.data.entities.WatchlistItem item : watchlistItems) {
                    // Check if item matches search query
                    boolean matchesSearch = item.title.toLowerCase().contains(query.toLowerCase()) ||
                        (item.notes != null && item.notes.toLowerCase().contains(query.toLowerCase()));
                    
                    // Check if item matches current filter
                    boolean matchesFilter = currentFilter.equals("ALL") ||
                        (item.whereToWatch != null && item.whereToWatch.equals(currentFilter));
                    
                    if (matchesSearch && matchesFilter) {
                        filteredItems.add(item);
                    }
                }
                
                if (!filteredItems.isEmpty()) {
                    adapter.submitList(filteredItems);
                    binding.textEmptyState.setVisibility(View.GONE);
                    binding.recyclerViewWatchlist.setVisibility(View.VISIBLE);
                } else {
                    binding.textEmptyState.setText("No watchlist items found matching \"" + query + "\"");
                    binding.textEmptyState.setVisibility(View.VISIBLE);
                    binding.recyclerViewWatchlist.setVisibility(View.GONE);
                }
            }
        });
    }

    private void observeData() {
        // Show loading state initially
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerViewWatchlist.setVisibility(View.GONE);
        binding.textEmptyState.setVisibility(View.GONE);
        
        viewModel.getAllWatchlist().observe(getViewLifecycleOwner(), watchlistItems -> {
            binding.progressBar.setVisibility(View.GONE);
            
            if (watchlistItems != null && !watchlistItems.isEmpty()) {
                adapter.submitList(watchlistItems);
                binding.textEmptyState.setVisibility(View.GONE);
                binding.recyclerViewWatchlist.setVisibility(View.VISIBLE);
            } else {
                adapter.submitList(new ArrayList<>());
                binding.textEmptyState.setVisibility(View.VISIBLE);
                binding.recyclerViewWatchlist.setVisibility(View.GONE);
            }
        });
    }

    private void convertToWatched(com.entertainment.moviememo.data.entities.WatchlistItem item) {
        // Create a new watched entry from the watchlist item
        com.entertainment.moviememo.data.entities.WatchedEntry entry = new com.entertainment.moviememo.data.entities.WatchedEntry(
            item.title,
            new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date()),
            "HOME", // Default location
            "EVENING" // Default time
        );
        
        if (item.notes != null) {
            entry.notes = item.notes;
        }
        
        // Insert into watched entries
        com.entertainment.moviememo.viewmodels.WatchedViewModel watchedViewModel = new androidx.lifecycle.ViewModelProvider(this).get(com.entertainment.moviememo.viewmodels.WatchedViewModel.class);
        watchedViewModel.insertWatched(entry);
        
        // Remove from watchlist
        viewModel.deleteWatchlist(item);
        
        Toast.makeText(getContext(), "Moved to watched movies!", Toast.LENGTH_SHORT).show();
    }

    private void editWatchlistItem(com.entertainment.moviememo.data.entities.WatchlistItem item) {
        EditWatchlistFragment fragment = EditWatchlistFragment.newInstance(item);
        getParentFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void showDeleteConfirmation(com.entertainment.moviememo.data.entities.WatchlistItem item) {
        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Delete from Watchlist")
                .setMessage("Are you sure you want to remove \"" + item.title + "\" from your watchlist?")
                .setPositiveButton("Delete", (dialog, which) -> deleteFromWatchlist(item))
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Refresh the adapter to undo the swipe animation
                    adapter.notifyDataSetChanged();
                })
                .show();
    }

    private void deleteFromWatchlist(com.entertainment.moviememo.data.entities.WatchlistItem item) {
        viewModel.deleteWatchlist(item);
        Toast.makeText(getContext(), "Removed from watchlist!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
