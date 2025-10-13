package com.entertainment.moviememo.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    
    private static final String ISO_DATE_FORMAT = "yyyy-MM-dd";
    private static final String DISPLAY_DATE_FORMAT = "MMM dd, yyyy";
    private static final String MONTH_YEAR_FORMAT = "MMMM yyyy";
    
    private static final SimpleDateFormat isoFormatter = new SimpleDateFormat(ISO_DATE_FORMAT, Locale.getDefault());
    private static final SimpleDateFormat displayFormatter = new SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault());
    private static final SimpleDateFormat monthYearFormatter = new SimpleDateFormat(MONTH_YEAR_FORMAT, Locale.getDefault());
    
    public static String formatDateForDisplay(Date date) {
        return displayFormatter.format(date);
    }
    
    public static String formatDateForStorage(Date date) {
        return isoFormatter.format(date);
    }
    
    public static String formatMonthYear(Date date) {
        return monthYearFormatter.format(date);
    }
    
    public static Date parseIsoDate(String isoDate) throws ParseException {
        return isoFormatter.parse(isoDate);
    }
    
    public static String getCurrentIsoDate() {
        return isoFormatter.format(new Date());
    }
    
    public static String getMonthYearFromIso(String isoDate) {
        try {
            Date date = parseIsoDate(isoDate);
            return monthYearFormatter.format(date);
        } catch (ParseException e) {
            return isoDate.substring(0, 7); // Fallback to YYYY-MM
        }
    }
    
    public static boolean isToday(Date date) {
        Calendar today = Calendar.getInstance();
        Calendar targetDate = Calendar.getInstance();
        targetDate.setTime(date);
        
        return today.get(Calendar.YEAR) == targetDate.get(Calendar.YEAR) &&
               today.get(Calendar.DAY_OF_YEAR) == targetDate.get(Calendar.DAY_OF_YEAR);
    }
    
    public static boolean isThisWeek(Date date) {
        Calendar today = Calendar.getInstance();
        Calendar targetDate = Calendar.getInstance();
        targetDate.setTime(date);
        
        int daysDiff = (int) ((today.getTimeInMillis() - targetDate.getTimeInMillis()) / (1000 * 60 * 60 * 24));
        return daysDiff >= 0 && daysDiff <= 7;
    }
    
    public static boolean isThisMonth(Date date) {
        Calendar today = Calendar.getInstance();
        Calendar targetDate = Calendar.getInstance();
        targetDate.setTime(date);
        
        return today.get(Calendar.YEAR) == targetDate.get(Calendar.YEAR) &&
               today.get(Calendar.MONTH) == targetDate.get(Calendar.MONTH);
    }
}
