package com.entertainment.moviememo.ui.watched;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.entertainment.moviememo.databinding.FragmentAddWatchedBinding;
import com.entertainment.moviememo.viewmodels.WatchedViewModel;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddWatchedFragment extends Fragment {

    private FragmentAddWatchedBinding binding;
    private WatchedViewModel viewModel;
    private Calendar selectedDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddWatchedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(WatchedViewModel.class);
        selectedDate = Calendar.getInstance();

        setupSpinners();
        setupClickListeners();
        initializeDateButton();
        setupCompanionsAutocomplete();
        setupTheaterAutocomplete();
        setupCityAutocomplete();
        setupStreamingPlatformAutocomplete();
        setupLocationSpinnerListener();
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
        binding.spinnerLanguage.setSelection(0); // Default to English
    }

    private void setupLocationSpinnerListener() {
        binding.spinnerLocation.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                LocationType selectedLocation = LocationType.values()[position];
                if (selectedLocation == LocationType.THEATER) {
                    binding.layoutTheaterFields.setVisibility(View.VISIBLE);
                    binding.layoutHomeFields.setVisibility(View.GONE);
                } else if (selectedLocation == LocationType.HOME) {
                    binding.layoutTheaterFields.setVisibility(View.GONE);
                    binding.layoutHomeFields.setVisibility(View.VISIBLE);
                } else {
                    binding.layoutTheaterFields.setVisibility(View.GONE);
                    binding.layoutHomeFields.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                binding.layoutTheaterFields.setVisibility(View.GONE);
                binding.layoutHomeFields.setVisibility(View.GONE);
            }
        });
        
        // Set HOME as default selection
        binding.spinnerLocation.setSelection(LocationType.HOME.ordinal());
        binding.layoutHomeFields.setVisibility(View.VISIBLE);
    }

    private void setupStreamingPlatformAutocomplete() {
        // Get all previous streaming platforms from the database
        viewModel.getAllWatched().observe(getViewLifecycleOwner(), entries -> {
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
            
            // Set up the autocomplete adapter
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), 
                R.layout.autocomplete_item, platforms);
            binding.editStreamingPlatform.setAdapter(adapter);
        });
    }

    private void setupClickListeners() {
        binding.buttonDate.setOnClickListener(v -> showDatePicker());
        binding.buttonSave.setOnClickListener(v -> saveWatchedEntry());
        binding.buttonCancel.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        
        // Add focus change listener to companions field to capitalize first letter
        binding.editCompanions.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) { // When field loses focus
                String text = binding.editCompanions.getText().toString().trim();
                if (!text.isEmpty()) {
                    String capitalized = text.substring(0, 1).toUpperCase() + text.substring(1);
                    binding.editCompanions.setText(capitalized);
                }
            }
        });
    }

    private void initializeDateButton() {
        // Set the date button to show current date by default
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        binding.buttonDate.setText("📅 " + dateFormat.format(selectedDate.getTime()));
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

    private void setupTheaterAutocomplete() {
        // Get all previous theater names from the database
        viewModel.getAllWatched().observe(getViewLifecycleOwner(), entries -> {
            List<String> theaters = new ArrayList<>();
            for (WatchedEntry entry : entries) {
                if (entry.theaterName != null && !entry.theaterName.trim().isEmpty()) {
                    String trimmed = entry.theaterName.trim();
                    if (!theaters.contains(trimmed)) {
                        theaters.add(trimmed);
                    }
                }
            }
            
            // Set up the autocomplete adapter
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), 
                R.layout.autocomplete_item, theaters);
            binding.editTheaterName.setAdapter(adapter);
        });
    }

    private void setupCityAutocomplete() {
        // Get all previous cities from the database
        viewModel.getAllWatched().observe(getViewLifecycleOwner(), entries -> {
            List<String> cities = new ArrayList<>();
            for (WatchedEntry entry : entries) {
                if (entry.city != null && !entry.city.trim().isEmpty()) {
                    String trimmed = entry.city.trim();
                    if (!cities.contains(trimmed)) {
                        cities.add(trimmed);
                    }
                }
            }
            
            // Set up the autocomplete adapter
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), 
                R.layout.autocomplete_item, cities);
            binding.editCity.setAdapter(adapter);
        });
    }

    private void showDatePicker() {
        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            selectedDate.set(year, month, dayOfMonth);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            binding.buttonDate.setText("📅 " + dateFormat.format(selectedDate.getTime()));
        }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveWatchedEntry() {
        String title = binding.editTitle.getText().toString().trim();
        String genre = binding.editGenre.getText().toString().trim();
        String notes = binding.editNotes.getText().toString().trim();
        String companions = binding.editCompanions.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            binding.editTitle.setError("Title is required");
            return;
        }

        WatchedEntry entry = new WatchedEntry(
                title,
                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.getTime()),
                LocationType.values()[binding.spinnerLocation.getSelectedItemPosition()].name(),
                TimeOfDay.values()[binding.spinnerTime.getSelectedItemPosition()].name()
        );

        // Set optional fields
        if (!TextUtils.isEmpty(genre)) entry.genre = genre;
        if (!TextUtils.isEmpty(notes)) entry.notes = notes;
        if (!TextUtils.isEmpty(companions)) entry.companions = companions;
        
        // Set language
        entry.language = Language.values()[binding.spinnerLanguage.getSelectedItemPosition()].getCode();
        
        // Set theater fields if location is THEATER
        LocationType selectedLocation = LocationType.values()[binding.spinnerLocation.getSelectedItemPosition()];
        if (selectedLocation == LocationType.THEATER) {
            String theaterName = binding.editTheaterName.getText().toString().trim();
            String city = binding.editCity.getText().toString().trim();
            if (!TextUtils.isEmpty(theaterName)) entry.theaterName = theaterName;
            if (!TextUtils.isEmpty(city)) entry.city = city;
        } else if (selectedLocation == LocationType.HOME) {
            // Set streaming platform if location is HOME
            String streamingPlatform = binding.editStreamingPlatform.getText().toString().trim();
            if (!TextUtils.isEmpty(streamingPlatform)) entry.streamingPlatform = streamingPlatform;
        }

        // Parse rating
        String ratingText = binding.editRating.getText().toString().trim();
        if (!TextUtils.isEmpty(ratingText)) {
            try {
                entry.rating = Integer.parseInt(ratingText);
            } catch (NumberFormatException e) {
                binding.editRating.setError("Invalid rating");
                return;
            }
        }

        // Parse spend
        String spendText = binding.editSpend.getText().toString().trim();
        if (!TextUtils.isEmpty(spendText)) {
            try {
                double spend = Double.parseDouble(spendText.replace("$", "").replace(",", ""));
                entry.spendCents = (int) (spend * 100);
            } catch (NumberFormatException e) {
                binding.editSpend.setError("Invalid amount");
                return;
            }
        }

        // Parse duration
        String durationText = binding.editDuration.getText().toString().trim();
        if (!TextUtils.isEmpty(durationText)) {
            try {
                entry.durationMin = Integer.parseInt(durationText);
            } catch (NumberFormatException e) {
                binding.editDuration.setError("Invalid duration");
                return;
            }
        }

        viewModel.insertWatched(entry);
        Toast.makeText(getContext(), "Movie added successfully! 🎬", Toast.LENGTH_SHORT).show();
        getParentFragmentManager().popBackStack();
    }
}