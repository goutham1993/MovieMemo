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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.entertainment.moviememo.databinding.FragmentWatchlistBinding;
import com.entertainment.moviememo.viewmodels.WatchlistViewModel;
import com.entertainment.moviememo.ui.adapters.WatchlistAdapter;
import com.entertainment.moviememo.ui.adapters.SwipeToDeleteCallback;

import java.util.ArrayList;
import java.util.List;

public class WatchlistFragment extends Fragment {

    private FragmentWatchlistBinding binding;
    private WatchlistViewModel viewModel;
    private WatchlistAdapter adapter;

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
        
        // Set up swipe to delete
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback() {
            @Override
            public void onSwiped(@NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position >= 0 && position < adapter.getItemCount()) {
                    com.entertainment.moviememo.data.entities.WatchlistItem item = adapter.getCurrentList().get(position);
                    showDeleteConfirmation(item);
                }
            }
        };
        
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewWatchlist);
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
                    searchWatchlist(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void searchWatchlist(String query) {
        viewModel.getAllWatchlist().observe(getViewLifecycleOwner(), watchlistItems -> {
            if (watchlistItems != null) {
                List<com.entertainment.moviememo.data.entities.WatchlistItem> filteredItems = new ArrayList<>();
                for (com.entertainment.moviememo.data.entities.WatchlistItem item : watchlistItems) {
                    if (item.title.toLowerCase().contains(query.toLowerCase()) ||
                        (item.notes != null && item.notes.toLowerCase().contains(query.toLowerCase()))) {
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
