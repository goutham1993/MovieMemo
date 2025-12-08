package com.entertainment.moviememo.ui.premium;

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
import com.entertainment.moviememo.utils.PremiumManager;

public class PaywallFragment extends Fragment {

    private FragmentPaywallBinding binding;
    private PremiumManager premiumManager;
    private OnSubscriptionPurchasedListener listener;

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
        setupClickListeners();
    }

    private void setupClickListeners() {
        // Monthly subscription
        binding.btnMonthly.setOnClickListener(v -> purchaseSubscription(PremiumManager.SubscriptionType.MONTHLY));

        // Yearly subscription
        binding.btnYearly.setOnClickListener(v -> purchaseSubscription(PremiumManager.SubscriptionType.YEARLY));

        // Lifetime subscription
        binding.btnLifetime.setOnClickListener(v -> purchaseSubscription(PremiumManager.SubscriptionType.LIFETIME));

        // Restore purchases
        binding.btnRestore.setOnClickListener(v -> restorePurchases());
    }

    private void purchaseSubscription(PremiumManager.SubscriptionType type) {
        // Mock purchase - in a real app, this would integrate with Google Play Billing
        try {
            premiumManager.setSubscription(type);
            
            String message = getString(R.string.purchase_success);
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            
            // Notify listener that subscription was purchased
            if (listener != null) {
                listener.onSubscriptionPurchased();
            }
        } catch (Exception e) {
            String message = getString(R.string.purchase_error);
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void restorePurchases() {
        // Mock restore - in a real app, this would query Google Play Billing
        // For now, just check if there's an active subscription
        if (premiumManager.hasActiveSubscription()) {
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
}

