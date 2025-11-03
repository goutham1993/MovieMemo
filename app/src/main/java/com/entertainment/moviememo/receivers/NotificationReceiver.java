package com.entertainment.moviememo.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.entertainment.moviememo.utils.NotificationHelper;

public class NotificationReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String movieTitle = intent.getStringExtra("movie_title");
        
        // Show the notification
        NotificationHelper.showNotification(context, movieTitle);
    }
}

