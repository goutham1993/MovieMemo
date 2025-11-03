package com.entertainment.moviememo.utils;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.entertainment.moviememo.MainActivity;
import com.entertainment.moviememo.R;
import com.entertainment.moviememo.data.entities.WatchlistItem;
import com.entertainment.moviememo.receivers.NotificationReceiver;

import java.util.Calendar;
import java.util.List;

public class NotificationHelper {
    
    private static final String CHANNEL_ID = "watchlist_notifications";
    private static final String CHANNEL_NAME = "Watchlist Reminders";
    private static final int NOTIFICATION_ID_BASE = 1000;
    
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications for watchlist release dates");
            
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    
    public static void scheduleNotification(Context context, WatchlistItem item) {
        // This method is kept for backward compatibility but will be handled by rescheduleAllNotifications
        // Individual scheduling is not needed since we group by date
    }
    
    public static void cancelNotification(Context context, WatchlistItem item) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }
        
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) (NOTIFICATION_ID_BASE + item.id),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        alarmManager.cancel(pendingIntent);
    }
    
    public static void showNotification(Context context, String movieTitle) {
        createNotificationChannel(context);
        
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("navigate_to_watchlist", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("🎬 Movie Reminder")
                .setContentText("Movies are waiting in your watchlist. Check them out.")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Movies are waiting in your watchlist. Check them out."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID_BASE, builder.build());
        }
    }
    
    public static void rescheduleAllNotifications(Context context, List<WatchlistItem> items) {
        // Group items by release date (same day) - one notification per date
        java.util.Map<Long, java.util.List<WatchlistItem>> dateGroups = new java.util.HashMap<>();
        for (WatchlistItem item : items) {
            if (item.releaseDate != null && item.whereToWatch != null && 
                item.whereToWatch.equals("THEATER")) {
                // Get the date at midnight for grouping
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(item.releaseDate);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                Long dateKey = cal.getTimeInMillis();
                
                if (!dateGroups.containsKey(dateKey)) {
                    dateGroups.put(dateKey, new java.util.ArrayList<>());
                }
                dateGroups.get(dateKey).add(item);
            }
        }
        
        // Cancel notifications for dates that will be rescheduled
        for (Long dateKey : dateGroups.keySet()) {
            cancelNotificationForDate(context, dateKey);
        }
        
        // Schedule one notification per unique release date
        for (java.util.Map.Entry<Long, java.util.List<WatchlistItem>> entry : dateGroups.entrySet()) {
            WatchlistItem firstItem = entry.getValue().get(0);
            scheduleNotificationForDate(context, entry.getKey(), firstItem);
        }
    }
    
    // Helper method to cancel all notifications for a given date
    private static void cancelNotificationForDate(Context context, Long dateAtMidnight) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }
        
        Intent intent = new Intent(context, NotificationReceiver.class);
        int notificationId = (int) (NOTIFICATION_ID_BASE + (dateAtMidnight / (24 * 60 * 60 * 1000)) % Integer.MAX_VALUE);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        alarmManager.cancel(pendingIntent);
    }
    
    private static void scheduleNotificationForDate(Context context, Long dateAtMidnight, WatchlistItem representativeItem) {
        SharedPreferences prefs = context.getSharedPreferences("MovieMemoPrefs", Context.MODE_PRIVATE);
        int notificationHour = prefs.getInt("notification_hour", 9);
        int notificationMinute = prefs.getInt("notification_minute", 0);
        
        // Set the notification time for that day
        Calendar notificationTime = Calendar.getInstance();
        notificationTime.setTimeInMillis(dateAtMidnight);
        notificationTime.set(Calendar.HOUR_OF_DAY, notificationHour);
        notificationTime.set(Calendar.MINUTE, notificationMinute);
        notificationTime.set(Calendar.SECOND, 0);
        notificationTime.set(Calendar.MILLISECOND, 0);
        
        // Don't schedule if the time has passed
        if (notificationTime.before(Calendar.getInstance())) {
            return;
        }
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }
        
        Intent intent = new Intent(context, NotificationReceiver.class);
        // Use date as part of ID to ensure one notification per date
        int notificationId = (int) (NOTIFICATION_ID_BASE + (dateAtMidnight / (24 * 60 * 60 * 1000)) % Integer.MAX_VALUE);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    notificationTime.getTimeInMillis(),
                    pendingIntent
            );
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    notificationTime.getTimeInMillis(),
                    pendingIntent
            );
        } else {
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    notificationTime.getTimeInMillis(),
                    pendingIntent
            );
        }
    }
}

