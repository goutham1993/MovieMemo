package com.entertainment.moviememo.ui.watched;

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

import com.entertainment.moviememo.data.entities.WatchedEntry;
import com.entertainment.moviememo.databinding.FragmentWatchedListBinding;
import com.entertainment.moviememo.viewmodels.WatchedViewModel;
import com.entertainment.moviememo.data.enums.LocationType;
import com.entertainment.moviememo.ui.adapters.WatchedEntryAdapter;

import java.util.ArrayList;
import java.util.List;

public class WatchedListFragment extends Fragment {

    private FragmentWatchedListBinding binding;
    private WatchedViewModel viewModel;
    private WatchedEntryAdapter adapter;
    private String currentFilter = "ALL"; // ALL, HOME, THEATER
    private String currentSort = "DATE_DESC"; // DATE_DESC, DATE_ASC, RATING_DESC, RATING_ASC, SPEND_DESC, SPEND_ASC

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWatchedListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupViewModel();
        setupRecyclerView();
        setupSearch();
        setupFilters();
        setupSorting();
        observeData();
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(WatchedViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new WatchedEntryAdapter();
        binding.recyclerViewWatched.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewWatched.setAdapter(adapter);
        
        // Improve touch handling
        binding.recyclerViewWatched.setNestedScrollingEnabled(false);
        binding.recyclerViewWatched.setHasFixedSize(true);
        
        // Set up click listeners
        adapter.setOnItemClickListener(new WatchedEntryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(WatchedEntry entry) {
                editMovie(entry);
            }

            @Override
            public void onItemLongClick(WatchedEntry entry) {
                showDeleteConfirmation(entry);
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
                    observeData(); // Show all data
                } else {
                    searchMovies(query);
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
        
        binding.buttonFilterHome.setOnClickListener(v -> {
            currentFilter = "HOME";
            updateFilterButtons("HOME");
            filterByLocation(LocationType.HOME.name());
        });
        
        binding.buttonFilterTheater.setOnClickListener(v -> {
            currentFilter = "THEATER";
            updateFilterButtons("THEATER");
            filterByLocation(LocationType.THEATER.name());
        });
    }
    
    private void updateFilterButtons(String selectedFilter) {
        // Reset all buttons to unselected state
        binding.buttonFilterAll.setSelected(false);
        binding.buttonFilterHome.setSelected(false);
        binding.buttonFilterTheater.setSelected(false);
        
        // Set selected button
        switch (selectedFilter) {
            case "ALL":
                binding.buttonFilterAll.setSelected(true);
                break;
            case "HOME":
                binding.buttonFilterHome.setSelected(true);
                break;
            case "THEATER":
                binding.buttonFilterTheater.setSelected(true);
                break;
        }
    }
    
    private void setupSorting() {
        // Create sort options
        String[] sortOptions = {
            "📅 Date (Newest)",
            "📅 Date (Oldest)", 
            "⭐ Rating (Highest)",
            "⭐ Rating (Lowest)",
            "💰 Amount (Highest)",
            "💰 Amount (Lowest)"
        };
        
        // Setup dropdown adapter
        android.widget.ArrayAdapter<String> sortAdapter = new android.widget.ArrayAdapter<>(
            getContext(), 
            android.R.layout.simple_dropdown_item_1line, 
            sortOptions
        );
        binding.dropdownSort.setAdapter(sortAdapter);
        
        // Handle sort selection
        binding.dropdownSort.setOnItemClickListener((parent, view, position, id) -> {
            String selectedSort = sortOptions[position];
            binding.dropdownSort.setText(selectedSort, false);
            
            // Update current sort based on selection
            switch (position) {
                case 0: currentSort = "DATE_DESC"; break;
                case 1: currentSort = "DATE_ASC"; break;
                case 2: currentSort = "RATING_DESC"; break;
                case 3: currentSort = "RATING_ASC"; break;
                case 4: currentSort = "SPEND_DESC"; break;
                case 5: currentSort = "SPEND_ASC"; break;
            }
            
            // Apply sorting
            applySorting();
        });
    }
    
    private void applySorting() {
        if (currentFilter.equals("ALL")) {
            observeData();
        } else {
            // Reapply current filter with new sorting
            if (currentFilter.equals("HOME")) {
                filterByLocation(LocationType.HOME.name());
            } else if (currentFilter.equals("THEATER")) {
                filterByLocation(LocationType.THEATER.name());
            }
        }
    }
    
    private void filterByLocation(String locationType) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerViewWatched.setVisibility(View.GONE);
        binding.textEmptyState.setVisibility(View.GONE);
        
        viewModel.getWatchedByLocation(locationType).observe(getViewLifecycleOwner(), watchedEntries -> {
            binding.progressBar.setVisibility(View.GONE);
            
            if (watchedEntries != null && !watchedEntries.isEmpty()) {
                adapter.submitList(watchedEntries);
                binding.textEmptyState.setVisibility(View.GONE);
                binding.recyclerViewWatched.setVisibility(View.VISIBLE);
            } else {
                adapter.submitList(new ArrayList<>());
                binding.textEmptyState.setText("No movies watched at " + locationType.toLowerCase());
                binding.textEmptyState.setVisibility(View.VISIBLE);
                binding.recyclerViewWatched.setVisibility(View.GONE);
            }
        });
    }

    private void searchMovies(String query) {
        // If there's an active filter, we need to filter the search results
        if (!currentFilter.equals("ALL")) {
            // For now, just show all results and let user clear search to see filtered results
            // This could be enhanced to search within filtered results
            viewModel.searchWatched(query).observe(getViewLifecycleOwner(), watchedEntries -> {
                if (watchedEntries != null && !watchedEntries.isEmpty()) {
                    // Filter the search results by current location filter
                    List<WatchedEntry> filteredEntries = new ArrayList<>();
                    for (WatchedEntry entry : watchedEntries) {
                        if (currentFilter.equals("HOME") && entry.locationType.equals("HOME")) {
                            filteredEntries.add(entry);
                        } else if (currentFilter.equals("THEATER") && entry.locationType.equals("THEATER")) {
                            filteredEntries.add(entry);
                        }
                    }
                    
                    if (!filteredEntries.isEmpty()) {
                        adapter.submitList(filteredEntries);
                        binding.textEmptyState.setVisibility(View.GONE);
                        binding.recyclerViewWatched.setVisibility(View.VISIBLE);
                    } else {
                        binding.textEmptyState.setText("No movies found matching \"" + query + "\" in " + currentFilter.toLowerCase());
                        binding.textEmptyState.setVisibility(View.VISIBLE);
                        binding.recyclerViewWatched.setVisibility(View.GONE);
                    }
                } else {
                    binding.textEmptyState.setText("No movies found matching \"" + query + "\"");
                    binding.textEmptyState.setVisibility(View.VISIBLE);
                    binding.recyclerViewWatched.setVisibility(View.GONE);
                }
            });
        } else {
            // No filter active, show all search results
            viewModel.searchWatched(query).observe(getViewLifecycleOwner(), watchedEntries -> {
                if (watchedEntries != null && !watchedEntries.isEmpty()) {
                    adapter.submitList(watchedEntries);
                    binding.textEmptyState.setVisibility(View.GONE);
                    binding.recyclerViewWatched.setVisibility(View.VISIBLE);
                } else {
                    binding.textEmptyState.setText("No movies found matching \"" + query + "\"");
                    binding.textEmptyState.setVisibility(View.VISIBLE);
                    binding.recyclerViewWatched.setVisibility(View.GONE);
                }
            });
        }
    }

    private void observeData() {
        // Show loading state initially
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerViewWatched.setVisibility(View.GONE);
        binding.textEmptyState.setVisibility(View.GONE);
        
        // Get the appropriate LiveData based on current sort
        androidx.lifecycle.LiveData<List<WatchedEntry>> liveData = getSortedLiveData();
        
        liveData.observe(getViewLifecycleOwner(), watchedEntries -> {
            binding.progressBar.setVisibility(View.GONE);
            
            if (watchedEntries != null && !watchedEntries.isEmpty()) {
                adapter.submitList(watchedEntries);
                binding.textEmptyState.setVisibility(View.GONE);
                binding.recyclerViewWatched.setVisibility(View.VISIBLE);
            } else {
                adapter.submitList(new ArrayList<>());
                binding.textEmptyState.setVisibility(View.VISIBLE);
                binding.recyclerViewWatched.setVisibility(View.GONE);
            }
        });
    }
    
    private androidx.lifecycle.LiveData<List<WatchedEntry>> getSortedLiveData() {
        switch (currentSort) {
            case "DATE_DESC":
                return viewModel.getWatchedByDateDesc();
            case "DATE_ASC":
                return viewModel.getWatchedByDateAsc();
            case "RATING_DESC":
                return viewModel.getWatchedByRatingDesc();
            case "RATING_ASC":
                return viewModel.getWatchedByRatingAsc();
            case "SPEND_DESC":
                return viewModel.getWatchedBySpendDesc();
            case "SPEND_ASC":
                return viewModel.getWatchedBySpendAsc();
            default:
                return viewModel.getWatchedByDateDesc();
        }
    }

    private void editMovie(WatchedEntry entry) {
        EditWatchedFragment fragment = EditWatchedFragment.newInstance(entry);
        getParentFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void showDeleteConfirmation(WatchedEntry entry) {
        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Delete Movie")
                .setMessage("Are you sure you want to delete \"" + entry.title + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteMovie(entry))
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Refresh the adapter to undo the swipe animation
                    adapter.notifyDataSetChanged();
                })
                .show();
    }

    private void deleteMovie(WatchedEntry entry) {
        viewModel.deleteWatched(entry);
        Toast.makeText(getContext(), "Movie deleted successfully!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
