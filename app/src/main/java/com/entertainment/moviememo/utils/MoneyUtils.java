package com.entertainment.moviememo.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class MoneyUtils {
    
    private static final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault());
    
    public static String formatCentsAsCurrency(int cents) {
        double amount = cents / 100.0;
        return currencyFormatter.format(amount);
    }
    
    public static int parseCurrencyToCents(String currencyString) {
        try {
            // Remove currency symbols and parse
            String cleanString = currencyString.replaceAll("[^\\d.,]", "");
            double amount = Double.parseDouble(cleanString);
            return (int) Math.round(amount * 100);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    public static String formatCurrency(double amount) {
        return currencyFormatter.format(amount);
    }
    
    public static boolean isValidCurrencyAmount(String amountString) {
        try {
            String cleanString = amountString.replaceAll("[^\\d.,]", "");
            double amount = Double.parseDouble(cleanString);
            return amount >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public static String getCurrencySymbol() {
        return currencyFormatter.getCurrency().getSymbol();
    }
}
