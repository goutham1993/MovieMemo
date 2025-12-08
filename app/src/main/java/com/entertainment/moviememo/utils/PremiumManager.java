package com.entertainment.moviememo.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class PremiumManager {
    
    private static final String PREFS_NAME = "premium_prefs";
    private static final String KEY_SUBSCRIPTION_TYPE = "subscription_type";
    private static final String KEY_SUBSCRIPTION_EXPIRY = "subscription_expiry";
    
    public enum SubscriptionType {
        NONE,
        MONTHLY,
        YEARLY,
        LIFETIME
    }
    
    private static PremiumManager instance;
    private SharedPreferences prefs;
    
    private PremiumManager(Context context) {
        try {
            // Create or retrieve master key
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            
            // Create EncryptedSharedPreferences
            prefs = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            // Fallback to regular SharedPreferences if encryption fails
            // This should rarely happen, but provides a safety net
            prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
    }
    
    public static synchronized PremiumManager getInstance(Context context) {
        if (instance == null) {
            instance = new PremiumManager(context);
        }
        return instance;
    }
    
    public boolean isPremium() {
        return hasActiveSubscription();
    }
    
    public boolean hasActiveSubscription() {
        SubscriptionType type = getSubscriptionType();
        
        if (type == SubscriptionType.NONE) {
            return false;
        }
        
        if (type == SubscriptionType.LIFETIME) {
            return true; // Lifetime never expires
        }
        
        // Check expiry for monthly and yearly
        long expiryTime = prefs.getLong(KEY_SUBSCRIPTION_EXPIRY, 0);
        long currentTime = System.currentTimeMillis();
        
        return currentTime < expiryTime;
    }
    
    public SubscriptionType getSubscriptionType() {
        String typeString = prefs.getString(KEY_SUBSCRIPTION_TYPE, SubscriptionType.NONE.name());
        try {
            return SubscriptionType.valueOf(typeString);
        } catch (IllegalArgumentException e) {
            return SubscriptionType.NONE;
        }
    }
    
    public void setSubscription(SubscriptionType type) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_SUBSCRIPTION_TYPE, type.name());
        
        if (type == SubscriptionType.LIFETIME) {
            // Lifetime never expires, set a very far future date
            editor.putLong(KEY_SUBSCRIPTION_EXPIRY, Long.MAX_VALUE);
        } else if (type == SubscriptionType.MONTHLY) {
            // Monthly subscription expires in 30 days
            long expiryTime = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000);
            editor.putLong(KEY_SUBSCRIPTION_EXPIRY, expiryTime);
        } else if (type == SubscriptionType.YEARLY) {
            // Yearly subscription expires in 365 days
            long expiryTime = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000);
            editor.putLong(KEY_SUBSCRIPTION_EXPIRY, expiryTime);
        }
        
        editor.apply();
    }
    
    /**
     * Update subscription from Google Play purchase
     * This is called by BillingManager when a purchase is verified
     */
    public void setSubscriptionFromPurchase(SubscriptionType type, long purchaseTime) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_SUBSCRIPTION_TYPE, type.name());
        
        if (type == SubscriptionType.LIFETIME) {
            // Lifetime never expires
            editor.putLong(KEY_SUBSCRIPTION_EXPIRY, Long.MAX_VALUE);
        } else if (type == SubscriptionType.MONTHLY) {
            // Monthly subscription - Google Play will handle renewal
            // Set expiry to 31 days from purchase time (slightly longer for safety)
            long expiryTime = purchaseTime + (31L * 24 * 60 * 60 * 1000);
            editor.putLong(KEY_SUBSCRIPTION_EXPIRY, expiryTime);
        } else if (type == SubscriptionType.YEARLY) {
            // Yearly subscription - Google Play will handle renewal
            // Set expiry to 366 days from purchase time (slightly longer for safety)
            long expiryTime = purchaseTime + (366L * 24 * 60 * 60 * 1000);
            editor.putLong(KEY_SUBSCRIPTION_EXPIRY, expiryTime);
        }
        
        editor.apply();
    }
    
    public void clearSubscription() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_SUBSCRIPTION_TYPE);
        editor.remove(KEY_SUBSCRIPTION_EXPIRY);
        editor.apply();
    }
    
    public long getExpiryTime() {
        return prefs.getLong(KEY_SUBSCRIPTION_EXPIRY, 0);
    }
}

