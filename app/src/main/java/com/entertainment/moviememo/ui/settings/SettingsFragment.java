package com.entertainment.moviememo.ui.settings;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.entertainment.moviememo.data.database.AppDatabase;
import com.entertainment.moviememo.data.entities.NotificationSettings;
import com.entertainment.moviememo.databinding.FragmentSettingsBinding;
import com.entertainment.moviememo.utils.ExportImportHelper;
import com.entertainment.moviememo.utils.NotificationHelper;
import com.entertainment.moviememo.viewmodels.WatchedViewModel;
import com.entertainment.moviememo.viewmodels.WatchlistViewModel;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class SettingsFragment extends Fragment {

    private static final int REQUEST_CODE_IMPORT_JSON = 1001;
    private static final int REQUEST_CODE_IMPORT_CSV = 1002;

    private FragmentSettingsBinding binding;
    private WatchedViewModel watchedViewModel;
    private WatchlistViewModel watchlistViewModel;
    private NotificationSettings currentSettings;

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
        // Update time button
        if (currentSettings.notificationHour != null && currentSettings.notificationMinute != null) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, currentSettings.notificationHour);
            cal.set(Calendar.MINUTE, currentSettings.notificationMinute);
            
            java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault());
            String timeText = timeFormat.format(cal.getTime());
            binding.buttonNotificationTime.setText("⏰ " + timeText);
        } else {
            binding.buttonNotificationTime.setText("⏰ Set Notification Time");
        }
        
        // Update day checkboxes
        Set<Integer> selectedDays = getSelectedDays();
        
        // Calendar constants: SUNDAY=1, MONDAY=2, ..., SATURDAY=7
        // But we'll use: 0=Sunday, 1=Monday, ..., 6=Saturday
        binding.checkboxSunday.setChecked(selectedDays.contains(0));
        binding.checkboxMonday.setChecked(selectedDays.contains(1));
        binding.checkboxTuesday.setChecked(selectedDays.contains(2));
        binding.checkboxWednesday.setChecked(selectedDays.contains(3));
        binding.checkboxThursday.setChecked(selectedDays.contains(4));
        binding.checkboxFriday.setChecked(selectedDays.contains(5));
        binding.checkboxSaturday.setChecked(selectedDays.contains(6));
    }
    
    private Set<Integer> getSelectedDays() {
        Set<Integer> days = new HashSet<>();
        if (currentSettings.selectedDays != null && !currentSettings.selectedDays.isEmpty()) {
            String[] dayStrings = currentSettings.selectedDays.split(",");
            for (String dayString : dayStrings) {
                try {
                    days.add(Integer.parseInt(dayString.trim()));
                } catch (NumberFormatException e) {
                    // Skip invalid days
                }
            }
        }
        return days;
    }

    private void setupClickListeners() {
        binding.buttonBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        binding.buttonClearWatched.setOnClickListener(v -> showClearWatchedDialog());
        binding.buttonClearWatchlist.setOnClickListener(v -> showClearWatchlistDialog());
        
        binding.buttonNotificationTime.setOnClickListener(v -> showNotificationTimePicker());
        
        // Export/Import buttons
        binding.buttonExportJson.setOnClickListener(v -> exportToJson());
        binding.buttonExportCsv.setOnClickListener(v -> exportToCsv());
        binding.buttonImportJson.setOnClickListener(v -> importFromJson());
        binding.buttonImportCsv.setOnClickListener(v -> importFromCsv());
        
        // Day checkbox listeners
        binding.checkboxMonday.setOnCheckedChangeListener((buttonView, isChecked) -> onDayChanged());
        binding.checkboxTuesday.setOnCheckedChangeListener((buttonView, isChecked) -> onDayChanged());
        binding.checkboxWednesday.setOnCheckedChangeListener((buttonView, isChecked) -> onDayChanged());
        binding.checkboxThursday.setOnCheckedChangeListener((buttonView, isChecked) -> onDayChanged());
        binding.checkboxFriday.setOnCheckedChangeListener((buttonView, isChecked) -> onDayChanged());
        binding.checkboxSaturday.setOnCheckedChangeListener((buttonView, isChecked) -> onDayChanged());
        binding.checkboxSunday.setOnCheckedChangeListener((buttonView, isChecked) -> onDayChanged());
    }
    
    private void onDayChanged() {
        saveNotificationSettings();
        NotificationHelper.rescheduleAllNotifications(requireContext());
    }

    private void showNotificationTimePicker() {
        int currentHour = currentSettings.notificationHour != null ? currentSettings.notificationHour : 9;
        int currentMinute = currentSettings.notificationMinute != null ? currentSettings.notificationMinute : 0;
        
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    currentSettings.notificationHour = hourOfDay;
                    currentSettings.notificationMinute = minute;
                    saveNotificationSettings();
                    updateUI();
                    NotificationHelper.rescheduleAllNotifications(requireContext());
                    Toast.makeText(getContext(), "✅ Notification time set to " + String.format("%02d:%02d", hourOfDay, minute), Toast.LENGTH_SHORT).show();
                },
                currentHour,
                currentMinute,
                false
        );
        timePickerDialog.show();
    }

    private void saveNotificationSettings() {
        // Collect selected days from checkboxes
        Set<Integer> selectedDays = new HashSet<>();
        
        if (binding.checkboxMonday.isChecked()) selectedDays.add(1); // Monday
        if (binding.checkboxTuesday.isChecked()) selectedDays.add(2); // Tuesday
        if (binding.checkboxWednesday.isChecked()) selectedDays.add(3); // Wednesday
        if (binding.checkboxThursday.isChecked()) selectedDays.add(4); // Thursday
        if (binding.checkboxFriday.isChecked()) selectedDays.add(5); // Friday
        if (binding.checkboxSaturday.isChecked()) selectedDays.add(6); // Saturday
        if (binding.checkboxSunday.isChecked()) selectedDays.add(0); // Sunday
        
        // Convert to comma-separated string
        StringBuilder daysStringBuilder = new StringBuilder();
        boolean first = true;
        for (Integer day : selectedDays) {
            if (!first) {
                daysStringBuilder.append(",");
            }
            daysStringBuilder.append(day);
            first = false;
        }
        currentSettings.selectedDays = daysStringBuilder.toString();
        
        new Thread(() -> {
            AppDatabase.getDatabase(requireContext()).movieDao().insertNotificationSettings(currentSettings);
        }).start();
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

    private void exportToJson() {
        new Thread(() -> {
            ExportImportHelper.ExportResult result = ExportImportHelper.exportToJson(requireContext());
            requireActivity().runOnUiThread(() -> {
                if (result.success) {
                    showExportSuccessDialog("JSON", result.filePath);
                } else {
                    Toast.makeText(getContext(), "❌ Export failed: " + result.errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    private void exportToCsv() {
        new Thread(() -> {
            ExportImportHelper.ExportResult result = ExportImportHelper.exportToCsv(requireContext());
            requireActivity().runOnUiThread(() -> {
                if (result.success) {
                    showExportSuccessDialog("CSV", result.filePath);
                } else {
                    Toast.makeText(getContext(), "❌ Export failed: " + result.errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    private void showExportSuccessDialog(String format, String filePath) {
        new AlertDialog.Builder(getContext())
                .setTitle("✅ Export Successful")
                .setMessage("Data exported to " + format + " successfully!\n\nFile: " + filePath)
                .setPositiveButton("OK", null)
                .show();
    }

    private void importFromJson() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        String[] mimeTypes = {"application/json", "text/*"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(Intent.createChooser(intent, "Select JSON file"), REQUEST_CODE_IMPORT_JSON);
    }

    private void importFromCsv() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        String[] mimeTypes = {"text/csv", "text/*"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(Intent.createChooser(intent, "Select CSV file"), REQUEST_CODE_IMPORT_CSV);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (data == null || data.getData() == null) {
            return;
        }
        
        Uri uri = data.getData();
        String filePath = uri.getPath();
        
        // Handle different URI schemes
        if (uri.getScheme().equals("content")) {
            // For content:// URIs, we need to read the file differently
            try {
                java.io.InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                if (inputStream == null) {
                    Toast.makeText(getContext(), "❌ Unable to open file", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Create a temporary file to read from
                java.io.File tempFile = new java.io.File(requireContext().getCacheDir(), "temp_import_" + System.currentTimeMillis() + 
                    (requestCode == REQUEST_CODE_IMPORT_JSON ? ".json" : ".csv"));
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
                filePath = tempFile.getAbsolutePath();
            } catch (Exception e) {
                Toast.makeText(getContext(), "❌ Error reading file: " + e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
        }
        
        if (requestCode == REQUEST_CODE_IMPORT_JSON) {
            importJsonFile(filePath);
        } else if (requestCode == REQUEST_CODE_IMPORT_CSV) {
            importCsvFile(filePath);
        }
    }

    private void importJsonFile(String filePath) {
        new Thread(() -> {
            ExportImportHelper.ImportResult result = ExportImportHelper.importFromJson(requireContext(), filePath);
            requireActivity().runOnUiThread(() -> {
                if (result.success) {
                    Toast.makeText(getContext(), 
                        "✅ Import successful!\nWatched: " + result.watchedCount + "\nWatchlist: " + result.watchlistCount, 
                        Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "❌ Import failed: " + result.errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    private void importCsvFile(String filePath) {
        new Thread(() -> {
            ExportImportHelper.ImportResult result = ExportImportHelper.importFromCsv(requireContext(), filePath);
            requireActivity().runOnUiThread(() -> {
                if (result.success) {
                    Toast.makeText(getContext(), 
                        "✅ Import successful!\nWatched: " + result.watchedCount + "\nWatchlist: " + result.watchlistCount, 
                        Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "❌ Import failed: " + result.errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }
}