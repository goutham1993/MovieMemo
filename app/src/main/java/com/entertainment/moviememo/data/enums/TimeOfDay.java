package com.entertainment.moviememo.data.enums;

public enum TimeOfDay {
    NIGHT("Night"),
    MORNING("Morning"),
    AFTERNOON("Afternoon"),
    EVENING("Evening");
    
    private final String displayName;
    
    TimeOfDay(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
