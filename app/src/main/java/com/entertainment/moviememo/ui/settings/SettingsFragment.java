package com.entertainment.moviememo.ui.settings;

import android.app.AlertDialog;
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
import com.entertainment.moviememo.databinding.FragmentSettingsBinding;
import com.entertainment.moviememo.viewmodels.WatchedViewModel;
import com.entertainment.moviememo.viewmodels.WatchlistViewModel;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private WatchedViewModel watchedViewModel;
    private WatchlistViewModel watchlistViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        watchedViewModel = new ViewModelProvider(this).get(WatchedViewModel.class);
        watchlistViewModel = new ViewModelProvider(this).get(WatchlistViewModel.class);

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.buttonBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        binding.buttonClearWatched.setOnClickListener(v -> showClearWatchedDialog());
        binding.buttonClearWatchlist.setOnClickListener(v -> showClearWatchlistDialog());
    }

    private void showClearWatchedDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("🗑️ Clear Watched Movies")
                .setMessage("Are you sure you want to delete all watched movies? This action cannot be undone.")
                .setPositiveButton("Delete All", (dialog, which) -> {
                    watchedViewModel.clearAllWatched();
                    Toast.makeText(getContext(), "All watched movies deleted!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showClearWatchlistDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("🗑️ Clear Watchlist")
                .setMessage("Are you sure you want to delete all watchlist items? This action cannot be undone.")
                .setPositiveButton("Delete All", (dialog, which) -> {
                    watchlistViewModel.clearAllWatchlist();
                    Toast.makeText(getContext(), "Watchlist cleared!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}