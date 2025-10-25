package com.entertainment.moviememo.ui.watched;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.entertainment.moviememo.R;
import com.entertainment.moviememo.data.entities.WatchedEntry;
import com.entertainment.moviememo.data.enums.Language;
import com.entertainment.moviememo.data.enums.LocationType;
import com.entertainment.moviememo.data.enums.TimeOfDay;
import com.entertainment.moviememo.databinding.FragmentEditWatchedBinding;
import com.entertainment.moviememo.viewmodels.WatchedViewModel;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditWatchedFragment extends Fragment {

    private FragmentEditWatchedBinding binding;
    private WatchedViewModel viewModel;
    private WatchedEntry entryToEdit;
    private Calendar selectedDate;
    private SimpleDateFormat dateFormat;

    public static EditWatchedFragment newInstance(WatchedEntry entry) {
        EditWatchedFragment fragment = new EditWatchedFragment();
        Bundle args = new Bundle();
        args.putSerializable("entry", entry);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditWatchedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupViewModel();
        setupSpinners();
        loadEntryData();
        setupClickListeners();
        setupCompanionsAutocomplete();
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(WatchedViewModel.class);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedDate = Calendar.getInstance();
    }

    private void loadEntryData() {
        if (getArguments() != null) {
            entryToEdit = (WatchedEntry) getArguments().getSerializable("entry");
            if (entryToEdit != null) {
                populateForm();
            }
        }
    }

    private void setupSpinners() {
        // Location spinner
        List<String> locations = new ArrayList<>();
        for (LocationType location : LocationType.values()) {
            locations.add(location.getDisplayName());
        }
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, locations);
        locationAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        binding.spinnerLocation.setAdapter(locationAdapter);

        // Time spinner
        List<String> times = new ArrayList<>();
        for (TimeOfDay time : TimeOfDay.values()) {
            times.add(time.getDisplayName());
        }
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, times);
        timeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        binding.spinnerTime.setAdapter(timeAdapter);

        // Language spinner
        List<String> languages = new ArrayList<>();
        for (Language language : Language.values()) {
            languages.add(language.getDisplayName());
        }
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, languages);
        languageAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        binding.spinnerLanguage.setAdapter(languageAdapter);
    }

    private void setupCompanionsAutocomplete() {
        // Get all previous companions from the database
        viewModel.getAllWatched().observe(getViewLifecycleOwner(), entries -> {
            List<String> companions = new ArrayList<>();
            for (WatchedEntry entry : entries) {
                if (entry.companions != null && !entry.companions.trim().isEmpty()) {
                    // Split companions by comma and add each one
                    String[] companionArray = entry.companions.split(",");
                    for (String companion : companionArray) {
                        String trimmed = companion.trim();
                        if (!trimmed.isEmpty() && !companions.contains(trimmed)) {
                            companions.add(trimmed);
                        }
                    }
                }
            }
            
            // Set up the autocomplete adapter
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), 
                android.R.layout.simple_dropdown_item_1line, companions);
            binding.editCompanions.setAdapter(adapter);
        });
    }

    private void populateForm() {
        // Title
        binding.editTitle.setText(entryToEdit.title);

        // Rating
        if (entryToEdit.rating != null) {
            binding.editRating.setText(String.valueOf(entryToEdit.rating));
        }

        // Duration
        if (entryToEdit.durationMin != null) {
            binding.editDuration.setText(String.valueOf(entryToEdit.durationMin));
        }

        // Date
        try {
            Date date = dateFormat.parse(entryToEdit.watchedDate);
            if (date != null) {
                selectedDate.setTime(date);
                binding.buttonDate.setText("📅 " + dateFormat.format(selectedDate.getTime()));
            }
        } catch (ParseException e) {
            binding.buttonDate.setText("📅 " + entryToEdit.watchedDate);
        }

        // Location type
        try {
            LocationType locationType = LocationType.valueOf(entryToEdit.locationType);
            int locationIndex = locationType.ordinal();
            binding.spinnerLocation.setSelection(locationIndex);
        } catch (IllegalArgumentException e) {
            binding.spinnerLocation.setSelection(0);
        }

        // Time of day
        try {
            TimeOfDay timeOfDay = TimeOfDay.valueOf(entryToEdit.timeOfDay);
            int timeIndex = timeOfDay.ordinal();
            binding.spinnerTime.setSelection(timeIndex);
        } catch (IllegalArgumentException e) {
            binding.spinnerTime.setSelection(0);
        }

        // Genre
        if (entryToEdit.genre != null) {
            binding.editGenre.setText(entryToEdit.genre);
        }

        // Spend
        if (entryToEdit.spendCents != null) {
            double spend = entryToEdit.spendCents / 100.0;
            binding.editSpend.setText(String.valueOf(spend));
        }

        // Companions
        if (entryToEdit.companions != null) {
            binding.editCompanions.setText(entryToEdit.companions);
        }

        // Notes
        if (entryToEdit.notes != null) {
            binding.editNotes.setText(entryToEdit.notes);
        }

        // Language
        if (entryToEdit.language != null) {
            Language language = Language.fromCode(entryToEdit.language);
            int languageIndex = language.ordinal();
            binding.spinnerLanguage.setSelection(languageIndex);
        } else {
            binding.spinnerLanguage.setSelection(0); // Default to English
        }
    }

    private void setupClickListeners() {
        binding.buttonDate.setOnClickListener(v -> showDatePicker());
        binding.buttonSave.setOnClickListener(v -> updateMovie());
        binding.buttonCancel.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        binding.buttonDelete.setOnClickListener(v -> deleteMovie());
    }

    private void showDatePicker() {
        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            selectedDate.set(year, month, dayOfMonth);
            binding.buttonDate.setText("📅 " + dateFormat.format(selectedDate.getTime()));
        }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateMovie() {
        if (!validateForm()) {
            return;
        }

        try {
            WatchedEntry updatedEntry = createUpdatedEntry();
            viewModel.updateWatched(updatedEntry);
            
            Toast.makeText(getContext(), "✅ Movie updated successfully!", Toast.LENGTH_SHORT).show();
            
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error updating movie: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void deleteMovie() {
        if (entryToEdit != null) {
            viewModel.deleteWatched(entryToEdit);
            Toast.makeText(getContext(), "🗑️ Movie deleted!", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validate title (required)
        String title = binding.editTitle.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            binding.editTitle.setError("Title is required");
            isValid = false;
        } else {
            binding.editTitle.setError(null);
        }

        // Validate rating (0-10)
        String ratingText = binding.editRating.getText().toString().trim();
        if (!TextUtils.isEmpty(ratingText)) {
            try {
                int rating = Integer.parseInt(ratingText);
                if (rating < 0 || rating > 10) {
                    binding.editRating.setError("Rating must be between 0 and 10");
                    isValid = false;
                } else {
                    binding.editRating.setError(null);
                }
            } catch (NumberFormatException e) {
                binding.editRating.setError("Invalid rating format");
                isValid = false;
            }
        }

        // Validate duration
        String durationText = binding.editDuration.getText().toString().trim();
        if (!TextUtils.isEmpty(durationText)) {
            try {
                int duration = Integer.parseInt(durationText);
                if (duration <= 0) {
                    binding.editDuration.setError("Duration must be positive");
                    isValid = false;
                } else {
                    binding.editDuration.setError(null);
                }
            } catch (NumberFormatException e) {
                binding.editDuration.setError("Invalid duration format");
                isValid = false;
            }
        }

        // Validate spend
        String spendText = binding.editSpend.getText().toString().trim();
        if (!TextUtils.isEmpty(spendText)) {
            try {
                double spend = Double.parseDouble(spendText);
                if (spend < 0) {
                    binding.editSpend.setError("Amount cannot be negative");
                    isValid = false;
                } else {
                    binding.editSpend.setError(null);
                }
            } catch (NumberFormatException e) {
                binding.editSpend.setError("Invalid amount format");
                isValid = false;
            }
        }

        return isValid;
    }

    private WatchedEntry createUpdatedEntry() {
        String title = binding.editTitle.getText().toString().trim();
        String genre = binding.editGenre.getText().toString().trim();
        String notes = binding.editNotes.getText().toString().trim();
        String companions = binding.editCompanions.getText().toString().trim();

        WatchedEntry updatedEntry = new WatchedEntry(
                title,
                dateFormat.format(selectedDate.getTime()),
                LocationType.values()[binding.spinnerLocation.getSelectedItemPosition()].name(),
                TimeOfDay.values()[binding.spinnerTime.getSelectedItemPosition()].name()
        );

        // Set the original ID
        updatedEntry.id = entryToEdit.id;

        // Set optional fields
        if (!TextUtils.isEmpty(genre)) updatedEntry.genre = genre;
        if (!TextUtils.isEmpty(notes)) updatedEntry.notes = notes;
        if (!TextUtils.isEmpty(companions)) updatedEntry.companions = companions;
        
        // Set language
        updatedEntry.language = Language.values()[binding.spinnerLanguage.getSelectedItemPosition()].getCode();

        // Parse rating
        String ratingText = binding.editRating.getText().toString().trim();
        if (!TextUtils.isEmpty(ratingText)) {
            updatedEntry.rating = Integer.parseInt(ratingText);
        }

        // Parse spend
        String spendText = binding.editSpend.getText().toString().trim();
        if (!TextUtils.isEmpty(spendText)) {
            double spend = Double.parseDouble(spendText);
            updatedEntry.spendCents = (int) (spend * 100);
        }

        // Parse duration
        String durationText = binding.editDuration.getText().toString().trim();
        if (!TextUtils.isEmpty(durationText)) {
            updatedEntry.durationMin = Integer.parseInt(durationText);
        }

        return updatedEntry;
    }
}