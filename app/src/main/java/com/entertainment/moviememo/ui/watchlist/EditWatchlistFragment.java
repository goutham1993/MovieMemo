package com.entertainment.moviememo.ui.watchlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.entertainment.moviememo.R;
import com.entertainment.moviememo.data.entities.WatchlistItem;
import com.entertainment.moviememo.databinding.FragmentEditWatchlistBinding;
import com.entertainment.moviememo.viewmodels.WatchlistViewModel;

public class EditWatchlistFragment extends Fragment {

    private FragmentEditWatchlistBinding binding;
    private WatchlistViewModel viewModel;
    private WatchlistItem itemToEdit;

    public static EditWatchlistFragment newInstance(WatchlistItem item) {
        EditWatchlistFragment fragment = new EditWatchlistFragment();
        Bundle args = new Bundle();
        args.putSerializable("item", item);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditWatchlistBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(WatchlistViewModel.class);

        loadItemData();
        setupSpinner();
        binding.buttonSave.setOnClickListener(v -> updateWatchlistItem());
        binding.buttonCancel.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        binding.buttonDelete.setOnClickListener(v -> deleteWatchlistItem());
        binding.buttonConvertToWatched.setOnClickListener(v -> convertToWatched());
    }

    private void loadItemData() {
        if (getArguments() != null) {
            itemToEdit = (WatchlistItem) getArguments().getSerializable("item");
            if (itemToEdit != null) {
                populateForm();
            }
        }
    }

    private void populateForm() {
        // Title
        binding.editTitle.setText(itemToEdit.title);

        // Notes
        if (itemToEdit.notes != null) {
            binding.editNotes.setText(itemToEdit.notes);
        }

        // Priority
        if (itemToEdit.priority != null) {
            binding.spinnerPriority.setSelection(itemToEdit.priority - 1); // Convert 1-3 to 0-2
        } else {
            binding.spinnerPriority.setSelection(1); // Default to Medium
        }
    }

    private void setupSpinner() {
        String[] priorities = {"Low", "Medium", "High"};
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, priorities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerPriority.setAdapter(adapter);
    }

    private void updateWatchlistItem() {
        String title = binding.editTitle.getText().toString().trim();
        String notes = binding.editNotes.getText().toString().trim();
        int priority = binding.spinnerPriority.getSelectedItemPosition() + 1; // 1-3

        if (title.isEmpty()) {
            binding.editTitle.setError("Title is required");
            return;
        }

        // Update the existing item
        itemToEdit.title = title;
        itemToEdit.notes = notes.isEmpty() ? null : notes;
        itemToEdit.priority = priority;

        viewModel.updateWatchlist(itemToEdit);
        Toast.makeText(getContext(), "🎫 Watchlist item updated!", Toast.LENGTH_SHORT).show();
        getParentFragmentManager().popBackStack();
    }

    private void deleteWatchlistItem() {
        if (itemToEdit != null) {
            viewModel.deleteWatchlist(itemToEdit);
            Toast.makeText(getContext(), "🎫 Watchlist item deleted!", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
        }
    }

    private void convertToWatched() {
        if (itemToEdit != null) {
            // Create a new watched entry from the watchlist item
            com.entertainment.moviememo.data.entities.WatchedEntry entry = new com.entertainment.moviememo.data.entities.WatchedEntry(
                itemToEdit.title,
                new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date()),
                "HOME", // Default location
                "EVENING" // Default time
            );
            
            if (itemToEdit.notes != null) {
                entry.notes = itemToEdit.notes;
            }
            
            // Insert into watched entries
            com.entertainment.moviememo.viewmodels.WatchedViewModel watchedViewModel = new androidx.lifecycle.ViewModelProvider(this).get(com.entertainment.moviememo.viewmodels.WatchedViewModel.class);
            watchedViewModel.insertWatched(entry);
            
            // Remove from watchlist
            viewModel.deleteWatchlist(itemToEdit);
            
            Toast.makeText(getContext(), "🎬 Moved to watched movies!", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
        }
    }
}
