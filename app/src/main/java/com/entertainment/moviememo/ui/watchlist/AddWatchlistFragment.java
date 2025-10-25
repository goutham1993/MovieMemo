package com.entertainment.moviememo.ui.watchlist;

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
import com.entertainment.moviememo.data.entities.WatchlistItem;
import com.entertainment.moviememo.data.enums.Language;
import com.entertainment.moviememo.databinding.FragmentAddWatchlistBinding;
import com.entertainment.moviememo.viewmodels.WatchlistViewModel;

public class AddWatchlistFragment extends Fragment {

    private FragmentAddWatchlistBinding binding;
    private WatchlistViewModel viewModel;

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

        setupSpinner();
        binding.buttonSave.setOnClickListener(v -> saveWatchlistItem());
        binding.buttonCancel.setOnClickListener(v -> getParentFragmentManager().popBackStack());
    }

    private void setupSpinner() {
        // Language spinner
        String[] languages = new String[Language.values().length];
        for (int i = 0; i < Language.values().length; i++) {
            languages[i] = Language.values()[i].getDisplayName();
        }
        android.widget.ArrayAdapter<String> languageAdapter = new android.widget.ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, languages);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerLanguage.setAdapter(languageAdapter);
        binding.spinnerLanguage.setSelection(0); // Default to English
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

        viewModel.insertWatchlist(item);
        Toast.makeText(getContext(), "🎫 Added to watchlist!", Toast.LENGTH_SHORT).show();
        getParentFragmentManager().popBackStack();
    }
}
