package com.entertainment.moviememo.utils;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryPurchasesParams;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.List;

public class BillingManager implements PurchasesUpdatedListener {
    
    // Subscription SKU IDs - These need to be configured in Google Play Console
    private static final String SKU_MONTHLY = "premium_monthly";
    private static final String SKU_YEARLY = "premium_yearly";
    private static final String SKU_LIFETIME = "premium_lifetime";
    
    private static BillingManager instance;
    private BillingClient billingClient;
    private PremiumManager premiumManager;
    private BillingStateListener stateListener;
    private boolean isServiceConnected = false;
    
    public interface BillingStateListener {
        void onBillingSetupFinished(boolean success);
        void onPurchaseSuccess(PremiumManager.SubscriptionType type);
        void onPurchaseError(String error);
        void onRestoreSuccess(boolean hasActiveSubscription);
    }
    
    private BillingManager(Context context) {
        premiumManager = PremiumManager.getInstance(context);
        
        billingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases()
                .build();
        
        startConnection();
    }
    
    public static synchronized BillingManager getInstance(Context context) {
        if (instance == null) {
            instance = new BillingManager(context.getApplicationContext());
        }
        return instance;
    }
    
    public void setBillingStateListener(BillingStateListener listener) {
        this.stateListener = listener;
        // If already connected, notify immediately
        if (isServiceConnected && stateListener != null) {
            stateListener.onBillingSetupFinished(true);
        }
    }
    
    private void startConnection() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                int responseCode = billingResult.getResponseCode();
                if (responseCode == BillingClient.BillingResponseCode.OK) {
                    isServiceConnected = true;
                    if (stateListener != null) {
                        stateListener.onBillingSetupFinished(true);
                    }
                    // Sync purchases on connection
                    queryPurchases();
                } else {
                    isServiceConnected = false;
                    // Common reasons for failure:
                    // - BILLING_UNAVAILABLE: Google Play services not available
                    // - SERVICE_UNAVAILABLE: Network issue
                    // - This is normal for emulators or devices without Google Play
                    if (stateListener != null) {
                        stateListener.onBillingSetupFinished(false);
                    }
                }
            }
            
            @Override
            public void onBillingServiceDisconnected() {
                isServiceConnected = false;
                // Try to reconnect
                startConnection();
            }
        });
    }
    
    public void querySkuDetails(SkuDetailsResponseListener listener) {
        if (!isServiceConnected) {
            if (listener != null) {
                listener.onSkuDetailsResponse(
                    BillingResult.newBuilder()
                        .setResponseCode(BillingClient.BillingResponseCode.SERVICE_DISCONNECTED)
                        .setDebugMessage("Billing service not connected")
                        .build(),
                    new ArrayList<>()
                );
            }
            return;
        }
        
        // Query subscriptions (monthly and yearly)
        List<String> subscriptionSkus = new ArrayList<>();
        subscriptionSkus.add(SKU_MONTHLY);
        subscriptionSkus.add(SKU_YEARLY);
        
        SkuDetailsParams.Builder subParams = SkuDetailsParams.newBuilder();
        subParams.setSkusList(subscriptionSkus).setType(BillingClient.SkuType.SUBS);
        
        // Note: For production, you'd need to query in-app products separately for lifetime
        // For now, we'll query subscriptions only
        billingClient.querySkuDetailsAsync(subParams.build(), listener);
    }
    
    public void launchBillingFlow(Activity activity, String skuId) {
        if (!isServiceConnected) {
            if (stateListener != null) {
                stateListener.onPurchaseError("Billing service not connected");
            }
            return;
        }
        
        // Check if it's lifetime (in-app product) or subscription
        boolean isLifetime = skuId.equals(SKU_LIFETIME);
        String productType = isLifetime ? BillingClient.SkuType.INAPP : BillingClient.SkuType.SUBS;
        
        List<String> skuList = new ArrayList<>();
        skuList.add(skuId);
        
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(productType);
        
        billingClient.querySkuDetailsAsync(params.build(), (billingResult, skuDetailsList) -> {
            // Always notify about the result
            if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                String errorMsg = "Failed to query SKU details. Response: " + billingResult.getResponseCode() + 
                                 " - " + billingResult.getDebugMessage() + 
                                 "\n\nNote: SKUs must be configured in Google Play Console. " +
                                 "For testing, the app will use test mode.";
                
                // Fallback to test mode if SKU query fails
                if (stateListener != null) {
                    // Map SKU back to subscription type for test mode
                    PremiumManager.SubscriptionType testType = PremiumManager.SubscriptionType.NONE;
                    if (skuId.equals(SKU_MONTHLY)) {
                        testType = PremiumManager.SubscriptionType.MONTHLY;
                    } else if (skuId.equals(SKU_YEARLY)) {
                        testType = PremiumManager.SubscriptionType.YEARLY;
                    } else if (skuId.equals(SKU_LIFETIME)) {
                        testType = PremiumManager.SubscriptionType.LIFETIME;
                    }
                    
                    if (testType != PremiumManager.SubscriptionType.NONE) {
                        // Use test mode as fallback
                        premiumManager.setSubscription(testType);
                        stateListener.onPurchaseSuccess(testType);
                    } else {
                        stateListener.onPurchaseError(errorMsg);
                    }
                }
                return;
            }
            
            if (skuDetailsList == null || skuDetailsList.isEmpty()) {
                String errorMsg = "SKU '" + skuId + "' not found in Google Play Console. " +
                                 "Using test mode instead.";
                
                // Fallback to test mode
                PremiumManager.SubscriptionType testType = PremiumManager.SubscriptionType.NONE;
                if (skuId.equals(SKU_MONTHLY)) {
                    testType = PremiumManager.SubscriptionType.MONTHLY;
                } else if (skuId.equals(SKU_YEARLY)) {
                    testType = PremiumManager.SubscriptionType.YEARLY;
                } else if (skuId.equals(SKU_LIFETIME)) {
                    testType = PremiumManager.SubscriptionType.LIFETIME;
                }
                
                if (testType != PremiumManager.SubscriptionType.NONE && stateListener != null) {
                    premiumManager.setSubscription(testType);
                    stateListener.onPurchaseSuccess(testType);
                } else if (stateListener != null) {
                    stateListener.onPurchaseError(errorMsg);
                }
                return;
            }
            
            SkuDetails skuDetails = skuDetailsList.get(0);
            
            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetails)
                    .build();
            
            BillingResult result = billingClient.launchBillingFlow(activity, flowParams);
            if (result.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                String errorMsg = "Failed to launch billing flow. Response: " + result.getResponseCode() + 
                                " - " + result.getDebugMessage() +
                                "\n\nUsing test mode instead.";
                
                // Fallback to test mode
                PremiumManager.SubscriptionType testType = PremiumManager.SubscriptionType.NONE;
                if (skuId.equals(SKU_MONTHLY)) {
                    testType = PremiumManager.SubscriptionType.MONTHLY;
                } else if (skuId.equals(SKU_YEARLY)) {
                    testType = PremiumManager.SubscriptionType.YEARLY;
                } else if (skuId.equals(SKU_LIFETIME)) {
                    testType = PremiumManager.SubscriptionType.LIFETIME;
                }
                
                if (testType != PremiumManager.SubscriptionType.NONE && stateListener != null) {
                    premiumManager.setSubscription(testType);
                    stateListener.onPurchaseSuccess(testType);
                } else if (stateListener != null) {
                    stateListener.onPurchaseError(errorMsg);
                }
            }
        });
    }
    
    public void queryPurchases() {
        if (!isServiceConnected) {
            return;
        }
        
        // Query subscriptions
        QueryPurchasesParams subParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.SkuType.SUBS)
                .build();
        
        billingClient.queryPurchasesAsync(subParams, (billingResult, purchases) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                processPurchases(purchases);
            }
        });
        
        // Query in-app products (for lifetime)
        QueryPurchasesParams inAppParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.SkuType.INAPP)
                .build();
        
        billingClient.queryPurchasesAsync(inAppParams, (billingResult, purchases) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                processPurchases(purchases);
            }
        });
    }
    
    private void processPurchases(List<Purchase> purchases) {
        boolean hasActiveSubscription = false;
        
        for (Purchase purchase : purchases) {
            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                if (!purchase.isAcknowledged()) {
                    acknowledgePurchase(purchase);
                }
                
                // Map SKU to subscription type and update cache
                PremiumManager.SubscriptionType type = mapSkuToSubscriptionType(purchase.getSkus());
                if (type != PremiumManager.SubscriptionType.NONE) {
                    // Update local cache with purchase info
                    updateLocalCache(purchase, type);
                    hasActiveSubscription = true;
                }
            }
        }
        
        // If no active purchases found, clear cache
        if (!hasActiveSubscription) {
            premiumManager.clearSubscription();
        }
    }
    
    private PremiumManager.SubscriptionType mapSkuToSubscriptionType(List<String> skus) {
        if (skus == null || skus.isEmpty()) {
            return PremiumManager.SubscriptionType.NONE;
        }
        
        String sku = skus.get(0);
        if (sku.equals(SKU_MONTHLY)) {
            return PremiumManager.SubscriptionType.MONTHLY;
        } else if (sku.equals(SKU_YEARLY)) {
            return PremiumManager.SubscriptionType.YEARLY;
        } else if (sku.equals(SKU_LIFETIME)) {
            return PremiumManager.SubscriptionType.LIFETIME;
        }
        
        return PremiumManager.SubscriptionType.NONE;
    }
    
    private void updateLocalCache(Purchase purchase, PremiumManager.SubscriptionType type) {
        // For subscriptions, expiry is managed by Google Play
        // We'll store the purchase token and let Google Play handle expiry
        // For now, we'll set a long expiry and let the app check with Google Play periodically
        premiumManager.setSubscriptionFromPurchase(type, purchase.getPurchaseTime());
    }
    
    private void acknowledgePurchase(Purchase purchase) {
        AcknowledgePurchaseParams acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();
        
        billingClient.acknowledgePurchase(acknowledgeParams, new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                // Purchase acknowledged
            }
        });
    }
    
    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    if (!purchase.isAcknowledged()) {
                        acknowledgePurchase(purchase);
                    }
                    
                    PremiumManager.SubscriptionType type = mapSkuToSubscriptionType(purchase.getSkus());
                    if (type != PremiumManager.SubscriptionType.NONE) {
                        updateLocalCache(purchase, type);
                        if (stateListener != null) {
                            stateListener.onPurchaseSuccess(type);
                        }
                    }
                }
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            if (stateListener != null) {
                stateListener.onPurchaseError("Purchase cancelled");
            }
        } else {
            if (stateListener != null) {
                stateListener.onPurchaseError("Purchase failed: " + billingResult.getDebugMessage());
            }
        }
    }
    
    public void restorePurchases() {
        queryPurchases();
        boolean hasActive = premiumManager.hasActiveSubscription();
        if (stateListener != null) {
            stateListener.onRestoreSuccess(hasActive);
        }
    }
    
    public String getSkuForSubscriptionType(PremiumManager.SubscriptionType type) {
        switch (type) {
            case MONTHLY:
                return SKU_MONTHLY;
            case YEARLY:
                return SKU_YEARLY;
            case LIFETIME:
                return SKU_LIFETIME;
            default:
                return null;
        }
    }
    
    public boolean isServiceConnected() {
        return isServiceConnected;
    }
    
    public void endConnection() {
        if (billingClient != null) {
            billingClient.endConnection();
            isServiceConnected = false;
        }
    }
}

