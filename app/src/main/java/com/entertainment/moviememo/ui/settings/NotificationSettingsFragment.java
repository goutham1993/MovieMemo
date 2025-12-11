package com.entertainment.moviememo.ui.settings;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.entertainment.moviememo.data.database.AppDatabase;
import com.entertainment.moviememo.data.entities.NotificationSettings;
import com.entertainment.moviememo.databinding.FragmentNotificationSettingsBinding;
import com.entertainment.moviememo.utils.NotificationHelper;

import java.util.HashSet;
import java.util.Set;

public class NotificationSettingsFragment extends Fragment {

    private static final int REQUEST_NOTIFICATION_PERMISSION = 1003;

    private FragmentNotificationSettingsBinding binding;
    private NotificationSettings currentSettings;
    private boolean isUpdatingUI = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNotificationSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadNotificationSettings();
        setupClickListeners();
    }

    private void loadNotificationSettings() {
        new Thread(() -> {
            currentSettings = AppDatabase.getDatabase(requireContext()).movieDao().getNotificationSettings();
            if (currentSettings == null) {
                currentSettings = new NotificationSettings();
                currentSettings.id = 1;
            }
            
            requireActivity().runOnUiThread(() -> {
                updateUI();
            });
        }).start();
    }

    private void updateUI() {
        isUpdatingUI = true;
        
        // Update notification switch based on:
        // 1. System notifications are enabled
        // 2. App has notification settings configured (selectedDays is not empty)
        boolean systemNotificationsEnabled = NotificationHelper.areNotificationsEnabled(requireContext());
        boolean appNotificationsConfigured = currentSettings != null 
                && currentSettings.selectedDays != null 
                && !currentSettings.selectedDays.isEmpty();
        
        // Switch should be checked only if both system and app notifications are enabled
        boolean notificationsEnabled = systemNotificationsEnabled && appNotificationsConfigured;
        binding.switchNotifications.setChecked(notificationsEnabled);
        
        // Update weekday checkboxes
        updateWeekdayCheckboxes();
        
        isUpdatingUI = false;
    }
    
    private void updateWeekdayCheckboxes() {
        if (currentSettings == null || currentSettings.selectedDays == null || currentSettings.selectedDays.isEmpty()) {
            // Set default: all days selected
            if (currentSettings != null) {
                currentSettings.selectedDays = "0,1,2,3,4,5,6";
            }
            return;
        }
        
        // Parse selected days (0=Sunday, 1=Monday, ..., 6=Saturday)
        String[] dayStrings = currentSettings.selectedDays.split(",");
        Set<Integer> selectedDays = new HashSet<>();
        for (String dayString : dayStrings) {
            try {
                int day = Integer.parseInt(dayString.trim());
                if (day >= 0 && day <= 6) {
                    selectedDays.add(day);
                }
            } catch (NumberFormatException e) {
                // Skip invalid days
            }
        }
        
        // Update checkboxes (0=Sunday, 1=Monday, ..., 6=Saturday)
        binding.checkboxSunday.setChecked(selectedDays.contains(0));
        binding.checkboxMonday.setChecked(selectedDays.contains(1));
        binding.checkboxTuesday.setChecked(selectedDays.contains(2));
        binding.checkboxWednesday.setChecked(selectedDays.contains(3));
        binding.checkboxThursday.setChecked(selectedDays.contains(4));
        binding.checkboxFriday.setChecked(selectedDays.contains(5));
        binding.checkboxSaturday.setChecked(selectedDays.contains(6));
    }
    
    private void saveSelectedDays() {
        if (currentSettings == null) {
            return;
        }
        
        Set<Integer> selectedDays = new HashSet<>();
        if (binding.checkboxSunday.isChecked()) selectedDays.add(0);
        if (binding.checkboxMonday.isChecked()) selectedDays.add(1);
        if (binding.checkboxTuesday.isChecked()) selectedDays.add(2);
        if (binding.checkboxWednesday.isChecked()) selectedDays.add(3);
        if (binding.checkboxThursday.isChecked()) selectedDays.add(4);
        if (binding.checkboxFriday.isChecked()) selectedDays.add(5);
        if (binding.checkboxSaturday.isChecked()) selectedDays.add(6);
        
        // Convert to comma-separated string
        StringBuilder sb = new StringBuilder();
        for (Integer day : selectedDays) {
            if (sb.length() > 0) sb.append(",");
            sb.append(day);
        }
        currentSettings.selectedDays = sb.toString();
        
        saveNotificationSettings();
        NotificationHelper.rescheduleAllNotifications(requireContext());
    }

    private void setupClickListeners() {
        // Header buttons
        binding.buttonBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        
        // Notification switch
        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isUpdatingUI) return;
            
            if (isChecked) {
                enableNotifications();
            } else {
                disableNotifications();
            }
        });
        
        // Weekday checkboxes
        binding.checkboxSunday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isUpdatingUI) saveSelectedDays();
        });
        binding.checkboxMonday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isUpdatingUI) saveSelectedDays();
        });
        binding.checkboxTuesday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isUpdatingUI) saveSelectedDays();
        });
        binding.checkboxWednesday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isUpdatingUI) saveSelectedDays();
        });
        binding.checkboxThursday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isUpdatingUI) saveSelectedDays();
        });
        binding.checkboxFriday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isUpdatingUI) saveSelectedDays();
        });
        binding.checkboxSaturday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isUpdatingUI) saveSelectedDays();
        });
    }

    private void enableNotifications() {
        // Check permissions first
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        requireActivity(),
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION
                );
                binding.switchNotifications.setChecked(false);
                return;
            }
        }
        
        // Check if notifications are enabled in system settings
        if (!NotificationHelper.areNotificationsEnabled(requireContext())) {
            showPermissionWarning("Notifications are disabled. Please enable them in system settings to receive reminders.");
            binding.switchNotifications.setChecked(false);
            return;
        }
        
        // Enable notifications
        if (currentSettings.selectedDays == null || currentSettings.selectedDays.isEmpty()) {
            // Set default: all days selected
            currentSettings.selectedDays = "0,1,2,3,4,5,6";
            updateWeekdayCheckboxes();
        }
        if (currentSettings.notificationHour == null) {
            currentSettings.notificationHour = 9;
            currentSettings.notificationMinute = 0;
        }
        
        saveNotificationSettings();
        NotificationHelper.rescheduleAllNotifications(requireContext());
        
        // Update UI to reflect enabled state
        updateUI();
        
        Toast.makeText(getContext(), "✅ Notifications enabled", Toast.LENGTH_SHORT).show();
    }

    private void disableNotifications() {
        NotificationHelper.cancelAllNotifications(requireContext());
        
        // Clear notification settings to reflect disabled state
        if (currentSettings != null) {
            currentSettings.selectedDays = "";
            saveNotificationSettings();
        }
        
        // Update UI to reflect disabled state
        updateUI();
        
        Toast.makeText(getContext(), "Notifications disabled", Toast.LENGTH_SHORT).show();
    }

    private void saveNotificationSettings() {
        new Thread(() -> {
            AppDatabase.getDatabase(requireContext()).movieDao().insertNotificationSettings(currentSettings);
        }).start();
    }

    private void showPermissionWarning(String message) {
        new AlertDialog.Builder(requireContext())
                .setTitle("⚠️ Permission Required")
                .setMessage(message)
                .setPositiveButton("Open Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().getPackageName());
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                binding.switchNotifications.setChecked(true);
                enableNotifications();
            } else {
                binding.switchNotifications.setChecked(false);
                Toast.makeText(getContext(), "⚠️ Notifications require permission to work", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update UI when returning to the fragment (e.g., after changing system settings)
        loadNotificationSettings();
    }
}

