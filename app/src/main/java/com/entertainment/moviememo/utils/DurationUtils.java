package com.entertainment.moviememo.utils;

public class DurationUtils {
    
    public static String formatDuration(int totalMinutes) {
        if (totalMinutes <= 0) {
            return "0 min (0 total minutes)";
        }
        
        int days = totalMinutes / (24 * 60);
        int hours = (totalMinutes % (24 * 60)) / 60;
        int minutes = totalMinutes % 60;
        
        StringBuilder result = new StringBuilder();
        
        // Show breakdown first
        if (days > 0) {
            result.append(days).append(" day");
            if (days > 1) result.append("s");
            result.append(" ");
        }
        
        if (hours > 0) {
            result.append(hours).append(" hr");
            if (hours > 1) result.append("s");
            result.append(" ");
        }
        
        if (minutes > 0 || (days == 0 && hours == 0)) {
            result.append(minutes).append(" min");
        }
        
        // Add total minutes in parentheses
        result.append(" (").append(totalMinutes).append(" total minutes)");
        
        return result.toString();
    }
    
    public static String formatDurationShort(int totalMinutes) {
        if (totalMinutes <= 0) {
            return "0m";
        }
        
        int days = totalMinutes / (24 * 60);
        int hours = (totalMinutes % (24 * 60)) / 60;
        int minutes = totalMinutes % 60;
        
        StringBuilder result = new StringBuilder();
        
        if (days > 0) {
            result.append(days).append("d ");
        }
        
        if (hours > 0) {
            result.append(hours).append("h ");
        }
        
        if (minutes > 0 || (days == 0 && hours == 0)) {
            result.append(minutes).append("m");
        }
        
        return result.toString().trim();
    }
    
    public static String getDurationBreakdown(int totalMinutes) {
        if (totalMinutes <= 0) {
            return "No movies with duration recorded";
        }
        
        int days = totalMinutes / (24 * 60);
        int hours = (totalMinutes % (24 * 60)) / 60;
        int minutes = totalMinutes % 60;
        
        StringBuilder result = new StringBuilder();
        
        if (days > 0) {
            result.append(days).append(" day");
            if (days > 1) result.append("s");
            result.append(", ");
        }
        
        if (hours > 0) {
            result.append(hours).append(" hour");
            if (hours > 1) result.append("s");
            result.append(", ");
        }
        
        if (minutes > 0 || (days == 0 && hours == 0)) {
            result.append(minutes).append(" minute");
            if (minutes > 1) result.append("s");
        }
        
        return result.toString();
    }
    
    public static String formatDurationComprehensive(int totalMinutes) {
        if (totalMinutes <= 0) {
            return "0 min (0 total minutes)";
        }
        
        int days = totalMinutes / (24 * 60);
        int hours = (totalMinutes % (24 * 60)) / 60;
        int minutes = totalMinutes % 60;
        
        StringBuilder result = new StringBuilder();
        
        // Show total minutes first
        result.append(totalMinutes).append(" total minutes");
        
        // Add breakdown
        if (days > 0 || hours > 0 || minutes > 0) {
            result.append(" = ");
            
            if (days > 0) {
                result.append(days).append(" day");
                if (days > 1) result.append("s");
                if (hours > 0 || minutes > 0) result.append(", ");
            }
            
            if (hours > 0) {
                result.append(hours).append(" hour");
                if (hours > 1) result.append("s");
                if (minutes > 0) result.append(", ");
            }
            
            if (minutes > 0 || (days == 0 && hours == 0)) {
                result.append(minutes).append(" min");
            }
        }
        
        return result.toString();
    }
}
