package com.entertainment.moviememo.ui.settings;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.Observer;

import com.entertainment.moviememo.R;
import com.entertainment.moviememo.databinding.FragmentSettingsBinding;
import com.entertainment.moviememo.viewmodels.WatchedViewModel;
import com.entertainment.moviememo.viewmodels.WatchlistViewModel;
import com.entertainment.moviememo.utils.NotificationHelper;

import java.util.Calendar;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private WatchedViewModel watchedViewModel;
    private WatchlistViewModel watchlistViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        watchedViewModel = new ViewModelProvider(this).get(WatchedViewModel.class);
        watchlistViewModel = new ViewModelProvider(this).get(WatchlistViewModel.class);

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.buttonBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        binding.buttonClearWatched.setOnClickListener(v -> showClearWatchedDialog());
        binding.buttonClearWatchlist.setOnClickListener(v -> showClearWatchlistDialog());
        
        binding.buttonNotificationTime.setOnClickListener(v -> showNotificationTimePicker());
        
        updateNotificationTimeButton();
    }

    private void updateNotificationTimeButton() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MovieMemoPrefs", android.content.Context.MODE_PRIVATE);
        int hour = prefs.getInt("notification_hour", 9);
        int minute = prefs.getInt("notification_minute", 0);
        
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        
        java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault());
        String timeText = timeFormat.format(cal.getTime());
        binding.buttonNotificationTime.setText("⏰ " + timeText);
    }

    private void showNotificationTimePicker() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MovieMemoPrefs", android.content.Context.MODE_PRIVATE);
        int currentHour = prefs.getInt("notification_hour", 9);
        int currentMinute = prefs.getInt("notification_minute", 0);
        
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("notification_hour", hourOfDay);
                    editor.putInt("notification_minute", minute);
                    editor.apply();
                    
                    updateNotificationTimeButton();
                    
                    // Reschedule all notifications with new time (one-time observation)
                    Observer<java.util.List<com.entertainment.moviememo.data.entities.WatchlistItem>> observer = new Observer<java.util.List<com.entertainment.moviememo.data.entities.WatchlistItem>>() {
                        @Override
                        public void onChanged(java.util.List<com.entertainment.moviememo.data.entities.WatchlistItem> items) {
                            if (items != null) {
                                NotificationHelper.rescheduleAllNotifications(requireContext(), items);
                                // Remove observer after first update
                                watchlistViewModel.getAllWatchlist().removeObserver(this);
                            }
                        }
                    };
                    watchlistViewModel.getAllWatchlist().observe(getViewLifecycleOwner(), observer);
                    
                    Toast.makeText(getContext(), "✅ Notification time set to " + String.format("%02d:%02d", hourOfDay, minute), Toast.LENGTH_SHORT).show();
                },
                currentHour,
                currentMinute,
                false
        );
        timePickerDialog.show();
    }

    private void showClearWatchedDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("🗑️ Clear Watched Movies")
                .setMessage("Are you sure you want to delete all watched movies? This action cannot be undone.")
                .setPositiveButton("🗑️ Delete All", (dialog, which) -> {
                    watchedViewModel.clearAllWatched();
                    Toast.makeText(getContext(), "🗑️ All watched movies deleted!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showClearWatchlistDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("🗑️ Clear Watchlist")
                .setMessage("Are you sure you want to delete all watchlist items? This action cannot be undone.")
                .setPositiveButton("🗑️ Delete All", (dialog, which) -> {
                    watchlistViewModel.clearAllWatchlist();
                    Toast.makeText(getContext(), "🎫 Watchlist cleared!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}