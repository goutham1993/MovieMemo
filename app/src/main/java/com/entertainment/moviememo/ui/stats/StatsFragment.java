package com.entertainment.moviememo.ui.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.entertainment.moviememo.R;
import com.entertainment.moviememo.databinding.FragmentStatsBinding;
import com.entertainment.moviememo.viewmodels.StatsViewModel;

public class StatsFragment extends Fragment {

    private FragmentStatsBinding binding;
    private StatsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(StatsViewModel.class);
        observeData();
    }

    private void observeData() {
        // Show loading state initially
        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }

        viewModel.getWatchedCount().observe(getViewLifecycleOwner(), count -> {
            if (count != null && binding.textTotalMoviesValue != null) {
                binding.textTotalMoviesValue.setText(String.valueOf(count));
            }
        });

        viewModel.getAverageRating().observe(getViewLifecycleOwner(), rating -> {
            if (rating != null && binding.textAvgRatingValue != null) {
                binding.textAvgRatingValue.setText(String.format("%.1f ⭐", rating));
            }
        });

        viewModel.getTotalSpendCents().observe(getViewLifecycleOwner(), spendCents -> {
            if (spendCents != null && binding.textTotalSpendValue != null) {
                String spendText = String.format("$%.2f", spendCents / 100.0);
                binding.textTotalSpendValue.setText(spendText);
            }
        });

        // Hide loading when data is loaded
        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(View.GONE);
        }
    }
}