package com.entertainment.moviememo.ui.settings;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import androidx.lifecycle.ViewModelProvider;

import com.entertainment.moviememo.data.database.AppDatabase;
import com.entertainment.moviememo.data.entities.NotificationSettings;
import com.entertainment.moviememo.databinding.FragmentSettingsBinding;
import com.entertainment.moviememo.utils.BillingManager;
import com.entertainment.moviememo.utils.ExportImportHelper;
import com.entertainment.moviememo.utils.NotificationHelper;
import com.entertainment.moviememo.utils.PremiumManager;
import com.entertainment.moviememo.viewmodels.WatchedViewModel;
import com.entertainment.moviememo.viewmodels.WatchlistViewModel;


public class SettingsFragment extends Fragment {

    private static final int REQUEST_CODE_IMPORT_JSON = 1001;
    private static final int REQUEST_CODE_IMPORT_CSV = 1002;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1003;

    private FragmentSettingsBinding binding;
    private WatchedViewModel watchedViewModel;
    private WatchlistViewModel watchlistViewModel;
    private NotificationSettings currentSettings;
    private PremiumManager premiumManager;
    private BillingManager billingManager;
    private boolean isUpdatingUI = false;

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
        premiumManager = PremiumManager.getInstance(requireContext());
        billingManager = BillingManager.getInstance(requireContext());

        loadNotificationSettings();
        setupClickListeners();
        updatePremiumFeatures();
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
        // UI updates are now handled in NotificationSettingsFragment
        isUpdatingUI = false;
    }

    private void setupClickListeners() {
        // Header buttons
        binding.buttonBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        binding.buttonInfo.setOnClickListener(v -> showInfoDialog());
        
        // Notification card - navigate to notification settings
        binding.cardNotifications.setOnClickListener(v -> openNotificationSettings());
        
        // Subscription card
        binding.cardSubscription.setOnClickListener(v -> openSubscriptionScreen());
        
        // Restore subscription button
        binding.buttonRestoreSubscription.setOnClickListener(v -> restorePurchases());
        
        // Manage subscription button - open Google Play Store
        binding.buttonManageSubscription.setOnClickListener(v -> openGooglePlaySubscription());
        
        // Auto-sync switch (premium)
        binding.switchAutoSync.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isUpdatingUI) return;
            
            if (!premiumManager.isPremium()) {
                binding.switchAutoSync.setChecked(false);
                showPremiumRequiredDialog("Auto-Sync");
                return;
            }
            
            // TODO: Implement auto-sync logic
            Toast.makeText(getContext(), isChecked ? "Auto-Sync enabled" : "Auto-Sync disabled", Toast.LENGTH_SHORT).show();
        });
        
        // Auto-sync info button
        binding.buttonAutoSyncInfo.setOnClickListener(v -> showAutoSyncInfoDialog());
        
        // Delete All Data card
        binding.cardDeleteAllData.setOnClickListener(v -> showDeleteAllDataDialog());
        
        // FAB - Contact/Feedback
        binding.fabContact.setOnClickListener(v -> openContactEmail());
        
        // Import/Export - available to all users
        binding.cardImportExport.setOnClickListener(v -> showImportExportOptions());
        
        // Premium feature cards (backup/restore) are handled in updatePremiumFeatures()
    }

    private void openNotificationSettings() {
        NotificationSettingsFragment fragment = new NotificationSettingsFragment();
        getParentFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment)
                .addToBackStack(null)
                .commit();
    }
    
    private void restorePurchases() {
        if (!billingManager.isServiceConnected()) {
            Toast.makeText(getContext(), "Billing service not ready. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        billingManager.setBillingStateListener(new BillingManager.BillingStateListener() {
            @Override
            public void onBillingSetupFinished(boolean success) {}
            
            @Override
            public void onPurchaseSuccess(PremiumManager.SubscriptionType type) {}
            
            @Override
            public void onPurchaseError(String error) {}
            
            @Override
            public void onRestoreSuccess(boolean hasActiveSubscription) {
                if (hasActiveSubscription) {
                    Toast.makeText(getContext(), "✅ Subscription restored!", Toast.LENGTH_SHORT).show();
                    updatePremiumFeatures();
                } else {
                    Toast.makeText(getContext(), "No active subscription found", Toast.LENGTH_SHORT).show();
                }
                billingManager.setBillingStateListener(null);
            }
        });
        
        billingManager.restorePurchases();
        Toast.makeText(getContext(), "Restoring purchases...", Toast.LENGTH_SHORT).show();
    }
    
    private void openGooglePlaySubscription() {
        try {
            // Open Google Play Store subscription management page
            String packageName = requireContext().getPackageName();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://play.google.com/store/account/subscriptions?package=" + packageName));
            intent.setPackage("com.android.vending");
            
            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                // Fallback to web browser if Play Store app is not available
                intent.setPackage(null);
                startActivity(intent);
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Unable to open Google Play Store", Toast.LENGTH_SHORT).show();
        }
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

    private void showInfoDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("About Settings")
                .setMessage("Manage your app preferences, notifications, and data here. Premium features require a subscription.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showAutoSyncInfoDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Auto-Sync")
                .setMessage("Automatically sync your data to the cloud. This feature requires a premium subscription.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showDeleteAllDataDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete All Data")
                .setMessage("Are you sure you want to permanently delete all local data? This will remove:\n\n• All watched movies\n• All watchlist items\n• All notification settings\n\nNote: Cloud backup will remain if you have one.")
                .setPositiveButton("Delete All", (dialog, which) -> {
                    deleteAllData();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAllData() {
        // Clear watched movies
        watchedViewModel.clearAllWatched();
        
        // Clear watchlist
        watchlistViewModel.clearAllWatchlist();
        
        // Clear notification settings
        new Thread(() -> {
            if (currentSettings != null) {
                currentSettings.selectedDays = "";
                currentSettings.notificationHour = null;
                currentSettings.notificationMinute = null;
                AppDatabase.getDatabase(requireContext()).movieDao().insertNotificationSettings(currentSettings);
            }
            NotificationHelper.cancelAllNotifications(requireContext());
        }).start();
        
        Toast.makeText(getContext(), "✅ All local data deleted", Toast.LENGTH_SHORT).show();
    }

    private void updatePremiumFeatures() {
        boolean isPremium = premiumManager.isPremium();
        
        // Update subscription status text
        if (isPremium) {
            binding.textSubscriptionStatus.setText("Premium Active");
            binding.textSubscriptionDetails.setText("You have an active premium subscription");
        } else {
            binding.textSubscriptionStatus.setText("Premium Subscription");
            binding.textSubscriptionDetails.setText("Upgrade to unlock premium features");
        }
        
        // Enable/disable auto-sync switch based on premium status
        binding.switchAutoSync.setEnabled(isPremium);
        if (!isPremium) {
            binding.switchAutoSync.setChecked(false);
        }
        
        // Set click listeners for premium feature cards
        binding.cardBackupRestore.setOnClickListener(v -> {
            if (isPremium) {
                // TODO: Open backup/restore screen
                Toast.makeText(getContext(), "Backup & Restore (Coming soon)", Toast.LENGTH_SHORT).show();
            } else {
                showPremiumRequiredDialog("Backup & Restore");
            }
        });
    }
    
    private void openSubscriptionScreen() {
        com.entertainment.moviememo.ui.premium.PaywallFragment paywallFragment = new com.entertainment.moviememo.ui.premium.PaywallFragment();
        paywallFragment.setOnSubscriptionPurchasedListener(() -> {
            // Refresh premium features after purchase
            updatePremiumFeatures();
            Toast.makeText(getContext(), "✅ Premium activated!", Toast.LENGTH_SHORT).show();
        });
        
        getParentFragmentManager().beginTransaction()
                .replace(android.R.id.content, paywallFragment)
                .addToBackStack(null)
                .commit();
    }
    
    private void showImportExportOptions() {
        String[] options = {"Export to JSON", "Export to CSV", "Import from JSON", "Import from CSV"};
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Import & Export")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            exportToJson();
                            break;
                        case 1:
                            exportToCsv();
                            break;
                        case 2:
                            importFromJson();
                            break;
                        case 3:
                            importFromCsv();
                            break;
                    }
                })
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

    private void showPremiumRequiredDialog(String featureName) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Premium Feature")
                .setMessage(featureName + " is a premium feature. Upgrade to access this feature.")
                .setPositiveButton("Upgrade", (dialog, which) -> {
                    // TODO: Navigate to paywall/subscription screen
                    Toast.makeText(getContext(), "Opening subscription page...", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openContactEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@moviememo.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "MovieMemo Feedback");
        
        if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
            startActivity(Intent.createChooser(intent, "Send email"));
        } else {
            Toast.makeText(getContext(), "No email app found", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        // Update UI when returning to the fragment (e.g., after changing system settings)
        loadNotificationSettings();
        updatePremiumFeatures();
    }
}
