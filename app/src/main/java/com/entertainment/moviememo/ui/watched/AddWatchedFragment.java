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
    }

    private void setupSpinners() {
        // Location spinner
        List<String> locations = new ArrayList<>();
        for (LocationType location : LocationType.values()) {
            locations.add(location.name().replace("_", " "));
        }
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, locations);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerLocation.setAdapter(locationAdapter);

        // Time spinner
        List<String> times = new ArrayList<>();
        for (TimeOfDay time : TimeOfDay.values()) {
            times.add(time.name());
        }
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, times);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerTime.setAdapter(timeAdapter);

        // Language spinner
        List<String> languages = new ArrayList<>();
        for (Language language : Language.values()) {
            languages.add(language.getDisplayName());
        }
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, languages);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerLanguage.setAdapter(languageAdapter);
        binding.spinnerLanguage.setSelection(0); // Default to English
    }

    private void setupClickListeners() {
        binding.buttonDate.setOnClickListener(v -> showDatePicker());
        binding.buttonSave.setOnClickListener(v -> saveWatchedEntry());
        binding.buttonCancel.setOnClickListener(v -> getParentFragmentManager().popBackStack());
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