package com.entertainment.moviememo.utils;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.entertainment.moviememo.MainActivity;
import com.entertainment.moviememo.R;
import com.entertainment.moviememo.data.database.AppDatabase;
import com.entertainment.moviememo.data.entities.NotificationSettings;
import com.entertainment.moviememo.receivers.NotificationReceiver;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

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
    
    public static void cancelAllNotifications(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }
        
        // Cancel all possible notification IDs (using a reasonable range)
        for (int i = 0; i < 10000; i++) {
            Intent intent = new Intent(context, NotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    NOTIFICATION_ID_BASE + i,
                    intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        }
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
    
    public static void rescheduleAllNotifications(Context context) {
        // Run database access on background thread
        new Thread(() -> {
            // Cancel all existing notifications
            cancelAllNotifications(context);
            
            // Get notification settings from database
            NotificationSettings settings = AppDatabase.getDatabase(context).movieDao().getNotificationSettings();
            if (settings == null || settings.selectedDays == null || settings.selectedDays.isEmpty()) {
                return;
            }
            
            if (settings.notificationHour == null || settings.notificationMinute == null) {
                return;
            }
            
            // Parse selected days (0=Sunday, 1=Monday, ..., 6=Saturday)
            String[] dayStrings = settings.selectedDays.split(",");
            Set<Integer> selectedDays = new HashSet<>();
            for (String dayString : dayStrings) {
                try {
                    selectedDays.add(Integer.parseInt(dayString.trim()));
                } catch (NumberFormatException e) {
                    // Skip invalid days
                }
            }
            
            // Schedule one notification per selected day (recurring weekly)
            for (Integer dayOfWeek : selectedDays) {
                scheduleNotificationForDay(context, dayOfWeek, settings.notificationHour, settings.notificationMinute);
            }
        }).start();
    }
    
    private static void scheduleNotificationForDay(Context context, int dayOfWeek, int notificationHour, int notificationMinute) {
        // Calculate next occurrence of this day
        Calendar now = Calendar.getInstance();
        Calendar notificationTime = Calendar.getInstance();
        notificationTime.set(Calendar.HOUR_OF_DAY, notificationHour);
        notificationTime.set(Calendar.MINUTE, notificationMinute);
        notificationTime.set(Calendar.SECOND, 0);
        notificationTime.set(Calendar.MILLISECOND, 0);
        
        // Convert our day format (0=Sunday, 1=Monday, ..., 6=Saturday) to Calendar format (1=Sunday, 2=Monday, ..., 7=Saturday)
        int calendarDayOfWeek = dayOfWeek == 0 ? Calendar.SUNDAY : dayOfWeek + 1;
        
        // Find the next occurrence of this day
        int daysUntilNext = (calendarDayOfWeek - notificationTime.get(Calendar.DAY_OF_WEEK) + 7) % 7;
        if (daysUntilNext == 0) {
            // If today is the selected day, check if time has passed
            if (notificationTime.getTimeInMillis() <= now.getTimeInMillis()) {
                // Time has passed today, schedule for next week
                daysUntilNext = 7;
            }
        }
        
        notificationTime.add(Calendar.DAY_OF_MONTH, daysUntilNext);
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }
        
        Intent intent = new Intent(context, NotificationReceiver.class);
        // Use day of week as part of ID to ensure one notification per day
        int notificationId = NOTIFICATION_ID_BASE + dayOfWeek;
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Schedule recurring notification weekly
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
        
        // For recurring weekly notifications, we'll need to reschedule after each notification
        // This is handled by rescheduling on app startup and when settings change
    }
}

