package com.entertainment.moviememo.ui.about;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.entertainment.moviememo.R;
import com.entertainment.moviememo.databinding.FragmentAboutBinding;

public class AboutFragment extends Fragment {

    private FragmentAboutBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAboutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.textAppName.setText("🎬 MovieMemo");
        binding.textVersion.setText("Version 1.0.0");
        binding.textDescription.setText("Track your movie watching journey with MovieMemo! Keep a record of all the movies you've watched, maintain a watchlist, and discover insights about your viewing habits.");
        
        binding.buttonBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
    }
}