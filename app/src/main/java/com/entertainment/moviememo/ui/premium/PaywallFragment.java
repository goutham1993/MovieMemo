package com.entertainment.moviememo.ui.premium;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.entertainment.moviememo.R;
import com.entertainment.moviememo.databinding.FragmentPaywallBinding;
import com.entertainment.moviememo.utils.BillingManager;
import com.entertainment.moviememo.utils.PremiumManager;

public class PaywallFragment extends Fragment {

    private FragmentPaywallBinding binding;
    private PremiumManager premiumManager;
    private BillingManager billingManager;
    private OnSubscriptionPurchasedListener listener;
    private PremiumManager.SubscriptionType selectedSubscriptionType = PremiumManager.SubscriptionType.YEARLY;

    public interface OnSubscriptionPurchasedListener {
        void onSubscriptionPurchased();
    }

    public void setOnSubscriptionPurchasedListener(OnSubscriptionPurchasedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPaywallBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        premiumManager = PremiumManager.getInstance(requireContext());
        billingManager = BillingManager.getInstance(requireContext());
        
        setupBillingListener();
        setupClickListeners();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (billingManager != null) {
            billingManager.setBillingStateListener(null);
        }
    }
    
    private void setupBillingListener() {
        billingManager.setBillingStateListener(new BillingManager.BillingStateListener() {
            @Override
            public void onBillingSetupFinished(boolean success) {
                if (!success) {
                    // Don't show error immediately - user might be testing without Google Play
                    // Toast.makeText(requireContext(), "Billing service unavailable - Test mode enabled", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onPurchaseSuccess(PremiumManager.SubscriptionType type) {
                String message = getString(R.string.purchase_success);
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                
                // Notify listener that subscription was purchased
                if (listener != null) {
                    listener.onSubscriptionPurchased();
                }
            }

            @Override
            public void onPurchaseError(String error) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRestoreSuccess(boolean hasActiveSubscription) {
                if (hasActiveSubscription) {
                    String message = getString(R.string.restore_success);
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    
                    // Notify listener that subscription was restored
                    if (listener != null) {
                        listener.onSubscriptionPurchased();
                    }
                } else {
                    String message = getString(R.string.restore_error);
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupClickListeners() {
        // Make cards clickable to select radio buttons
        binding.cardMonthly.setOnClickListener(v -> {
            binding.radioMonthly.setChecked(true);
            binding.radioYearly.setChecked(false);
            binding.radioLifetime.setChecked(false);
            selectedSubscriptionType = PremiumManager.SubscriptionType.MONTHLY;
        });

        binding.cardYearly.setOnClickListener(v -> {
            binding.radioMonthly.setChecked(false);
            binding.radioYearly.setChecked(true);
            binding.radioLifetime.setChecked(false);
            selectedSubscriptionType = PremiumManager.SubscriptionType.YEARLY;
        });

        binding.cardLifetime.setOnClickListener(v -> {
            binding.radioMonthly.setChecked(false);
            binding.radioYearly.setChecked(false);
            binding.radioLifetime.setChecked(true);
            selectedSubscriptionType = PremiumManager.SubscriptionType.LIFETIME;
        });

        // Radio button listeners
        binding.radioMonthly.setOnClickListener(v -> {
            binding.radioYearly.setChecked(false);
            binding.radioLifetime.setChecked(false);
            selectedSubscriptionType = PremiumManager.SubscriptionType.MONTHLY;
        });

        binding.radioYearly.setOnClickListener(v -> {
            binding.radioMonthly.setChecked(false);
            binding.radioLifetime.setChecked(false);
            selectedSubscriptionType = PremiumManager.SubscriptionType.YEARLY;
        });

        binding.radioLifetime.setOnClickListener(v -> {
            binding.radioMonthly.setChecked(false);
            binding.radioYearly.setChecked(false);
            selectedSubscriptionType = PremiumManager.SubscriptionType.LIFETIME;
        });

        // Single subscribe button
        binding.btnSubscribe.setOnClickListener(v -> purchaseSubscription(selectedSubscriptionType));

        // Restore purchases
        binding.btnRestore.setOnClickListener(v -> restorePurchases());
    }

    private void purchaseSubscription(PremiumManager.SubscriptionType type) {
        Activity activity = getActivity();
        if (activity == null) {
            Toast.makeText(requireContext(), "Activity not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // For now, always use test mode when billing service is not available
        // This allows testing without Google Play Billing setup
        if (!billingManager.isServiceConnected()) {
            performMockPurchase(type);
            return;
        }
        
        // Try to use real Google Play Billing if available
        String skuId = billingManager.getSkuForSubscriptionType(type);
        if (skuId == null) {
            // Fallback to test mode if SKU mapping fails
            performMockPurchase(type);
            return;
        }
        
        // Show loading message
        Toast.makeText(requireContext(), "Loading purchase options...", Toast.LENGTH_SHORT).show();
        
        billingManager.launchBillingFlow(activity, skuId);
    }
    
    private void performMockPurchase(PremiumManager.SubscriptionType type) {
        // Mock purchase for testing when billing service is not available
        // This allows testing the subscription flow without Google Play Billing
        try {
            // Show a brief message that we're using test mode
            Toast.makeText(requireContext(), "Processing test purchase...", Toast.LENGTH_SHORT).show();
            
            // Set the subscription in the local cache
            premiumManager.setSubscription(type);
            
            // Show success message
            String message = getString(R.string.purchase_success) + " (Test Mode)";
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            
            // Notify listener that subscription was purchased
            // This will trigger the StatsFragment to refresh and show stats
            if (listener != null) {
                listener.onSubscriptionPurchased();
            }
        } catch (Exception e) {
            String message = getString(R.string.purchase_error) + ": " + e.getMessage();
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private void restorePurchases() {
        if (!billingManager.isServiceConnected()) {
            Toast.makeText(requireContext(), "Billing service not ready. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        billingManager.restorePurchases();
    }
}

