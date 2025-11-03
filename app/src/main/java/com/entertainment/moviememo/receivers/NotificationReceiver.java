package com.entertainment.moviememo.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.entertainment.moviememo.utils.NotificationHelper;

public class NotificationReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        // Show the notification
        NotificationHelper.showNotification(context, "Your Watchlist");
        
        // Reschedule all notifications to ensure they recur weekly
        NotificationHelper.rescheduleAllNotifications(context);
    }
}

