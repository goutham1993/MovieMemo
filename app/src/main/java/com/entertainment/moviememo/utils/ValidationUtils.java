package com.entertainment.moviememo.utils;

import android.text.TextUtils;

public class ValidationUtils {
    
    public static boolean isValidTitle(String title) {
        return !TextUtils.isEmpty(title) && title.trim().length() >= 1;
    }
    
    public static boolean isValidRating(String ratingText) {
        if (TextUtils.isEmpty(ratingText)) {
            return true; // Optional field
        }
        
        try {
            int rating = Integer.parseInt(ratingText.trim());
            return rating >= 0 && rating <= 10;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public static boolean isValidDuration(String durationText) {
        if (TextUtils.isEmpty(durationText)) {
            return true; // Optional field
        }
        
        try {
            int duration = Integer.parseInt(durationText.trim());
            return duration > 0 && duration <= 600; // Max 10 hours
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public static boolean isValidSpendAmount(String spendText) {
        if (TextUtils.isEmpty(spendText)) {
            return true; // Optional field
        }
        
        return MoneyUtils.isValidCurrencyAmount(spendText);
    }
    
    public static String getTitleError(String title) {
        if (TextUtils.isEmpty(title)) {
            return "Title is required";
        }
        if (title.trim().length() < 1) {
            return "Title cannot be empty";
        }
        if (title.length() > 100) {
            return "Title is too long";
        }
        return null;
    }
    
    public static String getRatingError(String ratingText) {
        if (TextUtils.isEmpty(ratingText)) {
            return null; // Optional field
        }
        
        try {
            int rating = Integer.parseInt(ratingText.trim());
            if (rating < 0) {
                return "Rating cannot be negative";
            }
            if (rating > 10) {
                return "Rating cannot exceed 10";
            }
        } catch (NumberFormatException e) {
            return "Invalid rating format";
        }
        return null;
    }
    
    public static String getDurationError(String durationText) {
        if (TextUtils.isEmpty(durationText)) {
            return null; // Optional field
        }
        
        try {
            int duration = Integer.parseInt(durationText.trim());
            if (duration <= 0) {
                return "Duration must be positive";
            }
            if (duration > 600) {
                return "Duration seems too long (max 10 hours)";
            }
        } catch (NumberFormatException e) {
            return "Invalid duration format";
        }
        return null;
    }
    
    public static String getSpendAmountError(String spendText) {
        if (TextUtils.isEmpty(spendText)) {
            return null; // Optional field
        }
        
        if (!MoneyUtils.isValidCurrencyAmount(spendText)) {
            return "Invalid amount format";
        }
        
        try {
            String cleanString = spendText.replaceAll("[^\\d.,]", "");
            double amount = Double.parseDouble(cleanString);
            if (amount < 0) {
                return "Amount cannot be negative";
            }
            if (amount > 10000) {
                return "Amount seems too high";
            }
        } catch (NumberFormatException e) {
            return "Invalid amount format";
        }
        return null;
    }
}
