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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.entertainment.moviememo.data.entities.WatchedEntry;
import com.entertainment.moviememo.databinding.FragmentWatchedListBinding;
import com.entertainment.moviememo.viewmodels.WatchedViewModel;
import com.entertainment.moviememo.ui.adapters.WatchedEntryAdapter;
import com.entertainment.moviememo.ui.adapters.SwipeToDeleteCallback;

import java.util.ArrayList;
import java.util.List;

public class WatchedListFragment extends Fragment {

    private FragmentWatchedListBinding binding;
    private WatchedViewModel viewModel;
    private WatchedEntryAdapter adapter;

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
        
        // Set up swipe to delete
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback() {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position >= 0 && position < adapter.getItemCount()) {
                    WatchedEntry entry = adapter.getCurrentList().get(position);
                    showDeleteConfirmation(entry);
                }
            }
        };
        
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewWatched);
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

    private void searchMovies(String query) {
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

    private void observeData() {
        // Show loading state initially
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerViewWatched.setVisibility(View.GONE);
        binding.textEmptyState.setVisibility(View.GONE);
        
        viewModel.getAllWatched().observe(getViewLifecycleOwner(), watchedEntries -> {
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
