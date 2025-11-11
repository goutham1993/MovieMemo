package com.entertainment.moviememo.ui.watchlist;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.entertainment.moviememo.R;
import com.entertainment.moviememo.data.entities.WatchlistItem;
import com.entertainment.moviememo.data.entities.WatchedEntry;
import com.entertainment.moviememo.data.enums.Language;
import com.entertainment.moviememo.data.enums.WhereToWatch;
import com.entertainment.moviememo.databinding.FragmentAddWatchlistBinding;
import com.entertainment.moviememo.viewmodels.WatchlistViewModel;
import com.entertainment.moviememo.viewmodels.WatchedViewModel;
import com.entertainment.moviememo.utils.NotificationHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddWatchlistFragment extends Fragment {

    private FragmentAddWatchlistBinding binding;
    private WatchlistViewModel viewModel;
    private Calendar selectedReleaseDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddWatchlistBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(WatchlistViewModel.class);
        selectedReleaseDate = null;

        setupSpinners();
        setupOttPlatformAutocomplete();
        setupWhereToWatchListener();
        setupReleaseDateToggle();
        setupReleaseDatePicker();
        
        // Make sure release date UI is visible (available for all options)
        binding.layoutTheaterReleaseDate.setVisibility(View.VISIBLE);
        binding.buttonSave.setOnClickListener(v -> saveWatchlistItem());
        binding.buttonCancel.setOnClickListener(v -> getParentFragmentManager().popBackStack());
    }

    private void setupSpinners() {
        // Language spinner
        String[] languages = new String[Language.values().length];
        for (int i = 0; i < Language.values().length; i++) {
            languages[i] = Language.values()[i].getDisplayName();
        }
        android.widget.ArrayAdapter<String> languageAdapter = new android.widget.ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, languages);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerLanguage.setAdapter(languageAdapter);
        binding.spinnerLanguage.setSelection(0); // Default to English

        // Where to watch spinner
        String[] whereToWatchOptions = new String[WhereToWatch.values().length];
        for (int i = 0; i < WhereToWatch.values().length; i++) {
            whereToWatchOptions[i] = WhereToWatch.values()[i].getDisplayName();
        }
        android.widget.ArrayAdapter<String> whereToWatchAdapter = new android.widget.ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, whereToWatchOptions);
        whereToWatchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerWhereToWatch.setAdapter(whereToWatchAdapter);
        binding.spinnerWhereToWatch.setSelection(0); // Default to Theater
    }

    private void setupOttPlatformAutocomplete() {
        // Get all previous streaming platforms from watched entries and watchlist items
        WatchedViewModel watchedViewModel = new ViewModelProvider(this).get(WatchedViewModel.class);
        watchedViewModel.getAllWatched().observe(getViewLifecycleOwner(), entries -> {
            List<String> platforms = new ArrayList<>();
            // Add common streaming platforms as defaults
            platforms.add("Netflix");
            platforms.add("Prime Video");
            platforms.add("Disney+");
            platforms.add("Hulu");
            platforms.add("HBO Max");
            platforms.add("Paramount+");
            platforms.add("Apple TV+");
            platforms.add("Peacock");
            platforms.add("YouTube");
            platforms.add("Crunchyroll");
            
            for (WatchedEntry entry : entries) {
                if (entry.streamingPlatform != null && !entry.streamingPlatform.trim().isEmpty()) {
                    String trimmed = entry.streamingPlatform.trim();
                    if (!platforms.contains(trimmed)) {
                        platforms.add(trimmed);
                    }
                }
            }
            
            // Also get from watchlist items
            viewModel.getAllWatchlist().observe(getViewLifecycleOwner(), watchlistItems -> {
                for (WatchlistItem item : watchlistItems) {
                    if (item.streamingPlatform != null && !item.streamingPlatform.trim().isEmpty()) {
                        String trimmed = item.streamingPlatform.trim();
                        if (!platforms.contains(trimmed)) {
                            platforms.add(trimmed);
                        }
                    }
                }
                
                // Set up the autocomplete adapter
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), 
                    R.layout.autocomplete_item, platforms);
                binding.editOttPlatform.setAdapter(adapter);
            });
        });
    }

    private void setupWhereToWatchListener() {
        binding.spinnerWhereToWatch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                WhereToWatch selected = WhereToWatch.values()[position];
                // Show OTT platform field only when OTT Streaming is selected
                if (selected == WhereToWatch.OTT_STREAMING) {
                    binding.layoutOttPlatform.setVisibility(View.VISIBLE);
                } else {
                    binding.layoutOttPlatform.setVisibility(View.GONE);
                    binding.editOttPlatform.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                binding.layoutOttPlatform.setVisibility(View.GONE);
            }
        });
    }

    private void setupReleaseDateToggle() {
        binding.switchReleaseDate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.buttonReleaseDate.setEnabled(isChecked);
            if (!isChecked) {
                binding.buttonReleaseDate.setText("📅 Select Release Date");
                selectedReleaseDate = null;
            }
        });
    }

    private void setupReleaseDatePicker() {
        binding.buttonReleaseDate.setOnClickListener(v -> {
            Calendar calendar = selectedReleaseDate != null ? selectedReleaseDate : Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        Calendar cal = Calendar.getInstance();
                        cal.set(selectedYear, selectedMonth, selectedDay);
                        selectedReleaseDate = cal;
                        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
                        binding.buttonReleaseDate.setText("📅 " + dateFormat.format(cal.getTime()));
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });
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
        item.language = Language.values()[binding.spinnerLanguage.getSelectedItemPosition()].getCode();
        
        // Where to watch
        WhereToWatch whereToWatch = WhereToWatch.values()[binding.spinnerWhereToWatch.getSelectedItemPosition()];
        item.whereToWatch = whereToWatch.name();
        
        // Streaming platform (only for OTT Streaming)
        if (whereToWatch == WhereToWatch.OTT_STREAMING) {
            String streamingPlatform = binding.editOttPlatform.getText().toString().trim();
            if (!TextUtils.isEmpty(streamingPlatform)) {
                item.streamingPlatform = streamingPlatform;
            }
        } else {
            item.streamingPlatform = null;
        }
        
        // Release date (available for all options if switch is on)
        if (binding.switchReleaseDate.isChecked() && selectedReleaseDate != null) {
            item.releaseDate = selectedReleaseDate.getTimeInMillis();
        } else {
            item.releaseDate = null;
        }

        viewModel.insertWatchlist(item);
        
        Toast.makeText(getContext(), "🎫 Added to watchlist!", Toast.LENGTH_SHORT).show();
        getParentFragmentManager().popBackStack();
    }
}
