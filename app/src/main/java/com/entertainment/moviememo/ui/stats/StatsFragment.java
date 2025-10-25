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
import com.entertainment.moviememo.utils.DurationUtils;
import com.entertainment.moviememo.ui.adapters.GenreStatsAdapter;
import com.entertainment.moviememo.ui.adapters.MonthlyStatsAdapter;
import com.entertainment.moviememo.data.entities.KeyCount;
import com.entertainment.moviememo.data.entities.MonthCount;
import com.entertainment.moviememo.data.enums.LocationType;
import com.entertainment.moviememo.data.enums.TimeOfDay;
import com.entertainment.moviememo.data.enums.Language;

import java.util.List;
import java.util.ArrayList;

public class StatsFragment extends Fragment {

    private FragmentStatsBinding binding;
    private StatsViewModel viewModel;
    private GenreStatsAdapter genreAdapter;
    private GenreStatsAdapter locationAdapter;
    private GenreStatsAdapter timeAdapter;
    private GenreStatsAdapter languageAdapter;
    private GenreStatsAdapter companionAdapter;
    private MonthlyStatsAdapter monthlyAdapter;

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
        setupAdapters();
        observeData();
    }
    
    private void setupAdapters() {
        // Setup genre adapter
        genreAdapter = new GenreStatsAdapter();
        binding.recyclerViewGenres.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext(), androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewGenres.setAdapter(genreAdapter);
        
        // Setup location adapter
        locationAdapter = new GenreStatsAdapter();
        binding.recyclerViewLocation.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext(), androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewLocation.setAdapter(locationAdapter);
        
        // Setup time adapter
        timeAdapter = new GenreStatsAdapter();
        binding.recyclerViewTime.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext(), androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewTime.setAdapter(timeAdapter);
        
        // Setup language adapter
        languageAdapter = new GenreStatsAdapter();
        binding.recyclerViewLanguage.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext(), androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewLanguage.setAdapter(languageAdapter);
        
        // Setup companion adapter
        companionAdapter = new GenreStatsAdapter();
        binding.recyclerViewCompanion.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext(), androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewCompanion.setAdapter(companionAdapter);
        
        // Setup monthly adapter
        monthlyAdapter = new MonthlyStatsAdapter();
        binding.recyclerViewMonthly.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        binding.recyclerViewMonthly.setAdapter(monthlyAdapter);
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

        viewModel.getTotalDurationMinutes().observe(getViewLifecycleOwner(), totalMinutes -> {
            if (totalMinutes != null && binding.textTotalDurationValue != null) {
                String durationText = DurationUtils.formatDurationComprehensive(totalMinutes);
                binding.textTotalDurationValue.setText(durationText);
            }
        });

        // Observe genre statistics
        viewModel.getTopGenres().observe(getViewLifecycleOwner(), genres -> {
            if (genres != null) {
                genreAdapter.updateGenres(genres);
            }
        });

        // Observe location statistics
        viewModel.getMoviesByLocation().observe(getViewLifecycleOwner(), locations -> {
            if (locations != null) {
                List<KeyCount> formattedLocations = new ArrayList<>();
                for (KeyCount location : locations) {
                    String displayName = getLocationDisplayName(location.category);
                    formattedLocations.add(new KeyCount(displayName, location.cnt));
                }
                locationAdapter.updateGenres(formattedLocations);
            }
        });

        // Observe time of day statistics
        viewModel.getTopTimeOfDay().observe(getViewLifecycleOwner(), times -> {
            if (times != null) {
                List<KeyCount> formattedTimes = new ArrayList<>();
                for (KeyCount time : times) {
                    String displayName = getTimeDisplayName(time.category);
                    formattedTimes.add(new KeyCount(displayName, time.cnt));
                }
                timeAdapter.updateGenres(formattedTimes);
            }
        });

        // Observe language statistics
        viewModel.getMoviesByLanguage().observe(getViewLifecycleOwner(), languages -> {
            if (languages != null) {
                List<KeyCount> formattedLanguages = new ArrayList<>();
                for (KeyCount language : languages) {
                    String displayName = getLanguageDisplayName(language.category);
                    formattedLanguages.add(new KeyCount(displayName, language.cnt));
                }
                languageAdapter.updateGenres(formattedLanguages);
            }
        });

        // Observe companion statistics
        viewModel.getMoviesByCompanion().observe(getViewLifecycleOwner(), companions -> {
            if (companions != null) {
                companionAdapter.updateGenres(companions);
            }
        });

        // Observe monthly statistics
        viewModel.getMoviesPerMonth().observe(getViewLifecycleOwner(), months -> {
            if (months != null) {
                monthlyAdapter.updateMonthlyStats(months);
            }
        });

        // Hide loading when data is loaded
        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(View.GONE);
        }
    }
    
    private String getLocationDisplayName(String locationType) {
        try {
            LocationType type = LocationType.valueOf(locationType);
            switch (type) {
                case HOME:
                    return "🏠 Home";
                case THEATER:
                    return "🎬 Theater";
                case FRIENDS_HOME:
                    return "👥 Friend's Home";
                case OTHER:
                    return "📍 Other";
                default:
                    return locationType;
            }
        } catch (IllegalArgumentException e) {
            return locationType;
        }
    }
    
    private String getTimeDisplayName(String timeOfDay) {
        try {
            TimeOfDay time = TimeOfDay.valueOf(timeOfDay);
            switch (time) {
                case MORNING:
                    return "🌅 Morning";
                case AFTERNOON:
                    return "☀️ Afternoon";
                case EVENING:
                    return "🌆 Evening";
                case NIGHT:
                    return "🌙 Night";
                default:
                    return timeOfDay;
            }
        } catch (IllegalArgumentException e) {
            return timeOfDay;
        }
    }
    
    private String getLanguageDisplayName(String languageCode) {
        try {
            Language language = Language.fromCode(languageCode);
            return "🌐 " + language.getDisplayName();
        } catch (Exception e) {
            return "🌐 " + languageCode;
        }
    }
}