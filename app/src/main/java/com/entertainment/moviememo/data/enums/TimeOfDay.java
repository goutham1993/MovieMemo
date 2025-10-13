package com.entertainment.moviememo.data.enums;

public enum TimeOfDay {
    MORNING("Morning"),
    AFTERNOON("Afternoon"),
    EVENING("Evening"),
    NIGHT("Night");
    
    private final String displayName;
    
    TimeOfDay(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
