package com.entertainment.moviememo.ui.watchlist;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.entertainment.moviememo.R;
import com.entertainment.moviememo.data.entities.WatchlistItem;
import com.entertainment.moviememo.databinding.DialogAddWatchlistBinding;
import com.entertainment.moviememo.viewmodels.WatchlistViewModel;

import java.util.Calendar;

public class AddWatchlistDialog extends DialogFragment {

    private DialogAddWatchlistBinding binding;
    private WatchlistViewModel viewModel;

    public static AddWatchlistDialog newInstance() {
        return new AddWatchlistDialog();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new androidx.lifecycle.ViewModelProvider(this).get(WatchlistViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogAddWatchlistBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupSpinner();
        binding.buttonSave.setOnClickListener(v -> saveWatchlistItem());
        binding.buttonCancel.setOnClickListener(v -> dismiss());
    }

    private void setupSpinner() {
        // No spinners to setup in dialog
    }

    private void saveWatchlistItem() {
        String title = binding.editTitle.getText().toString().trim();
        String notes = binding.editNotes.getText().toString().trim();

        if (title.isEmpty()) {
            binding.editTitle.setError("Title is required");
            return;
        }

        WatchlistItem item = new WatchlistItem(title);
        item.notes = notes.isEmpty() ? null : notes;
        item.priority = 2; // Default to Medium priority

        viewModel.insertWatchlist(item);
        Toast.makeText(getContext(), "Added to watchlist!", Toast.LENGTH_SHORT).show();
        dismiss();
    }
}