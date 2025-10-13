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
    private NumberFormat currencyFormat;

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
        loadEntryData();
        setupDatePicker();
        setupGenreAutocomplete();
        setupClickListeners();
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(WatchedViewModel.class);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
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
                binding.editDate.setText(dateFormat.format(date));
            }
        } catch (ParseException e) {
            binding.editDate.setText(entryToEdit.watchedDate);
        }

        // Location type
        switch (entryToEdit.locationType) {
            case "THEATER":
                binding.chipTheater.setChecked(true);
                break;
            case "HOME":
                binding.chipHome.setChecked(true);
                break;
            case "FRIENDS_HOME":
                binding.chipFriends.setChecked(true);
                break;
            case "OTHER":
                binding.chipOther.setChecked(true);
                break;
        }

        // Time of day
        switch (entryToEdit.timeOfDay) {
            case "MORNING":
                binding.chipMorning.setChecked(true);
                break;
            case "AFTERNOON":
                binding.chipAfternoon.setChecked(true);
                break;
            case "EVENING":
                binding.chipEvening.setChecked(true);
                break;
            case "NIGHT":
                binding.chipNight.setChecked(true);
                break;
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

        // Location notes
        if (entryToEdit.locationNotes != null) {
            binding.editLocationNotes.setText(entryToEdit.locationNotes);
        }

        // Notes
        if (entryToEdit.notes != null) {
            binding.editNotes.setText(entryToEdit.notes);
        }
    }

    private void setupDatePicker() {
        binding.editDate.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    binding.editDate.setText(dateFormat.format(selectedDate.getTime()));
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void setupGenreAutocomplete() {
        viewModel.getAllGenres().observe(getViewLifecycleOwner(), genres -> {
            if (genres != null) {
                List<String> genreNames = new ArrayList<>();
                for (com.entertainment.moviememo.data.entities.Genre genre : genres) {
                    genreNames.add(genre.name);
                }
                
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        getContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        genreNames
                );
                binding.editGenre.setAdapter(adapter);
            }
        });
    }

    private void setupClickListeners() {
        binding.buttonCancel.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        binding.buttonDelete.setOnClickListener(v -> showDeleteConfirmation());

        binding.buttonSave.setOnClickListener(v -> updateMovie());
    }

    private void showDeleteConfirmation() {
        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Delete Movie")
                .setMessage("Are you sure you want to delete \"" + entryToEdit.title + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteMovie())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteMovie() {
        viewModel.deleteWatched(entryToEdit);
        Toast.makeText(getContext(), "Movie deleted successfully!", Toast.LENGTH_SHORT).show();
        
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
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

    private boolean validateForm() {
        boolean isValid = true;

        // Validate title (required)
        String title = binding.editTitle.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            binding.layoutTitle.setError("Title is required");
            isValid = false;
        } else {
            binding.layoutTitle.setError(null);
        }

        // Validate rating (0-10)
        String ratingText = binding.editRating.getText().toString().trim();
        if (!TextUtils.isEmpty(ratingText)) {
            try {
                int rating = Integer.parseInt(ratingText);
                if (rating < 0 || rating > 10) {
                    binding.layoutRating.setError("Rating must be between 0 and 10");
                    isValid = false;
                } else {
                    binding.layoutRating.setError(null);
                }
            } catch (NumberFormatException e) {
                binding.layoutRating.setError("Invalid rating format");
                isValid = false;
            }
        }

        // Validate duration
        String durationText = binding.editDuration.getText().toString().trim();
        if (!TextUtils.isEmpty(durationText)) {
            try {
                int duration = Integer.parseInt(durationText);
                if (duration <= 0) {
                    binding.layoutDuration.setError("Duration must be positive");
                    isValid = false;
                } else {
                    binding.layoutDuration.setError(null);
                }
            } catch (NumberFormatException e) {
                binding.layoutDuration.setError("Invalid duration format");
                isValid = false;
            }
        }

        // Validate spend amount
        String spendText = binding.editSpend.getText().toString().trim();
        if (!TextUtils.isEmpty(spendText)) {
            try {
                double spend = Double.parseDouble(spendText);
                if (spend < 0) {
                    binding.layoutSpend.setError("Amount cannot be negative");
                    isValid = false;
                } else {
                    binding.layoutSpend.setError(null);
                }
            } catch (NumberFormatException e) {
                binding.layoutSpend.setError("Invalid amount format");
                isValid = false;
            }
        }

        return isValid;
    }

    private WatchedEntry createUpdatedEntry() {
        // Create a new entry with the same ID but updated data
        WatchedEntry updatedEntry = new WatchedEntry(
            binding.editTitle.getText().toString().trim(),
            dateFormat.format(selectedDate.getTime()),
            getSelectedLocationType().name(),
            getSelectedTimeOfDay().name()
        );
        
        updatedEntry.id = entryToEdit.id; // Keep the same ID

        // Set optional fields
        String ratingText = binding.editRating.getText().toString().trim();
        if (!TextUtils.isEmpty(ratingText)) {
            updatedEntry.rating = Integer.parseInt(ratingText);
        }

        String durationText = binding.editDuration.getText().toString().trim();
        if (!TextUtils.isEmpty(durationText)) {
            updatedEntry.durationMin = Integer.parseInt(durationText);
        }

        String spendText = binding.editSpend.getText().toString().trim();
        if (!TextUtils.isEmpty(spendText)) {
            double spend = Double.parseDouble(spendText);
            updatedEntry.spendCents = (int) Math.round(spend * 100);
        }

        String genre = binding.editGenre.getText().toString().trim();
        if (!TextUtils.isEmpty(genre)) {
            updatedEntry.genre = genre;
        }

        String companions = binding.editCompanions.getText().toString().trim();
        if (!TextUtils.isEmpty(companions)) {
            updatedEntry.companions = companions;
        }

        String locationNotes = binding.editLocationNotes.getText().toString().trim();
        if (!TextUtils.isEmpty(locationNotes)) {
            updatedEntry.locationNotes = locationNotes;
        }

        String notes = binding.editNotes.getText().toString().trim();
        if (!TextUtils.isEmpty(notes)) {
            updatedEntry.notes = notes;
        }

        return updatedEntry;
    }

    private LocationType getSelectedLocationType() {
        if (binding.chipHome.isChecked()) {
            return LocationType.HOME;
        } else if (binding.chipFriends.isChecked()) {
            return LocationType.FRIENDS_HOME;
        } else if (binding.chipOther.isChecked()) {
            return LocationType.OTHER;
        } else {
            return LocationType.THEATER;
        }
    }

    private TimeOfDay getSelectedTimeOfDay() {
        if (binding.chipMorning.isChecked()) {
            return TimeOfDay.MORNING;
        } else if (binding.chipAfternoon.isChecked()) {
            return TimeOfDay.AFTERNOON;
        } else if (binding.chipNight.isChecked()) {
            return TimeOfDay.NIGHT;
        } else {
            return TimeOfDay.EVENING;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
