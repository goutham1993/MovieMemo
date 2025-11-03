package com.entertainment.moviememo.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notification_settings")
public class NotificationSettings {
    @PrimaryKey
    public int id = 1; // Single row, always id = 1
    
    public String selectedDays; // Comma-separated list of day numbers (0=Sunday, 1=Monday, ..., 6=Saturday)
    
    public Integer notificationHour; // Hour (0-23)
    
    public Integer notificationMinute; // Minute (0-59)
    
    public NotificationSettings() {
    }
    
    public NotificationSettings(String selectedDays, Integer notificationHour, Integer notificationMinute) {
        this.selectedDays = selectedDays;
        this.notificationHour = notificationHour;
        this.notificationMinute = notificationMinute;
    }
}

