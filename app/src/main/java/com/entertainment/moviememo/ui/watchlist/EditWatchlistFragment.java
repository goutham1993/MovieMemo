package com.entertainment.moviememo.ui.watchlist;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.entertainment.moviememo.R;
import com.entertainment.moviememo.data.entities.WatchlistItem;
import com.entertainment.moviememo.data.enums.Language;
import com.entertainment.moviememo.data.enums.WhereToWatch;
import com.entertainment.moviememo.databinding.FragmentEditWatchlistBinding;
import com.entertainment.moviememo.viewmodels.WatchlistViewModel;

import java.util.Calendar;

public class EditWatchlistFragment extends Fragment {

    private FragmentEditWatchlistBinding binding;
    private WatchlistViewModel viewModel;
    private WatchlistItem itemToEdit;
    private Calendar selectedReleaseDate;

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
        selectedReleaseDate = null;

        loadItemData();
        setupSpinners();
        setupWhereToWatchListener();
        setupReleaseDateToggle();
        setupReleaseDatePicker();
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

        // Language
        if (itemToEdit.language != null) {
            Language language = Language.fromCode(itemToEdit.language);
            int languageIndex = language.ordinal();
            binding.spinnerLanguage.setSelection(languageIndex);
        } else {
            binding.spinnerLanguage.setSelection(0); // Default to English
        }

        // Where to watch
        if (itemToEdit.whereToWatch != null) {
            try {
                WhereToWatch whereToWatch = WhereToWatch.valueOf(itemToEdit.whereToWatch);
                int whereToWatchIndex = whereToWatch.ordinal();
                binding.spinnerWhereToWatch.setSelection(whereToWatchIndex);
                
                // Show/hide theater release date UI based on selection
                if (whereToWatch == WhereToWatch.THEATER) {
                    binding.layoutTheaterReleaseDate.setVisibility(View.VISIBLE);
                    // Set release date if exists
                    if (itemToEdit.releaseDate != null) {
                        binding.switchReleaseDate.setChecked(true);
                        binding.buttonReleaseDate.setEnabled(true);
                        selectedReleaseDate = Calendar.getInstance();
                        selectedReleaseDate.setTimeInMillis(itemToEdit.releaseDate);
                        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
                        binding.buttonReleaseDate.setText("📅 " + dateFormat.format(selectedReleaseDate.getTime()));
                    }
                }
            } catch (IllegalArgumentException e) {
                binding.spinnerWhereToWatch.setSelection(0); // Default to Theater
            }
        } else {
            binding.spinnerWhereToWatch.setSelection(0); // Default to Theater
        }
    }

    private void setupSpinners() {
        // Priority spinner
        String[] priorities = {"Low", "Medium", "High"};
        android.widget.ArrayAdapter<String> priorityAdapter = new android.widget.ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, priorities);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerPriority.setAdapter(priorityAdapter);

        // Language spinner
        String[] languages = new String[Language.values().length];
        for (int i = 0; i < Language.values().length; i++) {
            languages[i] = Language.values()[i].getDisplayName();
        }
        android.widget.ArrayAdapter<String> languageAdapter = new android.widget.ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, languages);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerLanguage.setAdapter(languageAdapter);

        // Where to watch spinner
        String[] whereToWatchOptions = new String[WhereToWatch.values().length];
        for (int i = 0; i < WhereToWatch.values().length; i++) {
            whereToWatchOptions[i] = WhereToWatch.values()[i].getDisplayName();
        }
        android.widget.ArrayAdapter<String> whereToWatchAdapter = new android.widget.ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, whereToWatchOptions);
        whereToWatchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerWhereToWatch.setAdapter(whereToWatchAdapter);
    }

    private void setupWhereToWatchListener() {
        binding.spinnerWhereToWatch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                WhereToWatch selected = WhereToWatch.values()[position];
                if (selected == WhereToWatch.THEATER) {
                    binding.layoutTheaterReleaseDate.setVisibility(View.VISIBLE);
                } else {
                    binding.layoutTheaterReleaseDate.setVisibility(View.GONE);
                    binding.switchReleaseDate.setChecked(false);
                    binding.buttonReleaseDate.setEnabled(false);
                    binding.buttonReleaseDate.setText("📅 Select Release Date");
                    selectedReleaseDate = null;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                binding.layoutTheaterReleaseDate.setVisibility(View.GONE);
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
        itemToEdit.language = Language.values()[binding.spinnerLanguage.getSelectedItemPosition()].getCode();
        
        // Where to watch
        WhereToWatch whereToWatch = WhereToWatch.values()[binding.spinnerWhereToWatch.getSelectedItemPosition()];
        itemToEdit.whereToWatch = whereToWatch.name();
        
        // Release date (only if Theater and switch is on)
        if (whereToWatch == WhereToWatch.THEATER && binding.switchReleaseDate.isChecked() && selectedReleaseDate != null) {
            itemToEdit.releaseDate = selectedReleaseDate.getTimeInMillis();
        } else {
            itemToEdit.releaseDate = null;
        }

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
